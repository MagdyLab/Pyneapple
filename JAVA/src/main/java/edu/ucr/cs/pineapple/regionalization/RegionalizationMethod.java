package edu.ucr.cs.pineapple.regionalization;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface RegionalizationMethod{
    public void execute_regionalization(Map<Integer, Set<Integer>> neighbor,
                                        ArrayList<Long> disAttr,
                                        ArrayList<Long> sumAttr,
                                        Double threshold);
    public int getP();
    public int[] getRegionList();
}