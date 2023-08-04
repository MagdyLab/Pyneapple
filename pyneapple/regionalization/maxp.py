import jpype
import array
import numpy as np
import geopandas
import pandas
import math
from jpype import java
from jpype import javax
from .expressive_maxp import expressive_maxp
from .scalable_maxp import scalable_maxp
import spopt
from spopt.region import maxp as MaxP

def maxp(df, w, disName, sumName = None, sumLow = -math.inf, sumHigh = math.inf, minName = None, minLow = -math.inf, minHigh = math.inf, maxName = None, maxLow = -math.inf, maxHigh = math.inf, avgName = None, avgLow = -math.inf, avgHigh = math.inf, countLow = -math.inf, countHigh = math.inf):
    """The API for the max-p regions algorithms. If only the parameters for the max-p regions problem are privided, it will call the scalable max-p algorithm. Otherwise, the expressive max-p regions algorithm is invoked.

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
    EMP_flag = False
    if(minLow != -float('inf') or minHigh != float('inf')):
        if(minName == None):
            print("Invalid MIN constraint!")
        else:
            EMP_flag = True
    if(maxLow != -float('inf') or maxHigh != float('inf')):
        if(maxName == None):
            print("Invalid MAX constraint!")
        else:
            EMP_flag = True
    if(avgLow != -float('inf') or maxHigh != float('inf')):
        if(avgName == None):
            print("Invalid AVG constraint!")
        else:
            EMP_flag = True
    if((not EMP_flag) and sumName == None):
        print("Invalid constraint")
        return
    if(EMP_flag):
        p, regions = expressive_maxp(df, w, disName, minName, minLow, minHigh, maxName, maxLow, maxHigh, avgName, avgLow, avgHigh, sumName, sumLow, sumHigh, countLow, countHigh)
        return p, regions
    else:
        results = scalable_maxp(df, w, disName, sumName, sumLow, 2)
        return results[1], results[0]


