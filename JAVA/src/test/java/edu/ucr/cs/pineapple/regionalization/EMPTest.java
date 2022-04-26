package edu.ucr.cs.pineapple.regionalization;

import edu.ucr.cs.pineapple.regionalization.EMPUtils.RegionCollection;
import junit.framework.TestCase;

public class EMPTest extends TestCase {
    static String normalDataset = "data/LACity/LACity.shp";
    static String negativeDataset = "data/LACity_negative_attr/LACity.shp";
    static EMP emp;
    public void test_set_input() throws Exception {
        EMP.set_input(negativeDataset,
                "pop_16up",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "unemployed",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "employed",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "pop2010",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "households"
        );
        EMP.set_input(normalDataset,
                "pop_16up",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "unemployed",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "employed",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "pop2010",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "households"
        );
    }
    //public void test
    public void test_getP() {


    }


    public void test_getRegionList() {
    }

    public void test_execute_regionalization_maxp() {
    }

    public void test_execute_regionalization_enriched() {
    }


}