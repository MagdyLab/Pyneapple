import sys
sys.path.append("..")
from region import pruc
import geopandas
import libpysal
import unittest

mexico = geopandas.read_file(libpysal.examples.get_path("mexicojoin.shp"))
mexico_w = libpysal.weights.Queen.from_dataframe(mexico)
mexico_label = [1, 1, 8, 8, 4, 4, 2, 5, 5, 5, 3, 8, 3, 7, 7, 5, 7, 5, 3, 3, 0, 0, 8, 9, 2, 8, 9, 4, 2, 6, 6, 0]
mexico_het = 155076.0

class TestPruc(unittest.TestCase):
    
    def test_pruc_basic(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        #the number of thread has to be set to 1 for testing purpose due to the uncontrolled schedueling sequence in the multithreading environment
        het , label = pruc(mexico, mexico_w,  sim_attr , ext_attr , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertListEqual(label,mexico_label)
        self.assertEqual(het, mexico_het)
        
    def test_pruc_gdf_format(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(None , mexico_w,  sim_attr , ext_attr , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("gdf must be a GeoDataFrame object" in str(context.exception))
    
    def test_pruc_w_format(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, None,  sim_attr , ext_attr , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("w must be a libpysal.weights.W object" in str(context.exception))
        
    def test_pruc_sim_attr_not_exist(self):
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  "any_input" , ext_attr , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("similarity attribute not in the attribute list" in str(context.exception))
        
    
    def test_pruc_sim_attr_not_numerical(self):
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  "CODE" , ext_attr , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("the values under the similarity attribute must be numerical values" in str(context.exception))
        

    def test_pruc_ext_attr_not_exist(self):
        sim_attr = 'PCGDP1940'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  sim_attr , "any_input" , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("extensive attribute not in the attribute list" in str(context.exception))
        
    
    def test_pruc_ext_attr_not_numerical(self):
        sim_attr = 'PCGDP1940'
        threshold = 3000000
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w, sim_attr , "CODE" , threshold , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("the values under the extensive attribute must be numerical values" in str(context.exception))
        
    
    def test_pruc_invalid_p(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  sim_attr , ext_attr , threshold , "1.2" ,  lo_iter , random_seed , num_thread)
        self.assertTrue("the number of regions must be positive integer" in str(context.exception))
        
    
    def test_pruc_invalid_threshold(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        p = 10
        lo_iter = mexico.shape[0]
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  sim_attr , ext_attr , "-34000" , p ,  lo_iter , random_seed , num_thread)
        self.assertTrue("threshold must be non-negative" in str(context.exception))
        
    
    def test_pruc_invalid_lo_iter(self):
        sim_attr = 'PCGDP1940'
        ext_attr = 'PERIMETER'
        threshold = 3000000
        p = 10
        random_seed = 2023117
        num_thread = 1
        with self.assertRaises(Exception) as context:
            pruc(mexico, mexico_w,  sim_attr , ext_attr , threshold , p ,  "-5" , random_seed , num_thread)
        self.assertTrue("lo_iter must be non-negative integers" in str(context.exception))
    
    
  
if __name__ == '__main__':
    unittest.main()
    