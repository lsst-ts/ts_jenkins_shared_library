import org.lsst.ts.jenkins.components.Csc

def call(config_repo, name, module_name){
    // Create a conda build pipeline
    Csc csc = new Csc()
    arg_str = ""
    clone_str = ""
    if (!config_repo.isEmpty()) {
        config_repo.each{ repo->
            arg_str.concat("--env ${repo.toUpperCase()}_DIR=/home/saluser/${repo}")
            clone_str.concat("git clone https://github.com/lsst-ts/${repo}\n")
            println(arg_str)
            println(clone_str)
        }
    }
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
        stages {
            stage("Clone configuration repository") {
                when {
                    expression { !config_repo.isEmpty() }
                }
                steps {
                    sh """
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
                            csc.build_conda("main")
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
                            csc.build_conda("dev")
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
            }
            regression {
                emailext body: 'Check console output at $BUILD_URL to view the results. \n\n ${CHANGES} \n\n -------------------------------------------------- \n${BUILD_LOG, maxLines=100, escapeHtml=false}', 
                    recipientProviders: [culprits()], 
                    subject: 'Build failed in Jenkins: $PROJECT_NAME - #$BUILD_NUMBER'
            }
            fixed {
            emailext body: 'Check console output at $BUILD_URL to view the results. \n\n ${CHANGES} \n', 
                    recipientProviders: [culprits()], 
                    subject: 'Build Fixed in Jenkins: $PROJECT_NAME - #$BUILD_NUMBER'
            }
        }
    }
}
