import jpype
import array
import numpy as np
import geopandas
import pandas
from jpype import java
from jpype import javax
def smp(df, w, attrs_name, threshold_name, threshold, maxItr = 100, lengthTabu = 100, t = 1, convSA = 90, alpha = 0.9, nRows = 2, nColumns = 2, cores = 4, random = 0):
    """The max-p-regions involves the aggregation of n areas into an unknown maximum number of
    homogeneous regions, while ensuring that each region is contiguous and satisfies a minimum
    threshold value imposed on a predefined spatially extensive attribute.

    Parameters
    ----------

    df : geopandas.GeoDataFrame, required
        Geodataframe containing original data

    w : libpysal.weights.W, required
        Weights object created from given data

    attrs_name : list, required
        Strings for attribute names to measure similarity (cols of ``geopandas.GeoDataFrame``).

    threshold_name : string, requied
        The name of the spatial extensive attribute variable.

    threshold : {int, float}, required
        The threshold value.

    top_n : int
        Max number of candidate regions for enclave assignment.

    max_iterations_construction : int
        Max number of iterations for construction phase.

    max_iterations_SA: int
        Max number of iterations for customized simulated annealing.

    verbose : boolean
        Set to ``True`` for reporting solution progress/debugging.
        Default is ``False``.

    Returns
    -------

    max_p : int
        The number of regions.

    labels : numpy.array
        Region IDs for observations.

    """
    neighborHashMap = java.util.HashMap()
    for key, value in w.neighbors.items():
        tempSet = java.util.TreeSet()
        for v in value:
            tempSet.add(jpype.JInt(v))
        neighborHashMap.put(jpype.JInt(key), tempSet)
    SMPI = jpype.JClass("edu.ucr.cs.pineapple.regionalization.SMPPPythonInterface")()
    idList = jpype.java.util.ArrayList()
    disAttr = jpype.java.util.ArrayList()
    sumAttr = jpype.java.util.ArrayList()
    disSucc = disAttr.addAll(df[attrs_name].tolist())
    sumSucc = sumAttr.addAll(df[threshold_name].tolist())
    gcolumn = df['geometry'].to_list()
    geometryStrings = jpype.java.util.ArrayList()
    for gEntry in gcolumn:
        geometryStrings.add(str(gEntry))
    SMPI.setGeometryStrings(geometryStrings)
    SMPI.setMaxItr(jpype.JInt(maxItr))
    SMPI.setLengthTabu(jpype.JInt(lengthTabu))
    SMPI.setT(jpype.JDouble(t))
    SMPI.setConvSA(jpype.JInt(convSA))
    SMPI.setAlpha(jpype.JDouble(alpha))
    SMPI.setNRows(jpype.JInt(nRows))
    SMPI.setNColumns(jpype.JInt(nColumns))
    SMPI.setCores(jpype.JInt(cores))
    SMPI.setRandom(jpype.JInt(random))
    SMPI.execute_regionalization(neighborHashMap, disAttr, sumAttr, jpype.JLong(threshold))
    return SMPI.getP(), np.array(SMPI.getRegionList())