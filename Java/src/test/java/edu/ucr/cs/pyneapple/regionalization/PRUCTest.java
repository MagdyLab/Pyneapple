package edu.ucr.cs.pyneapple.regionalization;

import edu.ucr.cs.pyneapple.utils.PRUCUtils.Preprocess;
import org.locationtech.jts.geom.Geometry;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.*;

/**
 * The testing class of PRUC
 */
public class PRUCTest extends TestCase{

    static String normalDataset = "data/LACity/LACity.shp";


    /**
     * The default constructor
     */
    public PRUCTest(){}


    /**
     * Test the invalid p values (negative p values)
     * @throws Exception InterruptedException and CloneNotSupportedExceptio
     */
    public void test_invalid_p_input() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, -1);
    }


    /**
     * test invalid threhsold values, i.e., negative threshold values
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_invalid_threshold_input() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x, centroids_y,-2000L, 10);
    }


    /**
     * test invalid similarity attributes, i.e., negative similarity attribute
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_invalid_similarity_attribtue() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        disAttr.set(0 , -1L);
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x, centroids_y,2000L, 10);
    }


    /**
     * test invalid extensive attribute, i.e., negative extensive attribute
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_invalid_extensive_attribtue() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        sumAttr.set(0, -1L);
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, 10);
    }

    /**
     * test the execution of PRUC regionalization in regular settings
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_execute_regionalization_pruc() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, 10);
        System.out.println("successfully executed PRUC regionalization with p = 10 and threshold = 2000");
    }


    /**
     * test the execution of PRUC regionalization in special case, i.e., when number of regions is 0
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_execute_regionalization_pruc_special() throws Exception {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,0L, 10);
        System.out.println("successfully executed PRUC regionalization in special case with p = 10 and threshold = 0");
    }

    /**
     * test the getRegionLables method in PRUC class
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_get_region_lable() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, 10);
        System.out.println(pruc.getRegionLabels());

    }


    /**
     * test the getHeterogeneity() method in PRUC class
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_get_hetero() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, 10);
        System.out.println(pruc.getHeterogeneity());
    }


    /**
     * test the getP() method in PRUC class
     * @throws Exception InterruptedException and CloneNotSupportedException
     */
    public void test_get_p() throws Exception
    {
        Object[] ret = Preprocess.GeoSetBuilder(normalDataset);
        Map<Integer, Set<Integer>> neighborSet = (Map<Integer, Set<Integer>>)ret[0];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];
        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }
        PRUC pruc = new PRUC();
        pruc.execute_regionalization(neighborSet,disAttr,sumAttr,centroids_x,centroids_y,2000L, 10);
        System.out.println(pruc.getP());
    }


}
