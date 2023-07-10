package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Graph class describes the Graph object which is used store areas as graph
 */

public class Graph {

    private HashMap<Integer,Area> graph_areas;
    private int ID;
    private ArrayList<Integer> parent_areas;
    private int starting_index;
    private ArrayList<Graph> coarsest_graphs;

    /**
     * Graph constructor
     * @param id graph ID
     */
    public Graph (int id) {

        this.ID = id;
        this.graph_areas = new HashMap<>();
        this.parent_areas = new ArrayList<>();
        this.starting_index = 0;
        this.coarsest_graphs = new ArrayList<>();
    }

    /**
     * set index
     * @param index index
     */
    public void set_starting_index(int index) {

        this.starting_index = index;
    }

    /**
     * add an area
     * @param ID area ID
     * @param area area
     */
    public void add_area(int ID, Area area) {

        this.graph_areas.put(ID, area);
    }


    /**
     * set graph areas
     * @param graph_areas graph areas hashmap
     */
    public void set_graph_areas(HashMap<Integer,Area> graph_areas) {

        this.graph_areas = graph_areas;
    }

    /**
     * get graph areas
     * @return graph areas
     */
    public HashMap<Integer,Area> get_graph_areas() {

        return this.graph_areas;
    }


    /**
     * set coarsest graphs
     * @param coarsest_graphs coarsest graphs
     */
    public void set_coarsest_graphs(ArrayList<Graph> coarsest_graphs) {

        this.coarsest_graphs = coarsest_graphs;
    }

    /**
     * get coarsest graphs
     * @return coarsest graphs
     */
    public ArrayList<Graph> get_coarsest_graphs() {

        return this.coarsest_graphs;
    }

    /**
     * copy areas
     * @return areas hashmap
     */
    public HashMap<Integer,Area> copy_graph_areas() {

        HashMap<Integer,Area> graph_areas_copy = new HashMap<>();

        for (int ID : this.graph_areas.keySet())
        {
            Area area = this.graph_areas.get(ID);
            Area area_copy = new Area(area.get_ID());

            ArrayList<Integer> neighbors = new ArrayList<>(area.get_neighbors());
            area_copy.set_neighbors(neighbors);

            HashMap<Integer, Integer> neighbors2 = new HashMap<>(area.get_neighbors2());
            area_copy.set_neighbors2(neighbors2);

            ArrayList<Integer> subareas_IDs_copy = new ArrayList<>(area.get_subareas_IDs());
            area_copy.set_subareas_IDs(subareas_IDs_copy);

            ArrayList<Integer> original_subareas_IDs_copy = new ArrayList<>(area.get_original_subareas_IDs());
            area_copy.set_original_subareas_IDs(original_subareas_IDs_copy);

            area_copy.set_degree(area.get_degree());

            if (area.is_nested())
                area_copy.set_nested(area.get_parent_ID());

            if (area.is_parent())
            {
                area_copy.set_parent();
                area_copy.set_nested_areas(area.get_nested_areas());
            }

            graph_areas_copy.put(area.get_ID(), area_copy);

        }

        return  graph_areas_copy;
    }

} // end class Graph


