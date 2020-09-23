# Jenkins Shared Library

This contains the shared libaries for the Telescope and Site Software Team's Jenkins instance.

According to the shared library documentation, pretty much any groovy object is allowed.
However, the caveat here is that classes are not allowed to use the built-in DSL calls like `sh`.
So we workaround it by making a file which defines functions and then at the end returing `this` which allows it to behave like an object.
`Csc.groovy` contains a set of functions to help with the lifecycle for a CSC build.
The `pipelines` contain the different types of pipelines for the CSC lifecycle.

An example is below of what a `Jenkinsfile` can look like.

```Jenkinsfile
import com.lsst.ts.jenkins.*

Pipelines pipelines = new Pipelines()

pipelines.condaPipeline("ts_config_attcs", "ts-athexapod", "lsst.ts.ATHexapod")

```
*Components*
Contains logic for abstracting tools used in building software.

*pipelines*
Contains logic for running different pipelines.

The hierarchy and patterns are taken from [Chris Cooney's Repo.](https://github.com/ChrisCooney/jenkins-global-library-simple)


## References
https://github.com/ChrisCooney/jenkins-global-library-simple
https://www.jenkins.io/doc/book/pipeline/shared-libraries/
https://groovy-lang.org/syntax.html
https://www.jenkins.io/doc/book/pipeline/pipeline-best-practices/#using-shared-libraries
