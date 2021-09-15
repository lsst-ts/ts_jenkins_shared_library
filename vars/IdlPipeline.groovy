import org.lsst.ts.jenkins.components.Csc

def call(){
    Csc csc = new Csc()
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
                args "--env LSST_DDS_DOMAIN=citest --env TS_XML_VERSION=${params.XML_Version} --env TS_SAL_VERSION=${params.SAL_Version} -u root --entrypoint=''"
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
            string(defaultValue: '7.1.0', description: 'The XML Version, exclude any preceeding "v" characters: X.Y.Z', name: 'XML_Version')
            string(defaultValue: '5.0.1', description: 'The ts_sal version, exclude any preceeding "v" characters: X.Y.Z', name: 'SAL_Version')
            choice choices: ['Release', 'Daily', 'Bleed'], description: 'The upstream build type (Bleed, Daily or Release). This determines from where to pull the RPM.', name: 'build_type'
            booleanParam(defaultValue: false, description: "Is this a development build?", name: 'develop')
            booleanParam(defaultValue: false, description: "Are we building the salobj conda package after this?", name: 'buildSalObjConda')
            booleanParam(defaultValue: false, description: "Are we going on to building the CSC package after salobj?", name: 'buildCSCConda')
        }
        stages {
            stage("Define TS_SAL_VERSION EnvVar") {
                steps {
                    echo 'Remove .pre### extension from TS_SAL_VERSION EnvVar.'
                    script {
                        sal_ver = sh(returnStdout: true, script: '#!/bin/bash -x\n' +
                            "echo ${params.SAL_Version} |sed 's/.pre[0-9]*//g'").trim()
                        sh "echo 'sal_ver: ${sal_ver}'"
                        env.TS_SAL_VERSION = "${sal_ver}"
                    }
                    echo "TS_SAL_VERSION: ${env.TS_SAL_VERSION}"
                }
            }
            stage("Create Conda Package") {
                when {
                    buildingTag()
                }
                steps {
                    withEnv(["WHOME=${env.WORKSPACE}", "TS_SAL_VERSION=${env.TS_SAL_VERSION}"]) {
                        script {
                            csc.build_idl_conda("main")
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
                            csc.build_idl_conda("dev")
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
                                csc.upload_conda("ts-idl","main","noarch")
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
                                csc.upload_conda("ts-idl","dev","noarch")
                            }
                        }
                    }
                }
            }//Push Dev
            stage("Create IDL pypi package") {
                steps {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        script {
                            csc.upload_pypi()
                        }
                    }
                }
            }//PyPy package
            stage("Trigger Salobj Conda Package build") {
                    when { expression { return env.buildSalObjConda.toBoolean() } }
                    steps {
                        script {
                            def RESULT = sh (returnStdout: true, script:
                            """
                            source /home/saluser/miniconda3/bin/activate > /dev/null &&
                            conda install -q -y setuptools_scm > /dev/null &&
                            python -c 'from setuptools_scm import get_version; print(get_version())'
                            """).trim()

                            idl_version = "${RESULT}"
                            echo "Starting the SalObj_Conda_package/develop job; sal_version: ${sal_ver}, xml_version: ${XML_Version}, idl_version: ${idl_version}, develop: ${develop}, buildCSCConda: ${buildCSCConda}"
                            build propagate: false, job: 'SalObj_Conda_package/develop', parameters: [
                                booleanParam(name: 'develop', value: "${develop}" ),
                                booleanParam(name: 'buildCSCConda', value: "${buildCSCConda}" ),
                                string(name: 'idl_version',value: "${idl_version}" ),
                                string(name: 'xml_version',value: "${XML_Version}" ),
                                string(name: 'sal_version',value: "${sal_ver}" )
                            ], wait: false
                    }
                }
            }//TriggerSalObj
        }//stages
        post {
            cleanup {
                sh """
                    chmod -R a+rw .
                    chown -R 1003:1003 .
                """
            }
            always {
                step([$class: 'Mailer',
                    notifyEveryUnstableBuild: false,
                    recipients: ["cwinslow@lsst.org", "tribeiro@lsst.org"]
                    sendToIndividuals: true])
            }
            regression {
                script {
                    def userId = "U6BCN6H43" //Colin
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                    def userId = "U72CH91L2" //Tiago
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }

            }
            fixed {
                script {
                    def userId = "U6BCN6H43" //Colin
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                    def userId = "U72CH91L2" //Tiago
                    slackSend(color: "danger", message: "<@$userId> ${JOB_NAME} has suffered a regression ${BUILD_URL}", channel: "#jenkins-builds, @$userId")
                }
            }
        }//post
    }//pipeline
}//def call
