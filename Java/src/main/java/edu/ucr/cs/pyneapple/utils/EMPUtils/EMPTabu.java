package edu.ucr.cs.pyneapple.utils.EMPUtils;

import edu.ucr.cs.pyneapple.regionalization.EMP;
import java.util.*;

/**
 * The tabu search algorithm for the EMP
 */
public class EMPTabu {
    static boolean debug = false;

    /**
     * The default constructor for the EMPTabu class. The functions should be all static so the constructor is not actually used.
     */
    public EMPTabu(){}
    /**1
     * Perform tabu search for the initial solution constructed in the construction phase
     * @param initLabels the initial region labels
     * @param integerRegionMap the initial region maps
     * @param r the spatial grid for the area set
     * @param dissimMatrix the pairwise dissimilarity matrix
     * @param tabuLength the length of the tabu list
     * @param max_no_move the maximum number of moves for the tabu search
     * @param minAttr the list of min attribute values
     * @param maxAttr the list of max attribute values
     * @param avgAttr the list of avg attribute values
     * @param sumAttr the list of sum attribute values
     * @return the partition after the tabu search
     */
    public static TabuReturn performTabu(int[] initLabels,
                                    Map<Integer, Region> integerRegionMap,
                                    SpatialGrid r,
                                    long[][] dissimMatrix,
                                    int tabuLength,
                                    int max_no_move,
                                    ArrayList<Long> minAttr,
                                    ArrayList<Long> maxAttr,
                                    ArrayList<Long> avgAttr,
                                    ArrayList<Long> sumAttr){
        //boolean debug = true;
        if(debug){
            int zeroCount = 0;
            int removedCount = 0;
            for(int i = 0; i < initLabels.length; i++){
                if(initLabels[i] == 0){
                    zeroCount++;
                    System.out.println(i + " label 0");
                }
                if(initLabels[i] < 0){
                    removedCount++;
                    System.out.println(i + " label " + initLabels[i]);
                }
            }
        }

        int ni_move_ct = 0;
        boolean make_move_flag = false;
        List<edu.ucr.cs.pyneapple.utils.EMPUtils.Move> tabuList = new ArrayList<edu.ucr.cs.pyneapple.utils.EMPUtils.Move>();
        List<Integer> potentialAreas = new ArrayList<Integer>();

        //potentialMove
        int[] labels = Arrays.copyOf(initLabels, initLabels.length);
        int[] bestLabels = Arrays.copyOf(initLabels, initLabels.length);
        Map<Integer, Region> regionList = integerRegionMap;
        //Map<Integer, Integer> regionSpatialAttrs = initRegionSpatialAttr;
        long withinRegionDistance = calculateWithinRegionDistance(integerRegionMap, dissimMatrix);
        long bestWDS = withinRegionDistance;

        while (ni_move_ct <= max_no_move){
            //System.out.println("Move count " + ni_move_ct);
            edu.ucr.cs.pyneapple.utils.EMPUtils.Move potentialMove = null;
            if(debug){
                EMP.checkLabels(labels, regionList);
                System.out.println(ni_move_ct + " vs " + max_no_move);
            }

            if (make_move_flag || potentialAreas.size() == 0){
                potentialAreas = pickMoveAreaNew(labels, integerRegionMap,
                        r,
                        dissimMatrix,
                        minAttr, maxAttr, avgAttr, sumAttr);
                Double maxDiff = -Double.POSITIVE_INFINITY;
                if(debug){
                    System.out.println("potentialAreas: " + potentialAreas);
                }
                for(Integer poa: potentialAreas){
                    int donorRegion = labels[poa];
                    Set<Integer> poaNeighbor = r.getNeighbors(poa);
                    int lostDistance = 0;
                    try{
                        for(Integer rn: integerRegionMap.get(donorRegion).getAreaList()){
                            lostDistance += dissimMatrix[poa][rn];
                        }
                    }catch(Exception e){
                        System.out.println("Error when getting the arealist for the donor region: " + donorRegion);
                        EMP.checkLabels(labels, regionList);
                    }

                    for (Integer poan: poaNeighbor){
                        int recipientRegion = labels[poan];
                        if(recipientRegion == donorRegion || recipientRegion <= 0){
                            continue;
                        }else{
                            if(debug){
                                System.out.println("recipientRegion: " + recipientRegion);
                            }
                            int addedDistance = 0;
                            //System.out.println(recipientRegion + " from " + donorRegion);
                            //RegionNew tmpR = integerRegionMap.get(recipientRegion);
                            List<Integer> tmp = integerRegionMap
                                    .get(recipientRegion)
                                    .getAreaList();
                            if(debug){
                                if(tmp ==null){
                                    System.out.println(recipientRegion + " Warning " + poan);
                                }
                            }

                            for(Integer rn: tmp){
                                addedDistance += dissimMatrix[poa][rn];
                            }
                            int diff = lostDistance - addedDistance;
                            if(diff > maxDiff){
                                if(integerRegionMap.get(recipientRegion).acceptable(poa, minAttr, maxAttr, avgAttr, sumAttr)){
                                    maxDiff = diff / 1.0;
                                    potentialMove = new edu.ucr.cs.pyneapple.utils.EMPUtils.Move(poa, donorRegion, recipientRegion);
                                    //potentialMove = new Move(poa, recipientRegion, donorRegion);
                                    if(debug){
                                        System.out.println("Potential move recorded");
                                        if(!(regionList.get(donorRegion).removable(poa, minAttr, maxAttr, avgAttr, sumAttr, r))){
                                            System.out.println("Error, region " +donorRegion + " no longer want to donate area " + poa);
                                            System.exit(2);
                                        }else{
                                            System.out.println("Good, region " +donorRegion + "can donate area " + poa + " to " + recipientRegion);
                                        }
                                    }
                                }else{
                                    if(debug){
                                        System.out.println("Area " + poa + " not acceptable by region " + recipientRegion);
                                    }
                                }

                            }else{
                                if(debug){
                                    System.out.println("Diff < maxDiff, skip: lost " + lostDistance + " added " +addedDistance + " max " + maxDiff);

                                }
                            }
                        }
                    }
                }
                if (potentialMove == null){
                    break;
                }
                if (!tabuList.contains(potentialMove)){
                    if(debug){
                        System.out.println("TabuList does not contain the move, make move: " + potentialMove.area + " from " + potentialMove.donorRegion + " to " + potentialMove.recipientRegion);
                    }
                    make_move_flag = true;
                    if(debug){
                        System.out.println("Status before move:");
                        Region errorRegion = regionList.get(potentialMove.donorRegion);
                        System.out.println("Area sum " + sumAttr.get(potentialMove.area));
                        System.out.println("Region ID:" + errorRegion.getId());
                        System.out.println("Min:" +errorRegion.getMin());
                        System.out.println("Max:" + errorRegion.getMax());
                        System.out.println("Avg:" + errorRegion.getAverage());
                        System.out.println("Sum:" + errorRegion.getSum());

                        System.out.println("Count:" + errorRegion.getCount());
                        System.out.println("Areas: " + errorRegion.getAreaList());
                        System.out.println("Satisfiable:" + errorRegion.satisfiable());
                    }
                    if(!regionList.get(potentialMove.donorRegion).removeArea(potentialMove.area, minAttr, maxAttr, avgAttr, sumAttr, r)){
                        System.out.println("The area " + potentialMove.area + " is not in " + labels[potentialMove.area]);
                    }
                    regionList.get(potentialMove.recipientRegion).addArea(potentialMove.area, minAttr.get(potentialMove.area), maxAttr.get(potentialMove.area), avgAttr.get(potentialMove.area), sumAttr.get(potentialMove.area), r);
                    labels[potentialMove.area] = potentialMove.recipientRegion;
                    //regionSpatialAttrs.put(potentialMove.donorRegion, regionSpatialAttrs.get(potentialMove.donorRegion) - spatially_extensive_attr.get(potentialMove.area));
                    //regionSpatialAttrs.put(potentialMove.recipientRegion, regionSpatialAttrs.get(potentialMove.recipientRegion) + spatially_extensive_attr.get(potentialMove.area));
                    withinRegionDistance -= maxDiff;
                    if(withinRegionDistance < bestWDS){
                        if(debug){
                            System.out.println("Distance " + bestWDS +" to " + withinRegionDistance);
                        }
                        bestLabels = Arrays.copyOf(labels, labels.length);
                        bestWDS = withinRegionDistance;
                        if(tabuList.size() == tabuLength){
                            tabuList.remove(0);
                        }
                        edu.ucr.cs.pyneapple.utils.EMPUtils.Move prohibitMove = new edu.ucr.cs.pyneapple.utils.EMPUtils.Move( potentialMove.area, potentialMove.recipientRegion, potentialMove.donorRegion);
                        tabuList.add(prohibitMove);
                        ni_move_ct = 0;
                        if(debug){
                            for(Map.Entry e: regionList.entrySet()){
                                Region tmpr = (Region)e.getValue();
                                boolean satisfyable = tmpr.satisfiable();
                                if(!satisfyable){
                                    System.out.println("Error: region " + tmpr.getId() + " not satisfiable after moving " + potentialMove.area + " from " + potentialMove.donorRegion + " to " +potentialMove.recipientRegion);

                                    Region errorRegion = regionList.get(tmpr.getId());
                                    System.out.println("Area sum " + sumAttr.get(potentialMove.area));
                                    System.out.println("Region ID:" + errorRegion.getId());
                                        System.out.println("Min:" +errorRegion.getMin());
                                        System.out.println("Max:" + errorRegion.getMax());
                                        System.out.println("Avg:" + errorRegion.getAverage());
                                        System.out.println("Sum:" + errorRegion.getSum());

                                        System.out.println("Count:" + errorRegion.getCount());
                                        System.out.println("Areas: " + errorRegion.getAreaList());
                                        System.out.println("Satisfiable:" + errorRegion.satisfiable());
                                    System.exit(1);
                                }
                            }
                            System.out.println("Pass the satisfiability checking after moving " + potentialMove.area + " from " + potentialMove.donorRegion + " to " +potentialMove.recipientRegion);

                        }

                    }else{
                        ni_move_ct += 1;
                    }
                }else{
                    if(withinRegionDistance - maxDiff < bestWDS){
                        if(debug){
                            System.out.println("Better than the current best, make move: " + potentialMove.area + " from " + potentialMove.donorRegion + " to " + potentialMove.recipientRegion);
                        }
                        withinRegionDistance = withinRegionDistance - maxDiff.intValue();
                        make_move_flag = true;
                        labels[potentialMove.area] = potentialMove.recipientRegion;
                        regionList.get(potentialMove.donorRegion).removeArea(potentialMove.area, minAttr, maxAttr, avgAttr, sumAttr, r);
                        regionList.get(potentialMove.recipientRegion).addArea(potentialMove.area, minAttr.get(potentialMove.area), maxAttr.get(potentialMove.area), avgAttr.get(potentialMove.area), sumAttr.get(potentialMove.area), r);
                        //regionSpatialAttrs.put(potentialMove.donorRegion, regionSpatialAttrs.get(potentialMove.donorRegion) - spatially_extensive_attr.get(potentialMove.area));
                        //regionSpatialAttrs.put(potentialMove.recipientRegion, regionSpatialAttrs.get(potentialMove.recipientRegion) + spatially_extensive_attr.get(potentialMove.area));

                        bestLabels = Arrays.copyOf(labels, labels.length);
                        bestWDS = withinRegionDistance;
                        if(tabuList.size() == tabuLength){
                            tabuList.remove(0);
                        }
                        //tabuList.add(potentialMove);
                        edu.ucr.cs.pyneapple.utils.EMPUtils.Move prohibitMove = new Move( potentialMove.area, potentialMove.recipientRegion, potentialMove.donorRegion);
                        tabuList.add(prohibitMove);
                        ni_move_ct = 0;
                        if(debug){
                            for(Map.Entry e: regionList.entrySet()){
                                Region tmpr = (Region)e.getValue();
                                boolean satisfyable = tmpr.satisfiable();
                                if(!satisfyable){
                                    System.out.println("Error: region " + tmpr.getId() + " not satisfiable after moving " + potentialMove.area + " from " + potentialMove.donorRegion + " to " +potentialMove.recipientRegion);
                                    System.exit(1);
                                }
                            }
                            System.out.println("Pass the satisfiability checking after moving " + potentialMove.area + " from " + potentialMove.donorRegion + " to " +potentialMove.recipientRegion);

                        }
                    }else{
                        ni_move_ct += 1;
                        make_move_flag = false;
                    }
                }

            }
        }
        if(debug){
            for(Map.Entry e: regionList.entrySet()){
                Region tmpr = (Region)e.getValue();
                boolean satisfyable = tmpr.satisfiable();
                if(!satisfyable){
                    System.out.println("Error: region " + tmpr.getId() + " not satisfiable during final checking!");
                    System.exit(1);
                }
            }
            System.out.println("Pass the satisfiability checking before returning!");

        }
        //System.out.println("totalWithinRegionDDistance after Tabu: " + bestWDS);


        //return bestLabels;
        TabuReturn tr = new TabuReturn();
        tr.labels = bestLabels;
        tr.WDS = bestWDS;
        return tr;
    }

