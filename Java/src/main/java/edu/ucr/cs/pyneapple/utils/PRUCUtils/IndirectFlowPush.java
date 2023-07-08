package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**
 * This class corresponds to the Indirect Flow Push in Section 5.1.5
 */
public class IndirectFlowPush {
    private Region[] regions;
    private double threshold;
    private ArrayList<Area> all_areas;
    ArrayList<Region>[] neighbor_regions;
    ArrayList<ArrayList<Area>>[] neighbor_region_areas;
    int[] regions_status;
    ArrayList<Region> unprocessed_regions;
    ArrayList<Region> exhausting_regions;
    ArrayList<Region> processed_regions;

    ArrayList<Region> incomplete_regions;
    private final int PROCESSED = 0;   //correspond to the status P in the paper
    private final int UNPROCESSED = 1; //correspond to the status UP in the paper
    private final int EXHAUSTING = 2; //include both EI and EC in the paper


    /**
     * the Indirect Flow Push phase that can make the incomplete regions complete
     * @param regions is the array of regions
     * @param threshold is the value of the user-defined constraint
     * @param all_areas is the input areas
     */

    public IndirectFlowPush(Region[] regions , double threshold , ArrayList<Area> all_areas)
    {
        this.regions = regions;
        this.threshold = threshold;
        this.all_areas = all_areas;
        regions_status = new int[regions.length];
        neighbor_regions = new ArrayList[regions.length];
        neighbor_region_areas = new ArrayList[regions.length];
        unprocessed_regions = new ArrayList<Region>();
        exhausting_regions = new ArrayList<Region>();
        processed_regions = new ArrayList<Region>();
        incomplete_regions = new ArrayList<Region>();

    }

    /**
     * This function executes the Indirect Flow Push procedure
     */
    public void flow_pushing()
    {
        initialize_neighbor(); //build the neighboring relations on the region level
        Comparator<Region> regionComparator = Comparator.comparingDouble(o -> o.get_region_extensive_attr());
        for(Region r : regions)
        {
            if(!r.is_region_complete())
            {
                incomplete_regions.add(r);
            }
        }

        while(unprocessed_regions.size() > 0 || exhausting_regions.size() > 0)
        {
            if(incomplete_regions.size() == 0)
            {
                return; //whenever there exists no more incomplete region, this phase terminates
            }

            Region processing_r; //the current selected region
            if(unprocessed_regions.size() > 0)
            {
                processing_r = Collections.max(unprocessed_regions , regionComparator);
            }

            else
            {
                processing_r = Collections.max(exhausting_regions , regionComparator);
                if(!processing_r.is_region_complete())
                {
                    return; //is the Exhausting region with the greatest extensive attribute is incomplete, then this phase terminates with failure
                }
            }


            while(regions_status[processing_r.get_region_index()] != PROCESSED) //the selected region will be processed until its status changes to P
            {
                ArrayList<Region> r_exhausting_complete_neigh = new ArrayList<>();
                ArrayList<Region> r_exhausting_incomplete_neigh = new ArrayList<>();
                ArrayList<Region> r_unprocessed_neigh = new ArrayList<>();
                //find the neighboring region of r_select and divide them into different categories
                for(Region r_neigh : neighbor_regions[processing_r.get_region_index()])
                {
                    if(regions_status[r_neigh.get_region_index()] == UNPROCESSED)
                    {
                        r_unprocessed_neigh.add(r_neigh);
                    }

                    else if(regions_status[r_neigh.get_region_index()] == EXHAUSTING && r_neigh.is_region_complete())
                    {
                        r_exhausting_complete_neigh.add(r_neigh);
                    }

                    else if(regions_status[r_neigh.get_region_index()] == EXHAUSTING && !r_neigh.is_region_complete())
                    {
                        r_exhausting_incomplete_neigh.add(r_neigh);
                    }

                }

                //process the neighboring EC region
                if(r_exhausting_complete_neigh.size() > 0)
                {
                    Region r_ec = Collections.max(r_exhausting_complete_neigh , regionComparator);
                    process_r_ec(processing_r , r_ec);
                }

                //process the neighboring EI region
                else if(r_exhausting_incomplete_neigh.size() > 0)
                {
                    Region r_ei = Collections.min(r_exhausting_incomplete_neigh , regionComparator);
                    int flag = process_r_ei(processing_r , r_ei);
                    if(flag == -1)
                    {
                        return; //the neighboring EI region does not have a chance to become complete, the phase terminates with failure
                    }
                }

                //process the neighboring UP region
                else if(r_unprocessed_neigh.size() > 0)
                {
                    process_r_u(processing_r , r_unprocessed_neigh , regionComparator);
                }


            }

        }

    }

