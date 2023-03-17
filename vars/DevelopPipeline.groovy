import org.lsst.ts.jenkins.components.Csc

def call(Map pipeline_args = [:]) {
    // create a developer build pipeline
    defaultArgs = [pre_commit_flags: "", required_idl: [], build_all_idl: false, extra_packages: []]
    pipeline_args = defaultArgs << pipeline_args
    Csc csc = new Csc()
    idl_string = "${pipeline_args.idl_name} "
    if (!pipeline_args.required_idl.isEmpty()) {
        pipeline_args.required_idl.each { idl ->
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
            IDL_NAME = "${pipeline_args.idl_name}"
            XML_REPORT= "jenkinsReport/report.xml"
            MODULE_NAME = "${pipeline_args.module_name}"
        }

        stages {
            stage ("Clone extra repos") {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            if (!pipeline_args.extra_packages.isEmpty()) {
                                pipeline_args.extra_packages.each { extra_package ->
                                    split = extra_package.split('/')
                                    org = split[0]
                                    package_name = split[1]
                                    dir("${env.WORKSPACE}/ci/${package_name}") {
                                        git url: "https://github.com/${org}/${package_name}.git", branch: "develop"
                                    }
                                }
                            }
                            dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
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
                            csc.make_idl_files(idl_string, pipeline_args.build_all_idl)
                        }
                    }
                }
                
            }
            stage ('Unit Tests and Coverage Analysis') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
                                csc.setup_and_run_pre_commit(pipeline_args.pre_commit_flags)
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
                            dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
                                csc.build_docs()
                                csc.upload_docs(pipeline_args.name)
                            }
                        }
                    }
                }
            }
            
        }
        post {
            always {
                dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
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
