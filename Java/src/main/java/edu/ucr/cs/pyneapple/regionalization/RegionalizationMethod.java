package edu.ucr.cs.pyneapple.regionalization;

import org.locationtech.jts.io.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * The interface for defining the regionalization methods. The regionalization algorithms partition a set of areas in to regions. The result contains the total number of regions (p value) and the region label of each area.
 */
public interface RegionalizationMethod{
    /**
     * This method formulates the execute_regionalization method for the regionalization algorithms.
     * @param neighborSet A hashmap. The key is the index of each area. The value is the set of indices of the neighbor areas of the given area. The area index is assumed to be 0 to the number of areas that correspond to the attribute lists.
     * @param disAttr The list of dissimilarity attributes
     * @param sumAttr The list of attributes for the summation constraint.
     * @param threshold The lower-bound threshold for the sum constraint
     */
    public void execute_regionalization(Map<Integer, Set<Integer>> neighborSet,
                                               ArrayList<Long> disAttr,
                                               ArrayList<Long> sumAttr,
                                               Long threshold);

    /**
     * The method for getting the number of regions
     * @return the number of regions (integer)
     */
    public int getP();

    /**
     * The method returns the region label of the areas.
     * @return the region label of the areas
     */
    public int[] getRegionLabels();
}