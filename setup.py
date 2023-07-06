
URL = 'https://github.com/MagdyLab/Pyneapple/tree/main/pyneapple/'

from setuptools import setup, find_packages

def _get_requirements_from_files(groups_files):
    groups_reqlist = {}

    for k, v in groups_files.items():
        with open(v, "r") as f:
            pkg_list = f.read().splitlines()
        groups_reqlist[k] = pkg_list

    return groups_reqlist

def setup_package():
   _groups_files = {
      "base": "requirements.txt",
      # "tests": "requirements_tests.txt",
      #  "docs": "requirements_docs.txt",
   }

   reqs = _get_requirements_from_files(_groups_files)
   install_reqs = reqs.pop("base")
   #extras_reqs = reqs

   setup(
   name='pyneapple',
   version='0.1.0',
   description='Scalable and expressive spatial analysis',
   author='Yunfan Kang',
   author_email='ykang040@ucr.edu',
   url = 'https://github.com/MagdyLab/Pyneapple/tree/main/pyneapple/',
   #packages=['pyneapple'],  #same as name
   packages = ['pyneapple', 'pyneapple.regionalization', 'pyneapple.weight', 'pyneapple.tests'],
   package_data={'': ['*.jar']},
   install_requires=install_reqs, #external packages as dependencies
)


if __name__ == "__main__":
    setup_package()