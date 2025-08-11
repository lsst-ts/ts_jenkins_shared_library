// vars/deleteKafkaTopics.groovy
def call(String XML_VERSION, String SUBNAME, Boolean FORCE, String COMPONENTS) {
    // Attributes
    // ----------
    //
    // XML_VERSION : `string`
    //     Defines the version of XML that SalObj uses to register the topics.
    // SUBNAME : `string`
    //     Defines the topic subname to delete..
    // FORCE : `boolean`
    //     Required when deleting the `sal` subname, which is a protected value.
    // COMPONENTS : `string`
    //     Space-separated, case-sensitive list of CSCs, e.g Test Script ScriptQueue,
    //     or ALL if creating topics for all components.
    //
    script {
        sh """
            echo I am deleting topics for "$COMPONENTS" components with the "$SUBNAME" subname using XML v${XML_VERSION}
            source ~/miniconda3/bin/activate
            conda install -qy -c lsstts/label/dev "ts-xml>=${XML_VERSION}"
            conda install -qy -c lsstts "ts-salobj>=8"
            echo Force: ${FORCE}
            if [ '${FORCE}' == true ]; then
                flag='--force'
            else
                flag=''
            fi
            echo I am deleting topics for "$COMPONENTS" components with the "$SUBNAME" subname using XML v${XML_VERSION}
            source ~/miniconda3/bin/activate
            conda install -qy -c lsstts/label/dev "ts-xml>=${XML_VERSION}"
            conda install -qy -c lsstts "ts-salobj>=8"
            csc_list="$COMPONENTS"
            echo CSCs: \$csc_list
            if [ "\${csc_list,,}" == "all" ]; then
                cscs="--all"
            else
                cscs="${COMPONENTS}"
            fi
            delete_topics --subname=${SUBNAME} \$cscs \$flag
        """
    }
}
