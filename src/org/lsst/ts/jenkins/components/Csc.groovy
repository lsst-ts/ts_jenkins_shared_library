package org.lsst.ts.jenkins.components

def email() {
    emails = [  "default": "rubinobs-tssw-worker@lsst.org",
                "love-commander": "saranda@lsst.org",
                "love-producer": "saranda@lsst.org",
                "ts-astrosky-model": "tribeiro@lsst.org",
                "ts-ataos": "tribeiro@lsst.org",
                "ts-atbuilding-csc": "bbrondel@lsst.org",
                "ts-atbuilding-vents": "bbrondel@lsst.org",
                "ts-atdome": "wvreeven@lsst.org",
                "ts-atdometrajectory": "wvreeven@lsst.org",
                "ts-athexapod": "ecoughlin@lsst.org",
                "ts-atmcs-simulator": "wvreeven@lsst.org",
                "ts-atpneumaticssimulator": "wvreeven@lsst.org",
                "ts-atspec": "tribeiro@lsst.org",
                "ts-attcpip": "wvreeven@lsst.org",
                "ts-atwhitelight": "ecoughlin@lsst.org",
                "ts-audio-broadcaster": "saranda@lsst.org",
                "ts-audiotrigger": "ecoughlin@lsst.org",
                "ts-authorize": "wvreeven@lsst.org",
                "ts-cbp": "ecoughlin@lsst.org",
                "ts-conda-build": "wvreeven@lsst.org",
                "ts-conda-build-aarch64": "wvreeven@lsst.org",
                "ts-conda-build-linux-aarch64": "wvreeven@lsst.org",
                "ts-criopy": "tribeiro@lsst.org",
                "ts-cycle": "wvreeven@lsst.org",
                "ts-dateloc": "tribeiro@lsst.org",
                "ts-dds": "tribeiro@lsst.org",
                "ts-ddsconfig": "tribeiro@lsst.org",
                "ts-develop": "wvreeven@lsst.org",
                "ts-develop-aarch64": "wvreeven@lsst.org",
                "ts-dimm": "dmills@lsst.org",
                "ts-dream": "bbrondel@lsst.org",
                "ts-dsm": "mreuter@lsst.org",
                "ts-eas": "wvreeven@lsst.org",
                "ts-electrometer": "ecoughlin@lsst.org",
                "ts-ess-csc": "wvreeven@lsst.org",
                "ts-ess-common": "wvreeven@lsst.org",
                "ts-ess-controller": "wvreeven@lsst.org",
                "ts-ess-earthquake": "wvreeven@lsst.org",
                "ts-ess-epm": "saranda@lsst.org",
                "ts-ess-labjack": "wvreeven@lsst.org",
                "ts-ess-ringss": "bbrondel@lsst.org",
                "ts-externalscripts": "tribeiro@lsst.org",
                "ts-fbs-utils": "tribeiro@lsst.org",
                "ts-fiberspectrograph": "ecoughlin@lsst.org",
                "ts-genericcamera": "pkubanek@lsst.org",
                "ts-gis": "ecoughlin@lsst.org",
                "ts-guitool": "ttsai@lsst.org",
                "ts-hexgui": "ttsai@lsst.org",
                "ts-hexrotcomm": "ecoughlin@lsst.org",
                "ts-hvac": "wvreeven@lsst.org",
                "ts-integrationtests": "rbovill@lsst.org",
                "ts-linearstage": "ecoughlin@lsst.org",
                "ts-m2": "tribeiro@lsst.org",
                "ts-m2com": "ttsai@lsst.org",
                "ts-m2gui": "ttsai@lsst.org",
                "ts-lasertracker": "dmills@lsst.org, pkubanek@lsst.org",
                "ts-ledprojector": "ecoughlin@lsst.org",
                "ts-mtaircompressor": "pkubanek@lsst.org",
                "ts-mtdome": "wvreeven@lsst.org",
                "ts-mtdomecom": "wvreeven@lsst.org",
                "ts-mtdomegui": "ttsai@lsst.org",
                "ts-mtdometrajectory": "wvreeven@lsst.org",
                "ts-mteec": "wvreeven@lsst.org",
                "ts-mthexapod": "ttsai@lsst.org",
                "ts-mtmount": "dmills@lsst.org",
                "ts-mtreflector": "ecoughlin@lsst.org",
                "ts-mtrotator": "ttsai@lsst.org",
                "ts-observatory-control": "tribeiro@lsst.org",
                "ts-observatory-model": "tribeiro@lsst.org",
                "ts-observing": "tribeiro@lsst.org",
                "ts-ofc": "tribeiro@lsst.org, ttsai@lsst.org",
                "ts-pmd": "ecoughlin@lsst.org" ,
                "ts-pre-commit-config": "wvreeven@lsst.org",
                "ts-rotgui": "ttsai@lsst.org",
                "ts-salkafka": "tribeiro@lsst.org",
                "ts-salobj": "tribeiro@lsst.org, wvreeven@lsst.org",
                "ts-scheduler": "tribeiro@lsst.org",
                "ts-scriptqueue": "tribeiro@lsst.org",
                "ts-simactuators": "tribeiro@lsst.org",
                "ts-standardscripts": "tribeiro@lsst.org",
                "ts-tcpip": "wvreeven@lsst.org",
                "ts-tunablelaser": "ecoughlin@lsst.org",
                "ts-utils": "tribeiro@lsst.org",
                "ts-watcher": "pkubanek@lsst.org",
                "ts-weatherforecast": "ecoughlin@lsst.org",
                "ts-xml": "rbovill@lsst.org",
                "vanward": "mreuter@lsst.org"]
    return emails
}

