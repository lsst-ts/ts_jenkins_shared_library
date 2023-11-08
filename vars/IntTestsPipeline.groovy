import org.lsst.ts.jenkins.components.Csc

def call(){
    Csc csc = new Csc()
    def conda_name = "ts-integrationtests"
    email = csc.email()
    slackID = csc.slack_id()
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
                args "--env LSST_DDS_DOMAIN=citest --env TS_XML_VERSION=${params.XML_Version} --env TS_SAL_VERSION=${params.SAL_Version} --entrypoint='' --network=kafka"
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
            booleanParam(defaultValue: false, description: "Is this a development build?", name: 'develop')
        }
        stages {
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}", "TS_SAL_VERSION=${env.TS_SAL_VERSION}"]) {
                        script {
                            csc.build_integrationtests_conda("main")
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
                            csc.build_integrationtests_conda("dev")
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
                                csc.upload_conda(conda_name,"main","noarch")
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
                                csc.upload_conda(conda_name,"dev","noarch")
                            }
                        }
                    }
                }
            }//Push Dev
        }//stages
        post {
            always {
                step([$class: 'Mailer',
                    notifyEveryUnstableBuild: false,
                    recipients: email[conda_name],
                    sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = slackID[conda_name]
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = slackID[conda_name]
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }//post
    }//pipeline
}//def call
