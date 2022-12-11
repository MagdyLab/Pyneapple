import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Test {

    public Test()
    {

    }

    public Object[] execute_GSLO(int p , long threshold , boolean island , int iter , HashMap<Integer , TreeSet<Integer>> neighbor_map , ArrayList<Long> ext_attr , ArrayList<Long> sim_attr , ArrayList<Double> centroid_x , ArrayList<Double> centroid_y) throws InterruptedException, CloneNotSupportedException {
		
		
		
        ArrayList<Area> areas = new ArrayList<>();
        int size = neighbor_map.size();
        for(int i = 0 ; i < size ; i++)
        {
            Area a = new Area(i , sim_attr.get(i) , ext_attr.get(i) , new double[]{centroid_x.get(i) , centroid_y.get(i)});
			ArrayList<Integer> neigh_list;
            if(neighbor_map.containsKey(i))
			{
				TreeSet<Integer> neighbor_ids = neighbor_map.get(i);
				neigh_list = new ArrayList<>(neighbor_ids);
			}
			
			else
			{
				neigh_list = new ArrayList<>();
			}
            
            a.set_neighbor_once(neigh_list);
            areas.add(a);
        }

        GlobalSearch sol = new GlobalSearch(areas , p , areas.size() , threshold , island);

        if(sol.solved())
        {
            LocalOptimization lo = new LocalOptimization(sol , iter , 0.99 , sol.get_all_areas() , sol.get_regions() , threshold);
            return new Object[]{lo.getBest_hetero() , lo.get_best_labels()};
        }

        else
        {
            return new Object[]{false};
        }
		
    }

}
