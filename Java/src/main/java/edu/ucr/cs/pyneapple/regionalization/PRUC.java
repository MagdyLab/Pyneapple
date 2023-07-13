package edu.ucr.cs.pyneapple.regionalization;


import edu.ucr.cs.pyneapple.utils.PRUCUtils.*;


import java.util.*;

/**
 * The PRUC problem is a generalized version for the p-regions problem
 */
public class PRUC implements RegionalizationMethod{

    private double heterogeneity = -1;
    private int[] regionLabels = null;

    private int p = 0;



    /**
     * The default constructor
     */
    public PRUC(){ }


    /**
     * test a manually constructed case
     * @throws InterruptedException multi-threading environment
     * @throws CloneNotSupportedException cloning the areas
     */
    public void Test_module() throws InterruptedException, CloneNotSupportedException {

        int p = 10;
        double threshold = 3000000;
        ArrayList<ArrayList<Integer>> lists = new ArrayList<>();
        lists.add(new ArrayList<>(Arrays.asList(1, 22)));
        lists.add(new ArrayList<>(Arrays.asList(0)));
        lists.add(new ArrayList<>(Arrays.asList(3,25,26,27)));
        lists.add(new ArrayList<>(Arrays.asList(2,4,5,8,11,27,28)));
        lists.add(new ArrayList<>(Arrays.asList(3,27)));
        lists.add(new ArrayList<>(Arrays.asList(3,6,8,27,28)));
        lists.add(new ArrayList<>(Arrays.asList(5,7,8,9,28)));
        lists.add(new ArrayList<>(Arrays.asList(6,9,15,17,28,31)));
        lists.add(new ArrayList<>(Arrays.asList(3,5,6,9,11,18)));
        lists.add(new ArrayList<>(Arrays.asList(6,7,8,10,12,15,17,18)));
        lists.add(new ArrayList<>(Arrays.asList(9,12)));
        lists.add(new ArrayList<>(Arrays.asList(3,8)));
        lists.add(new ArrayList<>(Arrays.asList(9,10,15,18)));
        lists.add(new ArrayList<>(Arrays.asList(14, 16)));
        lists.add(new ArrayList<>(Arrays.asList(13, 16, 20)));
        lists.add(new ArrayList<>(Arrays.asList(7,9,12,17,18,19,31)));
        lists.add(new ArrayList<>(Arrays.asList(13,14)));
        lists.add(new ArrayList<>(Arrays.asList(7,9,15)));
        lists.add(new ArrayList<>(Arrays.asList(8,9,12,15,19)));
        lists.add(new ArrayList<>(Arrays.asList(15,18,21,31)));
        lists.add(new ArrayList<>(Arrays.asList(14,21,31)));
        lists.add(new ArrayList<>(Arrays.asList(19,20,31)));
        lists.add(new ArrayList<>(Arrays.asList(0,23,25)));
        lists.add(new ArrayList<>(Arrays.asList(22,24,25,26)));
        lists.add(new ArrayList<>(Arrays.asList(23,26,27,28,29)));
        lists.add(new ArrayList<>(Arrays.asList(2,22,23,26)));
        lists.add(new ArrayList<>(Arrays.asList(2,23,24,25,27)));
        lists.add(new ArrayList<>(Arrays.asList(2,3,4,5,24,26,28,29)));
        lists.add(new ArrayList<>(Arrays.asList(3,5,6,7,24,27,29,30,31)));
        lists.add(new ArrayList<>(Arrays.asList(24,27,28,30)));
        lists.add(new ArrayList<>(Arrays.asList(28,29,31)));
        lists.add(new ArrayList<>(Arrays.asList(7,15,19,20,21,28,30)));

        Map<Integer, Set<Integer>> neighborSet = new HashMap<>();
        for(int i = 0 ; i <= 31 ; i++)
        {
            ArrayList<Integer> l = lists.get(i);
            Set<Integer> ts = new TreeSet<>(l);
            neighborSet.put(i , ts);
        }

        ArrayList<Double> ext_attr = new ArrayList<>(Arrays.asList(2040312.385, 2912880.772, 1034770.341, 2324727.436, 313895.53, 918758.241, 619581.709, 953861.244, 1431015.877, 888381.807, 149985.707, 354755.535, 335390.325, 955594.975, 1575361.146, 1472803.284, 1756848.578, 319017.395, 1387049.888, 1995816.284, 1244472.6, 1477195.199, 2735537.386, 2393736.228, 2107437.835, 2090624.512, 1866079.595, 2165307.921, 1529201.487, 1706261.492, 2077945.646, 2796252.499));
        ArrayList<Double> sim_attr = new ArrayList<>(Arrays.asList(22361.0, 9573.0, 4836.0, 5309.0, 10384.0, 4359.0, 11016.0, 4414.0, 3327.0, 3408.0, 17816.0, 6909.0, 6936.0, 7990.0, 3758.0, 3569.0, 21965.0, 3605.0, 2181.0, 1892.0, 2459.0, 2934.0, 6399.0, 8578.0, 8537.0, 4840.0, 12132.0, 3734.0, 4372.0, 9073.0, 7508.0, 5203.0));
        ArrayList<Double> centroid_x = new ArrayList<>(Arrays.asList(-115.08444993469801, -112.0531763525298, -104.91229277857447, -103.61919929677504, -102.3659992798087, -101.0216950636088, -99.85120607457536, -98.87456603615759, -101.88049866266942, -99.6337655140606, -99.12848299139927, -103.92580759281427, -99.06334973714358, -88.93982396854459, -90.43682565676336, -97.88183896111337, -88.25477854127803, -98.15402078343021, -99.90155686468316, -96.43184141727205, -92.57868268916083, -92.46523639155265, -110.80578514007713, -106.44446753154791, -102.03874590266828, -107.48367433614389, -104.92073367201407, -102.70061173367984, -100.44034382303118, -99.96025001417645, -98.6176641357791, -96.39869452166099));
        ArrayList<Double> centroid_y = new ArrayList<>(Arrays.asList(30.548240873480644, 25.92883970446684, 21.84638580354991, 20.600074392264805, 22.009333610459063, 20.896358200943524, 20.838643361009442, 20.46987060235712, 19.209264063124696, 19.360231080473305, 19.27260190640209, 19.141675184495977, 18.758538810911617, 20.778313972263124, 18.868679061086077, 19.014932050008614, 19.555944696096663, 19.41836228225203, 17.66677071705056, 16.958895390891758, 17.94880191099878, 16.499241452952553, 29.706803207455213, 28.802961550138825, 27.30176812625039, 25.017485978428542, 24.933491138650087, 23.306611848842675, 22.61562525404643, 25.578085484015574, 24.286158953672103, 19.373186237621805));
        ArrayList<double[]> centroids = new ArrayList<>();





        ArrayList<Long> sumAttr = new ArrayList<>();
        for (Double d : ext_attr) {
            sumAttr.add(Math.round(d));
        }

        ArrayList<Long> disAttr = new ArrayList<>();
        for (Double d : sim_attr) {
            disAttr.add(Math.round(d));
        }

        for(int i = 0 ; i < ext_attr.size() ; i++)
        {
            double[] centroid_xy = new double[2];
            centroid_xy[0] = centroid_x.get(i);
            centroid_xy[1] = centroid_y.get(i);
            centroids.add(centroid_xy);
        }

        ArrayList<Double> centroids_x = new ArrayList<>();
        ArrayList<Double> centroids_y = new ArrayList<>();
        for(int i = 0 ; i < centroids.size() ; i++)
        {
            centroids_x.add(centroids.get(i)[0]);
            centroids_y.add(centroids.get(i)[1]);
        }

        Object[] ret = execute_regionalization(neighborSet , disAttr , sumAttr , centroids_x, centroids_y, (long)threshold , p);
        double het = (Double)ret[0];
        ArrayList<Integer> label = (ArrayList<Integer>) ret[1];
        System.out.println("the het is " + het);
        for(int l : label)
        {
            System.out.print(l + " : ");
        }

    }


