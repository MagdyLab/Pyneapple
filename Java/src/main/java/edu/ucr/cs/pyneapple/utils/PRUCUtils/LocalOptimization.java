package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class corresponds to Section 5.2 Local Optimization
 */
public class LocalOptimization {
    private int max_no_improve;
    private ArrayList<Move> tabu_list;
    private double temperature = 1.0; //initial temperature
    private double alpha; //cooling rate
    private ArrayList<Area> all_areas;
    private ArrayList<Area> best_area_label;
    private Region[] regions;
    private double threshold;
    private final int tabu_len = 100; //length of the tabu list to avoid repetitive moves
    private long total_time;
    private double best_hetero;
    private GlobalSearch sol;
    private int num_thread;

    /**
     * The LocalOptimization class that further optimizes the heterogeneity by moving areas between regions
     * @param sol the partition identified from the global search
     * @param max_no_improve the maximum number of non-improving move
     * @param alpha the paramter for simulated annealing
     * @param all_areas the list of all input areas
     * @param regions the array of regions from the partition
     * @param threshold the threshold value from the user-defined constraint
     * @param num_thread the number of thread in the local optimization
     * @throws CloneNotSupportedException cloning areas
     */
    public LocalOptimization(GlobalSearch sol , int max_no_improve , double alpha, ArrayList<Area> all_areas , Region[] regions , double threshold, int num_thread) throws CloneNotSupportedException {
        this.max_no_improve = max_no_improve;
        this.alpha = alpha;
        this.all_areas = all_areas;
        this.regions = regions;
        this.threshold = threshold;
        tabu_list = new ArrayList<>();
        this.sol = sol;
        this.num_thread = num_thread;
        heuristic();
    }


    /**
     * get the labels of area to region in the partition that has the best heterogeneity
     * @return the labels of the regionalization results found through the searching that has the minimum heterogeneity
     */
    public ArrayList<Integer> get_best_labels()
    {
        ArrayList<Integer> ints = new ArrayList<>();
        for(Area a : best_area_label)
        {
            ints.add(a.get_associated_region_index());
        }

        return ints;
    }



    private void heuristic() throws CloneNotSupportedException {
        if(!sol.solved()) //If Global Search did not find a feasible partition, then Local Optimization would not be executed
        {
            this.total_time = 0;
            return;
        }

        long start_time = System.currentTimeMillis();
        ArrayList<Area> movable_units = new ArrayList<>();
        int no_improving_move = 0;

        double optimal_hetero = Region.get_all_region_hetero(regions);


        while(no_improving_move < max_no_improve)
        {
            //System.out.println("one iteration");
            if(movable_units.size() == 0)
            {
                movable_units = parallel_search_movable_units();
            }

            //we randomly select an area from the movable_units list and process this area based on a greedy method
            Object[] results = greedy_find(movable_units);

            //in this case, all the area's neighbor belongs to the same region or removing this area will cause the region not satisfy the threshold constraint
            if(results.length == 1)
            {
                continue;
            }

            Area area_to_move = (Area)results[0];
            Region donor = regions[area_to_move.get_associated_region_index()];
            Region receiver = (Region)results[1];
            double optimal_hetero_decre = (double)results[2];
            //System.out.println("the receiver is " + receiver + " donor is " + donor + " result len is " + results.length + " , "  +results[1] + " , " + results[2]);

            boolean move_flag;

            //suggesting the move increase the heterogeneity of the current partition
            //System.out.println("optimal hetero is " + optimal_hetero);
            if(optimal_hetero_decre > 0)
            {

                tabu_list.add(new Move(area_to_move , receiver , donor));
                if(tabu_list.size() == tabu_len)
                {
                    tabu_list.remove(0);
                }

                move_flag = true;
                //System.out.println("the donor is " + donor);
                //System.out.println("the receiver  is " + receiver);
                donor.remove_area_in_region(area_to_move);
                receiver.add_area_to_region(area_to_move);
                //System.out.println("movable unit size " + movable_units.size() + " " + area_to_move.get_geo_index() + " moved from " + donor.get_region_index() + " to " + receiver.get_region_index());
                movable_units.remove(area_to_move);

                double total_hetero = Region.get_all_region_hetero(regions);

                //suggesting the move increase the heterogeneity of the best partition
                if(total_hetero < optimal_hetero)
                {
                    no_improving_move = 0;
                    optimal_hetero = total_hetero;
                    best_area_label = Area.area_list_copy(all_areas);
                }

                //suggesting the move does not increase the heterogeneity of the best partition
                else
                {
                    no_improving_move ++;
                }
            }


            //if the move does not improve the quality of the current partition, whether or not it is accepted depends on the boltzmann probability
            else
            {
                no_improving_move ++;
                double random_num = new Random().nextDouble();
                double Boltzmann = Math.pow(Math.E , (optimal_hetero_decre / temperature));
                //double Boltzmann = Math.pow(Math.E , (  - ((double)(Region.get_all_region_hetero(regions) - optimal_hetero)/optimal_hetero) / temperature) );
                //double Boltzmann = Math.pow(Math.E , ((optimal_hetero_decre) / temperature));
                if(Boltzmann > random_num)
                {
                    if(tabu_list.contains(new Move(area_to_move , donor , receiver)))
                    {
                        move_flag = false;
                    }

                    else
                    {
                        tabu_list.add(new Move(area_to_move , receiver , donor));
                        donor.remove_area_in_region(area_to_move);
                        receiver.add_area_to_region(area_to_move);
                        move_flag = true;
                        best_area_label = Area.area_list_copy(all_areas);
                    }
                }

                else
                {
                    move_flag = false;
                }

            }
            movable_units.remove(area_to_move);

            if(move_flag)
            {
                ArrayList<Area> area_to_remove = new ArrayList<>();
                for(Area area : movable_units)
                {
                    if((area.get_associated_region_index() == donor.get_region_index()) || (area.get_associated_region_index() == receiver.get_region_index()))
                    {
                        area_to_remove.add(area);
                    }
                }
                movable_units.removeAll(area_to_remove);
                //System.out.println(donor.is_connected());
            }


            temperature = temperature * alpha;


        }

        long end_time = System.currentTimeMillis();
        total_time = end_time - start_time;

        this.best_hetero = optimal_hetero;
    }


