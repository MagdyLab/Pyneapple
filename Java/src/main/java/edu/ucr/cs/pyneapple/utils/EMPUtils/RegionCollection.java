package edu.ucr.cs.pyneapple.utils.EMPUtils;

import java.util.Map;

/**
 * The utility class for representing a partition. Mainly used for recording results.
 */
public class RegionCollection {
    private int max_p;
    private int[] labels;
    private Map<Integer, Region> regionMap;
    //private Map<Integer, Integer> regionSpatialAttr;
    private int unassignedCount;

    /**
     * Construct a partition for the given results
     * @param m The number of regions
     * @param l The area labels
     * @param rl The region maps
     */
    public RegionCollection(int m, int[] l, Map<Integer, Region> rl){
        max_p = m;
        labels = l;
        regionMap = rl;
        //regionSpatialAttr = null;
        unassignedCount = -1;
    }

    /**
     * Construct an empty partition
     */
    public RegionCollection(){
        max_p = 0;
        labels = null;
        regionMap = null;
        //regionSpatialAttr = null;
        unassignedCount = -1;
    }

    /**
     * Get the number of regions
     * @return the number of regions
     */
    public int getMax_p(){
        return max_p;
    }

    /**
     * Get the labels of areas
     * @return the labels of areas
     */
    public int[] getLabels() {
        return labels;
    }

    /**
     * Set the number of unassigned areas
     * @param unassignedCount the number of unassigned areas
     */
    public void setUnassignedCount(int unassignedCount) {
        this.unassignedCount = unassignedCount;
    }

    /**
     * Get the number of unassigned areas
     * @return  the number of unassigned areas
     */
    public int getUnassignedCount(){
        return this.unassignedCount;
    }

    /**
     * Set the number of regions
     * @param max_p the number of regions
     */
    public void setMax_p(int max_p){ this.max_p = max_p;}

    /**
     * Set the region labels of areas
     * @param labels the region labels of areas
     */
    public void setLabels(int [] labels){ this.labels = labels;}

    /**
     * Get the region map
     * @return the region map
     */
    public Map<Integer, Region> getRegionMap(){
        return regionMap;
    }
    /*public Map<Integer, Integer> getRegionSpatialAttr(){
        return regionSpatialAttr;
    }*/
}