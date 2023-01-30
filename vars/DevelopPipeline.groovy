import org.lsst.ts.jenkins.components.Csc

def call(name, idl_name, module_name, required_idl=[], build_all_idl=false, extra_packages=[]) {
    // create a developer build pipeline
    Csc csc = new Csc()
    idl_string = "${idl_name} "
    if (!required_idl.isEmpty()) {
        required_idl.each { idl ->
            idl_string = idl_string.concat("${idl} ")
        }
    }
    properties(
        [
        buildDiscarder(
            logRotator(
                artifactDaysToKeepStr: '',
                artifactNumToKeepStr: '',
                daysToKeepStr: '14',
                numToKeepStr: '10',
            )
        ),
        // Make new builds terminate existing builds
        disableConcurrentBuilds(
            abortPrevious: true,
        )
        ]
    )
    pipeline {
        agent {
            docker {
                alwaysPull true
                image 'lsstts/develop-env:develop'
                args "--entrypoint=''"
            }
        }
        environment {
            user_ci = credentials('lsst-io')
            LTD_USERNAME="${user_ci_USR}"
            LTD_PASSWORD="${user_ci_PSW}"
            WORK_BRANCHES = "${GIT_BRANCH} ${CHANGE_BRANCH} develop"
            IDL_NAME = "${idl_name}"
            XML_REPORT= "jenkinsReport/report.xml"
            MODULE_NAME = "${module_name}"
        }

        stages {
            stage ('Install Requirements and Update Branches') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.update_container_branches()
                            csc.make_idl_files("${idl_string}", build_all_idl)
                        }
                    }
                }
                
            }
            stage ('Unit Tests and Coverage Analysis') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.install()
                            csc.test()
                        }
                    }
                }
            }
            stage ('Build and Upload documentation') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_docs()
                            csc.upload_docs("${name}")
                        }
                    }
                }
            }
            
        }
        post {
            always {
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
