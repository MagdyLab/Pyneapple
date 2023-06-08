package edu.ucr.cs.pyneapple.regionalization;

import edu.ucr.cs.pyneapple.utils.ShapefileReader;
import junit.framework.TestCase;

import java.util.Arrays;

public class EMPTest extends TestCase {
    static String normalDataset = "data/LACity/LACity.shp";
    static String negativeDataset = "data/LACity_negative_attr/LACity.shp";
    static EMP emp;

    /**
     *The test case for the function that takes the shapefile and constraints to perform the regionalization.
     * The attributes of the dataset are supposed to be non-negative.
     * Exceptions will be thrown when the file is not available or the attributes contains negative values.
     * @throws Exception
     */
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

    /**
     * The test case for the EMP interface when dealing with only the max-p-regions configuration
     * @throws Exception
     */
    public void test_execute_regionalization_maxp() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
    }

    /**
     * The test case for EMP with enriched constraint configuration.
     * @throws Exception
     */
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

    /**
     * The test case for the getP() interface. The function should print the P value after the regionalization computation.
     * @throws Exception
     */
    public void test_getP() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
        System.out.println(emp.getP());

    }

    /**
     * The test case for the getRegionList() interface. An array storing the region lable of each area should be printed out.
     * @throws Exception
     */
    public void test_getRegionLabels() throws Exception {
        ShapefileReader sr = new ShapefileReader(normalDataset, "pop_16up", "unemployed", "employed", "pop2010", "households");
        EMP emp = new EMP();
        //emp.execute_regionalization(sr.getNeighborMap(), sr.getDistAttr(), sr.getSumAttr(), (long) 20000);
        System.out.println(Arrays.toString(emp.getRegionLabels()));
    }





}