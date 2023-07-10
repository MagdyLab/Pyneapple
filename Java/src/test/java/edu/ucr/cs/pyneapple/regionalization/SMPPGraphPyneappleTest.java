package edu.ucr.cs.pyneapple.regionalization;

import junit.framework.TestCase;
import org.locationtech.jts.geom.Geometry;

import java.util.*;

/**
 * this class provides a test for SMP algorithm
 */
public class SMPPGraphPyneappleTest extends TestCase {

    static String shp_file = "data/10K/10K.shp";

    /**
     * the constructor for SMPPGraphPyneappleTest class
     */
    public SMPPGraphPyneappleTest(){}


    /**
     * The test case for execute_regionalization() function
     * @throws Exception Exception when the shapefile cannot be loaded or the attributes of the corresponding name does not exist in the shapefile.
     */
    public void test_execute_regionalization() throws Exception {

        SMPPGraphPyneapple smpp = new SMPPGraphPyneapple();

        ArrayList<String> string_polygons = new ArrayList<>();
        ArrayList<Geometry> areas_polygons = new ArrayList<>();
        ArrayList<Integer> areas_IDs = new ArrayList<>();
        ArrayList<Double> diss_attr = new ArrayList<>();
        ArrayList<Double> ex_attr = new ArrayList<>();
        smpp.read_files(shp_file, string_polygons, areas_polygons, areas_IDs, diss_attr, ex_attr, "AWATER", "ALAND");

        Map<Integer, Set<Integer>> neighbors = smpp.createNeighborsList(areas_polygons);

        smpp.execute_regionalization(4, 2, 2, 250000000, 50, 100, 1, 0.9, 50, 0, diss_attr, ex_attr, neighbors);

    }


    /**
     * The test case for the getP() interface. The function should print the P value after the regionalization computation.
     * @throws Exception Exception when the shapefile cannot be loaded or the attributes of the corresponding name does not exist in the shapefile.
     */
    public void test_getP() throws Exception {

        SMPPGraphPyneapple smpp = new SMPPGraphPyneapple();

        ArrayList<String> string_polygons = new ArrayList<>();
        ArrayList<Geometry> areas_polygons = new ArrayList<>();
        ArrayList<Integer> areas_IDs = new ArrayList<>();
        ArrayList<Double> diss_attr = new ArrayList<>();
        ArrayList<Double> ex_attr = new ArrayList<>();
        smpp.read_files(shp_file, string_polygons, areas_polygons, areas_IDs, diss_attr, ex_attr, "AWATER", "ALAND");

        Map<Integer, Set<Integer>> neighbors = smpp.createNeighborsList(areas_polygons);

        smpp.execute_regionalization(4, 2, 2, 250000000, 50, 100, 1, 0.9, 50, 0, diss_attr, ex_attr, neighbors);

        System.out.println(smpp.getP());

    }


    /**
     * The test case for the getRegionList() interface. An array storing the region lable of each area should be printed out.
     * @throws Exception Exception when the shapefile cannot be loaded or the attributes of the corresponding name does not exist in the shapefile.
     */
    public void test_getRegionLabels() throws Exception {
        SMPPGraphPyneapple smpp = new SMPPGraphPyneapple();

        ArrayList<String> string_polygons = new ArrayList<>();
        ArrayList<Geometry> areas_polygons = new ArrayList<>();
        ArrayList<Integer> areas_IDs = new ArrayList<>();
        ArrayList<Double> diss_attr = new ArrayList<>();
        ArrayList<Double> ex_attr = new ArrayList<>();
        smpp.read_files(shp_file, string_polygons, areas_polygons, areas_IDs, diss_attr, ex_attr, "AWATER", "ALAND");

        Map<Integer, Set<Integer>> neighbors = smpp.createNeighborsList(areas_polygons);

        smpp.execute_regionalization(4, 2, 2, 250000000, 50, 100, 1, 0.9, 50, 0, diss_attr, ex_attr, neighbors);
        System.out.println(Arrays.toString(smpp.getRegionLabels()));
    }


}
