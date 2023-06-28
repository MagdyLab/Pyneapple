# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.


# Press the green button in the gutter to run the script.
# if __name__ == '__main__':
#    print_hi('PyCharm')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/


import jpype
import geopandas
import libpysal
import os
import re
import sys
import datetime
"""

The heuristic algorithm for the PRUC problem in python-java hybrid implementation
source: Yongyi Liu, Ahmed R. Mahmood, Amr Magdy, Sergio Rey, "PRUC : P-Regions with User-Defined Constraint", PVLDB, 15(3)

"""



def pruc(
        gdf,
        w,
        sim_attr,
        ext_attr,
        threshold,
        p,
        lo_iter = 1000,
        seed = datetime.datetime.now().strftime("%Y%m%d%H%M%S"),
        num_thread = os.cpu_count()
):
    """
    Parameters
    ----------

    gdf : geopandas.GeoDataFrame

    w : libpysal.weights.W

    sim_attr : string
        The name of the attribute to measure the heterogeneity

    ext_attr : string
        The name of the attribute to measure the spatial extensive attribute

    threshold : {int , float}
        The threshold value enforced on each region with regards to the regional extensive attribute

    p : int
        The number of regions
        Default is set to the size of the dataset

    lo_iter : int
        The number of iterations in local optimization
        Default is 1000
    
    seed : int
        The seed for randomness
        Default is set to the current time
    
    num_thread : int
        The number of thread used in the local optimization
        Default is set to os.cpu_count()
        
    Returns
    ----------
    (hetero , label):
        if a feasible partition is found
        hetero is the heterogeneity of the partition found by the GSLO algorithm and the label is a list that corresponds to the area-region mapping

    or

    None:
        if no feasible partition is found
    """
    if not isinstance(gdf, geopandas.GeoDataFrame):
        raise Exception("gdf must be a GeoDataFrame object")
    
    if not isinstance(w, libpysal.weights.W):
        raise Exception("w must be a libpysal.weights.W object")
    
    if sim_attr not in gdf.columns:
        raise Exception("similarity attribute not in the attribute list")
    else:
        for val in gdf[sim_attr]:           
            pattern = re.compile(r'^[-+]?[-0-9]\d*\.\d*|[-+]?\.?[0-9]\d*$')
            result = pattern.match(str(val))
            if not result:
                raise Exception("the values under the similarity attribute must be numerical values")
            
    
    if ext_attr not in gdf.columns:
        raise Exception("extensive attribute not in the attribute list")
    else:
        for val in gdf[ext_attr]:
            pattern = re.compile(r'^[-+]?[-0-9]\d*\.\d*|[-+]?\.?[0-9]\d*$')
            result = pattern.match(str(val))
            if not result:
                raise Exception("the values under the extensive attribute must be numerical values")
    
    if (not isinstance(p, int)) or (p <= 0) :
        raise Exception("the number of regions must be positive integer")
        
    
    if (not isinstance(threshold, int) and not isinstance(threshold, float)) or (threshold < 0):
        raise Exception("threshold must be non-negative")
    
    
    if (not isinstance(lo_iter , int)) or (lo_iter < 0):
        raise Exception("lo_iter must be non-negative integers")
    
    
    if not jpype.isJVMStarted():
        jpype.startJVM("-Xmx20480m", classpath = ["./Pineapple.jar"])

    neighborHashMap = jpype.java.util.HashMap()
    for key, value in w.neighbors.items():
        tempSet = jpype.java.util.TreeSet()
        for v in value:
            tempSet.add(jpype.JInt(v))
        neighborHashMap.put(jpype.JInt(key), tempSet)

    idList = jpype.java.util.ArrayList()
    sAttr = jpype.java.util.ArrayList()
    extAttr = jpype.java.util.ArrayList()
    x_centroids = jpype.java.util.ArrayList()
    y_centroids = jpype.java.util.ArrayList()
    
    
    for i in range(0, gdf.shape[0]):
        sAttr.add(jpype.JDouble(gdf[sim_attr][i]))
        extAttr.add(jpype.JDouble(gdf[ext_attr][i]))
        idList.add(jpype.JInt(i))
        x_centroids.add(jpype.JDouble(gdf['geometry'][i].centroid.x))
        y_centroids.add(jpype.JDouble(gdf['geometry'][i].centroid.y))
    
   
    PRUC = jpype.JClass('edu.ucr.cs.pineapple.regionalization.PRUC')()
    result = PRUC.execute_GSLO(jpype.JInt(p), jpype.JDouble(threshold), jpype.JInt(lo_iter),
                               neighborHashMap, extAttr, sAttr, x_centroids, y_centroids, jpype.JLong(seed), jpype.JInt(num_thread))

    results = None
    if len(result) == 2:
        hetero = float(result[0])
        l = list(result[1])

        results = [hetero, l]

    #jpype.shutdownJVM()
    return results



#gdf = geopandas.read_file(libpysal.examples.get_path("mexicojoin.shp"))
#w = libpysal.weights.Queen.from_dataframe(gdf)
#print(pruc(gdf, w, 'PCGDP1940', 'PERIMETER', 3000000, 10))
#print(pruc(gdf, w, 'PCGDP1940', 'PERIMETER', 3000000, 10,  gdf.shape[0], 2023117, 1))