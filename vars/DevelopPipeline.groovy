import org.lsst.ts.jenkins.components.Csc

def call(Map pipeline_args = [:]) {
    // create a developer build pipeline
    defaultArgs = [
        idl_names: [],
        build_all_idl: false,
        extra_packages: [],
        kickoff_jobs: [],
        slack_build_channel: "",
        has_doc_site: true,
        use_pyside6: false,
        require_git_lfs: false,
        require_scons: false
    ]
    pipeline_args = defaultArgs << pipeline_args
    if((!pipeline_args["name"]) || (!pipeline_args["module_name"] == null)) {
        error "Need to define name and module_name."
    }
    if(pipeline_args["module_name"] == "") {
	    pipeline_args["has_doc_site"] = false
    }
    if(pipeline_args["require_git_lfs"]) {
        label_str = 'Node3_4CPU'
    }
    else {
        label_str = 'Node1_4CPU || Node2_8CPU || Node3_4CPU'
    }
    Csc csc = new Csc()
    idl_string = ""
    if (!pipeline_args.idl_names.isEmpty()) {
        pipeline_args.idl_names.each { idl ->
            idl_string = idl_string.concat("${idl} ")
        }
    }
    // TODO: Remove this in DM-44795
    pipeline_args.use_pyside6 = Boolean.toString(pipeline_args.use_pyside6)
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
                args "--entrypoint='' --network kafka"
                label "${label_str}"
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
            USE_PYSIDE6 = "${pipeline_args.use_pyside6}"
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
            stage("Download git-lfs files") {
        when {
            expression {
                return pipeline_args.require_git_lfs
            }
        }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}/repo/${pipeline_args.name}"]) {
                        script {
                            csc.download_git_lfs_files()
                        }
                    }
                }
            }
            stage ('Install Requirements and Update Branches') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.update_container_branches()
                            if(idl_string) {
                                csc.make_idl_files(idl_string, pipeline_args.build_all_idl)
                            }
                        }
                    }
                }

            }
            stage ('Unit Tests and Coverage Analysis') {
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
                                if(!pipeline_args.name.equals("ts_pre_commit_conf")) {
                                    csc.setup_and_run_pre_commit()
                                }
                                csc.install()
                                csc.test(scons=pipeline_args.require_scons)
                            }
                        }
                    }
                }
            }
            stage ('Build and Upload documentation') {
		when {
		    expression {
			return pipeline_args.has_doc_site
		    }
		}
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
                                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                                    csc.build_docs()
                                    csc.upload_docs(pipeline_args.name)
                                }
                            }
                        }
                    }
                }
            }
            stage ('Kickoff jobs') {
		when {
		    expression {
			return pipeline_args.kickoff_jobs
		    }
		}
                steps {
                    script {
                        if(!pipeline_args.kickoff_jobs.isEmpty()) {
                            pipeline_args.kickoff_jobs.each { kickoff_job ->
                                build job: "LSST_Telescope-and-Site/${kickoff_job}", wait: false
                            }
                        }
                    }
                }
            }
        }
        post {
            always {
                dir("${env.WORKSPACE}/repo/${pipeline_args.name}") {
                    junit allowEmptyResults: true, testResults: 'jenkinsReport/*.xml'

                    publishHTML (target: [
                    allowMissing: true,
                    alwaysLinkToLastBuild: false,
                    keepAll: true,
                    reportDir: "htmlcov",
                    reportFiles: 'index.html',
                    reportName: "Coverage Report"
                    ])
                }
            }
            regression {
                script {
                    if(!pipeline_args.slack_build_channel.equals("")) {
                        slackSend(
                            color: "danger",
                            message: "${JOB_NAME} has suffered a regression ${BUILD_URL}",
                            channel: "#${pipeline_args.slack_build_channel}"
                        )
                    }
                }
            }
            fixed {
                script {
                    if(!pipeline_args.slack_build_channel.equals("")) {
                        slackSend(
                            color: "good",
                            message: "${JOB_NAME} has been fixed ${BUILD_URL}",
                            channel: "#${pipeline_args.slack_build_channel}"
                        )
                    }
                }
            }
            cleanup {
                deleteDir()
            }
        }
    }
}
