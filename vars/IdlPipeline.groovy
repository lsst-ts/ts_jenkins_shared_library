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
            PYPI_CREDS = credentials("pypi")
        }
        parameters {
            string(defaultValue: '20.0.0', description: 'The XML Version, exclude any preceeding "v" characters: X.Y.Z', name: 'XML_Version')
            string(defaultValue: '7.4.1', description: 'The ts_sal version, exclude any preceeding "v" characters: X.Y.Z', name: 'SAL_Version')
            choice choices: ['Release', 'Daily', 'Bleed'], description: 'The upstream build type (Bleed, Daily or Release). This determines from where to pull the RPM.', name: 'build_type'
            booleanParam(defaultValue: false, description: "Is this a development build?", name: 'develop')
            booleanParam(defaultValue: false, description: "Are we building the XML conda package after this?", name: 'buildXmlConda')
            booleanParam(defaultValue: false, description: "Are we building the SalObj conda package after this?", name: 'buildSalObjConda')
            booleanParam(defaultValue: false, description: "Are we going on to building the CSC package after salobj?", name: 'buildCSCConda')
        }
        stages {
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}", "TS_XML_VERSION=${params.XML_Version}", "TS_SAL_VERSION=${params.SAL_Version}"]) {
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
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
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
                        withEnv(["WHOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda("ts-idl","main","noarch")
                            }
                        }
                    }
                }
            }//Push Release
            stage("Push Conda Release Candidate package") {
                when {
                    buildingTag()
                    tag pattern: "^v\\d\\.\\d\\.\\d\\.rc\\.\\d\$", comparator: "REGEXP"
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["WHOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda("ts-idl","rc","noarch")
                            }
                        }
                    }
                }
            }//Push Release Candidate
            stage("Push Conda Dev package") {
                when {
                    not {
                        buildingTag()
                    }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["WHOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda("ts-idl","dev","noarch")
                            }
                        }
                    }
                }
            }//Push Dev
            stage("Trigger XML Conda Package build") {
                    when { expression { return params.buildXmlConda.toBoolean() } }
                    steps {
                        script {
                            def RESULT = sh (returnStdout: true, script:
                            """
                            source /home/saluser/miniconda3/bin/activate > /dev/null &&
                            conda config --set solver libmamba &&
                            conda install -y setuptools_scm=8 > /dev/null &&
                            python -c 'from setuptools_scm import get_version; print(get_version())'
                            """).trim()

                            idl_version = "${RESULT}"
                            echo "Starting the XmlObj_Conda_package/develop job; SAL_version: ${SAL_Version}, XML_version: ${XML_Version}, idl_version: ${idl_version}, develop: ${develop}, buildSalObjConda: ${buildSalObjConda}, buildCSCConda: ${buildCSCConda}"
                            build propagate: false, job: 'XML_conda_package/develop', parameters: [
                                booleanParam(name: 'develop', value: "${develop}" ),
                                booleanParam(name: 'buildSalObjConda', value: "${buildSalObjConda}" ),
                                booleanParam(name: 'buildCSCConda', value: "${buildCSCConda}" ),
                                string(name: 'idl_version',value: "${idl_version}" ),
                                string(name: 'XML_version',value: "${XML_Version}" ),
                                string(name: 'SAL_version',value: "${SAL_Version}" )
                            ], wait: false
                    }
                }
            }//TriggerXmlObj
        }//stages
        post {
            always {
                step([$class: 'Mailer',
                    notifyEveryUnstableBuild: false,
                    recipients: "tribeiro@lsst.org",
                    sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = "U72CH91L2" //Tiago
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = "U72CH91L2" //Tiago
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }//post
    }//pipeline
}//def call
