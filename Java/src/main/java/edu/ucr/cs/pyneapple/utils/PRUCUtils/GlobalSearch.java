package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;

/**
 * This class corresponds to the Section 5.1 Global Search
 */
public class GlobalSearch {
    private Region[] regions;
    private ArrayList<Area> all_areas;
    private Seed seed;
    private long total_running_time;
    private long seed_time;
    private long region_growth_time;
    private long enclaves_assign_time;
    private long interregion_update_time;
    private long indirect_flow_time;
    private boolean interregion_flag;
    private boolean flow_flag;

    /**
     * the global search stage of GSLO
     * @param all_areas the input areas
     * @param p the predefined number of regions
     * @param selection_max_iter the maximum number of iterations in Seed Identification
     * @param threshold the value on the user-defined constraint
     */
    public GlobalSearch(ArrayList<Area> all_areas, int p, int selection_max_iter, double threshold) {
        long start = System.currentTimeMillis();

        this.all_areas = all_areas;


        long seeding_start = System.nanoTime();

        seed = new SeedIdentification(all_areas , p , selection_max_iter).getBest_seed();


        long seeding_end = System.nanoTime();
        this.seed_time = seeding_end - seeding_start;

        long region_growth_start = System.nanoTime();
        regions = new RegionGrowth(seed , threshold , all_areas).grow_region_robust();
        long region_growth_end = System.nanoTime();
        this.region_growth_time = region_growth_end - region_growth_start;


        long assign_enclaves_start = System.nanoTime();
        new EnclavesAssignment(all_areas , regions);
        long assign_enclaves_ends = System.nanoTime();
        this.enclaves_assign_time = assign_enclaves_ends - assign_enclaves_start;


        if(!solved())
        {
            long interregion_start = System.currentTimeMillis();
            new InterregionUpdate(regions, all_areas).region_adjustment();
            long interregion_end = System.currentTimeMillis();
            this.interregion_update_time = interregion_end - interregion_start;
            this.interregion_flag = true;
        }

        else
        {
            this.interregion_flag = false;
        }

        if(!solved())
        {
            long flow_start = System.currentTimeMillis();
            new IndirectFlowPush(regions , threshold , all_areas).flow_pushing();
            long flow_end = System.currentTimeMillis();
            this.indirect_flow_time = flow_end - flow_start;
            this.flow_flag = true;

        }

        else
        {
            this.flow_flag = false;
        }



        long end = System.currentTimeMillis();

        total_running_time = end - start;
    }


    /**
     * get the list of all areas from the input set
     * @return the ArrayList of all areas involved in regionalization
     */
    public ArrayList<Area> get_all_areas()
    {
        return all_areas;
    }


    /**
     * get the array of generated regions from the partition
     * @return the regions produced in the regionalization process
     */
    public Region[] get_regions()
    {
        return regions;
    }


    /**
     * test if the partition is feasible
     * @return whether a feasible partition is identified
     */
    public boolean solved()
    {
        for(Region r : regions)
        {
            if(!r.is_region_complete())
            {
                return false;
            }
        }
        return true;
    }


}
