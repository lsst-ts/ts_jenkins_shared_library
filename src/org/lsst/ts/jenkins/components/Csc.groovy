package org.lsst.ts.jenkins.components

def email() {
    emails = [  "ts-ataos":"tribeiro@lsst.org",
                "ts-atdome":"rowen@uw.edu",
                "ts-atdometrajectory":"rowen@uw.edu",
                "ts-athexapod":"ecoughlin@lsst.org",
                "ts-atmcs-simulator": "rowen@uw.edu",
                "ts-atpneumaticssimulator": "rowen@uw.edu",
                "ts-atspec": "tribeiro@lsst.org",
                "ts-atwhitelightsource": "rowen@uw.edu",
                "ts-cbp": "ecoughlin@lsst.org",
                "ts-criopy": "tribeiro@lsst.org",
                "ts-ddsconfig": "tribeiro@lsst.org",
                "ts-dimm": "wvreeven@lsst.org",
                "ts-dsm": "mreuter@lsst.org",
                "ts-eas": "wvreeven@lsst.org",
                "ts-electrometer": "ecoughlin@lsst.org",
                "ts-ess": "wvreeven@lsst.org",
                "ts-ess-common": "wvreeven@lsst.org",
                "ts-ess-controller": "wvreeven@lsst.org",
                "ts-externalscripts": "tribeiro@lsst.org",
                "ts-fiberspectrograph": "rowen@uw.edu",
                "ts-genericcamera": "wvreeven@lsst.org",
                "ts-hexrotcomm": "rowen@uw.edu",
                "ts-hvac": "wvreeven@lsst.org",
                "ts-idl": "tribeiro@lsst.org",
                "ts-integrationtests": "rbovill@lsst.org",
                "ts-m2": "tribeiro@lsst.org",
                "ts-mtdome": "wvreeven@lsst.org",
                "ts-mtdometrajectory": "rowen@uw.edu",
                "ts-mteec": "wvreeven@lsst.org",
                "ts-mthexapod": "rowen@uw.edu",
                "ts-mtmount": "rowen@uw.edu",
                "ts-mtrotator": "rowen@uw.edu",
                "ts-observatory-control": "tribeiro@lsst.org",
                "ts-salkafka": "tribeiro@lsst.org",
                "ts-salobj": "tribeiro@lsst.org",
                "ts-scriptqueue": "tribeiro@lsst.org",
                "ts-simactuators": "rowen@uw.edu",
                "ts-standardscripts": "tribeiro@lsst.org",
                "ts-dds": "tribeiro@lsst.org",
                "ts-tunablelaser": "ecoughlin@lsst.org",
                "ts-watcher": "rowen@uw.edu",
                "ts-weatherstation": "wvreeven@lsst.org",
                "ts-scheduler": "tribeiro@lsst.org",
                "ts-pmd": "ecoughlin@lsst.org" ]
    return emails
}

def slack_id() {
    String eric = "UAS4QHFSB"
    String michael = "U2JPDUE86"
    String rob = "U2NF4GWV8"
    String russell = "U2JPAP0F6"
    String tiago = "U72CH91L2"
    String wouter = "URY8ACN4S"
    slack_ids = ["ts-ataos": tiago,
                "ts-atdome": russell,
                "ts-atdometrajectory": russell,
                "ts-athexapod": eric,
                "ts-atmcs-simulator": russell,
                "ts-atpneumaticssimulator": russell,
                "ts-atspec": tiago,
                "ts-atwhitelightsource": russell,
                "ts-cbp": eric,
                "ts-criopy": tiago,
                "ts-ddsconfig": tiago,
                "ts-dimm": wouter,
                "ts-dsm": michael,
                "ts-eas": wouter,
                "ts-electrometer": eric,
                "ts-ess": wouter,
                "ts-ess-common": wouter,
                "ts-ess-controller": wouter,
                "ts-externalscripts": tiago,
                "ts-fiberspectrograph": russell,
                "ts-genericcamera": wouter,
                "ts-hexrotcomm": russell,
                "ts-hvac": wouter,
                "ts-idl": tiago,
                "ts-integrationtests": rob,
                "ts-m2": tiago,
                "ts-mtdome": wouter,
                "ts-mtdometrajectory": russell,
                "ts-mteec": wouter,
                "ts-mthexapod": russell,
                "ts-mtmount": russell,
                "ts-mtrotator": russell,
                "ts-observatory-control": tiago,
                "ts-salkafka": tiago,
                "ts-salobj": tiago,
                "ts-scriptqueue": tiago,
                "ts-simacutuators": russell,
                "ts-standardscripts": tiago,
                "ts-dds": tiago,
                "ts-tunablelaser": eric,
                "ts-watcher": russell,
                "ts-weatherstation": wouter,
                "ts-pmd": eric,
                "ts-scheduler": tiago,
                "rubin-sim": tiago]
    return slack_ids
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
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
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
                script: "yum clean all ; yum list --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el7.x86_64 ",
                returnStatus: true
            )
            return r == 0
        }
    }
    sh """
        # yum clean all ; yum makecache fast; yum update ;
        yum install -y --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el7.x86_64
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_integrationtests_conda(label) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_salobj_conda(label, concatVersion) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build -c lsstts/label/${label} --variants "{idl_version: ${concatVersion}}" --prefix-length 100 .
    """
}

def download_git_lfs_files(){
    sh """
        cd ${WHOME}/
        git lfs install
        git lfs fetch --all
        git lfs checkout
    """
}

def upload_conda(name, label, arch) {
    // Upload the conda package
    // Takes the name of the package and a label
    if ((arch=="linux-aarch64") || (arch=="noarch") || (arch=="linux-64")) {
        sh """
            source /home/saluser/.setup.sh
            anaconda upload -u lsstts --label ${label} --force /home/saluser/miniconda3/conda-bld/${arch}/${name}*.tar.bz2
        """
    }
    else {
        currentBuild.result = 'ABORTED'
        error('Please properly define the arch parameter.')
    }
}

def upload_pypi() {
    sh """
        source /home/saluser/.setup.sh
        pip install --upgrade twine
        python setup.py sdist bdist_wheel
        python -m twine upload -u ${env.PYPI_CREDS_USR} -p ${env.PYPI_CREDS_PSW} dist/*
    """
}

// Return branch name. If changeTarget isn't defined, use branchName.
def getBranchName(changeTarget, branchName) {
    def branch = (changeTarget != "null") ? changeTarget : branchName
    print("!!! changeTarget: " + changeTarget + " branchName: " + branchName + " -> Returning " + branch + " !!!\n")
    return branch
}


return this
