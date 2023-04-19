import org.lsst.ts.jenkins.components.Csc

def call(@Deprecated config_repo=[], @Deprecated name="", @Deprecated module_name="", @ Deprecated arch="linux-64", Map pipeline_args=[:]){
    // Create a conda build pipeline
    if(config_repo) {
        echo 'config_repo positional parameter is deprecated. Use named parameter config_repo: [x] instead.'
    }
    if(name) {
        echo 'name positional parameter is deprecated. Use named parameter name: x instead.'
    }
    if(module_name) {
        echo 'module_name positional parameter is deprecated. Use named parameter module_name: x instead.'
    }
    if(arch) {
        echo 'arch positional parameter is deprecated. Use named parameter arch: x instead.'
    }
    if(pipeline_args["config_repo"]){
        config_repo = pipeline_args.config_repo
    }
    if(pipeline_args["name"]){
        name = pipeline_args.name
    }
    if(pipeline_args["module_name"]){
        module_name = pipeline_args.module_name
    }
    if(pipeline_args["arch"]){
        arch=pipeline_args.arch
    }
    else {
    	arch="linux-64"
    }
    Csc csc = new Csc()
    registry_url = "https://ts-dockerhub.lsst.org"
    registry_credentials_id = "nexus3-lsst_jenkins"
    image_value = "ts-dockerhub.lsst.org/conda_package_builder:latest"
    emails = csc.email()
    slack_ids = csc.slack_id()
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
                image image_value
                alwaysPull true
                label "${params.build_agent}"
                args arg_str.concat("--env LSST_DDS_DOMAIN=citest --entrypoint=''")
                registryUrl registry_url
                registryCredentialsId registry_credentials_id
            }
        }
        parameters {
            string(name: 'idl_version', defaultValue: '\'\'', description: 'The version of the IDL Conda package.')
            string(name: 'salobj_version', defaultValue: '\'\'', description: 'The version of the salobj Conda package.')
            choice choices: ['CSC_Conda_Node', 'Node1_4CPU', 'Node2_8CPU', 'Node3_4CPU'], description: 'Select the build agent', name: 'build_agent'
        }
        environment {
            package_name = "${name}"
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
            stage("Download git-lfs files"){
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.download_git_lfs_files()
                        }
                    }
                }
            }
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
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
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
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
                step([$class: 'Mailer', recipients: emails[name] ?: emails['default'], notifyEveryUnstableBuild: false, sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = slack_ids[name] ?: slack_ids['default']
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = slack_ids[name] ?: slack_ids['default']
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }
    }
}
