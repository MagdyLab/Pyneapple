package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class corresponds to Section 5.1.2 Region Growth
 */
public class RegionGrowth {
    private Seed seed;
    private Region[] regions;
    private double threshold;
    private ArrayList<Area> all_areas;
    private int p;

    /**
     * grow a region by adding areas to it
     * @param seed The seed obtained from the Seed Identification phase
     * @param threshold The value on the user-defined constraint
     * @param all_areas The input areas
     */
    public RegionGrowth(Seed seed , double threshold , ArrayList<Area> all_areas)
    {
        this.seed = seed;
        this.threshold = threshold;
        this.regions = new Region[seed.get_seed_size()];
        this.all_areas = all_areas;
        this.p = seed.get_seed_size();
    }

    /**
     * This method grows the regions sequentially. In each iteration, the region with the min extensive attribute is selected to grow
     * @return the grown regions
     */
    public Region[] grow_region_robust()
    {
        Comparator<Region> r_comparator = Comparator.comparingDouble(Region::get_region_extensive_attr);

        for(int i = 0 ; i < regions.length ; i++)
        {
            Region r = new Region(i , seed.get_seeds().get(i), threshold ,all_areas);
            regions[i] = r;
        }

        ArrayList<Region> growing_region = new ArrayList<>();
        Collections.addAll(growing_region, regions);

        growing_region.sort(r_comparator);

        while(growing_region.size() > 0)
        {
            Region region_to_grow = Collections.min(growing_region , r_comparator);
            grow(region_to_grow , growing_region);
        }
        return regions;
    }

    /**
     *
     * @param r the region currently selected to grow
     * @param all_growing_regions all the currently growing regions
     */
    private void grow(Region r, ArrayList<Region> all_growing_regions)
    {
        if(r.get_region_extensive_attr() > threshold)
        {
            all_growing_regions.remove(r);
            return;
        }

        Area area_to_add = greedy_grow(r);

        if(area_to_add == null)
        {
            all_growing_regions.remove(r);
            return;
        }
        r.add_area_to_region(area_to_add);
    }

    /**
     * The method locates the neighboring area of r that has the greatest conn() value
     * @param r the currently growing region
     * @return the unassigned area among r's neighbor that has the greatest conn() value
     */
    private Area greedy_grow(Region r)
    {
        ArrayList<Area> neighs = r.get_neigh_areas();
        int optimal_connection = 0;
        Area best_area = null;

        for (Area current_area : neighs) {
            if (current_area.get_associated_region_index() != -1) {
                continue;
            }
            int connection_num = r.compute_connection_num(current_area);
            if (connection_num > optimal_connection) {
                optimal_connection = connection_num;
                best_area = current_area;
            }
        }
        return best_area;
    }



}
