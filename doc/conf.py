"""Sphinx configuration file for TSSW package"""

from documenteer.sphinxconfig.stackconf import build_package_configs


_g = globals()
_g.update(build_package_configs(
    project_name='ts_jenkins_shared_library',
    version=""
))
