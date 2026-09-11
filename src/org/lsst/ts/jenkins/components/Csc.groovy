package org.lsst.ts.jenkins.components

def email() {
    emails = [  "default": "rubinobs-tssw-worker@lsst.org",
                "love-commander": "sebastian.aranda@noirlab.edu",
                "love-producer": "sebastian.aranda@noirlab.edu",
                "lsst-efd-client": "valerie.becker@noirlab.edu",
                "ts-astrosky-model": "tiago.ribeiro@noirlab.edu",
                "ts-ataos": "tiago.ribeiro@noirlab.edu",
                "ts-atbuilding-csc": "brian.brondel@noirlab.edu",
                "ts-atbuilding-vents": "brian.brondel@noirlab.edu",
                "ts-atdome": "wouter.vanreeven@noirlab.edu",
                "ts-atdometrajectory": "wouter.vanreeven@noirlab.edu",
                "ts-athexapod": "eric.coughlin@noirlab.edu",
                "ts-atmcs": "wouter.vanreeven@noirlab.edu",
                "ts-atpneumatics": "wouter.vanreeven@noirlab.edu",
                "ts-atspec": "tiago.ribeiro@noirlab.edu",
                "ts-attcpip": "wouter.vanreeven@noirlab.edu",
                "ts-atwhitelight": "eric.coughlin@noirlab.edu",
                "ts-audio-broadcaster": "sebastian.aranda@noirlab.edu",
                "ts-audiotrigger": "eric.coughlin@noirlab.edu",
                "ts-cbp": "eric.coughlin@noirlab.edu",
                "ts-conda-build": "wouter.vanreeven@noirlab.edu",
                "ts-conda-build-aarch64": "wouter.vanreeven@noirlab.edu",
                "ts-conda-build-linux-aarch64": "wouter.vanreeven@noirlab.edu",
                "ts-criopy": "tiago.ribeiro@noirlab.edu",
                "ts-cycle": "wouter.vanreeven@noirlab.edu",
                "ts-dateloc": "tiago.ribeiro@noirlab.edu",
                "ts-dds": "tiago.ribeiro@noirlab.edu",
                "ts-ddsconfig": "tiago.ribeiro@noirlab.edu",
                "ts-develop": "wouter.vanreeven@noirlab.edu",
                "ts-develop-aarch64": "wouter.vanreeven@noirlab.edu",
                "ts-dimm": "brian.brondel@noirlab.edu",
                "ts-dream": "brian.brondel@noirlab.edu",
                "ts-dsm": "michael.reuter@noirlab.edu",
                "ts-eas": "brian.brondel@noirlab.edu",
                "ts-electrometer": "eric.coughlin@noirlab.edu",
                "ts-ess-csc": "wouter.vanreeven@noirlab.edu",
                "ts-ess-common": "wouter.vanreeven@noirlab.edu",
                "ts-ess-controller": "wouter.vanreeven@noirlab.edu",
                "ts-ess-earthquake": "wouter.vanreeven@noirlab.edu",
                "ts-ess-epm": "sebastian.aranda@noirlab.edu",
                "ts-ess-labjack": "wouter.vanreeven@noirlab.edu",
                "ts-ess-ringss": "brian.brondel@noirlab.edu",
                "ts-externalscripts": "tiago.ribeiro@noirlab.edu",
                "ts-fbs-utils": "tiago.ribeiro@noirlab.edu",
                "ts-fiberspectrograph": "eric.coughlin@noirlab.edu",
                "ts-genericcamera": "petr.kubanek@noirlab.edu",
                "ts-gis": "eric.coughlin@noirlab.edu",
                "ts-guitool": "te-wei.tsai@noirlab.edu",
                "ts-hexgui": "te-wei.tsai@noirlab.edu",
                "ts-hexrotcomm": "eric.coughlin@noirlab.edu",
                "ts-hvac": "wouter.vanreeven@noirlab.edu",
                "ts-integrationtests": "rob.bovill@noirlab.edu",
                "ts-linearstage": "eric.coughlin@noirlab.edu",
                "ts-lasertracker": "eric.coughlin@noirlab.edu, petr.kubanek@noirlab.edu",
                "ts-ledprojector": "eric.coughlin@noirlab.edu",
                "ts-logging-and-reporting": "valerie.becker@noirlab.edu",
                "ts-m2": "tiago.ribeiro@noirlab.edu",
                "ts-m2com": "te-wei.tsai@noirlab.edu",
                "ts-m2gui": "te-wei.tsai@noirlab.edu",
                "ts-mtaircompressor": "petr.kubanek@noirlab.edu",
                "ts-mtdome": "wouter.vanreeven@noirlab.edu",
                "ts-mtdomecom": "wouter.vanreeven@noirlab.edu",
                "ts-mtdomegui": "te-wei.tsai@noirlab.edu",
                "ts-mtdometrajectory": "wouter.vanreeven@noirlab.edu",
                "ts-mthexapod": "te-wei.tsai@noirlab.edu",
                "ts-mtmount": "tiago.ribeiro@noirlab.edu",
                "ts-mtreflector": "eric.coughlin@noirlab.edu",
                "ts-mtrotator": "te-wei.tsai@noirlab.edu",
                "ts-observatory-control": "tiago.ribeiro@noirlab.edu",
                "ts-observatory-model": "tiago.ribeiro@noirlab.edu",
                "ts-observing": "tiago.ribeiro@noirlab.edu",
                "ts-ofc": "tiago.ribeiro@noirlab.edu, te-wei.tsai@noirlab.edu",
                "ts-pmd": "eric.coughlin@noirlab.edu" ,
                "ts-pre-commit-config": "wouter.vanreeven@noirlab.edu",
                "ts-rotgui": "te-wei.tsai@noirlab.edu",
                "ts-salkafka": "tiago.ribeiro@noirlab.edu",
                "ts-salobj": "tiago.ribeiro@noirlab.edu, wouter.vanreeven@noirlab.edu",
                "ts-scheduler": "tiago.ribeiro@noirlab.edu",
                "ts-scriptqueue": "tiago.ribeiro@noirlab.edu",
                "ts-simactuators": "tiago.ribeiro@noirlab.edu",
                "ts-standardscripts": "tiago.ribeiro@noirlab.edu",
                "ts-tcpip": "wouter.vanreeven@noirlab.edu",
                "ts-tunablelaser": "eric.coughlin@noirlab.edu",
                "ts-utils": "tiago.ribeiro@noirlab.edu",
                "ts-watcher": "wouter.vanreeven@noirlab.edu",
                "ts-weatherforecast": "eric.coughlin@noirlab.edu",
                "ts-xml": "rob.bovill@noirlab.edu",
                "vanward": "michael.reuter@noirlab.edu"]
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
    String valerie = "U07PA21TF5H"
    String wouter = "U07PMHC954K"
    slack_ids = ["default": rob,
                "love-commander": sebastian,
                "love-producer": sebastian,
                "lsst-efd-client": valerie,
                "ts-astrosky-model": tiago,
                "ts-ataos": tiago,
                "ts-atbuilding-csc": brianb,
                "ts-atbuilding-vents": brianb,
                "ts-atdome": wouter,
                "ts-atdometrajectory": wouter,
                "ts-athexapod": eric,
                "ts-atmcs": wouter,
                "ts-atpneumatics": wouter,
                "ts-atspec": tiago,
                "ts-attcpip": wouter,
                "ts-atwhitelight": eric,
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
                "ts-dimm": brianb,
                "ts-dream": brianb,
                "ts-dsm": michael,
                "ts-eas": brianb,
                "ts-electrometer": eric,
                "ts-ess-csc": wouter,
                "ts-ess-common": wouter,
                "ts-ess-controller": wouter,
                "ts-ess-earthquake": wouter,
                "ts-ess-epm": wouter,
                "ts-ess-labjack": wouter,
                "ts-ess-ringss": wouter,
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
                "ts-logging-and-reporting": valerie,
                "ts-m2": tiago,
                "ts-m2com": tewei,
                "ts-m2gui": tewei,
                "ts-mtaircompressor": petr,
                "ts-mtdome": wouter,
                "ts-mtdomecom": wouter,
                "ts-mtdomegui": tewei,
                "ts-mtdometrajectory": wouter,
                "ts-mthexapod": tewei,
                "ts-mtmount": tiago,
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
                "ts-watcher": wouter,
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
        pip install --no-deps .

        # Update the variables of QT
        # TODO: Remove this in DM-44795
        if [ "${env.USE_PYSIDE6}" = "true" ]; then
            export QT_API=PySide6
            export PYTEST_QT_API=PySide6
        fi

        sphinx-build -b html doc doc/_build/html
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
        pip install --no-deps .
        ltd -u \$user_ci_USR -p \$user_ci_PSW upload --product ${doc_name} --git-ref ${BRANCH_NAME} --dir doc/_build/html
    """
}

def install(eups=true) {
    // Install the development requirements
    if (!eups) {
        sh """
            set +x
            source /home/saluser/.setup_dev.sh || echo loading env failed. Continuing...
            pip install -e . --no-deps
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

        # Update the variables of QT
        # TODO: Remove this in DM-44795
        if [ "${env.USE_PYSIDE6}" = "true" ]; then
            export QT_API=PySide6
            export PYTEST_QT_API=PySide6
        fi
    """
    if (scons) {
        test_shell_script += """
            setup -kr .
            scons shebang || echo Failed to generate scons shebang
        """
    }
    test_shell_script += """
        # We compare to null for bash as the way groovy passes the value
        # is not the same as comparing for an empty string.
        if [ "${env.MODULE_NAME}" = "null" ]; then
            pytest -v
        else
            pytest -v --cov-report html --cov=${env.MODULE_NAME} --junitxml=${env.XML_REPORT}
        fi
    """
    sh test_shell_script
}

def build_standalone_conda(label, pyver) {
    // Build the XML Conda package
    sh """
        #!/bin/bash
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python ${pyver} -c lsstts/label/${label} --prefix-length 100 .
    """
}

def build_csc_conda(label, pyver) {
    // Build the conda package
    sh """
        #!/bin/bash
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python ${pyver} -c lsstts/label/${label} --variants "{salobj_version: ${params.salobj_version}, xml_version: ${params.xml_conda_version}, }" --prefix-length 100 .
    """
}

def build_salobj_conda(label, concatVersion, pyver) {
    sh """
        cd ${WHOME}/conda
        source /home/saluser/.setup.sh
        conda config --set solver libmamba
        conda config --add channels conda-forge
        conda config --add channels lsstts
        conda build --python ${pyver} -c lsstts/label/${label} --variants "{xml_version: ${params.xml_conda_version}}" --prefix-length 100 .
    """
}

def download_git_lfs_files(workDir=null) {
    if (workDir == null) {  // Use == for comparison, not = which is assignment
        workDir = "${WHOME}"
    }
    sh """
        # Since Jenkins sets the hooks path to /dev/null, set it to a real location.
        cd ${workDir}
        git config --local core.hooksPath .git/hooks
        git lfs install
        git lfs fetch
        git lfs checkout
    """
}

def upload_conda(name, label, arch) {
    withCredentials([usernamePassword(credentialsId: 'CondaForge', passwordVariable: 'anaconda_pass', usernameVariable: 'anaconda_user'), usernamePassword(credentialsId: 'nexus3-lsst_jenkins', passwordVariable: 'nexus_pass', usernameVariable: 'nexus_user')]) {
        // Upload the conda package
        // Takes the name of the package and a label
        if ((arch=="linux-aarch64") || (arch=="noarch") || (arch=="linux-64")) {
            if (label == "rc") {
                label_option = "--label ${label} --label main"
            } else {
                label_option = "--label ${label}"
            }
            if (label != "dev") {
                sh """
                    source /home/saluser/miniconda3/bin/activate
                    export ANACONDA_CLIENT_LEGACY_INTERACTIVE_LOGIN=1
                    anaconda org login --user ${anaconda_user} --password ${anaconda_pass}
                    source /home/saluser/.setup.sh
                    anaconda upload -u lsstts ${label_option} --force /home/saluser/miniconda3/conda-bld/${arch}/${name}*.conda
                """
            } else {
                sh """
                    curl -u ${nexus_user}:${nexus_pass} -w "%{http_code}" -sS --upload-file /home/saluser/miniconda3/conda-bld/noarch/${package_name}*.conda https://repo-nexus.lsst.org/nexus/repository/ssw-conda/dev/${arch}/
                """
            }
        } else {
            currentBuild.result = 'ABORTED'
            error('Please properly define the arch parameter.')
        }
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
            eups declare -r . -t current
            python -m pip install -e . --no-deps --ignore-installed || echo "Not able to be installed via pip"
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
