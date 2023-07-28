import jpype
import array
import numpy as np
import geopandas
import pandas
import libpysal
import math
from jpype import java
from jpype import javax
def expressive_maxp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh):
    """The expressive max-p-regions (EMP) involves the aggregation of n areas into an unknown maximum number of
    homogeneous regions, while ensuring that each region is contiguous and satisfies a set of constraints. The constraints 
    are 

    Parameters
    ----------

    df : geopandas.GeoDataFrame, required
        Geodataframe containing original data

    w : libpysal.weights.W, required
        Weights object created from given data

    disname : String, required
        Strings for attribute names to measure similarity (cols of ``geopandas.GeoDataFrame``).

    minName : string, requied
        The name of the spatial extensive attribute variable for the MIN constraint.

    minLow : {int, float}, required
        The lowerbound for the MIN range.

    minHigh : {int, float}, required
        The upperbound for the MIN range.

    maxName : string, requied
        The name of the spatial extensive attribute variable for the MAX constraint.

    maxLow : {int, float}, required
        The lowerbound for the MAX range.

    maxHigh : {int, float}, required
        The upperbound for the MAX range.

	avgName : string, requied
        The name of the spatial extensive attribute variable for the AVG constraint.

    avgLow : {int, float}, required
        The lowerbound for the AVG range.

    avgHigh : {int, float}, required
        The upperbound for the AVG range.

    sumName : string, requied
        The name of the spatial extensive attribute variable for the SUM constraint.

    sumLow : {int, float}, required
        The lowerbound for the SUM range.

    sumHigh : {int, float}, required
        The upperbound for the SUM range.

    countLow : {int, float}, required
        The lowerbound for the COUNT range.

    countHigh : {int, float}, required
        The upperbound for the COUNT range.


    Returns
    -------

    max_p : int
        The number of regions.

    labels : numpy.array
        Region IDs for observations.

    """
    #if not jpype.isJVMStarted():
        #jpype.startJVM("-Xmx20480m", classpath = ["../Pineapple.jar"])
    #The JVM is started when 'pyneapple' is imported
    if not isinstance(df, geopandas.GeoDataFrame):
        raise Exception("df must be a GeoDataFrame object")
    
    if not isinstance(w, libpysal.weights.W):
        raise Exception("w must be a libpysal.weights.W object")
    
    if disName not in df.columns:
        raise Exception("Dissimilarity attribute not in the attribute list")
    if minName not in df.columns:
        if minName == None and minLow == -math.inf and minHigh == math.inf:
            minName = disName
        else:
            raise Exception("Min attribute not in the attribute list")
    if maxName not in df.columns:
        if maxName == None and maxLow == -math.inf and maxHigh == math.inf:
            maxName = disName
        else:
            raise Exception("Max attribute not in the attribute list")
    if avgName not in df.columns:
        if avgName == None and avgLow == -math.inf and avgHigh == math.inf:
            avgName = disName
        else:
            raise Exception("Avg attribute not in the attribute list")
    if sumName not in df.columns:
        if sumName == None and sumLow == -math.inf and sumHigh == math.inf:
            sumName = disName
        else:
            raise Exception("Sum attribute not in the attribute list")


    neighborHashMap = java.util.HashMap()
    for key, value in w.neighbors.items():
        tempSet = java.util.TreeSet()
        for v in value:
            tempSet.add(jpype.JInt(v))
        neighborHashMap.put(jpype.JInt(key), tempSet)
    EMP = jpype.JClass("edu.ucr.cs.pyneapple.regionalization.EMP")()
    idList = jpype.java.util.ArrayList()
    disAttr = jpype.java.util.ArrayList()
    minAttr = jpype.java.util.ArrayList()
    maxAttr = jpype.java.util.ArrayList()
    avgAttr = jpype.java.util.ArrayList()
    sumAttr = jpype.java.util.ArrayList()
    #df['Field_2'] = df['Field_2'].apply(np.int64)
    disSucc = disAttr.addAll(df[disName].apply(np.int64).tolist())
    minSucc = minAttr.addAll(df[minName].apply(np.int64).tolist())
    maxSucc = maxAttr.addAll(df[maxName].apply(np.int64).tolist())
    avgSucc = avgAttr.addAll(df[avgName].apply(np.int64).tolist())
    sumSucc = sumAttr.addAll(df[sumName].apply(np.int64).tolist())#check for att assign
    for i in range(0, df.shape[0]):
        idList.add(jpype.JInt(i))
    EMP.execute_regionalization(neighborHashMap, disAttr, minAttr,minLow, minHigh, maxAttr, maxLow, maxHigh, avgAttr, avgLow, avgHigh, sumAttr, sumLow, sumHigh, countLow, countHigh)
   
    return EMP.getP(), np.array(EMP.getRegionLabels())


