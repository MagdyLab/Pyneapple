import sys
from pyneapple.regionalization.generalized_p import generalized_p
import geopandas
import libpysal
import unittest

mexico = geopandas.read_file(libpysal.examples.get_path("mexicojoin.shp"))
mexico_w = libpysal.weights.Queen.from_dataframe(mexico)
mexico_record_num = 32


class TestPruc(unittest.TestCase):
    
    def test_pruc_basic(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        het , label = generalized_p(mexico, mexico_w,  sim_attr , ext_attr , threshold , p)
        self.assertEqual(len(label),  mexico_record_num )
        
    def test_pruc_gdf_format(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(None , mexico_w,  sim_attr , ext_attr , threshold , p)
        self.assertTrue("gdf must be a GeoDataFrame object" in str(context.exception))
    
    def test_pruc_w_format(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, None,  sim_attr , ext_attr , threshold , p)
        self.assertTrue("w must be a libpysal.weights.W object" in str(context.exception))
        
    def test_pruc_sim_attr_not_exist(self):
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w,  "any_input" , ext_attr , threshold , p)
        self.assertTrue("similarity attribute not in the attribute list" in str(context.exception))
        
    
    def test_pruc_sim_attr_not_numerical(self):
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w,  "CODE" , ext_attr , threshold , p)
        self.assertTrue("the values under the similarity attribute must be numerical values" in str(context.exception))
        

    def test_pruc_ext_attr_not_exist(self):
        sim_attr = 'PCGDP1940'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w,  sim_attr , "any_input" , threshold , p)
        self.assertTrue("extensive attribute not in the attribute list" in str(context.exception))
        
    
    def test_pruc_ext_attr_not_numerical(self):
        sim_attr = 'PCGDP1940'
        threshold = 3000000
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w, sim_attr , "CODE" , threshold , p)
        self.assertTrue("the values under the extensive attribute must be numerical values" in str(context.exception))
        
    
    def test_pruc_invalid_p(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w,  sim_attr , ext_attr , threshold , "1.2")
        self.assertTrue("the number of regions must be positive integer" in str(context.exception))
        
    
    def test_pruc_invalid_threshold(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        p = 10
        with self.assertRaises(Exception) as context:
            generalized_p(mexico, mexico_w,  sim_attr , ext_attr , "-34000" , p)
        self.assertTrue("threshold must be non-negative" in str(context.exception))
        

    
    
  
if __name__ == '__main__':
    unittest.main()
    