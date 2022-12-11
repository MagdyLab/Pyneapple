


import jpype
import pysal
import geopandas
import libpysal
from shapely.geometry import Polygon
from shapely.geometry import Point

"""

PRUC problem algorithm in python implementation 
source: Yongyi Liu, Ahmed R. Mahmood, Amr Magdy, Sergio Rey, PRUC : P-Regions with User-Defined Constraint", PVLDB, 15(3)

"""



def pruc(
        gdf,
        w,
        sim_attr,
        ext_attr,
        threshold,
        p,
        has_island,
        lo_iter,
):
    """
    
    The PRUC problem involves the aggregation of n areas into a predefined number of homogeneous regions
    while ensuring each region is contiguous and satisfies a user-defined constraint on some spatial 
    extensive attribute.
    
    Example: partition the 100 census tracts into 10 regions where each region must have at least 10k 
    population and the reigonal income difference is minimized.
    
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

    has_island : bool
        Whether or not the input data include island, default is false

    lo_iter : int
        The number of iterations in local optimization

    Returns
    ----------
    (hetero , label):
        if a feasible partition is found
        hetero is the heterogeneity of the partition found by the GSLO algorithm and the label is a list that corresponds to the area-region mapping

    or

    (None , None):
        if no feasible partition is found
    """
    jpype.startJVM()
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
        sAttr.add(jpype.JLong(gdf[sim_attr][i]))
        extAttr.add(jpype.JLong(gdf[ext_attr][i]))
        idList.add(jpype.JInt(i))
        x_centroids.add(jpype.JDouble(gdf['geometry'][i].centroid.x))
        y_centroids.add(jpype.JDouble(gdf['geometry'][i].centroid.y))

    PRUC = jpype.JClass('Test')()
    result = PRUC.execute_GSLO(jpype.JInt(p), jpype.JLong(threshold), jpype.JBoolean(has_island), jpype.JInt(lo_iter),
                               neighborHashMap, extAttr, sAttr, x_centroids, y_centroids)

    if len(result) == 2:
        hetero = float(result[0])
        l = list(result[1])
        return hetero, l
    else:
        return None, None


class PrucHeuristic():
    
    """
    
    The PRUC problem involves the aggregation of n areas into a predefined number of homogeneous regions
    while ensuring each region is contiguous and satisfies a user-defined constraint on some spatial 
    extensive attribute.
    
    Example: partition the 100 census tracts into 10 regions where each region must have at least 10k 
    population and the reigonal income difference is minimized.
    
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

    has_island : bool
        Whether or not the input data include island, default is false

    lo_iter : int
        The number of iterations in local optimization

    Returns
    ----------
    (hetero , label):
        if a feasible partition is found
        hetero is the heterogeneity of the partition found by the GSLO algorithm and the label is a list that corresponds to the area-region mapping

    or

    (None , None):
        if no feasible partition is found
    """
    
    def __init__(
            self,
            gdf,
            w,
            sim_attr,
            ext_attr,
            threshold,
            p,
            has_island,
            lo_iter
    ):
        
        self.gdf = gdf
        self.w = w
        self.sim_attr = sim_attr
        self.ext_attr = ext_attr
        self.threshold = threshold
        self.p = p
        self.has_island = has_island
        self.lo_iter = lo_iter
    
    def solve(self):
        """ solve pruc poblem and get back the results """
        hetero, l = pruc(
                self.gdf,
                self.w,
                self.sim_attr,
                self.ext_attr,
                self.threshold,
                self.p,
                self.has_island,
                self.lo_iter,
        )
        
        self.hetero = hetero
        self.l = l

'''
gdf = geopandas.read_file('DataFile/5K/5K.shp')
w = libpysal.weights.Rook.from_shapefile('DataFile/5K/5K.shp')

print(pruc(gdf, w, 'ALAND', 'AWATER', 100000, 10, True, gdf.shape[0]))
jpype.shutdownJVM()

'''