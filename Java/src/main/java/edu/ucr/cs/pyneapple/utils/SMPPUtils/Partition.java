package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Partition class describes the Partition object which is used to store data for a max-p solution
 */
public class Partition {

    private int ID;
    private HashMap<Integer, Region> regions;
    private ArrayList<Integer> enclaves;
    private ArrayList<Integer> assignedAreas;
    private HashMap<Integer, Integer> areasWithRegions;
    private double dissimilarity;

    /**
     * copies a Partition
     * @param partition partition object
     */

    public Partition(Partition partition) {

        this.ID = partition.ID;
        this.regions = new HashMap<>(partition.regions);
        this.enclaves = new ArrayList<>(partition.enclaves);
        this.assignedAreas = new ArrayList<>(partition.assignedAreas);
        this.areasWithRegions = new HashMap<>(partition.areasWithRegions);
        this.dissimilarity = partition.dissimilarity;
    }

    /*public Partition(int ID, HashMap<Integer, Region> regions, double dissimilarity) {

        this.ID = ID;
        this.regions = new HashMap<>(regions);
        this.dissimilarity = dissimilarity;
    }*/

    /**
     * constructor for Partition
     * @param id partition ID
     */
    public Partition(int id) {

        this.ID = id;
        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    /**
     * constructor for Partition
     */

    public Partition() {

        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    /**
     * adds a region to the partition
     * @param regionID region ID
     * @param region region object
     */

    public void addRegion(int regionID, Region region) {

        this.regions.put(regionID, region);
    }

    /**
     * gets the regions of a partition
     * @return regions hashmap
     */

    public HashMap<Integer, Region> getRegions() {

        return this.regions;
    }

    /**
     * adds enclaves to a partition
     * @param enclave enclave ID
     */

    public void addEnclaves(ArrayList<Integer> enclave) {

        this.enclaves.addAll(enclave);
    }

    /**
     * gets the enclaves of a partition
     * @return encalves list
     */

    public ArrayList<Integer> getEnclaves() {

        return this.enclaves;
    }

    /**
     * adds the assigned areas of a partition
     * @param assignedArea assigned areas list
     */

    public void addAssignedAreas(ArrayList<Integer> assignedArea) {

        this.assignedAreas.addAll(assignedArea);
    }

    /**
     * gets the assigned areas of a partition
     * @return assigned areas list
     */

    public ArrayList<Integer> getAssignedAreas() {

        return this.assignedAreas;
    }

    /**
     * resets the regions of a partition
     * @param newRegions regions of a partition
     */

    public void resetRegions(HashMap<Integer, Region> newRegions) {

        this.regions = new HashMap<>(newRegions);
    }

    /**
     * sets the areas labels with regions labels
     * @param areasWithRegions areas labels with regions label hashmap
     */

    public void setAreasWithRegions(HashMap<Integer, Integer> areasWithRegions) {

        this.areasWithRegions = areasWithRegions;
    }

    /**
     * gets the areas labels with regions labels
     * @return areas labels with regions labels hashmap
     */

    public HashMap<Integer, Integer> getAreasWithRegions() {

        return this.areasWithRegions;
    }

    /*public void updateAreasWithRegions(int newRegion, int area) {

        this.areasWithRegions.put(area, newRegion);
    }*/

    /**
     * sets the dissimilarity of the region
     * @param dissimilarity dissimilarity of the region
     */

    public void setDissimilarity(double dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    /**
     * gets the dissimilarity of the region
     * @return dissimilarity of the region
     */

    public double getDissimilarity() {

        return this.dissimilarity;
    }

    /*public double calculateDissimilarity() {

        double dissimilarity = 0;

        for (int regionID : this.regions.keySet()) {

            double regionDissimilarity = this.regions.get(regionID).getDissimilarity();
            dissimilarity = dissimilarity + regionDissimilarity;
        }

        return dissimilarity;
    }*/


    /*public int getPartitionID() {

        return this.ID;
    }*/
}
