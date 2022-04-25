package edu.ucr.cs.pineapple.regionalization;

import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface RegionalizationMethod{
    public void execute_regionalization(Map<Integer, Set<Integer>> neighbor,
                                               ArrayList<Long> disAttr,
                                               ArrayList<Long> sumAttr,
                                               Long threshold) throws ParseException, IOException;

    public int getP();
    public int[] getRegionList();
}