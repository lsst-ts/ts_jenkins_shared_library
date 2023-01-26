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
    }
    stages{
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
