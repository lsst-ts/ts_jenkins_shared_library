package com.lsst.ts.pipelines

def condaPipeline(config_repo, name, module_name){
    // Create a conda build pipeline
    Csc csc = new Csc()
    pipeline {
        agent {
            docker {
                image 'lsstts/conda_package_builder:latest'
                alwaysPull true
                label 'CSC_Conda_Node'
                args "--env ${config_repo.toUpperCase}_DIR=/home/saluser/${config_repo} --env LSST_DDS_DOMAIN=citest -u root --entrypoint=''"
            }
        }
        environment {
            package_name = ${name}
            OSPL_HOME="/opt/OpenSpliceDDS/v6.9.0/HDE/x86_64.linux"
        }
        stages {
            stage("Clone ${config_repo}") {
                steps {
                    sh """
                        cd /home/saluser
                        git clone https://github.com/lsst-ts/${config_repo}
                    """
                }
            }
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["HOME=${env.WORKSPACE}"]) {
                        sh """
                            cd ${HOME}/conda
                            source /home/saluser/miniconda3/bin/activate
                            conda config --add channels conda-forge
                            conda config --add channels lsstts
                            source ${OSPL_HOME}/release.com
                        """
                        csc.build_conda()
                    }
                }
            }
            stage("Push Conda Release Candidate package") {
                when {
                    buildingTag()
                    tag pattern: "^v\\d\\.\\d\\.\\d-rc\\.\\d\$", comparator: "REGEXP"
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["HOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            csc.upload_conda("rc")
                        }
                    }
                }
            }
            stage("Push Conda Release package") {
                when {
                    buildingTag()
                    not {
                        tag pattern: "^v\\d\\.\\d\\.\\d-rc\\.\\d\$", comparator: "REGEXP"
                    }
                }
                steps {
                    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user')]) {
                        withEnv(["HOME=${env.WORKSPACE}"]) {
                            sh """
                            source /home/saluser/miniconda3/bin/activate
                            anaconda login --user ${anaconda_user} --password ${anaconda_pass}
                            """
                            csc.upload_conda("main")
                        }
                    }
                }
            }
        }
    }
}
