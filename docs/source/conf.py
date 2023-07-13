# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information
import os
import sys
import jpype
import array
import numpy as np
import geopandas
import pandas
from jpype import java
from jpype import javax
sys.path.insert(0, os.path.abspath('C:/Users/50476/Documents/GitHub/Pineapple/Pineapple/Pyneapple/Python/regionalization'))
project = 'Pyneapple'
copyright = '2023, MagdyLab'
author = 'Yunfan Kang Yongyi Liu, Hussah Alrashid,  Laila Abdelhafeez, Mohammad Reza Shahneh, Ahmed Mahmood, Samet Oymak, Sergio Rey, Vassilis J. Tsotras, Amr Magdy'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = ['sphinx.ext.autodoc']

templates_path = ['_templates']
exclude_patterns = []



# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output
import sphinx_rtd_theme
html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']