    /**
     * Initialize the neighbors on the region level
     */
    private void initialize_neighbor()
    {
        for(int i = 0 ; i < regions.length ; i++)
        {
            Region r = regions[i];
            ArrayList<Area> r_neigh_areas = r.get_neigh_areas();
            ArrayList<Region> neigh_regions = new ArrayList<>();
            ArrayList<ArrayList<Area>> neigh_areas_each_neigh_regions = new ArrayList<>();
            for(Area r_neigh_area : r_neigh_areas)
            {
                Region associated_r = regions[r_neigh_area.get_associated_region_index()];
                if(!neigh_regions.contains(associated_r))
                {
                    neigh_regions.add(associated_r);
                    ArrayList<Area> corresponding_areas = new ArrayList<Area>();
                    corresponding_areas.add(r_neigh_area);
                    neigh_areas_each_neigh_regions.add(corresponding_areas);
                }

                else
                {
                    int index = neigh_regions.indexOf(associated_r);
                    ArrayList<Area> areas_corresponded_r = neigh_areas_each_neigh_regions.get(index);
                    areas_corresponded_r.add(r_neigh_area);
                }
            }
            neighbor_regions[i] = neigh_regions;
            neighbor_region_areas[i] = neigh_areas_each_neigh_regions;



            if(neighbor_regions[i].size() == 1)
            {
                regions_status[i] = EXHAUSTING;
                exhausting_regions.add(r);
            }

            else
            {
                regions_status[i] = UNPROCESSED;
                unprocessed_regions.add(r);
            }
        }

    }

    /**
     * This method moves areas from neighboring EC region to the current processing region
     * @param processing_r is the region being selected to process
     * @param r_ec is the neighboring EC region of processing_r
     */
    private void process_r_ec(Region processing_r , Region r_ec)
    {
        Area best_area = find_best_movable_area(r_ec , processing_r);

        if(best_area == null)
        {
            mark_label(r_ec , PROCESSED); //if there is no more areas to move, the EI region coverts to P
        }
        else
        {
            move(best_area , r_ec , processing_r);
        }
    }

    /**
     * This method moves ares from the current processing region to neighboring EI region
     * @param processing_r the currently processing region
     * @param r_ei the neighboring EI region
     * @return indicator which shows whether the current EI region has a chance to become complete
     * if returns -1, then the current EI region does not have a chance to become complete and the phase terminates with failure
     */
    private int process_r_ei(Region processing_r , Region r_ei)
    {
        Area best_area = find_best_movable_area(processing_r , r_ei);
        if(best_area == null)
        {
            return -1;
        }
        else
        {
            move(best_area , processing_r , r_ei);
            if(r_ei.is_region_complete())
            {
                mark_label(r_ei , PROCESSED);
            }
        }
        return 0;
    }

    /**
     * This method moves are from the currently processing region with its neighboring UP regions
     * @param processing_r the currently processing region
     * @param r_unprocessed_neigh all the neighboring UP region of the current processing region
     * @param regionComparator the comparing rules of different region
     */
    private void process_r_u(Region processing_r , ArrayList<Region> r_unprocessed_neigh , Comparator<Region> regionComparator)
    {
        while (r_unprocessed_neigh.size() > 0)
        {
            Region receiver = Collections.min(r_unprocessed_neigh , regionComparator);
            Area best_area = find_best_movable_area(processing_r , receiver);
            if(best_area == null)
            {
                r_unprocessed_neigh.remove(receiver);
            }

            else
            {
                move(best_area , processing_r , receiver);
                return;
            }
        }

        mark_label(processing_r , PROCESSED);
    }


    /**
     * This method locates the best area to move from the donor region to the receiver region.
     * The best area is defined to be the area that has the max (conn(a, receiver) - conn(a, donor))
     * @param donor the region that donates an area
     * @param receiver the region that receives an area
     * @return the best area to move from the donor to receiver
     */
    private Area find_best_movable_area(Region donor, Region receiver)
    {
        if(donor.get_region_size() == 1)
        {
            return null;
        }
        ArrayList<Area> donor_aps = new Tarjan(donor , all_areas).findAPs_Tarjan();
        int index = neighbor_regions[receiver.get_region_index()].indexOf(donor);
        ArrayList<Area> dr_margin = (ArrayList<Area>) (neighbor_region_areas[receiver.get_region_index()].get(index).clone());
        dr_margin.removeAll(donor_aps);

        int max_conn = Integer.MIN_VALUE;
        Area best_area = null;
        for(Area area : dr_margin)
        {
            if(donor.get_region_extensive_attr() - area.get_extensive_attr() > threshold)
            {
                int conn_diff = receiver.compute_connection_num(area) - donor.compute_connection_num(area);
                if(conn_diff > max_conn)
                {
                    best_area = area;
                    max_conn = conn_diff;
                }
            }
        }

        return best_area;
    }

