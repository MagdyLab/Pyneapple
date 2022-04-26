package edu.ucr.cs.pineapple.regionalization.EMPUtils;

import java.util.Map;

public class RegionCollection {
    private int max_p;
    private int[] labels;
    private Map<Integer, Region> regionList;
    private Map<Integer, Integer> regionSpatialAttr;
    private int unassignedCount;
    public RegionCollection(int m, int[] l, Map<Integer, Region> rl){
        max_p = m;
        labels = l;
        regionList = rl;
        regionSpatialAttr = null;
        unassignedCount = -1;
    }
    public RegionCollection(){
        max_p = 0;
        labels = null;
        regionList = null;
        regionSpatialAttr = null;
        unassignedCount = -1;
    }
    public int getMax_p(){
        return max_p;
    }
    public int[] getLabels() {
        return labels;
    }

    public void setUnassignedCount(int unassignedCount) {
        this.unassignedCount = unassignedCount;
    }
    public int getUnassignedCount(){
        return this.unassignedCount;
    }
    public void setMax_p(int max_p){ this.max_p = max_p;}
    public void setLabels(int [] labels){ this.labels = labels;}

    public Map<Integer, Region> getRegionList(){
        return regionList;
    }
    public Map<Integer, Integer> getRegionSpatialAttr(){
        return regionSpatialAttr;
    }
}