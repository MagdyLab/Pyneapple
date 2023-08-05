# -*- coding: utf-8 -*-
import jpype 
import os
import libpysal
import geopandas



def scalable_maxp(df, 
         w, 
         d_attribute_col, 
         s_attribute_col, 
         threshold, 
         maxItr = 40, 
         convSA = 90,    
         cores = 4,
         nRows = 2,
         nColumns = 2):
    
    """The max-p-regions involves the aggregation of n areas into an unknown maximum number of
    homogeneous regions, while ensuring that each region is contiguous and satisfies a minimum
    threshold value imposed on a predefined spatially extensive attribute.

    Parameters
    ----------

    df : geopandas.GeoDataFrame, required
        Geodataframe containing original data

    w : libpysal.weights.W, required
        Weights object created from given data

    d_attribute_col : list, required
        Strings for attribute names to measure similarity (cols of ``geopandas.GeoDataFrame``).

    s_attribute_col : string, requied
        The name of the spatially extensive attribute variable.

    threshold : {int, float}, required
        The threshold value.

    maxItr : int
        Max number of iterations for construction phase.

    convSA: int
        Max number of iterations for customized simulated annealing.

    cores: int
       Number of cores
       
    nRows:
       Number of rows for partitioning the data
       
    nRows:
       Number of columns for partitioning the data
       

    Returns
    -------

    max_p : int
        The number of regions.

    labels : list
        list that corresponds to the area-region mapping where the index is the area ID and the value is the Region ID

    """
    
    
    if not jpype.isJVMStarted():
        #print("starting jvm")
        path = os.path.split(os.path.abspath(__file__))[0] + "/Pyneapple.jar"
        #print(path)
        #jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", '-Djava.class.path='+ path)
        jpype.startJVM("-Xmx20480m", classpath = [path])
         
         
         
    random = 0
    lengthTabu = 100
    t = 1
    alpha = 0.9
    
    
    
    if not isinstance(df, geopandas.GeoDataFrame):
        raise Exception("df must be a GeoDataFrame object")
        

    if not isinstance(w, libpysal.weights.W):
        raise Exception("w must be a libpysal.weights.W object")
        
    
    if d_attribute_col not in df.columns:
        raise Exception("similarity attribute not in the attribute list")
                
    
    if s_attribute_col not in df.columns:
        raise Exception("extensive attribute not in the attribute list")
     
    
    if (not isinstance(threshold, int) and not isinstance(threshold, float)) or (threshold < 0):
        raise Exception("threshold must be non-negative")
    
    
    if (not isinstance(maxItr , int)) or (maxItr < 0):
        raise Exception("maxItr must be a non-negative integer")
        
        
    if (not isinstance(convSA , int)) or (convSA < 0):
        raise Exception("convSA must be a non-negative integer")
        
        
    if (not isinstance(cores , int)) or (cores < 0):
        raise Exception("cores must be a non-negative integer")
           
    
    if (not isinstance(nRows , int)) or (nRows < 0):
        raise Exception("nRows must be a non-negative integer")
        
        
    if (not isinstance(nColumns , int)) or (nColumns < 0):
        raise Exception("nColumns must be a non-negative integer")
        
        
    

    neighbors = jpype.java.util.HashMap()
    for key, value in w.neighbors.items():
        tempSet = jpype.java.util.TreeSet()
        for v in value:
            tempSet.add(jpype.JInt(v))
        neighbors.put(jpype.JInt(key), tempSet)
    #print(neighbors)
    #print(w.neighbors.items())




    d_attribute_df = df[d_attribute_col]
    d_attribute = jpype.java.util.ArrayList()
    for d in d_attribute_df:
        d_attribute.add(jpype.JDouble(d))
    #print(d_attribute)
    #print(d_attribute_df)

   

 
    s_attribute_df = df[s_attribute_col]
    s_attribute = jpype.java.util.ArrayList()
    for s in s_attribute_df:
        s_attribute.add(jpype.JDouble(s))
    #print(s_attribute)
    #print(s_attribute_df)


    
    
    SMPP = jpype.JClass('edu.ucr.cs.pyneapple.regionalization.SMPPGraphPyneapple')()
    result = SMPP.execute_regionalization(jpype.JInt(cores), jpype.JInt(nRows), jpype.JInt(nColumns), 
                               jpype.JDouble(threshold), jpype.JInt(maxItr), 
                               jpype.JInt(lengthTabu), jpype.JDouble(t), jpype.JDouble(alpha),
                               jpype.JInt(convSA), jpype.JInt(random), d_attribute, 
                               s_attribute, neighbors)
                               
    areas = list(result[0])
    max_p = int(result[1])
    #SMPP.execute_regionalization(neighbors, d_attribute, 
                               #s_attribute, jpype.JLong(threshold))

    #areas = list(SMPP.getRegionLabels())
    #max_p = int(SMPP.getP())


    results = [max_p, areas]
    

    
    return results




########################################################################################


     
"""
if not jpype.isJVMStarted():
    #print("starting jvm")
     path = os.path.split(os.path.abspath(__file__))[0] + "/smpp_graph.jar"
    #print(path)
    #jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", "-Xmx20480m", '-Djava.class.path='+ path)
     jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", "-Xmx20480m", classpath = ["./smpp_graph.jar"])
 """
    
# test scalable_maxp

"""
path = libpysal.examples.get_path("mexicojoin.shp")
print("start df") 
df = geopandas.read_file(path)
print("end df") 
print("start w") 
#GRAPH = jpype.JClass('org.geotools.SMPPPineapple')()
#w = libpysal.weights.W(GRAPH.execute_neighbors(path))
#print(w)
w = libpysal.weights.Rook.from_dataframe(df)
print(w)
print("end w") 
result = scalable_maxp(df, w,'PCGDP1940', 'PERIMETER', 0)
print(result[0])
print(result[1])
"""

"""
path = "/Users/hessah/IdeaProjects/SMPPPineapple/Datasets/10K/10K.shp"
print("start df") 
df = geopandas.read_file(path)
print("end df") 
print("start w") 
GRAPH = jpype.JClass('org.geotools.SMPPGraphPineapple')()
#w = libpysal.weights.W(GRAPH.execute_neighbors(path))
w = libpysal.weights.Rook.from_dataframe(df)
print("end w") 
result = scalable_maxp(df, w, 'AWATER', 'ALAND', 250000000)
print(result[1])
print(result[2])
"""





#jpype.shutdownJVM()