    /**
     * This method moves an area from one region to another region and update the neighboring relations and the status of the regions
     * @param area the area to move
     * @param donor the region that donates this area
     * @param receiver the region that receives this area
     */
    private void move(Area area , Region donor, Region receiver)
    {

        boolean previous_flag = receiver.is_region_complete();
        donor.remove_area_in_region(area);
        receiver.add_area_to_region(area);
        boolean after_flag = receiver.is_region_complete();
        if(after_flag && !previous_flag)
        {
            incomplete_regions.remove(receiver);
        }

        ArrayList<Region> region_involved = new ArrayList<Region>();

        for(Area neigh_area : area.get_neigh_area(all_areas))
        {
            if(!region_involved.contains(regions[neigh_area.get_associated_region_index()]))
            {
                region_involved.add(regions[neigh_area.get_associated_region_index()]);
            }
        }

        fix_region_around_area(region_involved); //reconstruct the neighbor list of the neighboring regions

        for(Region r : region_involved)
        {
            if(regions_status[r.get_region_index()] != PROCESSED)
            {
                mark_label(r , return_label(r)); //change the status of the regions if needed
            }
        }


    }

    /**
     * This method updates the neighbor relations of the regions whose one neighboring area has been moved
     * @param region_involved the list of neighboring region of the area that has been moved
     */
    private void fix_region_around_area(ArrayList<Region> region_involved)
    {
        for(Region r : region_involved)
        {
            ArrayList<Area> r_neigh_areas = r.get_neigh_areas();

            ArrayList<Region> neigh_regions = new ArrayList<Region>();
            ArrayList<ArrayList<Area>> neigh_areas_each_neigh_regions = new ArrayList<>();

            for(Area r_neigh_area : r_neigh_areas)
            {
                Region associated_r = regions[r_neigh_area.get_associated_region_index()];

                if(!neigh_regions.contains(associated_r))
                {
                    neigh_regions.add(associated_r);
                    ArrayList<Area> corresponding_areas = new ArrayList<Area>();
                    corresponding_areas.add(r_neigh_area);
                    neigh_areas_each_neigh_regions.add(corresponding_areas);
                }

                else
                {
                    int index = neigh_regions.indexOf(associated_r);
                    ArrayList<Area> areas_corresponded_r = neigh_areas_each_neigh_regions.get(index);
                    areas_corresponded_r.add(r_neigh_area);
                }
            }

            neighbor_regions[r.get_region_index()] = neigh_regions;
            neighbor_region_areas[r.get_region_index()] = neigh_areas_each_neigh_regions;
        }

    }


    private int return_label(Region r)
    {
        int count = 0;
        for(Region neigh : neighbor_regions[r.get_region_index()])
        {
            if(regions_status[neigh.get_region_index()] == UNPROCESSED || regions_status[neigh.get_region_index()] == EXHAUSTING)
            {
                count ++;
            }
        }
        if(count == 0)
        {
            return PROCESSED;
        }
        else if(count == 1)
        {
            return EXHAUSTING;
        }
        else
        {
            return UNPROCESSED;
        }
    }


    /**
     * This function recursively updates the label of the regions
     */
    private void mark_label(Region r , int new_label)
    {
        if(regions_status[r.get_region_index()] == new_label)
        {
            return;
        }

        ArrayList<Region> belong_list = null;
        if(regions_status[r.get_region_index()] == UNPROCESSED)
        {
            belong_list = unprocessed_regions;
        }

        else if(regions_status[r.get_region_index()] == EXHAUSTING)
        {
            belong_list = exhausting_regions;
        }

        belong_list.remove(r);

        regions_status[r.get_region_index()] = new_label;

        if(new_label == PROCESSED)
        {
            processed_regions.add(r);
        }

        else if(new_label == UNPROCESSED)
        {
            unprocessed_regions.add(r);
        }

        else
        {
            exhausting_regions.add(r);
        }


        if(new_label == PROCESSED)
        {
            for(Region neigh : neighbor_regions[r.get_region_index()])
            {
                if(regions_status[neigh.get_region_index()] != PROCESSED)
                {
                    if(regions_status[neigh.get_region_index()] == EXHAUSTING)
                    {
                        mark_label(neigh , PROCESSED);
                    }

                    else if(regions_status[neigh.get_region_index()] == UNPROCESSED)
                    {
                        int count = 0;
                        for(Region nn_r : neighbor_regions[neigh.get_region_index()])
                        {
                            if(regions_status[nn_r.get_region_index()] == UNPROCESSED || regions_status[nn_r.get_region_index()] == EXHAUSTING)
                            {
                                count ++;
                            }
                        }
                        if(count == 1)
                        {

                            mark_label(neigh , EXHAUSTING);
                        }

                    }
                }
            }
        }

    }

}
