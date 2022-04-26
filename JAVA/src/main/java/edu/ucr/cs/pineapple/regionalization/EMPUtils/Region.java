package edu.ucr.cs.pineapple.regionalization.EMPUtils;

import edu.ucr.cs.pineapple.utils.SpatialGrid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Region {
    static boolean debug = false;
    static double minLowerBound, minUpperBound, maxLowerBound, maxUpperBound, avgLowerBound, avgUpperBound, sumLowerBound, sumUpperBound, countLowerBound, countUpperBound;
    List<Integer> areaList;
    int id;
    int numOfAreas;
    double average, max, min, sum;
    double acceptLow, acceptHigh;
    Set<Integer> areaNeighborSet;
    public static void setRange(Double minLowerBound,
                                Double minUpperBound,
                                Double maxLowerBound,
                                Double maxUpperBound,
                                Double avgLowerBound,
                                Double avgUpperBound,
                                Double sumLowerBound,
                                Double sumUpperBound,
                                Double countLowerBound,
                                Double countUpperBound){
        Region.minLowerBound = minLowerBound;
        Region.minUpperBound = minUpperBound;
        Region.maxLowerBound = maxLowerBound;
        Region.maxUpperBound = maxUpperBound;
        Region.avgLowerBound = avgLowerBound;
        Region.avgUpperBound = avgUpperBound;
        Region.sumLowerBound = sumLowerBound;
        Region.sumUpperBound = sumUpperBound;
        Region.countLowerBound = countLowerBound;
        Region.countUpperBound = countUpperBound;
    }
    //Set<Integer>
    public Region(int id){
        numOfAreas = 0;
        average = 0;
        this.id = id;
        this.acceptLow = Region.avgLowerBound;
        this.acceptHigh = Region.avgUpperBound;
        this.areaNeighborSet = new HashSet<Integer>();
        this.areaList = new ArrayList<Integer>();
        this.max = -Double.POSITIVE_INFINITY;
        this.min = Double.POSITIVE_INFINITY;
        this.sum = 0;
    }
    public boolean addArea(Integer id, long minAttrVal, long maxAttrVal, long avgAttrVal, long sumAttrVal, SpatialGrid sg){
        if (areaList.contains(id)){
            if(debug){
                System.out.println("Area " + id + " is already contained in the current region " + this.id + "!");
            }

            return false;
        }else{
            //System.out.println("Add " + id + " to " + this.getId() );
            this.areaList.add(id);
            areaNeighborSet.remove(id);
            for(Integer a: sg.getNeighbors(id)){
                if (!areaList.contains(a)){
                    areaNeighborSet.add(a);
                }
            }
            average = (average * numOfAreas + avgAttrVal) / (numOfAreas + 1);
            this.numOfAreas = this.numOfAreas + 1;
            acceptLow = Region.avgLowerBound * (numOfAreas + 1) - average * numOfAreas;
            acceptHigh = Region.avgUpperBound * (numOfAreas +1) - average * numOfAreas;
            if(this.min > minAttrVal){
                this.min = minAttrVal;
            }
            if(this.max < maxAttrVal){
                this.max = maxAttrVal;
            }
            this.sum += sumAttrVal;

            if(debug){
                System.out.println("Area " + id + " added to region " +this.getId());
            }

            return true;
        }

    }
    public boolean removeArea(Integer id, ArrayList<Long> minAttr, ArrayList<Long> maxAttr, ArrayList<Long> avgAttr,  ArrayList<Long> sumAttr, SpatialGrid sg){
        if (!areaList.contains(id)){
            System.out.println("Area to be removed is not in the region: area Id " + id + " region Id " + this.getId());
            return false;
        }else{
            areaList.remove(id);
            areaNeighborSet.add(id);
            this.areaNeighborSet.clear();
            //System.out.println("Sum before removal： " + this.sum);
            this.sum = this.sum - sumAttr.get(id);
            //System.out.println("Area sum attr: " + sumAttr.get(id));
           // System.out.println("Sum after removal： " + this.sum);
            this.average = (this.average * numOfAreas - avgAttr.get(id)) / (numOfAreas - 1);
            numOfAreas --;
            double oldMin = this.min;
            this.max = -Double.POSITIVE_INFINITY;
            this.min = Double.POSITIVE_INFINITY;
            for(Integer area: areaList){
                if(this.min > minAttr.get(area)){
                    this.min = minAttr.get(area);
                }
                if(this.max < maxAttr.get(area)){
                    this.max = maxAttr.get(area);
                }
                for(Integer neighborArea: sg.getNeighbors(area)){
                    if(!areaList.contains(neighborArea)){
                        areaNeighborSet.add(neighborArea);
                    }
                }
            }
            //System.out.println("Remove: min changes after removing " + minAttr.get(id) + " from " +oldMin + " to " +this.min);
            return true;
        }
    }
    public double getAverage(){
        return this.average;
    }
    public double getSum(){return this.sum;}
    public Set<Integer> getAreaNeighborSet(){
        return areaNeighborSet;
    }
    public Set<Integer> getRegionNeighborSet(int[] labels){
        Set <Integer> regionNeighborSet = new HashSet<Integer>();
        for(Integer a: this.areaNeighborSet){
            if(labels[a] <= 0)
                continue;
            regionNeighborSet.add(labels[a]);
        }
        return regionNeighborSet;
    }
    public int[] updateId(int newId, int[] labels){
        this.id = newId;
        for(Integer i: areaList){
            if(debug){
                System.out.println("Area " + i + " id changes from " + labels[i] + " to " + newId);
            }

            labels[i] = newId;
        }
        return labels;
    }
    public List<Integer> getAreaList(){
        return this.areaList;
    }
    public double getAcceptLow(){
        return this.acceptLow;
    }
    public double getAcceptHigh(){
        return this.acceptHigh;
    }

    public int getId() {
        return this.id;
    }
    public double getMin(){
        return this.min;
    }
    public double getMax(){
        return this.max;
    }
    public int getCount(){
        return this.numOfAreas;
    }
    public boolean acceptable(Integer area, ArrayList<Long> minAttr, ArrayList <Long> maxAttr, ArrayList<Long> avgAttr, ArrayList<Long> sumAttr){
        if(this.numOfAreas + 1 <= countUpperBound){
            if(this.sum + sumAttr.get(area) <= sumUpperBound){
                if((minAttr.get(area) < this.min && minAttr.get(area) <= minUpperBound && minAttr.get(area) >= minLowerBound) ||
                        minAttr.get(area) >= this.min){
                    if((maxAttr.get(area) > this.max && maxAttr.get(area) <= maxUpperBound && maxAttr.get(area) >= maxLowerBound) ||
                            maxAttr.get(area) <= this.max){
                        double tmpAvg = (this.numOfAreas * this.average + avgAttr.get(area)) / (this.numOfAreas + 1);
                        if(tmpAvg >= avgLowerBound && tmpAvg <= avgUpperBound){
                            return true;
                        }else{
                            if(debug){
                                System.out.println("Adding area " +area + " to region " + this.id + " exceeds one of the avg");
                            }
                        }
                    }else{
                        if(debug){
                            System.out.println("Adding area " +area + " to region " + this.id + " exceeds one of the max");
                        }
                    }
                }else{
                    if(debug){
                        System.out.println("Adding area " +area + " to region " + this.id + " exceeds one of the Min");
                    }
                }
            }else{
                if(debug){
                    System.out.println("Adding area " +area + " to region " + this.id + " exceeds the sumUpperBound");
                }
            }
        }else{
            if(debug){
                System.out.println("Adding area " +area + " to region " + this.id + " exceeds the countUpperBound");
            }
        }
        return false;
    }

    public boolean removable(Integer area, ArrayList<Long> minAttr, ArrayList <Long> maxAttr, ArrayList<Long> avgAttr, ArrayList<Long> sumAttr, SpatialGrid sg){
        if(!areaList.contains(area)){
            System.out.println("Area " + area + " not removable because area not in the list of " + this.getId());
            return false;
        }
        if((this.numOfAreas - 1) >= Region.countLowerBound &&
                ((this.sum - sumAttr.get(area)) >= Region.sumLowerBound) &&
                ((this.average * numOfAreas - avgAttr.get(area)) / (numOfAreas - 1) >= Region.avgLowerBound) &&
                ((this.average * numOfAreas - avgAttr.get(area)) / (numOfAreas - 1) <= Region.avgUpperBound)){
            if(this.min == minAttr.get(area) || this.max == maxAttr.get(area)){
                Double tmpMin = Double.POSITIVE_INFINITY;
                Double tmpMax = -Double.POSITIVE_INFINITY;
                List<Integer> tmpList = new ArrayList<>();
                tmpList.addAll(this.areaList);
                tmpList.remove(area);
                for(Integer otherArea: tmpList){
                    //if(otherArea != area){
                        if(tmpMin > minAttr.get(otherArea)){
                            tmpMin = Double.valueOf(minAttr.get(otherArea));
                        }
                        if(tmpMax < maxAttr.get(otherArea)){
                            tmpMax = Double.valueOf(maxAttr.get(otherArea));
                        }
                   // }
                }
                //System.out.println("Able: New min after removing area with Min value " + minAttr.get(area) + " is " + tmpMin);
                if(tmpMin <= Region.minUpperBound && tmpMin >= Region.minLowerBound && tmpMax <= Region.maxUpperBound && tmpMax >= Region.maxLowerBound){
                    if(numOfAreas - 1 > countLowerBound && numOfAreas - 1 > 0){
                        List<Integer> leftAreas = new ArrayList<Integer>();
                        for(int i = 0; i < this.areaList.size(); i++){
                            leftAreas.add(this.areaList.get(i));
                        }
                        leftAreas.remove(area);
                        List<Integer> connectedNeighbor = new ArrayList<Integer>();
                        boolean[] visited =new boolean[leftAreas.size()];
                        for(Integer i: sg.getNeighbors(leftAreas.get(0))){
                            connectedNeighbor.add(i);
                        }
                        visited[0] = true;
                        boolean grow = true;
                        while (grow){
                            grow = false;
                            for(int i = 1; i < leftAreas.size(); i++){
                                if(visited[i] == false && connectedNeighbor.contains(leftAreas.get(i))){
                                    visited[i] = true;
                                    for(Integer j: sg.getNeighbors(leftAreas.get(i))){
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
                        if(onecc)
                            return true;
                    }
                }


            }

        }

        return false;

    }

    public Region mergeWith(Region expandR, ArrayList<Long> minAttr, ArrayList<Long> maxAttr, ArrayList<Long> avgAttr, ArrayList<Long> sumAttr, SpatialGrid sg) {
        if(expandR == null){
            System.out.println("Error, region to be merged is NULL! " + this.getId());
        }
        if(this.id == expandR.getId()){
            return this;
        }
        Region tmpR = new Region(-1);
        for(Integer a: this.areaList){
            tmpR.addArea(a, minAttr.get(a), maxAttr.get(a), avgAttr.get(a), sumAttr.get(a),  sg);
        }
        for(Integer a: expandR.getAreaList()){
            tmpR.addArea(a, minAttr.get(a), maxAttr.get(a), avgAttr.get(a), sumAttr.get(a),  sg);
        }
        //System.out.println("Merged region:" + tmpR.getCount());
        return tmpR;

    }
    public boolean satisfiable(){
        return(this.min <= minUpperBound &&
                this.min >= minLowerBound &&
                this.max <= maxUpperBound &&
                this.max >= maxLowerBound &&
                this.average >= avgLowerBound &&
                this.average <= avgUpperBound &&
                this.sum >= sumLowerBound &&
                this.sum <= sumUpperBound &&
                this.numOfAreas >= countLowerBound &&
                this.numOfAreas <= countUpperBound);
    }
}