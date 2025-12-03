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
                label 'CSC_Conda_Node || CSC_Conda_Overflow_Node'
                args "--entrypoint='' -e LSST_KAFKA_BROKER_ADDR='35.85.18.232:9092' -e LSST_SCHEMA_REGISTRY_URL='http://35.85.18.232:8081'"
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
            string(name: 'salobj_version', defaultValue: '\'\'', description: 'The version of the salobj Conda package.')
            string(name: 'xml_conda_version', defaultValue: '\'\'', description: 'The XML Conda Version')
        }
        stages {
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_csc_conda("main", "3.12")
                            warnError(message: "Python 3.13 failed to build.") {
                                csc.build_csc_conda("main", "3.13")
                            }
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
                            csc.build_csc_conda("dev", "3.12")
                            warnError(message: "Python 3.13 failed to build") {
                                csc.build_csc_conda("dev", "3.13")
                            }
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