    /**
     * Pick the areas that can be moved between regions
     * @param labels the region label of the areas
     * @param regionMap the region maps (id to region)
     * @param r the spatial grid of the area set
     * @param dissimMatrix the dissim matrix
     * @param minAttr the list of min attribute values of the areas
     * @param maxAttr the list of max attribute values of the areas
     * @param avgAttr the list of avg attribute values of the areas
     * @param sumAttr the list of sum attribute values of the areas
     * @return the list of areas that can be moved between regions
     */
    public static List<Integer> pickMoveAreaNew(int[] labels, Map<Integer, Region> regionMap, SpatialGrid r, long[][] dissimMatrix, ArrayList<Long> minAttr, ArrayList<Long> maxAttr, ArrayList<Long> avgAttr, ArrayList<Long> sumAttr){
        List<Integer> potentialAreas = new ArrayList<Integer>();
        for(Map.Entry<Integer, Region> e: regionMap.entrySet()){
            List<Integer> rla = e.getValue().getAreaList();
            List<Integer> pas_indices = new ArrayList<Integer>();

            for(int i = 0; i < rla.size(); i++){

                if (e.getValue().removable(rla.get(i),minAttr, maxAttr, avgAttr, sumAttr, r)){
                    if(debug){
                        System.out.println("Area " + rla.get(i) + " can be removed from region " + e.getValue().getId());
                        System.out.println("Region " +e.getValue().getSum() + " area " +sumAttr.get(rla.get(i)));
                    }
                    pas_indices.add(i);
                }

            }
            if (pas_indices.size() > 0){//如果有可以移出的Area
                for(Integer pasi: pas_indices){
                    Set<Integer> pasin = r.getNeighbors(rla.get(pasi));
                    boolean neighborFromAnotherR = false;
                    for(Integer a: pasin){

                        if(neighborFromAnotherR)
                            break;
                        if(labels[a] != -2 && labels[a] != labels[rla.get(pasi)]){
                            neighborFromAnotherR = true;
                        }
                    }
                    if(neighborFromAnotherR){
                        List<Integer> leftAreas = new ArrayList<Integer>();
                        for(Integer i: rla){
                            leftAreas.add(i);
                        }
                        leftAreas.remove(pasi);
                        //removable detects if there is only one area in the region
                        if(leftAreas.size() == 0)
                            continue;
                        List<Integer> connectedNeighbor = new ArrayList<Integer>();
                        boolean[] visited =new boolean[leftAreas.size()];
                        for(Integer i: r.getNeighbors(leftAreas.get(0))){
                            connectedNeighbor.add(i);
                        }
                        visited[0] = true;
                        boolean grow = true;
                        while (grow){
                            grow = false;
                            for(int i = 1; i < leftAreas.size(); i++){
                                if(visited[i] == false && connectedNeighbor.contains(leftAreas.get(i))){
                                    visited[i] = true;
                                    for(Integer j: r.getNeighbors(leftAreas.get(i))){
                                        connectedNeighbor.add(j);
                                    }
                                    grow = true;
                                }
                            }
                        }
                        boolean onecc = true;
                        for(int i = 0; i < visited.length; i++){
                            if(visited[i] == false){
                                onecc = false;
                            }
                        }
                        if (onecc){
                            potentialAreas.add(rla.get(pasi));
                        }


                    }
                }
            }else{
                continue;
            }


        }
        return potentialAreas;
    }

