pipeline{
    agent{
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
        service_account = credentials('14e4c262-1fb1-4b73-b395-5fe617420c85')
        service_user="${service_account_USR}"
        service_pass="${service_account_PSW}"
    }
    stages{
        stage("Verify Jenkinsfile.broker syntax"){
            steps{
                sh """
                    output=`curl -s -X POST --user '$service_user:$service_pass' -F 'jenkinsfile=<Jenkinsfile.broker' '${JENKINS_URL}pipeline-model-converter/validate'`
                    echo \$output
                    if [[ "\$output" == *"Jenkinsfile successfully validated."* ]]; then
                        echo "Test passed"
                    else
                        echo "Jenkinsfile.broker: Invalid Syntax"
                        exit 1
                    fi
                """
            }
        }
        stage("Build and Upload Documentation"){
            steps{
                sh """
                    source /home/saluser/.setup_dev.sh
                    pip install -r doc/requirements.txt
                    package-docs -d doc build
                    ltd upload --product ts-jenkins-shared-library --git-ref ${GIT_BRANCH} --dir doc/_build/html
                """
            }
        }
    }
    post{
       cleanup {
            deleteDir()
        }
    }
}
