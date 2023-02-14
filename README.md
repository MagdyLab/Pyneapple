# `Pineapple`

**Pineapple** is an open-source Python library for scalable and enriched spatial data analysis. The regionalization module provides the algorithm for the enriched p-regions problem, the scalable max-p-regions solution, and the enriched max-p-regions problem solution. The spatial regression module provides the scalable Multi-scale Geographically Weighted Regression method. It is under development for the inclusion of newly proposed algorithms for scalable spatial analysis methods.

## Modules


- pineapple.regionalization.pruc
P-regions with user-defined constraint
- pineapple.regionalization.smp
Scalable max-P regionalization
- pineapple.regionalization.emp
Max-P Regionalization with Enriched Constraints


## Examples
-[PRUC](https://github.com/YunfanKang/Pineapple/blob/main/notebooks/pruc.ipynb)
-[SMP](https://github.com/YunfanKang/Pineapple/blob/main/notebooks/SMP.ipynb)
-[EMP](https://github.com/YunfanKang/Pineapple/blob/main/notebooks/max-p-enriched.ipynb)
All examples can be run interactively by launching this repository as a [![Binder](https://mybinder.org/badge_logo.svg)](https://mybinder.org/v2/gh/pysal/spopt/main) or opened using Jupyter Notebook.

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

To get started, please make sure that [`Java`](https://www.java.com/) is installed and the environment variables are cofigured. 

You can download the source distribution (.tar.gz) and decompress it to your selected destination. Open a command shell and navigate to the decompressed folder. Type:
```
$ pip install .
```

## Support
If you are having trouble, please [create an issue](https://github.com/YunfanKang/Pineapple/issues), [start a discussion](https://github.com/YunfanKang/Pineapple/discussions).
[//]: <>, or talk to us in the [gitter room](https://gitter.im/YunfanKang/Pineapple).
