import unittest
from pyneapple.regionalization.expressive_maxp import expressive_maxp
import pyneapple.weight.rook as rook
import libpysal 
import time
from libpysal.weights import Queen, Rook, KNN, Kernel, DistanceBand
import numpy as np
import geopandas
import pandas
import matplotlib.pyplot as plt
import jpype
from jpype import java
from jpype import javax

class TestEMP(unittest.TestCase):
    #pth = libpysal.examples.get_path("mexicojoin.shp")
    #mexico = geopandas.read_file(pth)
    def test_rook(self):
        pth = libpysal.examples.get_path("mexicojoin.shp")
        mexico = geopandas.read_file(pth)
        wj = rook.from_dataframe(mexico)
        wp = libpysal.weights.Rook.from_dataframe(mexico)
        self.assertTrue(wj[0], wp[0])

    def test_EMP(self):
        pth = libpysal.examples.get_path("mexicojoin.shp")
        mexico = geopandas.read_file(pth)
        w = rook.from_dataframe(mexico)
        mexico["count"] = 1
        inf = java.lang.Double.POSITIVE_INFINITY
        p, regions = expressive_maxp(mexico, w, 'count', 'count', -inf, inf, 'count', -inf, inf, 'count', -inf, inf, 'count', 1.0, inf, -inf, inf) 
        self.assertTrue(p, mexico.shape[0])
        
if __name__ == '__main__':
    unittest.main()