import org.lsst.ts.jenkins.components.Csc


def call(Object... varargs){
    // Create a conda build pipeline
    // and assume ordered parameters.
    // Mixing these are not supported nor handled
    name = varargs[0]
    module_name = varargs[1]
    arch = varargs[2]
    Csc csc = new Csc()
    registry_url = "https://ts-dockerhub.lsst.org"
    registry_credentials_id = "nexus3-lsst_jenkins"
    image_value = "ts-dockerhub.lsst.org/conda_package_builder:latest"
    emails = csc.email()
    slack_ids = csc.slack_id()
    arg_str = ""
    clone_str = ""
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
                image image_value
                alwaysPull true
                label "${params.build_agent}"
                args arg_str.concat("--env LSST_DDS_DOMAIN=citest --entrypoint='' --network=kafka")
                registryUrl registry_url
                registryCredentialsId registry_credentials_id
            }
        }
        parameters {
            choice choices: ['CSC_Conda_Node', 'Node1_4CPU', 'Node2_8CPU', 'Node3_4CPU'], description: 'Select the build agent', name: 'build_agent'
            string(name: 'idl_version', defaultValue: '\'\'', description: 'The version of the IDL Conda package.')
            string(name: 'XML_Version', defaultValue: '20.0.0', description: 'The XML Version, exclude any preceeding "v" characters: X.Y.Z')
            string(name: 'SAL_Version', defaultValue: '7.4.1', description: 'The SAL version, exclude any preceeding "v" characters: X.Y.Z')
            choice name: 'build_type', choices: ['Release', 'Daily', 'Bleed'], description: 'The upstream build type (Bleed, Daily or Release). This determines from where to pull the RPM.'
            booleanParam(defaultValue: false, description: "Is this a development build?", name: 'develop')
            booleanParam(defaultValue: false, description: "Are we building the salobj conda package after this?", name: 'buildSalObjConda')
            booleanParam(defaultValue: false, description: "Are we going on to building the CSC package after salobj?", name: 'buildCSCConda')
        }
        environment {
            package_name = "${name}"
        }
        options {
            disableConcurrentBuilds()
        }
        stages {
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_csc_conda("main")
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
                            csc.build_csc_conda("dev")
                        }
                    }
                }
            }//Create Dev
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
                                csc.upload_conda(package_name,"rc", arch)
                            }
                        }
                    }
                }
            }//Push Release Candidate
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
                                csc.upload_conda(package_name,"main",arch)
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
                        withEnv(["WHOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            script {
                                csc.upload_conda(package_name,"dev","noarch")
                            }
                        }
                    }
                }
            }//Push Dev
            stage("Trigger Salobj Conda Package build") {
                    when { expression { return params.buildSalObjConda.toBoolean() } }
                    steps {
                        script {
                            def RESULT = sh (returnStdout: true, script:
                            """
                            source /home/saluser/miniconda3/bin/activate > /dev/null &&
                            conda config --set solver libmamba &&
                            conda install -y setuptools_scm=8 > /dev/null &&
                            python -c 'from setuptools_scm import get_version; print(get_version())'
                            """).trim()

                            xml_conda_version = "${RESULT}"
                            echo "Starting the SalObj_Conda_package/develop job; sal_version: ${SAL_Version}, xml_version: ${XML_Version}, xml_conda_version: ${xml_conda_version}, idl_version: ${idl_version}, develop: ${develop}, buildCSCConda: ${buildCSCConda}"
                            build propagate: false, job: 'SalObj_Conda_package/develop', parameters: [
                                booleanParam(name: 'develop', value: "${develop}" ),
                                booleanParam(name: 'buildCSCConda', value: "${buildCSCConda}" ),
                                string(name: 'idl_version',value: "${idl_version}" ),
                                string(name: 'xml_version',value: "${XML_Version}" ),
                                string(name: 'xml_conda_version',value: "${xml_conda_version}" ),
                                string(name: 'sal_version',value: "${SAL_Version}" )
                            ], wait: false
                    }
                }
            }//TriggerSalObj
        }
        post {
            always {
                step([$class: 'Mailer', recipients: emails[name] ?: emails['default'], notifyEveryUnstableBuild: false, sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = slack_ids[name] ?: slack_ids['default']
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = slack_ids[name] ?: slack_ids['default']
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }
    }
}

