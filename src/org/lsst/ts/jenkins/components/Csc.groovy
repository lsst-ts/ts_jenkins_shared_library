package org.lsst.ts.jenkins.components

def email() {
    emails = [  "ts-ataos": "tribeiro@lsst.org",
                "ts-atdome": "rowen@uw.edu",
                "ts-atdometrajectory": "rowen@uw.edu",
                "ts-athexapod": "ecoughlin@lsst.org",
                "ts-atmcs-simulator": "rowen@uw.edu",
                "ts-atpneumaticssimulator": "rowen@uw.edu",
                "ts-atspec": "tribeiro@lsst.org",
                "ts-atwhitelightsource": "rowen@uw.edu",
                "ts-cbp": "ecoughlin@lsst.org",
                "ts-conda-build": "wvreeven@lsst.org",
                "ts-conda-build-aarch64": "wvreeven@lsst.org",
                "ts-criopy": "tribeiro@lsst.org",
                "ts-cycle": "wvreeven@lsst.org",
                "ts-dds": "tribeiro@lsst.org",
                "ts-ddsconfig": "tribeiro@lsst.org",
                "ts-develop": "wvreeven@lsst.org",
                "ts-develop-aarch64": "wvreeven@lsst.org",
                "ts-dimm": "dmills@lsst.org",
                "ts-dream": "pkubanek@lsst.org",
                "ts-dsm": "mreuter@lsst.org",
                "ts-eas": "wvreeven@lsst.org",
                "ts-electrometer": "ecoughlin@lsst.org",
                "ts-ess-csc": "wvreeven@lsst.org",
                "ts-ess-common": "wvreeven@lsst.org",
                "ts-ess-controller": "wvreeven@lsst.org",
                "ts-externalscripts": "tribeiro@lsst.org",
                "ts-fiberspectrograph": "rowen@uw.edu",
                "ts-genericcamera": "pkubanek@lsst.org",
                "ts-hexrotcomm": "rowen@uw.edu",
                "ts-hvac": "wvreeven@lsst.org",
                "ts-idl": "tribeiro@lsst.org",
                "ts-integrationtests": "rbovill@lsst.org",
                "ts-m2": "tribeiro@lsst.org",
                "ts-mtaircompressor": "pkubanek@lsst.org",
                "ts-mtdome": "wvreeven@lsst.org",
                "ts-mtdometrajectory": "rowen@uw.edu",
                "ts-mteec": "wvreeven@lsst.org",
                "ts-mthexapod": "rowen@uw.edu",
                "ts-mtmount": "rowen@uw.edu",
                "ts-mtrotator": "rowen@uw.edu",
                "ts-observatory-control": "tribeiro@lsst.org",
                "ts-pmd": "ecoughlin@lsst.org" ,
                "ts-salkafka": "tribeiro@lsst.org",
                "ts-salobj": "tribeiro@lsst.org, wvreeven@lsst.org",
                "ts-scheduler": "tribeiro@lsst.org",
                "ts-scriptqueue": "tribeiro@lsst.org",
                "ts-simactuators": "rowen@uw.edu",
                "ts-standardscripts": "tribeiro@lsst.org",
                "ts-tcpip": "rowen@uw.edu",
                "ts-tunablelaser": "ecoughlin@lsst.org",
                "ts-utils": "tribeiro@lsst.org",
                "ts-watcher": "rowen@uw.edu",
                "ts-weatherstation": "wvreeven@lsst.org"]
    return emails
}

def slack_id() {
    String dave = "U2N3R1J5R"
    String eric = "UAS4QHFSB"
    String michael = "U2JPDUE86"
    String petr = "UURCFJJVB"
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
                "ts-conda-build": wouter,
                "ts-conda-build-aarch64": wouter,
                "ts-criopy": tiago,
                "ts-cycle": wouter,
                "ts-dds": tiago,
                "ts-ddsconfig": tiago,
                "ts-develop": wouter,
                "ts-develop-aarch64": wouter,
                "ts-dimm": dave,
                "ts-dream": petr,
                "ts-dsm": michael,
                "ts-eas": wouter,
                "ts-electrometer": eric,
                "ts-ess-csc": wouter,
                "ts-ess-common": wouter,
                "ts-ess-controller": wouter,
                "ts-externalscripts": tiago,
                "ts-fiberspectrograph": russell,
                "ts-genericcamera": petr,
                "ts-hexrotcomm": russell,
                "ts-hvac": wouter,
                "ts-idl": tiago,
                "ts-integrationtests": rob,
                "ts-m2": tiago,
                "ts-mtaircompressor": petr,
                "ts-mtdome": wouter,
                "ts-mtdometrajectory": russell,
                "ts-mteec": wouter,
                "ts-mthexapod": russell,
                "ts-mtmount": russell,
                "ts-mtrotator": russell,
                "ts-observatory-control": tiago,
                "ts-pmd": eric,
                "ts-salkafka": tiago,
                "ts-salobj": wouter,
                "ts-scheduler": tiago,
                "ts-scriptqueue": tiago,
                "ts-simactuators": russell,
                "ts-standardscripts": tiago,
                "ts-utils": tiago,
                "ts-tcpip": russell,
                "ts-tunablelaser": eric,
                "ts-watcher": russell,
                "ts-weatherstation": wouter,
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
    // if the boa package is installed, then mambabuild can be used, otherwise the normal conda build
    sh """
        #!/bin/bash
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        pkg="boa"
        build="build"
        if conda list \$pkg | grep -q "^\$pkg "; then
          build="mambabuild"
        fi
        conda \$build -c lsstts/label/${label} --variants "{salobj_version: ${params.salobj_version}, idl_version: ${params.idl_version}}" --prefix-length 100 .
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
                script: "yum clean all ; yum list --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el8.x86_64 ",
                returnStatus: true
            )
            return r == 0
        }
    }
    sh """
        # yum clean all ; yum makecache fast; yum update ;
        yum install -y --enablerepo=${rpm_repo} ts_sal_runtime-${params.XML_Version}-${params.SAL_Version}.el8.x86_64
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        pkg="boa"
        build="build"
        if conda list \$pkg | grep -q "^\$pkg "; then
          build="mambabuild"
        fi
        conda \$build -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_integrationtests_conda(label) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        pkg="boa"
        build="build"
        if conda list \$pkg | grep -q "^\$pkg "; then
          build="mambabuild"
        fi
        conda \$build -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_salobj_conda(label, concatVersion) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --add channels conda-forge
        conda config --add channels lsstts
        pkg="boa"
        build="build"
        if conda list \$pkg | grep -q "^\$pkg "; then
          build="mambabuild"
        fi
        conda \$build -c lsstts/label/${label} --variants "{idl_version: ${concatVersion}}" --prefix-length 100 .
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
        if (label == "rc") {
            label_option = "--label ${label} --label main"
        } else {
            label_option = "--label ${label}"
        }
        sh """
            source /home/saluser/.setup.sh
            anaconda upload -u lsstts ${label_option} --force /home/saluser/miniconda3/conda-bld/${arch}/${name}*.tar.bz2
        """
    }
    else {
        currentBuild.result = 'ABORTED'
        error('Please properly define the arch parameter.')
    }
}

// Return branch name. If changeTarget isn't defined, use branchName.
def getBranchName(changeTarget, branchName) {
    def branch = (changeTarget != "null") ? changeTarget : branchName
    print("!!! changeTarget: " + changeTarget + " branchName: " + branchName + " -> Returning " + branch + " !!!\n")
    return branch
}


return this
