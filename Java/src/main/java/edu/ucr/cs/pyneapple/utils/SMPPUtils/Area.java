package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Area class describes the area object
 */
public class Area {

    private ArrayList<Integer> subareas_IDs; // coarsened graph areas
    private ArrayList<Integer> original_subareas_IDs; // original graph areas
    private ArrayList<Area> subareas;
    private ArrayList<Integer> neighbors;
    private int ID;
    private int degree;
    private boolean parent; // is the area a parent area?
    private boolean nested; // is the area a nested area?
    private Geometry polygon;
    private ArrayList<Integer> nested_areas; // if it is a parent area
    private int parent_ID; // if it is a nested area
    private HashMap<Integer, Integer> neighbors2; // neighbor ID, degree

    /**
     * Area constructor
     * @param id area ID
     */
    public Area(int id) {

        this.ID = id;
        this.subareas_IDs = new ArrayList<>();
        this.original_subareas_IDs = new ArrayList<>();
        this.subareas = new ArrayList<>();
        this.neighbors = new ArrayList<>();
        this.neighbors2 = new HashMap<>();
        this.degree = 1;
        this.parent = false;
        this.nested = false;
        this.polygon = null;
        this.nested_areas = new ArrayList<>();
        this.parent_ID = -1;
    }

    /**
     * gets the area ID
     * @return area ID
     */

    public int get_ID() {

        return this.ID;
    }

    /**
     * set sub areas
     * @param areas sub areas
     */
    public void set_subareas_IDs(ArrayList<Integer> areas) {

        this.subareas_IDs = areas;
    }

    /**
     * gets sub areas
     * @return sub areas
     */
    public ArrayList<Integer> get_subareas_IDs() {

        return this.subareas_IDs;
    }

    /**
     * sets sub areas
     * @param areas sub areas
     */
    public void set_original_subareas_IDs(ArrayList<Integer> areas) {

        this.original_subareas_IDs = areas;
    }

    /**
     * get sub areas
     * @return sub areas
     */
    public ArrayList<Integer> get_original_subareas_IDs() {

        return this.original_subareas_IDs;
    }

    /**
     * set sub area ID
     * @param ID sub area ID
     */
    public void add_original_subarea_ID(int ID) {

        this.original_subareas_IDs.add(ID);
    }


    /**
     * set neighbors
     * @param neighbors neighbors list
     */
    public void set_neighbors(ArrayList<Integer> neighbors) {

        this.neighbors = neighbors;
    }

    /**
     * get neighbors
     * @return neighbors list
     */
    public ArrayList<Integer> get_neighbors() {

        return this.neighbors;
    }

    /**
     * set neighbors
     * @param neighbors2 neighbors hashmap
     */
    public void set_neighbors2(HashMap<Integer, Integer> neighbors2) {

        this.neighbors2 = neighbors2;
    }

    /**
     * get neighbors
     * @return neighbors hashmap
     */
    public HashMap<Integer, Integer> get_neighbors2() {

        return this.neighbors2;
    }

    /**
     * set degree
     * @param degree area degree
     */
    public void set_degree(int degree) {

        this.degree = degree;
    }

    /**
     * get degree
     * @return area degree
     */
    public int get_degree() {

        return this.degree;
    }

    /**
     * set as parent
     */
    public void set_parent() {

        this.parent = true;
    }

    /**
     * check if it is parent area
     * @return true if it is parent area
     */
    public boolean is_parent() {

        return this.parent;
    }

    /**
     * set nested
     * @param parent_ID parent ID
     */
    public void set_nested(int parent_ID) {

        this.nested = true;
        this.parent_ID = parent_ID;
    }

    /**
     * checks if it is nested area
     * @return true if it is nested area
     */
    public boolean is_nested() {

        return this.nested;
    }

    /**
     * set nested areas
     * @param nested_areas nested areas list
     */
    public void set_nested_areas(ArrayList<Integer> nested_areas) {

        this.nested_areas = nested_areas;
    }

    /**
     * get nested areas
     * @return nested areas list
     */
    public ArrayList<Integer> get_nested_areas() {

        return this.nested_areas;
    }

    /**
     * get parent ID
     * @return parent ID
     */
    public int get_parent_ID() {

        return this.parent_ID;
    }

} // end class Area

