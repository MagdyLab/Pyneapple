package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import org.geotools.geometry.jts.ReferencedEnvelope;

import java.util.ArrayList;

/**
 * DataPartition class class describes the DataPartition object which is used to store data related to a data partition
 */
public class DataPartition {

    int ID;
    int seed;
    ArrayList<Integer> areas;
    ReferencedEnvelope envelope;

    /**
     * sets the partition ID
     * @param ID partition ID
     */

    public DataPartition(int ID) {

        this.ID = ID;
        this.areas = new ArrayList<>();
    }

    /**
     * copies a DataPartition object
     * @param dataPartition DataPartition object
     */

    public DataPartition(DataPartition dataPartition) {

        this.ID = dataPartition.ID;
        this.seed = dataPartition.seed;
        this.areas = new ArrayList<>(dataPartition.areas);
        if (dataPartition.envelope != null)
        this.envelope = new ReferencedEnvelope(dataPartition.envelope);
    }

    /**
     * sets the envelope for a DataPartition
     * @param envelope envelope for a DataPartition
     */

    public void setEnvelope(ReferencedEnvelope envelope) {

        this.envelope = envelope;
    }

    /**
     * returns the envelope of a DataPartition
     * @return the envelope of a DataPartition
     */

    public ReferencedEnvelope getEnvelope() {

        return envelope;
    }

    /**
     * adds an area
     * @param area area ID
     */
    public void addArea(int area) {

        this.areas.add(area);
    }

    /**
     * add areas
     * @param areas areas list
     */
    public  void addAreas(ArrayList<Integer> areas) {

        this.areas.addAll(areas);
    }

    /**
     * gets the areas
     * @return areas' list
     */

    public ArrayList<Integer> getAreas() {

        return this.areas;
    }

    /**
     * gets the ID
     * @return partition ID
     */

    public int getID() {

        return this.ID;
    }
}
