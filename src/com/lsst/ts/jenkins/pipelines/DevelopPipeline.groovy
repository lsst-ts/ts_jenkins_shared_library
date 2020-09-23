package com.lsst.ts.jenkins.pipelines

def developPipeline(String name, idl_name) {
    // create a developer build pipeline
    Csc csc = new Csc()
    pipeline {
        agent {
            docker {
                alwaysPull true
                image 'lsstts/develop-env:develop'
                args "-u root --entrypoint=''"
            }
        }
        environment {
            user_ci = credentials('lsst-io')
            LTD_USERNAME="${user_ci_USR}"
            LTD_PASSWORD="${user_ci_PSW}"
            work_branches = "${GIT_BRANCH} ${CHANGE_BRANCH} develop"
            idl_name = ${idl_name}
            XML_REPORT= "jenkinsReport/report.xml"
            MODULE_NAME = ${module_name}
        }

        stages {
            stage ('Install Requirements and Update Branches') {
                steps {
                    withEnv(["HOME=${env.WORKSPACE}"]) {
                        sh """
                            source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
                            cd /home/saluser/repos/ts_xml
                            /home/saluser/.checkout_repo.sh ${work_branches}
                            git pull
                            cd /home/saluser/repos/ts_salobj
                            /home/saluser/.checkout_repo.sh ${work_branches}
                            git pull
                            cd /home/saluser/repos/ts_sal
                            /home/saluser/.checkout_repo.sh ${work_branches}
                            git pull
                            cd /home/saluser/repos/ts_idl
                            /home/saluser/.checkout_repo.sh ${work_branches}
                            git pull
                            make_idl_files.py ${idl_name}
                        """
                    }
                }
            }
            stage ('Unit Tests and Coverage Analysis') {
                withEnv(["HOME=${env.WORKSPACE}"]) {
                    sh 'source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...'
                    csc.install()
                    csc.test()
                }
            }
            stage ('Build and Upload documentation') {
                withEnv(["HOME=${env.WORKSPACE}"]) {
                    sh 'source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...'
                    csc.build_docs()
                    csc.upload_docs(${name})
                }
            }
        }
        post {
            always {
                withEnv(["HOME=${env.WORKSPACE}"]) {
                    sh 'chown -R 1003:1003 ${HOME}/'
                }

                junit 'jenkinsReport/*.xml'

                publishHTML (target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: 'htmlcov',
                reportFiles: 'index.html',
                reportName: "Coverage Report"
              ])
            }
            cleanup {
                deleteDir()
            }
        }
    }
}
