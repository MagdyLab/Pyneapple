package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;

/**
 * This class describes the Region that is composed of a set of areas
 */
public class Region {
    private ArrayList<Area> all_areas;
    private ArrayList<Area> areas_in_region;
    private ArrayList<Area> areas_on_margin;
    private int region_id;
    private boolean region_complete;
    private double threshold;
    private double region_extensive_attr;
    private double region_heterogeneity;
    private ArrayList<Area> neigh_areas;


    /**
     * The class for a region
     * @param region_id the index of a region
     * @param g the first area in the region
     * @param threshold the threshold constraint
     * @param all_areas the list of all areas
     */
    public Region(int region_id , Area g , double threshold , ArrayList<Area> all_areas)
    {
        areas_in_region = new ArrayList<>();
        areas_on_margin = new ArrayList<>();
        this.threshold = threshold;
        this.all_areas = all_areas;
        g.set_region(region_id);
        areas_in_region.add(g);
        areas_on_margin.add(g);
        region_complete = false;
        this.region_id = region_id;
        neigh_areas = (ArrayList<Area>)g.get_neigh_area(all_areas).clone();
        region_extensive_attr = g.get_extensive_attr();
        if(region_extensive_attr > threshold)
        {
            region_complete = true;
        }
    }


    /**
     * the constructor of region class
     * @param areas_in_region the list of all areas in the region
     * @param threshold the threshold constraints
     * @param hetero the pre-computed heterogeneity of the region
     * @param total_extensive_attribute the pre-computed total extensive attribute of the region
     */
    public Region(ArrayList<Area> areas_in_region , double threshold , double hetero , double total_extensive_attribute)
    {
        if(hetero > 0 && total_extensive_attribute > 0)
        {
            this.areas_in_region = areas_in_region;
            this.region_extensive_attr = total_extensive_attribute;
            this.region_heterogeneity = hetero;
            region_complete = region_extensive_attr >= threshold;
        }

        else
        {
            this.areas_in_region = areas_in_region;
            this.region_extensive_attr = 0;
            this.region_heterogeneity = 0;


            for(Area area : areas_in_region)
            {
                region_extensive_attr += area.get_extensive_attr();
            }

            for(int i = 0; i < areas_in_region.size() ; i++)
            {
                for(int j = i + 1; j < areas_in_region.size() ; j++)
                {
                    region_heterogeneity += Math.abs(areas_in_region.get(i).get_internal_attr() - areas_in_region.get(j).get_internal_attr());
                }
            }

            region_complete = region_extensive_attr >= threshold;
        }
    }


    /**
     * Add an area to the region, update the neighboring relationship, heterogeneity, and regional extensive attribute
     * @param area the area to be added to the region
     */
    public void add_area_to_region(Area area) {

        area.set_region(region_id);
        areas_in_region.add(area);
        neigh_areas.remove(area);

        ArrayList<Area> area_to_add = new ArrayList<Area>();
        for(Area neigh_area : area.get_neigh_area(all_areas))
        {
            if(neigh_area.get_associated_region_index() != this.region_id && !neigh_areas.contains(neigh_area))
            {
                if(!area_to_add.contains(neigh_area))
                {
                    area_to_add.add(neigh_area);
                }
            }
        }
        neigh_areas.addAll(area_to_add);


        /*
        ArrayList<GeoArea> areas_to_add = (ArrayList<GeoArea>)area.get_neigh_area(all_areas).clone();
        areas_to_add.removeAll(neigh_areas);
        areas_to_add.removeAll(areas_in_region);
        neigh_areas.addAll(areas_to_add);


         */



        region_extensive_attr += area.get_extensive_attr();
        if(region_extensive_attr > threshold)
        {
            this.region_complete = true;
        }
        double incre = compute_hetero_incre(area);
        region_heterogeneity += incre;



        boolean add_flag = false;
        for(Area area_neigh : area.get_neigh_area(all_areas))
        {
            if(area_neigh.get_associated_region_index() != this.get_region_index())
            {
                add_flag = true;
                break;
            }
        }
        if(add_flag)
        {
            areas_on_margin.add(area);
        }

        for(Area neigh : area.get_neigh_area(all_areas))
        {
            if(neigh.get_associated_region_index() == this.region_id)
            {
                if(areas_on_margin.contains(neigh))
                {
                    boolean on_margin_flag = false;
                    for(Area neigh_neigh : neigh.get_neigh_area(all_areas))
                    {
                        if(neigh_neigh.get_associated_region_index() != this.region_id)
                        {
                            on_margin_flag = true;
                        }
                    }

                    if(!on_margin_flag)
                    {
                        areas_on_margin.remove(neigh);
                    }
                }

                if(!areas_on_margin.contains(neigh))
                {
                    boolean on_margin_flag = false;
                    for(Area neigh_neigh : neigh.get_neigh_area(all_areas))
                    {
                        if (neigh_neigh.get_associated_region_index() != this.region_id) {
                            on_margin_flag = true;
                            break;
                        }
                    }
                    if(on_margin_flag)
                    {
                        areas_on_margin.add(neigh);
                    }
                }
            }
        }


    }


