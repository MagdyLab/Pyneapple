

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class describes the Area class which is a spatial area that has numerical attribute and represented by a set of marginal coordinates
 */
public class Area implements Cloneable , Serializable {

    private int index;
    private double sim_attr;
    private double extensive_attr;
    private double[] centroid;
    private ArrayList<Integer> neigh_area_index;
    private int associate_region_index;

    /**
     *
     * @param index the unique identifier of an area
     * @param sim_attr the similarity attribute
     * @param extensive_attr the extensive attribute
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
     *
     * @param a the area to compute distance with
     * @return the euclidean distance between this area and area a
     */
    public double compute_dist(Area a)
    {
        double[] a_centroid = a.get_centroid();
        return  Math.sqrt((centroid[0] - a.get_centroid()[0]) * (centroid[0] - a.get_centroid()[0]) + (centroid[1] - a_centroid[1]) * (centroid[1] - a_centroid[1]));

    }

    public void set_centroid(double[] centroid)
    {
        this.centroid = centroid;
    }

    public void set_region(int region_index)
    {
        this.associate_region_index = region_index;
    }

    public void add_neighbor(int add_index)
    {
        neigh_area_index.add(add_index);
    }

    public void set_neighbor_once(ArrayList<Integer> neighbor_to_set)
    {
        this.neigh_area_index = neighbor_to_set;
    }

    public int get_geo_index() { return index; }

    public double get_internal_attr()
    {
        return sim_attr;
    }

    public double get_extensive_attr()
    {
        return extensive_attr;
    }


    public ArrayList<Area> get_neigh_area(ArrayList<Area> all_areas) {
        ArrayList<Area> neigh_areas = new ArrayList<>();
        for(int neigh_index : neigh_area_index)
        {
            neigh_areas.add(all_areas.get(neigh_index));
        }
        return neigh_areas;
    }

    public ArrayList<Integer> get_neigh_area_index()
    {
        return neigh_area_index;
    }

    public int get_associated_region_index() { return associate_region_index; }


    public double[] get_centroid() { return centroid; }


    public double compute_hetero(Area neigh_area) {
        return Math.abs(sim_attr - neigh_area.get_internal_attr());
    }

    public void initialize_neighbor() {
        neigh_area_index = new ArrayList<>();
    }



    @Override
    protected Object clone() {
        Area g = new Area(this.get_geo_index() , this.get_internal_attr() , this.get_extensive_attr() , this.get_centroid());
        g.set_region(this.get_associated_region_index());
        g.set_neighbor_once((ArrayList<Integer>)neigh_area_index.clone());
        return g;
    }


    public static ArrayList<Area> area_list_copy(ArrayList<Area> all_areas) throws CloneNotSupportedException {
        ArrayList<Area> returned_areas = new ArrayList<>();
        for(Area g : all_areas)
        {
            returned_areas.add((Area)g.clone());
        }
        return returned_areas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Area)) return false;
        return this.get_geo_index() == ((Area) o).get_geo_index();
    }





}
