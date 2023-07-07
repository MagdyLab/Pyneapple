package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class describes the Area class which is a spatial area that has numerical attribute and represented by a set of marginal coordinates
 */
public class Area implements Cloneable , Serializable {


    /**
     * the index of the area
     */
    private int index;

    /**
     * the similarity attribute of the area
     */
    private double sim_attr;

    /**
     * the extensive attribute of the area
     */
    private double extensive_attr;

    /**
     * the centroids of the area
     */
    private double[] centroid;

    /**
     * the index of the neighboring areas
     */
    private ArrayList<Integer> neigh_area_index;
    /**
     * //the region it is associated to
     */
    private int associate_region_index;

    /**
     * The Area class correspond to a spatial polygon, which is the basic unit in regionalization
     * @param index the unique identifier of an area
     * @param sim_attr the similarity attribute
     * @param extensive_attr the extensive attribute
     * @param centroid the arrays of centroids for each of the spatial polygons
     */
    public Area(int index , double sim_attr, double extensive_attr , double[] centroid)
    {
        this.index = index;
        this.sim_attr = sim_attr;
        this.extensive_attr = extensive_attr;
        this.centroid = centroid;
        neigh_area_index = new ArrayList<>();
        associate_region_index = -1;
    }




    /**
     * Compute the Euclidean between this area and area a based on their centroids
     * @param a the area to compute distance with
     * @return the Euclidean distance between this area and area a
     */
    public double compute_dist(Area a)
    {
        double[] a_centroid = a.get_centroid();
        return  Math.sqrt((centroid[0] - a.get_centroid()[0]) * (centroid[0] - a.get_centroid()[0]) + (centroid[1] - a_centroid[1]) * (centroid[1] - a_centroid[1]));

    }


    /**
     * set the centroids of this area
     * @param centroid the centroid of this area (regarding x and y)
     */
    public void set_centroid(double[] centroid)
    {
        this.centroid = centroid;
    }

    /**
     * assign this area to a specific region by using region index
     * @param region_index the index of the region this area is assigned to
     */
    public void set_region(int region_index)
    {
        this.associate_region_index = region_index;
    }


    /**
     * add a neighboring area to this area through the index of areas
     * @param add_index the index of the newly added area
     */
    public void add_neighbor(int add_index)
    {
        neigh_area_index.add(add_index);
    }


    /**
     * construct the neighboring areas of this area
     * @param neighbor_to_set the indicecs of the neighboring areas for this area
     */
    public void set_neighbor_once(ArrayList<Integer> neighbor_to_set)
    {
        this.neigh_area_index = neighbor_to_set;
    }


    /**
     * get the index of the area
     * @return the index of this area
     */
    public int get_geo_index() { return index; }


    /**
     * get the similarity attribute of the area
     * @return the similarity attribute of this area
     */
    public double get_internal_attr()
    {
        return sim_attr;
    }

    /**
     * get the extensive attribute of the area
     * @return the extensive attribute of this area
     */
    public double get_extensive_attr()
    {
        return extensive_attr;
    }


    /**
     * get the neighboring areas for this area
     * @param all_areas the Arraylist that includes all the areas
     * @return the neighboring areas of this area (in actual Area type rather than index)
     */
    public ArrayList<Area> get_neigh_area(ArrayList<Area> all_areas) {
        ArrayList<Area> neigh_areas = new ArrayList<>();
        for(int neigh_index : neigh_area_index)
        {
            neigh_areas.add(all_areas.get(neigh_index));
        }
        return neigh_areas;
    }

    /**
     * get the indices of the neighboring areas for this area
     * @return the indices of the neighboring areas for this area
     */
    public ArrayList<Integer> get_neigh_area_index()
    {
        return neigh_area_index;
    }

    /**
     * get the index of the region that this area is associated with
     * @return the index of the region that this area is associated with
     */
    public int get_associated_region_index() { return associate_region_index; }


    /**
     * get the centroids of the current area
     * @return the centroid coordinate of this area
     */
    public double[] get_centroid() { return centroid; }


    /**
     * compute the heterogeneity when adding neigh_area to the current region
     * @param neigh_area the area to compute heterogeneity with
     * @return the heterogeneity between this area and neigh_area
     */
    public double compute_hetero(Area neigh_area) {
        return Math.abs(sim_attr - neigh_area.get_internal_attr());
    }


    /**
     * initialize the neighbors of this area
     */
    public void initialize_neighbor() {
        neigh_area_index = new ArrayList<>();
    }



    protected static ArrayList<Area> area_list_copy(ArrayList<Area> all_areas) throws CloneNotSupportedException {
        ArrayList<Area> returned_areas = new ArrayList<>();
        for(Area g : all_areas)
        {
            returned_areas.add((Area)g.clone());
        }
        return returned_areas;
    }



    @Override
    protected Object clone() {
        Area g = new Area(this.get_geo_index() , this.get_internal_attr() , this.get_extensive_attr() , this.get_centroid());
        g.set_region(this.get_associated_region_index());
        g.set_neighbor_once((ArrayList<Integer>)neigh_area_index.clone());
        return g;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area)) return false;
        return this.get_geo_index() == ((Area) o).get_geo_index();
    }





}