    /**
     * Examine whether the neighborhood relationship of areas is computed correctly
     * @param neighborSet the neighboring set
     * @return whether the neighborhood relationship if areas is computed correctly
     */
    public boolean exam_neighborhood(Map<Integer, Set<Integer>> neighborSet) {
        if(neighborSet == null) {
            System.out.println("neighborSet is null");
            return false;
        }

        for(Integer key1 : neighborSet.keySet()) {
            if(key1 == null) {
                System.out.println("null key found in neighborSet");
                return false;
            }

            Set<Integer> s = neighborSet.get(key1);

            if(s == null) {
                System.out.println("null set found for key: " + key1);
                return false;
            }

            for(Integer key2: s) {
                if(key2 == null) {
                    System.out.println("null value found in set for key: " + key1);
                    return false;
                }

                if(!neighborSet.containsKey(key2) || !neighborSet.get(key2).contains(key1)) {
                    System.out.println("neighborhood relationship is incorrect: " + key1 + " not in the neighborhood of " + key2);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Exam if the heterogeneity of the region is computed correctly
     * @param distAttr the list of similarity attribute
     * @return whether the regional heterogeneity is correctly computed
     */
    public boolean exam_disAttr(ArrayList<Long> distAttr)
    {
        if(distAttr == null)
        {
            System.out.println("the similarity attribute list is null");
            return false;
        }
        for(Long dist : distAttr)
        {
            if(dist == null || dist < 0)
            {
                System.out.println("negative value or null in similarity attribute");
                return false;
            }
        }
        return true;
    }

    /**
     * Exam if the extensive attribute of the region is computed correctly
     * @param sumAttr the list of extensive attribute
     * @return whether the regional extensive attribute is correctly computed
     */
    public boolean exam_sumAttr(ArrayList<Long> sumAttr)
    {
        if(sumAttr == null)
        {
            System.out.println("the extensive attribute list is null");
            return false;
        }

        for(Long ext : sumAttr)
        {
            if(ext == null || ext < 0)
            {
                System.out.println("negative value or null in extensive attribute");
                return false;
            }
        }
        return true;
    }

    /**
     * Execute the regionalization
     * @param neighborSet the neighborhood relationship of the areas
     * @param disAttr the list of similarity attribtues
     * @param sumAttr the list of extensive attributes
     * @param centroids_x the list of centroids latitudes of the areas
     * @param centroids_y the list of centroids longitudes of the areas
     * @param threshold the threshold on the constraint
     * @param p the predefined number of regions
     * @return the heterogeneity and labels of the partition and areas if a feasible partition is found or null if a feasible partition is not found
     * @throws InterruptedException multi-threading environment
     * @throws CloneNotSupportedException cloneing the set of areas
     */
    public Object[] execute_regionalization(Map<Integer, Set<Integer>> neighborSet, ArrayList<Long> disAttr, ArrayList<Long> sumAttr,  ArrayList<Double> centroids_x, ArrayList<Double> centroids_y, long threshold, int p) throws InterruptedException, CloneNotSupportedException {

        boolean issue = exam_neighborhood(neighborSet) && exam_disAttr(disAttr) && exam_sumAttr(sumAttr);

        if(!issue)
        {
            return new Object[]{false};
        }

        if(threshold < 0)
        {
            System.out.println("threshold value cannot be negative");
            return new Object[]{false};
        }

        if(p < 0)
        {
            System.out.println("the number of regions cannot be negative");
            return new Object[]{false};
        }

        this.p = p;
        ArrayList<Area> areas = new ArrayList<>();
        int size = sumAttr.size();
        for(int i = 0 ; i < size ; i++)
        {
            Area a = new Area(i , disAttr.get(i) , sumAttr.get(i) , new double[]{centroids_x.get(i) , centroids_y.get(i)});
			ArrayList<Integer> neigh_list;
            if(neighborSet.containsKey(i))
			{
				Set<Integer> neighbor_ids = neighborSet.get(i);
				neigh_list = new ArrayList<>(neighbor_ids);
			}
			
			else
			{
				neigh_list = new ArrayList<>();
			}
            
            a.set_neighbor_once(neigh_list);
            areas.add(a);
        }



        GlobalSearch sol = new GlobalSearch(areas , p , areas.size() , threshold);

        //the number of iterations in the local optimization is set to the default value, which equals to the number of areas
        int iter = areas.size();
        int num_thread = Runtime.getRuntime().availableProcessors();
        if(sol.solved())
        {
            LocalOptimization lo = new LocalOptimization(sol , iter , 0.99 , sol.get_all_areas() , sol.get_regions() , threshold, num_thread);
            //Region.test_result_correctness(sol.get_regions(), sol.get_all_areas(), threshold, true);
            this.heterogeneity = lo.getBest_hetero();
            this.regionLabels = lo.get_best_labels().stream().mapToInt(i->i).toArray();
            return new Object[]{lo.getBest_hetero() , lo.get_best_labels()};
        }


        else
        {
            //Region.test_result_correctness(sol.get_regions(), sol.get_all_areas(), threshold, true);
            return new Object[]{false};
        }




		
    }


    @Override
    public void execute_regionalization(Map<Integer, Set<Integer>> neighborSet, ArrayList<Long> disAttr, ArrayList<Long> sumAttr, Long threshold) {

    }


    /**
     *
     * @return the number of regions
     */
    @Override
    public int getP() {
        return p;
    }

    /**
     *
     * @return the labels of the assignment of areas to regions
     */
    @Override
    public int[] getRegionLabels() {
        return regionLabels;
    }


    /**
     * get the heterogeneity of all regions
     * @return the heterogeneity of the region
     */
    public double getHeterogeneity()
    {
        return heterogeneity;
    }
}
