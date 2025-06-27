// vars/registerKafkaTopics.groovy
def call(String XML_VERSION) {
    script {
        sh """
            echo I am registering the $LSST_TOPIC_SUBNAME subname for topics defined using XML v${XML_VERSION}
            source ~/miniconda3/bin/activate
            conda install -y -c lsstts/label/dev "ts-xml>=${XML_VERSION}"
            conda install -y -c lsstts "ts-salobj>=8"
            create_topics --all
        """
    }
}