def slack_id() {
    String brianb = "U07PB4HGR3P"
    String dave = "U07Q7P9QA06"
    String eric = "U07Q1MF9TKJ"
    String michael = "U07N613SX19"
    String petr = "U07PA4Y8H4K"
    String rob = "U07PA6ALBDY"
    String sebastian = "U07QPRAJHHQ"
    String tiago = "U07N2QKCTJP"
    String tewei = "U07PBTMJ4BC"
    String wouter = "U07PMHC954K"
    slack_ids = ["default": rob,
                "love-commander": sebastian,
                "love-producer": sebastian,
                "ts-astrosky-model": tiago,
                "ts-ataos": tiago,
                "ts-atbuilding-csc": brianb,
                "ts-atbuilding-vents": brianb,
                "ts-atdome": wouter,
                "ts-atdometrajectory": wouter,
                "ts-athexapod": eric,
                "ts-atmcs-simulator": wouter,
                "ts-atpneumaticssimulator": wouter,
                "ts-atspec": tiago,
                "ts-attcpip": wouter,
                "ts-atwhitelight": eric,
                "ts-authorize": wouter,
                "ts-audio-broadcaster": sebastian,
                "ts-audiotrigger": eric,
                "ts-cbp": eric,
                "ts-conda-build": wouter,
                "ts-conda-build-aarch64": wouter,
                "ts-conda-build-linux-aarch64": wouter,
                "ts-criopy": tiago,
                "ts-cycle": wouter,
                "ts-dateloc": tiago,
                "ts-dds": tiago,
                "ts-ddsconfig": tiago,
                "ts-develop": wouter,
                "ts-develop-aarch64": wouter,
                "ts-dimm": dave,
                "ts-dream": brianb,
                "ts-dsm": michael,
                "ts-eas": wouter,
                "ts-electrometer": eric,
                "ts-ess-csc": wouter,
                "ts-ess-common": wouter,
                "ts-ess-controller": wouter,
                "ts-ess-earthquake": wouter,
                "ts-ess-epm": sebastian,
                "ts-ess-labjack": wouter,
                "ts-ess-ringss": brianb,
                "ts-externalscripts": tiago,
                "ts-fbs-utils": tiago,
                "ts-fiberspectrograph": eric,
                "ts-genericcamera": petr,
                "ts-gis": eric,
                "ts-guitool": tewei,
                "ts-hexgui": tewei,
                "ts-hexrotcomm": eric,
                "ts-hvac": wouter,
                "ts-integrationtests": rob,
                "ts-lasertracker": petr,
                "ts-linearstage": eric,
                "ts-ledprojector": eric,
                "ts-m2": tiago,
                "ts-m2com": tewei,
                "ts-m2gui": tewei,
                "ts-mtaircompressor": petr,
                "ts-mtdome": wouter,
                "ts-mtdomecom": wouter,
                "ts-mtdomegui": tewei,
                "ts-mtdometrajectory": wouter,
                "ts-mteec": wouter,
                "ts-mthexapod": tewei,
                "ts-mtmount": dave,
                "ts-mtreflector": eric,
                "ts-mtrotator": tewei,
                "ts-observatory-control": tiago,
                "ts-observatory-model": tiago,
                "ts-observing": tiago,
                "ts-ofc": tewei,
                "ts-pmd": eric,
                "ts-pre-commit-config": wouter,
                "ts-rotgui": tewei,
                "ts-salkafka": tiago,
                "ts-salobj": wouter,
                "ts-scheduler": tiago,
                "ts-scriptqueue": tiago,
                "ts-simactuators": tiago,
                "ts-standardscripts": tiago,
                "ts-utils": tiago,
                "ts-tcpip": wouter,
                "ts-tunablelaser": eric,
                "ts-watcher": petr,
                "ts-weatherforecast": eric,
                "ts-xml": rob,
                "vanward": michael,
                "rubin-sim": tiago]
    return slack_ids
}

