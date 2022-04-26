package edu.ucr.cs.pineapple.regionalization;

import edu.ucr.cs.pineapple.regionalization.EMPUtils.RegionCollection;
import edu.ucr.cs.pineapple.utils.ShapefileReader;
import junit.framework.TestCase;

import static edu.ucr.cs.pineapple.regionalization.EMP.set_shapefile_input;

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
    public void test_execute_regionalization_maxp() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
    }

    public void test_execute_regionalization_enriched() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(),
                sr.getDistAttr(),
                sr.getMinAttr(),
                1000.0,
                Double.POSITIVE_INFINITY,
                sr.getMaxAttr(),
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                sr.getAvgAttr(),
                1500.0,
                3500.0,
                sr.getSumAttr(),
                20000.0,
                Double.POSITIVE_INFINITY,
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }
    public void test_getP() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
        System.out.println(emp.getP());

    }


    public void test_getRegionList() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
        System.out.println(emp.getRegionList());
    }




}