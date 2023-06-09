package edu.ucr.cs.pyneapple.regionalization;

import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public interface RegionalizationMethod{
    /**
     * This method formulates the execute_regionalization method for the regionalization algorithms.
     * @param neighborSet A hashmap. The key is the index of each area. The value is the set of indices of the neighbor areas of the given area. The area index is assumed to be 0 to the number of areas that correspond to the attribute lists.
     * @param disAttr The list of dissimilarity attributes
     * @param sumAttr The list of attributes for the summation constraint.
     * @param threshold The lower-bound threshold for the sum constraint
     * @throws ParseException
     * @throws IOException
     */
    public void execute_regionalization(Map<Integer, Set<Integer>> neighborSet,
                                               ArrayList<Long> disAttr,
                                               ArrayList<Long> sumAttr,
                                               Long threshold) throws ParseException, IOException;

    /**
     * The method for getting the number of regions
     * @return the number of regions (integer)
     */
    public int getP();

    /**
     * The method returns the region label of the areas.
     * @return
     */
    public int[] getRegionLabels();
}