package com.lsst.ts.jenkins.components

def build_docs() {
    // Build the documentation
    sh 'package-docs build'
}

def upload_docs(name) {
    // upload the documentation
    // Takes the product name as an argument
    sh 'ltd upload --product ${name} --git-ref ${GIT_BRANCH} --dir doc/_build/html'
}

def install() {
    // Install the development requirements
    sh 'pip install .[dev]'
}

def test() {
    // Run the tests
    sh 'pytest --cov-report html --cov=${env.MODULE_NAME} --junitxml=${env.XML_REPORT}'
}

def build_conda() {
    // Build the conda package
    sh 'conda build --prefix-length 100 .'
}

def upload_conda(String name, String label) {
    // Upload the conda package
    // Takes the name of the package and a label
    sh 'anaconda upload -u lsstts --label ${label} --force /home/saluser/miniconda3/conda-bld/linux64/${name}'
}

return this
