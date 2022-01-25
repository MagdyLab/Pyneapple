package edu.ucr.cs.pineapple.regionalization;

import java.util.Map;

public class RegionCollectionNew{
    private int max_p;
    private int[] labels;
    private Map<Integer, RegionNew> regionList;
    private Map<Integer, Integer> regionSpatialAttr;
    private int unassignedCount;
    public RegionCollectionNew(int m, int[] l, Map<Integer, RegionNew> rl){
        max_p = m;
        labels = l;
        regionList = rl;
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

    public Map<Integer, RegionNew> getRegionList(){
        return regionList;
    }
    public Map<Integer, Integer> getRegionSpatialAttr(){
        return regionSpatialAttr;
    }
}