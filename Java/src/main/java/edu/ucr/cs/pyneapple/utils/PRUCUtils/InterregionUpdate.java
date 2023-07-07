package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.*;

/**
 * This class corresponds to Section 5.1.4 Inter-region Update
 */
public class InterregionUpdate {

    private Region[] regions;
    private Queue<Region> incomplete_regions;
    private int adjust_max_iter;
    private ArrayList<Area> all_areas;

    /**
     * The Inter-Region Update phase that makes the incomplete regions complete
     * @param regions the grown regions
     * @param all_areas the input areas
     */

    public InterregionUpdate(Region[] regions, ArrayList<Area> all_areas) {
        this.adjust_max_iter = all_areas.size();
        this.regions = regions;
        this.all_areas = all_areas;
    }

    /***
     * This function extract all incomplete regions and move areas from complete region to incomplete region
      */
    public void region_adjustment() {
        extract_incomplete_regions();
        inter_regional_move();
    }

    /**
     * This method identifies all the incomplete regions
     */
    private void extract_incomplete_regions() {
        incomplete_regions = new LinkedList<>();
        for (Region all_region : regions) {
            if (!all_region.is_region_complete()) {
                incomplete_regions.add(all_region);
            }
        }
    }

    /**
     * This method executes the Inter-region Update.
     * In each iteration, this algorithm attemps to move an area from a complete region to an incomplete region
     * When the all the incomplete regions become complete, this phase terminates
     * The maximum number of iterations is set to be the number of areas in the input
     * If the number of iterations exceed the maximum but still exists incomplete regions, this phase terminates with failure
     */
    private void inter_regional_move(){
        int iter = 0;
        Comparator<Region> regionComparator = Comparator.comparingDouble(Region::get_region_extensive_attr);

        Comparator<Area> areaComparator = Comparator.comparingDouble(Area::get_extensive_attr);

        while(incomplete_regions.size() != 0)
        {
            iter ++ ;
            if(iter == adjust_max_iter)
            {
                return;
            }

            Region receiver = incomplete_regions.remove();

            ArrayList<Region> potential_donor_regions = new ArrayList<Region>();
            ArrayList<ArrayList<Area>> potential_donor_margin_areas = new ArrayList<>();

            //detect all the possible donor areas by iterating the neighbor areas of the receiver region
            for(Area receiver_neigh_area : receiver.get_neigh_areas())
            {
                Region donor = regions[receiver_neigh_area.get_associated_region_index()];
                if(donor.is_region_complete() && donor.get_region_size() > 1)
                {
                    if(potential_donor_regions.contains(donor))
                    {
                        int index = potential_donor_regions.indexOf(donor);
                        potential_donor_margin_areas.get(index).add(receiver_neigh_area);
                    }

                    else
                    {
                        potential_donor_regions.add(donor);
                        ArrayList<Area> donor_margin_area = new ArrayList<Area>();
                        donor_margin_area.add(receiver_neigh_area);
                        potential_donor_margin_areas.add(donor_margin_area);
                    }
                }
            }

            boolean move_flag = false;
            while(potential_donor_regions.size() != 0) //iterating the neighboring complete regions, starting from the one with maximum extensive attribute
            {
                Region donor = Collections.max(potential_donor_regions, regionComparator);
                int index = potential_donor_regions.indexOf(donor);
                ArrayList<Area> donor_margin_areas = potential_donor_margin_areas.get(index);
                ArrayList<Area> donor_APs = new Tarjan(donor , all_areas).findAPs_Tarjan(); //filter out all the articulation areas
                donor_margin_areas.removeAll(donor_APs);
                if(donor_margin_areas.size() == 0)
                {
                    potential_donor_margin_areas.remove(index);
                    potential_donor_regions.remove(index);
                }
                else
                {
                    Area area = Collections.max(donor_margin_areas , areaComparator);
                    conduct_move(area , donor , receiver);
                    move_flag = true;
                    break;
                }
            }

            if(move_flag)
            {
                incomplete_regions.add(receiver);
            }
        }

    }


    private void conduct_move(Area area , Region donor , Region receiver)
    {
        donor.remove_area_in_region(area);
        receiver.add_area_to_region(area);
        if(!donor.is_region_complete())
        {
            incomplete_regions.add(donor);
        }

        if(!receiver.is_region_complete())
        {
            incomplete_regions.add(receiver);
        }
    }











}