    /**
     * Compute the total within region dissimilarity
     * @param regionMap the region map
     * @param dissimMatrix the matrix for the pairwise dissimilarity
     * @return a long value for the total within region dissimilarity
     */
    public static long calculateWithinRegionDistance(Map<Integer, Region> regionMap, long[][] dissimMatrix){
        long totalWithinRegionDistance = 0;
        System.out.println(regionMap.size());
        for(Map.Entry<Integer, Region> entry: regionMap.entrySet()){
            long regionDistance = 0;
            //System.out.println( entry.getValue().getAreaList().size());
            for(Integer i: entry.getValue().getAreaList()){
                for(Integer j:entry.getValue().getAreaList()){
                    regionDistance += dissimMatrix[i][j];

                }
            }
            //System.out.println("Region Distance for region " +entry.getKey() + " is " +regionDistance/2);
            totalWithinRegionDistance += regionDistance / 2;
        }
        return totalWithinRegionDistance;
    }

    /**
     * Computing the dissimilarity matrix
     * @param attr the disimilarity attribute values
     * @return an nxn matrix with entry i,j being the absolutute difference of the dissimilarity attribute of area i and j.
     */
    public static long[][] pdist(ArrayList<Long> attr){
        int attr_size = attr.size();
        long [][] distanceMatrix = new long[attr_size][attr_size];
        for (int i = 0; i < attr_size; i++){
            for (int j = 0; j < attr_size; j++){
                distanceMatrix[i][j] = Math.abs(attr.get(i) - attr.get(j));

            }
        }
        return distanceMatrix;
    }
}
