package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.LinkedList;

/**
   *  This class corresponds to Section 5.1.3 Enclaves Assignment
   *  In detail, Enclaves Assignment assigns all the unassigned areas left from the Region Growth Phase using a greedy method
 */
public class EnclavesAssignment {
    LinkedList<Area> enclaves;
    ArrayList<Area> all_geoareas;
    private Region[] regions;

    /**
     * The function assigns all the unassigned areas to regions
     * @param all_areas is the reference to the set of input areas
     * @param regions is the regions grown from the Region Growth Phase
     */
    public EnclavesAssignment(ArrayList<Area> all_areas, Region[] regions)
    {
        this.all_geoareas = all_areas;
        this.regions = regions;
        extract_enclaves();
        assign_enclaves();
    }


    private void extract_enclaves()
    {
        enclaves = new LinkedList<>();
        for (Area current_area : all_geoareas) {
            if (current_area.get_associated_region_index() == -1) {
                enclaves.add(current_area);
            }
        }
    }


    /**
     * This method assigns the enclaves by processing the enclaves one by one from the enclaves list using a greedy strategy.

     */
    private void assign_enclaves()
    {
        while(enclaves.size() != 0)
        {
            Area g = enclaves.remove();

            Region optimal_complete_region = find_best_neigh_r(g);
            if(optimal_complete_region != null)
            {
                optimal_complete_region.add_area_to_region(g);
                continue;
            }

            enclaves.add(g);
        }
    }


    private Region find_best_neigh_r(Area e)
    {
        ArrayList<Region> complete_region_neighs = new ArrayList<>();

        for(Area current_neigh_area : e.get_neigh_area(all_geoareas))
        {
            if(current_neigh_area.get_associated_region_index() == -1) //the neighbor is also an enclave
            {
                continue;
            }

            Region associate_region = regions[current_neigh_area.get_associated_region_index()];
            if(!complete_region_neighs.contains(associate_region))
            {
                complete_region_neighs.add(associate_region);
            }
        }


        Region optimal_region = null;
        double optimal_hetero_incre = Double.MAX_VALUE;
        for (Region current_region : complete_region_neighs) {
            double hetero_incre = current_region.compute_hetero_incre(e);
            if (hetero_incre < optimal_hetero_incre) {
                optimal_hetero_incre = hetero_incre;
                optimal_region = current_region;
            }
        }

        return optimal_region;
    }

}
