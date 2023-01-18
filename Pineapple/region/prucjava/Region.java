

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
    public double region_extensive_attr;
    private double region_heterogeneity;
    private ArrayList<Area> neigh_areas;


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

    public double compute_hetero_incre(Area area)
    {
        double hetero_incre = 0;
        for (Area current_area : areas_in_region) {
            hetero_incre += Math.abs(area.get_internal_attr() - current_area.get_internal_attr());
        }
        return hetero_incre;
    }

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


    public boolean area_disconect_region(Area area)
    {
        ArrayList<Area> areas_in_region_copy = (ArrayList<Area>) areas_in_region.clone();
        areas_in_region_copy.remove(area);
        boolean[] visited = new boolean[areas_in_region_copy.size()];
        Area first_area_to_move = areas_in_region_copy.get(0);
        DFS(first_area_to_move , visited , areas_in_region_copy);
        for(boolean b : visited)
        {
            if(!b)
            {
                return true;
            }
        }
        return false;
    }



    public void DFS(Area visiting_area , boolean[] visited , ArrayList<Area> areas)
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





    public ArrayList<Area> getAreas_on_margin()
    {
        return areas_on_margin;
    }

    public int get_region_size() {return areas_in_region.size();}

    public double getThreshold() {return threshold;}


    public int get_region_index() { return region_id; }

    public boolean is_region_complete()
    {
        return region_complete;
    }

    public ArrayList<Area> get_neigh_areas()
    {
        return neigh_areas;
    }

    public ArrayList<Area> get_areas_in_region() {return areas_in_region; }

    public double get_region_extensive_attr()
    {
        return region_extensive_attr;
    }

    public double get_region_hetero()
    {
        return region_heterogeneity;
    }


    public static double get_all_region_hetero(Region[] regions)
    {
        double total_hetero = 0;
        for(Region r : regions)
        {
            total_hetero += r.get_region_hetero();
        }
        return total_hetero;
    }

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

    public static Region[] construct_region_from_areas(ArrayList<Area> all_areas , Seed seed , double threshold)
    {
        Region[] regions = new Region[seed.get_seeds().size()];
        for(int i = 0 ; i < regions.length ; i++)
        {
            int g_index = seed.get_seeds().get(i).get_geo_index();
            Area g = all_areas.get(g_index);
            regions[i] = new Region(i , g , threshold , all_areas);
        }


        for(Area g : all_areas)
        {
            int r_id = g.get_associated_region_index();
            Region r = regions[r_id];
            if(r.get_areas_in_region().contains(g))
            {
                continue;
            }
            r.add_area_to_region(g);
        }

        return regions;
    }



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
