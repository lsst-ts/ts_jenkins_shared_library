import org.lsst.ts.jenkins.components.Csc

def call(name, arch="noarch"){
    // Create a conda build pipeline
    Csc csc = new Csc()
    label_value = "CSC_Conda_Node"
    image_value = "ts-dockerhub.lsst.org/conda_package_builder:latest"
    registry_url = "https://ts-dockerhub.lsst.org"
    registry_credentials_id = "nexus3-lsst_jenkins"
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
                label label_value
                args arg_str.concat("-u root --entrypoint=''")
                registryUrl registry_url
                registryCredentialsId registry_credentials_id
            }
        }

        environment {
            package_name = "${name}"
            branch = csc.getBranchName("${env.CHANGE_BRANCH}", "${env.BRANCH_NAME}")
        }

        options {
          disableConcurrentBuilds()
          skipDefaultCheckout()
        }

        stages {

            stage ('Cloning Recipe Repos') {
                steps {
                    dir(env.WORKSPACE + '/ts_recipes') {
                        git branch: 'main', url: 'https://github.com/lsst-ts/ts_recipes'
                    }
                    script{
                        sh "printenv"
                    }
                }
            }

            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}/ts_recipes/${package_name}", "CONDA_BUILD_TAG=${TAG_NAME}"]) {
                        script {
                            csc.build_csc_conda("main")
                        }
                    }
                }
            }
            stage("Create Conda dev package") {
                when {
                    not {
                        buildingTag()
                    }
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}/ts_recipes/${package_name}", "CONDA_BUILD_TAG=${env.branch}"]) {
                        dir(env.WORKSPACE + '/ts_recipes') {
                            git branch: "${env.branch}", url: 'https://github.com/lsst-ts/ts_recipes'
                        }
                        script {
                            csc.build_csc_conda("dev")
                        }
                    }
                }
            }
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
            }
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
            }
        }
        post {
            always {
                withEnv(["HOME=${env.WORKSPACE}"]) {
                    sh 'chown -R 1003:1003 ${HOME}/'
                }
                step([$class: 'Mailer', recipients: emails[name], notifyEveryUnstableBuild: false, sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = slack_ids[name]
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = slack_ids[name]
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }
    }
}
