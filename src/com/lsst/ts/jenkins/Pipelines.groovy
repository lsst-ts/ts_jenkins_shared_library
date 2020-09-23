package com.lsst.ts.jenkins

import com.lsst.ts.jenkins.pipelines.DevelopPipeline
import com.lsst.ts.jenkins.pipelines.CondaPipeline

def developPipeline(name, idl_name) {
    DevelopPipeline pipeline = new DevelopPipeline()
    pipeline.developPipeline(name, idl_name)
}

def condaPipeline(config_repo, name){
    CondaPipeline pipeline = new CondaPipeline()
    pipeline.condaPipeline(config_repo, name)
}

return this
