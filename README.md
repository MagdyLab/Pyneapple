# `Pyneapple`

**Pyneapple** is an open-source Python library for scalable and enriched spatial data analysis. The regionalization module provides the algorithm for the generalized p-regions problem, the scalable max-p-regions solution, and the expressive max-p-regions problem solution. The spatial regression module provides the scalable Multi-scale Geographically Weighted Regression method. It is under development for the inclusion of newly proposed algorithms for scalable spatial analysis methods.

## Documentation

- The documentation for the Python APIs can be found at: [Pyneapple Python APIs](https://pyneapple.readthedocs.io/)
- The JavaDocs is available at : [Pyneapple JavaDocs](https://github.com/MagdyLab/Pyneapple/tree/main/JavaDoc).

## Modules


- pyneapple.regionalization.pruc
P-regions with user-defined constraint
- pyneapple.regionalization.smp
Scalable max-P regionalization
- pyneapple.regionalization.emp
Max-P Regionalization with Enriched Constraints


## Examples
- [Generalized P-Regions](https://github.com/MagdyLab/Pyneapple/blob/main/notebooks/generalized_p.ipynb)
- [Scalable Max-P Regions](https://github.com/MagdyLab/Pyneapple/blob/main/notebooks/scalable_maxp.ipynb)
- [Expressive Max-P Regions](https://github.com/MagdyLab/Pyneapple/blob/main/notebooks/expressive_max_p.ipynb)

All examples can be run interactively by launching this repository as a [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/MagdyLab/Pyneapple/HEAD) or opened using Jupyter Notebook.

## Requirements

### Python
- [`JPype`](https://jpype.readthedocs.io/en/latest/)
- [`numpy`](https://numpy.org/devdocs/)
- [`geopandas`](https://geopandas.org/en/stable/)
- [`pandas`](https://pandas.pydata.org/)
- [`Java`](https://www.java.com/)

### Notebook
- [`libpysal`](https://github.com/pysal/libpysal)
- [`matplotlib`](https://matplotlib.org/)

### Java
- [`JPype`](https://jpype.readthedocs.io/en/latest/)


## Installation
<!--- Not on pip or conda yet --->
<!--- To get started, please make sure that [`Java`](https://www.java.com/) is installed and the environment variables are cofigured. --->
You can try to install the package using the following command:
```
$ pip install git+https://github.com/MagdyLab/Pyneapple.git@main
```

You can also download the source distribution (.tar.gz) and decompress it to your selected destination. Open a command shell and navigate to the decompressed folder. Type:
```
$ pip install .
```
## Contribute

**Pyneapple** is under active development and contributors are welcome.

If you have any suggestions, feature requests, or bug reports, please open new [issues](https://github.com/pysal/PACKAGE_NAME/issues) on GitHub. To submit patches, please open a [pull request](https://github.com/YunfanKang/Pyneapple/pulls). Once your changes get merged, you’ll automatically be added to the [Contributors List](https://github.com/YunfanKang/Pyneapple/graphs/contributors).

## Support
If you are having trouble, please [create an issue](https://github.com/YunfanKang/Pyneapple/issues), or [start a discussion](https://github.com/YunfanKang/Pyneapple/discussions).
<!---, or talk to us in the [gitter room](https://gitter.im/YunfanKang/Pyneapple).--->
