import org.lsst.ts.jenkins.components.Csc

def call(){
    Csc csc = new Csc()
    properties(
        [
        buildDiscarder
            (logRotator (
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                daysToKeepStr: '14',
                numToKeepStr: '10'
            ) ),
        disableConcurrentBuilds()
        ]
    )
    pipeline {
        agent {
            docker {
                image 'ts-dockerhub.lsst.org/conda_package_builder:latest'
                alwaysPull true
                label 'CSC_Conda_Node'
                args "--env LSST_DDS_DOMAIN=citest --env TS_XML_VERSION=${params.XML_Version} --env TS_SAL_VERSION=${params.SAL_Version} -u root --entrypoint=''"
                registryUrl 'https://ts-dockerhub.lsst.org'
                registryCredentialsId 'nexus3-lsst_jenkins'
            }
        }
        options {
            disableConcurrentBuilds()
        }    
        environment {
            dockerImageName = "lsstts/conda_package_builder:latest"
            container_name = "idl_${BUILD_ID}_${JENKINS_NODE_COOKIE}"
            PYPI_CREDS = credentials("pypi")
            OSPL_HOME="/opt/OpenSpliceDDS/V6.10.4/HDE/x86_64.linux"
        }
        parameters {
            string(defaultValue: '7.1.0', description: 'The XML Version, exclude any preceeding "v" characters: X.Y.Z', name: 'XML_Version')
            string(defaultValue: '5.0.1', description: 'The ts_sal version, exclude any preceeding "v" characters: X.Y.Z', name: 'SAL_Version')
            choice choices: ['Release', 'Daily', 'Bleed'], description: 'The upstream build type (Bleed, Daily or Release). This determines from where to pull the RPM.', name: 'build_type'
            booleanParam(defaultValue: false, description: "Is this a development build?", name: 'develop')
            booleanParam(defaultValue: false, description: "Are we building the salobj conda package after this?", name: 'buildSalObjConda')
            booleanParam(defaultValue: false, description: "Are we going on to building the CSC package after salobj?", name: 'buildCSCConda')
        }
        stages {
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["HOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_idl_conda("main")
                        }
                    }
                }
            }//Create Release
            stage("Create Conda dev package") {
                when {
                    not {
                        buildingTag()
                    }
                }
                steps {
                    withEnv(["HOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_idl_conda("dev")
                        }
                    }
                }
            }//Create Dev
            stage("Push Conda Release package") {
                when {
                    buildingTag()
                    not {
                        tag pattern: "^v\\d\\.\\d\\.\\d\\.rc\\.\\d\$", comparator: "REGEXP"
                    }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["HOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda("ts-idl","main")
                            }
                        }
                    }
                }
            }//Push Release
            stage("Push Conda Dev package") {
                when {
                    not {
                        buildingTag()
                    }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["HOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda("ts-idl","dev")
                            }
                        }
                    }
                }
            }//Push Dev
            stage("Create IDL pypi package") {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        script {
                            csc.upload_pypi()
                        }
                    }
                }
            }//PyPy package
            stage("Trigger Salobj Conda Package build") {
                    when { expression { return env.buildSalObjConda.toBoolean() } }
                    steps {
                        script {
                            def RESULT = sh (returnStdout: true, script:
                            """
                            source /home/saluser/miniconda3/bin/activate > /dev/null &&
                            conda install -q -y setuptools_scm > /dev/null &&
                            python -c 'from setuptools_scm import get_version; print(get_version())'
                            """).trim()

                            idl_version = "${RESULT}" + "_" + "${XML_Version}" + "_" + "${SAL_Version}"
                            echo "Starting the SalObj_Conda_package/develop job; sal_version: ${SAL_Version}, xml_version: ${XML_Version}, idl_version: ${idl_version}, develop: ${develop}, buildCSCConda: ${buildCSCConda}"
                            build propagate: false, job: 'SalObj_Conda_package/develop', parameters: [
                                booleanParam(name: 'develop', value: "${develop}" ), 
                                booleanParam(name: 'buildCSCConda', value: "${buildCSCConda}" ), 
                                string(name: 'idl_version',value: "${idl_version}" )
                            ], wait: false
                    }
                }
            }//TriggerSalObj
        }//stages
        post {
            cleanup {
                sh """
                    chmod -R a+rw .
                    chown -R 1003:1003 .
                """
            }
            always {
                step([$class: 'Mailer',
                    notifyEveryUnstableBuild: false,
                    recipients: "cwinslow@lsst.org",
                    sendToIndividuals: true])
            }
        }//post
    }//pipeline
}//def call