import org.lsst.ts.jenkins.components.Csc

def call(config_repo){
    Csc csc = new Csc()
    arg_str = ""
    clone_str = ""
    if (!config_repo.isEmpty()) {
        config_repo.each{ repo ->
            arg_str = arg_str.concat("--env ${repo.toUpperCase()}_DIR=/home/saluser/${repo} ")
            clone_str = clone_str.concat("git clone https://github.com/lsst-ts/${repo}\n")
            println(arg_str)
            println(clone_str)
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
        options {
            disableConcurrentBuilds()
        }
        environment {
            PYPI_CREDS = credentials("pypi")
        }
        parameters {
            string(defaultValue: '\'\'', description: 'The IDL Version', name: 'idl_version')
            string(defaultValue: '\'\'', description: 'The XML Version', name: 'xml_version')
            string(defaultValue: '\'\'', description: 'The SAL Version', name: 'sal_version')
            booleanParam(defaultValue: false, description: "Are we going on to building the CSC package after salobj?", name: 'buildCSCConda')

        }
        stages {
            stage("Clone configuration repository") {
                when {
                    not {
                        expression { config_repo.isEmpty() }
                    }
                }
                steps {
                    script{
                        if ((params.idl_version == '\'\'') && (params.xml_version == '\'\'') && (params.sal_version == '\'\'')) {
                            concatVersion = ''
                        }
                        else {
                            concatVersion = params.idl_version  + '_' + params.xml_version + '_' + params.sal_version
                        }
                    }
                    sh """
                        echo "The concatenated IDL_XML_SAL version: ${concatVersion}"
                        cd /home/saluser
                        ${clone_str}
                    """
                }
            }//Clone config
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_salobj_conda("main", "${concatVersion}")
                        }
                    }
                }
            }//Create Release
            stage("Create Conda dev package") {
                when {
                    not {
                        buildingTag()
                    }
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}"]) {
                        script {
                            csc.build_salobj_conda("dev", "${concatVersion}")
                        }
                    }
                }
            }//Create Dev
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
                                csc.upload_conda("ts-salobj","main","noarch")
                            }
                        }
                    }
                }
            }//Push Release
            stage("Push Conda Dev package") {
                when {
                    not {
                        buildingTag()
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
                                csc.upload_conda("ts-salobj","dev","noarch")
                            }
                        }
                    }
                }
            }//Push Dev
            stage("Create SALObj pypi package") {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        script {
                            csc.upload_pypi()
                        }
                    }
                }
            }//PyPy package
            stage("Trigger CSC Conda Broker Job") {
                    when { expression { return env.buildCSCConda.toBoolean() } }
                    steps {
                        script {
                            def SALOBJVERSION = sh (returnStdout: true, script:
                            """
                            source /home/saluser/miniconda3/bin/activate > /dev/null &&
                            conda install -q -y setuptools_scm > /dev/null &&
                            python -c 'from setuptools_scm import get_version; print(get_version())'
                            """).trim()


                            echo "Starting the CSC_Conda_broker/develop job; idl_version: ${idl_version}, salobj_version: ${SALOBJVERSION}, XML_Version: ${xml_version}, SAL_Version: ${sal_version}"
                            build job: 'CSC_Conda_Broker', parameters: [\
                                string(name: 'idl_version', value: idl_version ), \
                                string(name: 'salobj_version', value: SALOBJVERSION ), \
                                string(name: 'XML_Version', value: xml_version ), \
                                string(name: 'SAL_Version', value: sal_version ), \
                                booleanParam(name: 'Bleed', value: false), \
                                booleanParam(name: 'Daily', value: true), \
                                booleanParam(name: 'Release', value: false), \
                                string(name: 'Branch', value: 'develop')]
                    }
                }
            }//Trigger Broker
        }//stages
        post {
            cleanup {
                withEnv(["WHOME=${env.WORKSPACE}"]) {
                    sh 'chown -R 1003:1003 ${WHOME}/'
                }
            }
            always {
                step([$class: 'Mailer',
                    notifyEveryUnstableBuild: false,
                    recipients: "cwinslow@lsst.org",
                    sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = "U6BCN6H43"
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = "U6BCN6H43"
                    slackSend(color: "good", message: "<@$userId> ${JOB_NAME} has been fixed ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }//post
    }//pipeline
}//def call
