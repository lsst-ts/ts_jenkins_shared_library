import org.lsst.ts.jenkins.components.Csc

def call(config_repo, name, module_name){
    // Create a conda build pipeline
    Csc csc = new Csc()
    emails = csc.email()
    arg_str = ""
    clone_str = ""
    if (!config_repo.isEmpty()) {
        config_repo.each{ repo ->
            arg_str = arg_str.concat("--env ${repo.toUpperCase()}_DIR=/home/saluser/${repo} ")
            clone_str = clone_str.concat("git clone https://github.com/lsst-ts/${repo}\n")
            println(arg_str)
            println(clone_str)
            println(emails[name])
        }
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
                image 'ts-dockerhub.lsst.org/conda_package_builder:latest'
                alwaysPull true
                label 'CSC_Conda_Node'
                args arg_str.concat("--env LSST_DDS_DOMAIN=citest -u root --entrypoint=''")
                registryUrl 'https://ts-dockerhub.lsst.org'
                registryCredentialsId 'nexus3-lsst_jenkins'
            }
        }
        parameters {
            string(name: 'idl_version', defaultValue: '\'\'', description: 'The version of the IDL Conda package.')
            string(name: 'salobj_version', defaultValue: '\'\'', description: 'The version of the salobj Conda package.')
        }
        environment {
            package_name = "${name}"
            OSPL_HOME="/opt/OpenSpliceDDS/V6.10.4/HDE/x86_64.linux"
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
                                csc.upload_conda(package_name,"rc")
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
                                csc.upload_conda(package_name,"main")
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
        }
    }
}