def build_docs() {
    // Build the documentation
    sh """
        set +x
        source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
        setup -kr .

        # Update the variables of QT
        # TODO: Remove this in DM-44795
        if [ "${env.USE_PYSIDE6}" = "true" ]; then
            export QT_API=PySide6
            export PYTEST_QT_API=PySide6
        fi

        package-docs build
    """
}

def upload_docs(name) {
    // upload the documentation
    // Takes the product name as an argument
    // Changes the underscore to a dash for doc upload
    doc_name = name.replace("_", "-")
    sh """
        set +x
        source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
        cd $WHOME/repo/${name}
        setup -kr .
        ltd -u \$user_ci_USR -p \$user_ci_PSW upload --product ${doc_name} --git-ref ${BRANCH_NAME} --dir doc/_build/html
    """
}

def install(eups=true) {
    // Install the development requirements
    if (!eups) {
        sh """
            set +x
            source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
            pip install -e .
        """
    } else {
            sh """
            set +x
            source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
            setup -kr .
        """
    }
}

def test(scons=false) {
    // Run the tests
    test_shell_script = """
        set +x
        source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
        setup -kr .

        # Update the variables of QT
        # TODO: Remove this in DM-44795
        if [ "${env.USE_PYSIDE6}" = "true" ]; then
            export QT_API=PySide6
            export PYTEST_QT_API=PySide6
        fi
    """
    if (scons) {
        test_shell_script += """
            scons shebang
        """
    }
    test_shell_script += """
        # We compare to null for bash as the way groovy passes the value
        # is not the same as comparing for an empty string.
        if [ "${env.MODULE_NAME}" = "null" ]; then
            pytest -ra
        else
            pytest -ra --cov-report html --cov=${env.MODULE_NAME} --junitxml=${env.XML_REPORT}
        fi
    """
    sh test_shell_script
}

def build_standalone_conda(label) {
    // Build the XML Conda package
    sh """
        #!/bin/bash
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python 3.11 -c lsstts/label/${label} --prefix-length 100 .
        conda build --python 3.12 -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_csc_conda(label) {
    // Build the conda package
    sh """
        #!/bin/bash
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python 3.11 -c lsstts/label/${label} --variants "{salobj_version: ${params.salobj_version}, xml_version: ${params.xml_conda_version}, }" --prefix-length 100 .
        conda build --python 3.12 -c lsstts/label/${label} --variants "{salobj_version: ${params.salobj_version}, xml_version: ${params.xml_conda_version}, }" --prefix-length 100 .
    """
}

def build_salobj_conda(label, concatVersion) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python 3.11 -c lsstts/label/${label} --variants "{xml_version: ${params.xml_conda_version}}" --prefix-length 100 .
        conda build --python 3.12 -c lsstts/label/${label} --variants "{xml_version: ${params.xml_conda_version}}" --prefix-length 100 .
    """
}

def download_git_lfs_files(){
    sh """
        cd ${WHOME}/
        # Since Jenkins sets the hooks path to /dev/null, set it to a real location.
        git config --local core.hooksPath .git/hooks
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
            anaconda upload -u lsstts ${label_option} --force /home/saluser/miniconda3/conda-bld/${arch}/${name}*.conda
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

def update_container_branches() {
    withEnv([]){
        sh """
        set +x
        shopt -s extglob
        source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...

        # Update branches internal to container
        for repo in \$(ls /home/saluser/repos/)
        do
            cd /home/saluser/repos/\$repo
            /home/saluser/.checkout_repo.sh ${WORK_BRANCHES}
        done
	# only sanitize if extra packages are there
	if [ -d ${env.WORKSPACE}/ci/ ]
	then
           cd ${env.WORKSPACE}/ci/
           # Deal with some extraneous files
           rm Jenkinsfile || true
           rm -rf *@tmp*
	fi
        # Update branches for extra packages if used
        for repo in \$(ls ${env.WORKSPACE}/ci/)
        do
            echo \$repo
            cd ${env.WORKSPACE}/ci/\$repo
            git_branch=\$(git rev-parse --abbrev-ref HEAD)
            git branch --set-upstream-to=origin/\$git_branch \$git_branch
            /home/saluser/.checkout_repo.sh ${WORK_BRANCHES} || echo FAILED to update branches.
            eups declare -r . -t current || python -m pip install -e . --no-deps --ignore-installed
        done
    """
    }
}

def setup_and_run_pre_commit() {
    sh """
        set +x
        source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
        generate_pre_commit_conf --skip-pre-commit-install
        pre-commit run --all
    """
}


return this
