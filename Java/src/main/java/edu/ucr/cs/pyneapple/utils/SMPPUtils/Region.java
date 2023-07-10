package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import java.util.ArrayList;

/**
 * Region class describes a Region object which is used to store data about a region in max-p solution
 */
public class Region {

    private ArrayList<Integer> areas;
    private int ID;
    private double regionalThreshold;
    private double dissimilarity;

    /**
     * copies a region
     * @param region object
     */

    public Region(Region region) {

        this.areas = new ArrayList<>(region.areas);
        this.ID = region.ID;
        this.regionalThreshold = region.regionalThreshold;
        this.dissimilarity = region.dissimilarity;
    }

    /**
     * region constructor
     * @param id region ID
     */
    public Region(int id) {

        this.areas = new ArrayList<>();
        this.ID = id;
        this.regionalThreshold = 0;
        this.dissimilarity = 0;
    }

    /**
     * sets the region ID
     * @param id region ID
     */

    public void setID(int id) {

        this.ID = id;
    }

    /**
     * gets the region ID
     * @return region ID
     */

    public int getID() {

        return this.ID;
    }

    /**
     * adds an area to the region
     * @param area area ID
     */

    public void addArea(Integer area) {

        this.areas.add(area);
    }

    /**
     * removes an area from the region
     * @param area area ID
     */
    public void removeArea(Integer area) {

        this.areas.remove(area);
    }

    /**
     * gets the region areas
     * @return region areas
     */

    public ArrayList<Integer> getAreas() {

        return this.areas;
    }

    /**
     * sets the region threshold
     * @param threshold region threshold
     */

    public void setRegionalThreshold(double threshold) {

        this.regionalThreshold = threshold;
    }

    /**
     * gets the region threshold
     * @return region threshold
     */

    public double getRegionalThreshold() {

        return this.regionalThreshold;
    }

    /**
     * sets the region dissimilarity
     * @param dissimilarity region dissimilarity
     */
    public void setDissimilarity(double dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    /**
     * gets the region dissimilarity
     * @return region dissimilarity
     */

    public double getDissimilarity() {

        return this.dissimilarity;
    }
}