    /**
     * remove an area from the region, fixing the neighborhood relationship, the heterogeneity, and the extensive attribute
     * @param area the area to be removed
     */
    public void remove_area_in_region(Area area)
    {
        area.set_region(-1);
        areas_in_region.remove(area);
        neigh_areas.add(area);


        for(Area g1 : area.get_neigh_area(all_areas))
        {

            if(neigh_areas.contains(g1))
            {
                boolean flag = false;
                for(Area g2 : g1.get_neigh_area(all_areas))
                {
                    if (areas_in_region.contains(g2)) {
                        flag = true;
                        break;
                    }
                }

                if(!flag)
                {
                    neigh_areas.remove(g1);
                }
            }
        }





        region_extensive_attr -= area.get_extensive_attr();
        if(region_extensive_attr < threshold)
        {
            this.region_complete = false;
        }
        double decre = compute_hetero_decre(area);
        region_heterogeneity -= decre;



        areas_on_margin.remove(area);
        for(Area neigh : area.get_neigh_area(all_areas))
        {
            if(neigh.get_associated_region_index() == this.region_id)
            {
                if(areas_on_margin.contains(neigh))
                {
                    boolean on_margin_flag = false;
                    for(Area neigh_neigh : neigh.get_neigh_area(all_areas))
                    {
                        if (neigh_neigh.get_associated_region_index() != this.region_id) {
                            on_margin_flag = true;
                            break;
                        }
                    }

                    if(!on_margin_flag)
                    {
                        areas_on_margin.remove(neigh);
                    }
                }

                if(!areas_on_margin.contains(neigh))
                {
                    boolean on_margin_flag = false;
                    for(Area neigh_neigh : neigh.get_neigh_area(all_areas))
                    {
                        if (neigh_neigh.get_associated_region_index() != this.region_id) {
                            on_margin_flag = true;
                            break;
                        }
                    }
                    if(on_margin_flag)
                    {
                        areas_on_margin.add(neigh);
                    }
                }
            }
        }


    }

    /**
     * compute the heterogeneity increase when adding an area to this region
     * @param area the area to be added to the region
     * @return the heterogeneity increase
     */
    public double compute_hetero_incre(Area area)
    {
        double hetero_incre = 0;
        for (Area current_area : areas_in_region) {
            hetero_incre += Math.abs(area.get_internal_attr() - current_area.get_internal_attr());
        }
        return hetero_incre;
    }

    /**
     * compute the number of connections between a new area g to the areas in the region
     * @param g the area to be evaluated
     * @return the number of connections between g and the areas in the region
     */
    public int compute_connection_num(Area g)
    {
        int connection = 0;
        for(Area area : g.get_neigh_area(all_areas))
        {
            if(areas_in_region.contains(area))
            {
                connection++;
            }
        }
        return connection;
    }


    /**
     * Compute the decrease of the heterogeneity when removing an area from the region
     * @param area the area to be removed from the region
     * @return the heterogeneity decrease when removing this area from the region
     */
    public double compute_hetero_decre(Area area)
    {
        double hetero_decre = 0;
        for(Area current_area : areas_in_region){
            if(current_area == area)
            {
                continue;
            }
            hetero_decre += Math.abs(area.get_internal_attr() - current_area.get_internal_attr());
        }

        return hetero_decre;
    }


    /**
     * determine whether the region is connected using graph traversal
     * @return whether the region is connected
     */
    public boolean is_connected() {
        boolean[] visited = new boolean[areas_in_region.size()];
        Area first_area_move = areas_in_region.get(0);
        DFS(first_area_move , visited , areas_in_region);
        for(boolean b : visited)
        {
            if(!b)
            {
                return false;
            }
        }
        return true;
    }



    private void DFS(Area visiting_area , boolean[] visited , ArrayList<Area> areas)
    {
        visited[areas.indexOf(visiting_area)] = true;
        for(Area neigh_area : visiting_area.get_neigh_area(all_areas))
        {
            if(areas.contains(neigh_area))
            {
                if(!visited[areas.indexOf(neigh_area)])
                {
                    DFS(neigh_area , visited , areas);
                }
            }
        }
    }