    //in this greedy method, the parameter is the list of all movable units, we randomly process one of these movable units and try to ressign the unit
    //to the region with maximum heterogeneity decrease
    private Object[] greedy_find(ArrayList<Area> movable_units)
    {
        //System.out.print(movable_units.size() + " | ");
        Area area = movable_units.get(new Random().nextInt(movable_units.size()));


        int current_r_index = area.get_associated_region_index();

        //suggesting that removing this area will cause the region to fall below the threshold
        if(regions[current_r_index].get_region_extensive_attr() - area.get_extensive_attr() < threshold || regions[current_r_index].get_region_size() == 1)
        {
            movable_units.remove(area);
            return new Object[]{null};
        }

        ArrayList<Region> region_neighbors = new ArrayList<>();

        for(Area neigh_area : area.get_neigh_area(all_areas))
        {
            if(neigh_area.get_associated_region_index() != current_r_index)
            {
                Region r = regions[neigh_area.get_associated_region_index()];
                if(!region_neighbors.contains(r))
                {
                    region_neighbors.add(r);
                }
            }
        }

        if(region_neighbors.size() == 0)
        {
            movable_units.remove(area);
            return new Object[]{null};
        }

        double optimal_hetero_decre = Double.NEGATIVE_INFINITY;
        Region best_region = null;
        for(Region r : region_neighbors)
        {
            Region belonging_region = regions[area.get_associated_region_index()];
            double hetero_decre = belonging_region.compute_hetero_decre(area) - r.compute_hetero_incre(area);
            //System.out.println("hetero decre is " + hetero_decre);
            //System.out.println("optimal hetero decre is " + optimal_hetero_decre);
            //System.out.println(hetero_decre > optimal_hetero_decre);
            if(hetero_decre > optimal_hetero_decre)
            {
                optimal_hetero_decre = hetero_decre;
                best_region = r;
            }

        }

        //System.out.println("area " + area + " best region " + best_region + " optiaml hetero" + optimal_hetero_decre + " region neigh size " + region_neighbors.size() );
        return new Object[]{area , best_region , optimal_hetero_decre};


    }

    /**
     * This method applies multi-threading parallelization to find the movable areas from each region
     * @return the list of all movable areas from the partition
     */
    private ArrayList<Area> parallel_search_movable_units() {
        ArrayList<Area> movable_units = new ArrayList<>();
        ReentrantLock lock = new ReentrantLock();
        ExecutorService threadPool = Executors.newFixedThreadPool(num_thread);
        ArrayList<ParallelMovableUnitsSearch> tasks = new ArrayList<>();
        for (Region region : regions) {
            tasks.add(new ParallelMovableUnitsSearch(region, movable_units, lock));
        }

        for(ParallelMovableUnitsSearch task : tasks)
        {
            threadPool.execute(task);
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException ignored) {}
        return movable_units;
    }

    /**
     * Get the runtime from Local Optimization
     * @return runtime from Local Optimization
     */
    public long getTotal_time()
    {
        return total_time;
    }

    /**
     * Get the best heterogeneity found
     * @return the best heterogeneity
     */
    public double getBest_hetero()
    {
        return best_hetero;
    }


    /**
     * This class extends the Thread class and executes the task of finding the movable areas from a region
     */
    class ParallelMovableUnitsSearch extends Thread
    {
        Region r;
        ArrayList<Area> all_movable_units;
        ReentrantLock lock;
        ArrayList<Area> areas_in_r;

        public ParallelMovableUnitsSearch(Region r , ArrayList<Area> all_movable_units , ReentrantLock lock)
        {
            this.r = r;
            this.lock = lock;
            this.all_movable_units = all_movable_units;
            areas_in_r = r.get_areas_in_region();
        }

        public void run()
        {
            ArrayList<Area> r_articulation_pts = new Tarjan(r , all_areas).findAPs_Tarjan();
            lock.lock();
            ArrayList<Area> movable_areas = (ArrayList<Area>)r.getAreas_on_margin().clone();
            //take the intersect from all the articulation points and areas on the margin
            movable_areas.removeAll(r_articulation_pts);
            all_movable_units.addAll(movable_areas);
            lock.unlock();
        }





    }










}
