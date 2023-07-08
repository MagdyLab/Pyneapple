package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;


/**
 * This class corresponds to Section 5.1.1 Seed Identification
 */

public class SeedIdentification {
    private ArrayList<Area> all_geoareas;
    private Seed best_seed;


    /**
     * identify p areas as the seed
     * @param all_areas the input areas
     * @param seed_num the number of seeded area, which equals to the number of predefined regions, p
     * @param max_iter the maximum number of iteration in Seed Identification, when set to 0 means random seeding
     */
    public SeedIdentification(ArrayList<Area> all_areas , int seed_num , int max_iter) {
        this.all_geoareas = all_areas;

        boolean detect_island = !(find_connected_component(all_areas.get(0)).size() == all_areas.size());
        if(!detect_island)
        {
            //System.out.println("no island seeding");
            this.best_seed = naive_seed_selection(all_areas , seed_num , max_iter);
        }

        else
        {
            //System.out.println("island seeding");
            this.best_seed = island_seeding(all_areas , seed_num , max_iter);
        }
    }


    /**
     *
     * @param all_areas the input areas
     * @param s_num the number of areas in the seed
     * @param maxiter the maximum number of iterations
     * @return The selected seed
     */
    private Seed island_seeding(ArrayList<Area> all_areas , int s_num , int maxiter)
    {
        ArrayList<ConnectedComponent> ccs = new ArrayList<>();
        Comparator<ConnectedComponent> cc_comparator = Comparator.comparingDouble(ConnectedComponent::getTotal_ext);

        ArrayList<Area> unvisited = (ArrayList<Area>) (all_areas.clone());
        while(unvisited.size() > 0)
        {
            Area a = unvisited.get(new Random().nextInt(unvisited.size()));
            ArrayList<Area> visited = find_connected_component(a);
            double cc_ext = 0;
            for(Area cc_a : visited)
            {
                cc_ext += cc_a.get_extensive_attr();
            }
            ccs.add(new ConnectedComponent(visited, cc_ext));
            unvisited.removeAll(visited);
        }

        ccs.sort(cc_comparator);

        double total_ext = 0;
        for(ConnectedComponent cc : ccs)
        {
            total_ext += cc.getTotal_ext();
        }


        int remaining_seed = s_num;
        ArrayList<Seed> seeds_in_cc = new ArrayList<>();

        for(int i = 0 ; i < ccs.size() - 1 ; i++)
        {
            ConnectedComponent c = ccs.get(i);
            int seed_num = (int) ((c.getTotal_ext() * 1.0 / total_ext) * s_num);
            if(seed_num < 1)
            {
                seed_num = 1;
            }
            int max_iter = (int) ((c.getTotal_ext() * 1.0 / total_ext) * maxiter);
            Seed s = naive_seed_selection(c.getAreas_in_cc() , seed_num , max_iter);
            seeds_in_cc.add(s);
            remaining_seed -= seed_num;
        }

        ConnectedComponent c = ccs.get(ccs.size() - 1);
        int max_iter = (int) ((c.getTotal_ext() * 1.0 / total_ext) * maxiter);
        Seed s = naive_seed_selection(c.getAreas_in_cc() , remaining_seed ,max_iter);
        seeds_in_cc.add(s);

        ArrayList<Area> all_seed_areas = new ArrayList<>();
        for(Seed s_in_cc : seeds_in_cc)
        {
            all_seed_areas.addAll(s_in_cc.get_seeds());
        }
        return new Seed(all_seed_areas);
    }

    private ArrayList<Area> find_connected_component(Area area)
    {
        HashSet<Area> visited = new HashSet<>();
        DFS(area , visited);
        return new ArrayList<>(visited);
    }

    private void DFS(Area area , HashSet<Area> visited)
    {
        visited.add(area);
        for(Area neigh_area : area.get_neigh_area(all_geoareas))
        {
            if(!visited.contains(neigh_area))
            {
                DFS(neigh_area , visited);
            }
        }
    }


    private Seed naive_seed_selection(ArrayList<Area> all_areas , int s_num , int maxiter)
    {
        Seed seed = new Seed(all_areas , s_num);
        int iter_time = 0;
        while(iter_time < maxiter)
        {
            seed.random_replacement();
            iter_time ++;
        }
        return seed;
    }


    /**
     * get the seed with the best objective function value
     * @return the seed with the best quality
     */
    public Seed getBest_seed() {
        return best_seed;
    }


    static class ConnectedComponent{
        ArrayList<Area> areas_in_cc;
        double total_ext;
        public ConnectedComponent(ArrayList<Area> areas , double total_e)
        {
            areas_in_cc = areas;
            total_ext = total_e;
        }

        public ArrayList<Area> getAreas_in_cc()
        {
            return areas_in_cc;
        }

        public double getTotal_ext()
        {
            return total_ext;
        }

    }
}

