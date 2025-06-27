// vars/deleteKafkaTopics.groovy
def call(String XML_VERSION, String SUBNAME, Boolean FORCE) {
    script {
        sh """
            echo ${FORCE}
            if [ '${FORCE}' = true ]; then
                flag = '--force'
            else
                flag = ''
            fi
            echo I am deleting the '${SUBNAME}' subname for topics defined using XML v${XML_VERSION}
            source ~/miniconda3/bin/activate
            conda install -y -c lsstts/label/dev "ts-xml>=${XML_VERSION}"
            conda install -y -c lsstts "ts-salobj>=8"
            delete_topics --subname=${SUBNAME} --all \$flag
        """
    }
}
