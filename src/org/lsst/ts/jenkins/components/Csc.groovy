package org.lsst.ts.jenkins.components

def email() {
    emails = ["ts-ataos":"tribeiro@lsst.org", "ts-atdome":"rowen@uw.edu", "ts-athexapod":"ecoughlin@lsst.org", "ts-atmcs": "rowen@uw.edu", "ts-atpneumatics": "rowen@uw.edu", "ts-atsprectrograph": "rowen@uw.edu", "ts-cbp": "ecoughlin@lsst.org", "ts-dimm": "wvreeven@lsst.org", "ts-dsm": "mreuter@lsst.org", "ts-eas": "wvreeven@lsst.org", "ts-electrometer": "ecoughlin@lsst.org", "ts-ess": "wvreeven@lsst.org", "ts-externalscripts": "tribeiro@lsst.org", "ts-fiberspectrograph": "rowen@uw.edu", "ts-genericcamera": "wvreeven@lsst.org", "ts-hexrotcomm": "rowen@uw.edu", "ts-m2": "tribeiro@lsst.org", "ts-mtdome": "rowen@uw.edu", "ts-mtdometrajectory": "rowen@uw.edu", "ts-mteec": "wvreeven@lsst.org", "ts-mthexapod": "rowen@uw.edu", "ts-mtmount": "rowen@uw.edu", "ts-mtrotator": "rowen@uw.edu", "ts-observatorycontrol":"tribeiro@lsst.org", "ts-salkafka": "tribeiro@lsst.org", "ts-simactuators": "rowen@uw.edu", "ts-standardscripts": "tribeiro@lsst.org", "ts-tunablelaser": "ecoughlin@lsst.org", "ts-watcher": "rowen@uw.edu", "ts-weatherstation": "wvreeven@lsst.org"]
    return emails
}

def build_docs() {
    // Build the documentation
    sh "package-docs build"
}

def upload_docs(name) {
    // upload the documentation
    // Takes the product name as an argument
    sh "ltd upload --product ${name} --git-ref ${GIT_BRANCH} --dir doc/_build/html"
}

def install() {
    // Install the development requirements
    sh "pip install .[dev]"
}

def test() {
    // Run the tests
    sh "pytest --cov-report html --cov=${env.MODULE_NAME} --junitxml=${env.XML_REPORT}"
}

def build_csc_conda(label) {
    // Build the conda package
    sh """
        cd ${HOME}/conda
        source /home/saluser/miniconda3/bin/activate
        conda config --add channels conda-forge
        conda config --add channels lsstts
        source ${OSPL_HOME}/release.com
        conda build -c lsstts/label/${label} --variants "{salobj_version: ${params.salobj_version}, idl_version: ${params.idl_version}}" --prefix-length 100 .
    """
}

def build_idl_conda(label) {
    sh """
        echo 'The XML version: ${params.XML_Version}'
        echo 'The SAL version: ${params.SAL_Version}'
        echo 'The BuildType: ${params.build_type}'
    """
    echo "The TS_SAL_VERSION EnvVar: ${env.TS_SAL_VERSION}"
    if ( params.build_type == "Bleed" ) {
        rpm_repo = "lsst-ts-bleed"
    } else if ( params.build_type == "Daily" ) {
        rpm_repo = "lsst-ts-daily"
    } else if ( params.build_type == "Release" ) {
        rpm_repo = "lsst-ts"
    } else {
        currentBuild.result = 'ABORTED'
        error('Please properly define the build_type parameter.')
    }
    timeout(5) {
        waitUntil(initialRecurrencePeriod: 15000, quiet: true) {
            // The RPMs can take a few minutes to appear in the repo. This will wait 5 minutes then fail the build if the RPM is not found.
            def r = sh (
                script: "yum clean all ; " +
                    " yum list --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el7.x86_64 ",
                returnStatus: true
            )
            return r == 0
        }
    }
    sh """
        # yum clean all ; yum makecache fast; yum update ; 
        yum install -y --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el7.x86_64
        cd ${HOME}/conda
        source /home/saluser/miniconda3/bin/activate 
        conda config --add channels conda-forge
        conda config --add channels lsstts
        source ${OSPL_HOME}/release.com
        conda build -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_salobj_conda(label, concatVersion) {
    sh """
        cd ${HOME}/conda
        source /home/saluser/miniconda3/bin/activate
        conda config --add channels conda-forge
        conda config --add channels lsstts
        source ${OSPL_HOME}/release.com
        conda build -c lsstts/label/${label} --variants "{idl_version: ${concatVersion}}" --prefix-length 100 .
    """
}

def upload_conda(name, label) {
    // Upload the conda package
    // Takes the name of the package and a label
    if(name.contains("ts-salobj")) {
        arch = "noarch"
    } else {
        arch = "linux-64"
    }
    sh """
        source /home/saluser/miniconda3/bin/activate
        anaconda upload -u lsstts --label ${label} --force /home/saluser/miniconda3/conda-bld/${arch}/${name}*.tar.bz2
    """
}

def upload_pypi() {
    sh """
        source /home/saluser/miniconda3/bin/activate
        source ${OSPL_HOME}/release.com
        pip install --upgrade twine
        python setup.py sdist bdist_wheel
        python -m twine upload -u ${env.PYPI_CREDS_USR} -p ${env.PYPI_CREDS_PSW} dist/*
    """ 
}

return this
