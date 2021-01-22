.. _Developer_Guide:

######################################
Jenkins Shared Library Developer Guide
######################################

The architecture of the library consists of two kinds of objects.
These are the components and the pipelines.
The Components are located under the ``src`` folder and they serve as a means for creating objects which are reuseable in pipelines.
The Pipelines are located under the ``vars`` folder and each one has its own Groovy file.

Writing a Component
===================
A component is like a set of functions grouped together but does not actually define a class structure.
This is because a class in Jenkins must define a Serializable method.
Instead, a workaround has been created by using functions and then returning the object so that the "class" can be called.
An important thing to remember is that each function is self contained and any Jenkins block such as ``sh`` will not carry over to the next function.
So be sure to repeat any steps that are setting up an environment to do something.

Writing a Pipeline
==================
Writing a pipeline is slightly different in a Jenkins Shared Library as it is wrapped in a ``call`` function.
The call function can contain parameters.
In the ``call`` block, define components and functions there.
After that, define the pipeline as normal and inside the stages if using a component method then add a script block and call it inside of that.


.. _Build:

Build and Test
==============

For testing changes, make a ticket branch in this repository and push the changes.
Then in a different repository, set it to use that ticket branch by doing the following.

.. code::

    @Library('JenkinsShared@tickets/DM-XXXXX')_
    ...

Once successful, make sure to remove the qualifier when merging to develop.

.. _Usage:

Usage
=====

To use the Jenkins Shared Library, put the following in the Jenkinsfile

.. code::

    @Library('JenkinsShared')_
    CondaPipeline(["config_repo"], "package-name", "module.location")

.. _Documentation:

Building the Documentation
==========================

.. prompt:: bash

    package-docs clean && package-docs build

.. _Contributing:

Contributing
============

Code and documentation contributions utilize pull-requests on github.
Feature requests can be made by filing a Jira ticket with the `ts_jenkins_shared_library` label.
In all cases, reaching out to the contacts for this library is recommended.

