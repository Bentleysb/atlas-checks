#!/usr/bin/env python

import os
from setuptools import setup, find_packages
from pip._internal.req import parse_requirements

here = os.path.abspath(os.path.dirname(__file__))
README = open(os.path.join(here, 'README.md')).read()
exclude_dirs = ['tests', 'venv']

install_reqs = parse_requirements(os.path.join(here, 'requirements.txt'), session=False)
reqs = [str(ir.requirement) for ir in install_reqs]

packs = find_packages(exclude=exclude_dirs)

setup(name='atlas_checks_pyspark_tools',
      description="Pyspark tools for AtlasChecks",
      version="0.1.0",
      long_description=README,
      keywords='PySpark, Spark, Atlas, AtlasChecks',
      packages=packs,
      include_package_data=True,
      setup_requires=["flake8==3.3.0", "pep8", "nose"],
      dependency_links=[],
      tests_require=[],
      install_requires=reqs
      )
