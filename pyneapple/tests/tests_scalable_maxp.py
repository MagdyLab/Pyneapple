# -*- coding: utf-8 -*-
"""
Spyder Editor

This is a temporary script file.
"""
import sys
#sys.path.append("..")
from pyneapple.regionalization.scalable_maxp import scalable_maxp
import geopandas
import libpysal
import unittest



path = libpysal.examples.get_path("mexicojoin.shp")
df = geopandas.read_file(path)
w = libpysal.weights.Rook.from_dataframe(df)



class Test_SMP(unittest.TestCase):
    
    
    def test_smpp(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = 0
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        #the number of thread has to be set to 1 for testing purpose due to the uncontrolled schedueling sequence in the multithreading environment
        areas, max_p = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertEqual(max_p, df.shape[0])
       
       
    def test_smpp_df_format(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = 0
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(None, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("df must be a GeoDataFrame object") 
        
        
    def test_smpp_w_format(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = 0
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, None, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("w must be a libpysal.weights.W object") 
     
        
    def test_smpp_s_attr_not_exist(self):
        d_attribute_col_name = "any_input"
        s_attribute_col_name = 'PERIMETER'
        threshold = 0
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("similarity attribute not in the attribute list") 
        
        
    def test_smpp_d_attr_not_exist(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = "any_input"
        threshold = 0
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("extensive attribute not in the attribute list") 
        
        
    def test_smpp_invalid_threshold(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = -1000
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("threshold must be non-negative") 
        

    def test_smpp_invalid_maxItr(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = 0
        maxItr = -40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("maxItr must be a non-negative integer") 
        
        
    def test_smpp_invalid_convSA(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = -1000
        maxItr = 40
        convSA = -90
        cores = 1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("convSA must be a non-negative integer")
        
        
    def test_smpp_invalid_cores(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = -1000
        maxItr = 40
        convSA = 90
        cores = -1
        nRows = 1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("cores must be a non-negative integer")
        
        
    def test_smpp_invalid_nRows(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = -1000
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = -1
        nColumns = 1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("nRows must be a non-negative integer")
        
        
    def test_smpp_invalid_nColumns(self):
        d_attribute_col_name = 'PCGDP1940'
        s_attribute_col_name = 'PERIMETER'
        threshold = -1000
        maxItr = 40
        convSA = 90
        cores = 1
        nRows = 1
        nColumns = -1
        with self.assertRaises(Exception):
            max_p, areas = scalable_maxp(df, w, d_attribute_col_name, s_attribute_col_name, threshold, maxItr, convSA, cores, nRows, nColumns)
        self.assertTrue("convSA must be a non-negative integer")
        
        
        

if __name__ == '__main__':
    unittest.main()