    /**
     * get the marginal areas from the region
     * @return the list that the areas on the margin of the region, i.e., area having at least one neighbor not in the region
     */
    public ArrayList<Area> getAreas_on_margin()
    {
        return areas_on_margin;
    }


    /**
     * get the number of areas in the region
     * @return the number of areas in the region
     */
    public int get_region_size() {return areas_in_region.size();}


    /**
     * get the index of the region
     * @return the index of the region
     */
    public int get_region_index() { return region_id; }


    /**
     * get whether the region is complete
     * @return whether the region satisfies the user-defined constraint
     */
    public boolean is_region_complete()
    {
        return region_complete;
    }


    /**
     * get the neigboring areas of a region
     * @return the neighboring areas of this region
     */
    public ArrayList<Area> get_neigh_areas()
    {
        return neigh_areas;
    }


    /**
     * get the neighboring areas of a region
     * @return the list of areas in the region
     */
    public ArrayList<Area> get_areas_in_region() {return areas_in_region; }


    /**
     * get the total extensive attribute of a region
     * @return the total extensive attribute of the region
     */
    public double get_region_extensive_attr()
    {
        return region_extensive_attr;
    }

    /**
     * get the heterogeneity of the region
     * @return the heterogeneity of the region
     */
    public double get_region_hetero()
    {
        return region_heterogeneity;
    }


    /**
     * get the heterogeneity of all regions
     * @param regions all regions from the partition
     * @return the heterogeneity of the partition, i.e., the sum of heterogeneity of all regions
     */
    public static double get_all_region_hetero(Region[] regions)
    {
        double total_hetero = 0;
        for(Region r : regions)
        {
            total_hetero += r.get_region_hetero();
        }
        return total_hetero;
    }


    /**
     * test if there exists incomplete region
     * @param regions the regions from all partitions
     * @return whether there exists region that fails to satisfy the user-defined constraint
     */
    public static boolean exist_incomplete_region(Region[] regions)
    {
        for(Region r : regions)
        {
            if(!r.is_region_complete())
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Test the correctness of the partition on heterogeneity, satisfaction of constraints, and connectivity
     * @param regions the regions from the partition
     * @param all_areas the list of all areas
     * @param threshold the threshold from the constraint
     * @param PRUC test whether connected
     */
    public static void test_result_correctness(Region[] regions , ArrayList<Area> all_areas , double threshold , boolean PRUC)
    {
        if(regions == null)
        {
            return;
        }
        double total_ex_accurate = 0;
        for(Area area : all_areas)
        {
            total_ex_accurate += area.get_extensive_attr();
        }

        double test_ex = 0;
        int total_size = 0;
        double total_hetero = 0;

        for(Region r : regions)
        {
            double r_ex = 0;
            ArrayList<Area> areas_in_r = r.get_areas_in_region();
            for(Area area : areas_in_r)
            {
                r_ex += area.get_extensive_attr();
            }

            if(r_ex != r.get_region_extensive_attr())
            {
                System.out.println("the accumalted extensive attribute does not equal to the region extensive attribute");
                System.out.println("verified value is " + r_ex);
                System.out.println("the stored value is " + r.get_region_extensive_attr());
            }


            if(r.get_region_size() != r.get_areas_in_region().size())
            {
                System.out.println("the two size does not match");
            }

            if(r.get_region_extensive_attr() < threshold)
            {
                System.out.println("smaller than threshold");
            }

            double r_hetero = 0;
            for(int i = 0 ; i < r.get_areas_in_region().size() ; i++)
            {
                for(int j = i + 1 ; j < r.get_areas_in_region().size() ; j++)
                {
                    Area a1 = r.get_areas_in_region().get(i);
                    Area a2 = r.get_areas_in_region().get(j);
                    r_hetero += Math.abs(a1.get_internal_attr() - a2.get_internal_attr());
                }
            }

            if(r_hetero != r.get_region_hetero())
            {
                System.out.println("hetero does not match");
            }

            total_hetero += r.get_region_hetero();


            if(PRUC)
            {
                if(!r.is_connected())
                {
                    System.out.println("the region is not connected");
                }
            }



            test_ex += r_ex;
            total_size += r.get_region_size();

        }

        if(test_ex != total_ex_accurate)
        {
            System.out.println("total ex not match");
            System.out.println("total ex = " + test_ex);
            System.out.println("total ex accurate is " + total_ex_accurate);
        }

        if(total_size != all_areas.size())
        {
            System.out.println("total size not match");
        }

        if(total_hetero != Region.get_all_region_hetero(regions))
        {
            System.out.println("total hetero not match");
        }





    }






}
