package org.lsst.ts.jenkins.components

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

def build_conda(label) {
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

def build_salobj_conda(label) {
    sh """
        cd ${HOME}/conda
        source /home/saluser/miniconda3/bin/activate
        conda config --add channels conda-forge
        conda config --add channels lsstts
        source ${OSPL_HOME}/release.com
        conda build -c lsstts/label/${label} --variants "{idl_version: ${params.idl_version}}" --prefix-length 100 .
    """
}

def upload_conda(name, label) {
    // Upload the conda package
    // Takes the name of the package and a label
    sh """
        source /home/saluser/miniconda3/bin/activate
        anaconda upload -u lsstts --label ${label} --force /home/saluser/miniconda3/conda-bld/linux-64/${name}*.tar.bz2
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
