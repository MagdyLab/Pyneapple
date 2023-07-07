package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * This prerequisite datastructure for Seed Identification
 */
public class Seed implements Cloneable{
    private int seed_size;
    private ArrayList<Area> seeds;
    private ArrayList<Area> not_seeds;
    private ArrayList<Area> all_geoareas;
    private double min_dist;
    private Area[] min_pair_area;

    /**
     * The construct of Seed
     * @param all_geoareas The input areas
     * @param seed_size The number of areas in the seed.
     */
    public Seed(ArrayList<Area> all_geoareas , int seed_size)
    {
        this.all_geoareas = all_geoareas;
        seeds = new ArrayList<>();
        this.seed_size = seed_size;
        min_pair_area = new Area[2];
        select_initial_seeds(all_geoareas , seed_size);
    }

    /**
     * The overwriting constructor
     * @param seeded_areas The list of seeded areas
     */
    public Seed(ArrayList<Area> seeded_areas)
    {
        this.seeds = seeded_areas;
        this.seed_size = seeded_areas.size();
    }


    /**
     * This method randomly select a number of areas to be the initial seed
     * @param areas_set the input areas
     * @param seed_size the number of areas in the seed
     */
    private void select_initial_seeds(ArrayList<Area> areas_set , int seed_size)
    {

        int[] seed_index = choose_random_num(areas_set.size() , seed_size);
        for (int current_index : seed_index) {
            seeds.add(areas_set.get(current_index));
        }
        not_seeds = (ArrayList<Area>)all_geoareas.clone();
        not_seeds.removeAll(seeds);
        min_dist = compute_min_dist();
    }


    /**
     *
     * This function randomly selects a predefined number of value from a given range
     */
    private int[] choose_random_num(int max , int n)
    {

        int len = max;
        int[] source = new int[len];
        for (int i = 0; i < len; i++)
        {
            source[i] = i;
        }
        int[] result = new int[n];
        Random rd = new Random();
        int index;
        for (int i = 0; i < result.length; i++)
        {
            index = Math.abs(rd.nextInt() % len--);
            result[i] = source[index];
            source[index] = source[len];
        }
        return result;
    }


    /**
     * This method computes the minimum area-area pair distance of the seeded areas
     * @return the minimum area-area pair distance
     */
    private double compute_min_dist()
    {
        double min_pair_dist = Double.MAX_VALUE;
        for(int i = 0 ; i < seeds.size() ; i++)
        {
            for(int j = i+1 ; j < seeds.size() ; j++)
            {
                Area g1 = seeds.get(i);
                Area g2 = seeds.get(j);
                double dist = g1.compute_dist(g2);
                if(dist < min_pair_dist)
                {
                    min_pair_dist = dist;
                    min_pair_area[0] = g1;
                    min_pair_area[1] = g2;
                }

            }
        }
        return  min_pair_dist;
    }

    /**
     * This method randomly replace a seeded area by an unseeded area.
     * If after the replacement the min area-area pair distance is improved, then the replacement is accepted
     * Otherwise, the replacement is not accepted
     */
    public void random_replacement()
    {
        Area area_in_seed = seeds.get(new Random().nextInt(seeds.size()));
        Area area_not_in_seed = not_seeds.get(new Random().nextInt(not_seeds.size()));
        replace_area(area_in_seed , area_not_in_seed);

        double current_min_dist = compute_min_dist();

        if(current_min_dist > min_dist)
        {
            min_dist = current_min_dist;
        }

        else
        {
            replace_area(area_not_in_seed , area_in_seed);
        }

    }

    private void replace_area(Area area_in_seed , Area area_not_in_seed)
    {
        seeds.remove(area_in_seed);
        seeds.add(area_not_in_seed);
        not_seeds.remove(area_not_in_seed);
        not_seeds.add(area_in_seed);
    }


    /**
     * get the areas within the seed
     * @return the seed identified in this phase
     */
    public ArrayList<Area> get_seeds()
    {
        return seeds;
    }


    /**
     * get the number of areas within the seed
     * @return the size of the seed, i.e., the number of areas in the seed
     */
    public int get_seed_size()
    {
        return seed_size;
    }

}
