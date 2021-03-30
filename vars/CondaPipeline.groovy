import org.lsst.ts.jenkins.components.Csc

def call(config_repo, name, module_name,arch="linux-64"){
    // Create a conda build pipeline
    Csc csc = new Csc()
    emails = csc.email()
    slack_ids = csc.slack_id()
    arg_str = ""
    clone_str = ""
    if (arch!="linux-aarch64") {
        ospl_home = "/opt/OpenSpliceDDS/V6.10.4/HDE/x86_64.linux"
    }
    else {
        ospl_home = "/opt/OpenSpliceDDS/V6.9.0/HDE/aarch64.linux"
    }
    if (!config_repo.isEmpty()) {
        config_repo.each{ repo ->
            arg_str = arg_str.concat("--env ${repo.toUpperCase()}_DIR=/home/saluser/${repo} ")
            clone_str = clone_str.concat("git clone https://github.com/lsst-ts/${repo}\n")
            println(arg_str)
            println(clone_str)
            println(emails[name])
        }
    }
    if (arch=="linux-aarch64") {
        label_value = "Arm64_2CPU"
        image_value = "lsstts/conda_package_builder_aarch64:latest"
        registry_url = ""
        registry_credentials_id = ""
    }
    else {
        label_value = "CSC_Conda_Node"
        image_value = "ts-dockerhub.lsst.org/conda_package_builder:latest"
        registry_url = "https://ts-dockerhub.lsst.org"
        registry_credentials_id = "nexus3-lsst_jenkins"
    }
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
                args arg_str.concat("--env LSST_DDS_DOMAIN=citest -u root --entrypoint=''")
                registryUrl registry_url
                registryCredentialsId registry_credentials_id
            }
        }
        parameters {
            string(name: 'idl_version', defaultValue: '\'\'', description: 'The version of the IDL Conda package.')
            string(name: 'salobj_version', defaultValue: '\'\'', description: 'The version of the salobj Conda package.')
        }
        environment {
            package_name = "${name}"
            OSPL_HOME="${ospl_home}"
        }
        options {
            disableConcurrentBuilds()
        }    
        stages {
            stage("Clone configuration repository") {
                when {
                    not {
                        expression { config_repo.isEmpty() }
                    }
                }
                steps {
                    sh """
                        yum clean expire-cache
                        yum check-update || true
                        echo "The IDL version: ${params.idl_version}"
                        echo "The SalObj version: ${params.salobj_version}"
                        cd /home/saluser
                        ${clone_str}
                    """
                }
            }
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["HOME=${env.WORKSPACE}"]) {
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
                    withEnv(["HOME=${env.WORKSPACE}"]) {
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
                        withEnv(["HOME=${env.WORKSPACE}"]) {
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
                        withEnv(["HOME=${env.WORKSPACE}"]) {
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
