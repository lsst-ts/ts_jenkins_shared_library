import org.lsst.ts.jenkins.components.Csc

def call(name, idl_name, module_name, pre_commit_flags="", required_idl=[], build_all_idl=false, extra_packages=[]) {
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
        options {
            skipDefaultCheckout()
        }
        environment {
            user_ci = credentials('lsst-io')
            WORK_BRANCHES = "${env.BRANCH_NAME} ${env.CHANGE_BRANCH} develop"
            IDL_NAME = "${idl_name}"
            XML_REPORT= "jenkinsReport/report.xml"
            MODULE_NAME = "${module_name}"
        }

        stages {
            stage ("Clone extra repos") {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            if (!extra_packages.isEmpty()) {
                                extra_packages.each { extra_package ->
                                    split = extra_package.split('/')
                                    org = split[0]
                                    package_name = split[1]
                                    branch_name = split[2]
                                    dir("${env.WORKSPACE}/ci/${package_name}") {
                                        git url: "https://github.com/${org}/${package_name}.git", branch: "${branch_name}"
                                    }
                                }
                            }
                            dir("${env.WORKSPACE}/repo/${name}") {
                                checkout scm
                            }
                        }
                    }
                }
            }
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
                            dir("${env.WORKSPACE}/repo/${name}") {
                                csc.setup_and_run_pre_commit(pre_commit_flags)
                                csc.install()
                                csc.test()
                            }
                        }
                    }
                }
            }
            stage ('Build and Upload documentation') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            dir("${env.WORKSPACE}/repo/${name}") {
                                csc.build_docs()
                                csc.upload_docs("${name}")
                            }
                        }
                    }
                }
            }
            
        }
        post {
            always {
                dir("${env.WORKSPACE}/repo/${name}") {
                    junit 'jenkinsReport/*.xml'

                    publishHTML (target: [
                    allowMissing: false,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: "htmlcov",
                    reportFiles: 'index.html',
                    reportName: "Coverage Report"
                    ])
                }
            }
            cleanup {
                deleteDir()
            }
        }
    }
}
