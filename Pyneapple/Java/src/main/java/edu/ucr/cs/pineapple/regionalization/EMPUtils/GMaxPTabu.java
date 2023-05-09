package edu.ucr.cs.pineapple.regionalization.EMPUtils;

import edu.ucr.cs.pineapple.utils.SpatialGrid;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;


public class GMaxPTabu {
    static boolean debug = true;
    static boolean labelCheck = true;
    public static void main(String[] args) throws Exception {

        // display a data store file chooser dialog for shapefiles
        //File file = JFileDataStoreChooser.showOpenFile("shp", null);
        //if (file == null) {
        // return;
        // }
        //int[] tList = {1000, 5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000};
        int[] tList = {10000};
        /*for(int i = 0; i < tList.length; i++){
            System.out.println(tList[i]+ "： ");
            set_input(tList[i]);
        }*/
        for(int i = 0; i < 1; i++){
            set_input("data/experiment/without_island/SouthCal/SouthCal_noisland.shp",
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    3000.0,
                    "pop2010",
                    10000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households");

        }
        /*set_input("data/experiment/without_island/Cal/Cal_noisland.shp",
                "pop_16up",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "unemployed",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "pop2010",
                2000.0,
                4000.0,
                "pop2010",
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                -Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,
                "households");*/


    }
    public static void  set_input(String fileName,
                                  String minAttrName,
                                  Double minAttrLow,
                                  Double minAttrHigh,
                                  String maxAttrName,
                                  Double maxAttrLow,
                                  Double maxAttrHigh,
                                  String avgAttrName,
                                  Double avgAttrLow,
                                  Double avgAttrHigh,
                                  String sumAttrName,
                                  Double sumAttrLow,
                                  Double sumAttrHigh,
                                  Double countLow,
                                  Double countHigh,
                                  String distAttrName

    ) throws Exception {
        double startTime = System.currentTimeMillis()/ 1000.0;
        File file = new File(fileName);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
        ArrayList<Long> minAttr = new ArrayList<>();
        ArrayList<Long> maxAttr = new ArrayList<>();
        ArrayList<Long> avgAttr = new ArrayList<>();
        ArrayList<Long> sumAttr = new ArrayList<>();
        ArrayList<Long> distAttr = new ArrayList<>();

        ArrayList<SimpleFeature> fList = new ArrayList<>();
        ArrayList<Integer> idList = new ArrayList<>();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = - Double.POSITIVE_INFINITY, maxY = -Double.POSITIVE_INFINITY;
        Double minAttrVal = Double.POSITIVE_INFINITY;
        Double maxAttrVal = -Double.POSITIVE_INFINITY;
        int count = 0;
        Double avgTotal = 0.0;
        Double sumTotal = 0.0;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                //System.out.print(feature.getID());
                //System.out.print(": ");

                minAttr.add(Long.parseLong(feature.getAttribute(minAttrName).toString()));
                maxAttr.add(Long.parseLong(feature.getAttribute(maxAttrName).toString()));
                avgAttr.add(Long.parseLong(feature.getAttribute(avgAttrName).toString()));
                sumAttr.add(Long.parseLong(feature.getAttribute(sumAttrName).toString()));
                distAttr.add(Long.parseLong(feature.getAttribute(distAttrName).toString()));
                fList.add(feature);
                if (Long.parseLong(feature.getAttribute(minAttrName).toString()) < minAttrVal){
                    minAttrVal = Double.parseDouble(feature.getAttribute(minAttrName).toString());
                }
                if(Long.parseLong(feature.getAttribute(maxAttrName).toString()) > maxAttrVal){
                    maxAttrVal = Double.parseDouble(feature.getAttribute(maxAttrName).toString());
                }
                count ++;
                avgTotal += Double.parseDouble(feature.getAttribute(avgAttrName).toString());
                sumTotal += Double.parseDouble(feature.getAttribute(sumAttrName).toString());
                //System.out.println(feature.getID());
                idList.add(Integer.parseInt(feature.getID().split("\\.")[1]) - 1);
                //System.out.print(feature.getID());
                //System.out.print(": ");
                //fList.add(feature);
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                double cminx = geometry.getEnvelope().getCoordinates()[0].getX();
                double cminy = geometry.getEnvelope().getCoordinates()[0].getY();
                double cmaxx = geometry.getEnvelope().getCoordinates()[2].getX();
                double cmaxy = geometry.getEnvelope().getCoordinates()[2].getY();
                if (minX > cminx){
                    minX = cminx;
                }
                if (minY > cminy){
                    minY = cminy;
                }
                if (maxX < cmaxx){
                    maxX = cmaxx;
                }
                if (maxY < cmaxy){
                    maxY = cmaxy;
                }

                //idList.add(Integer.parseInt(feature.getID().split("\\.")[1]) - 1);
            }

            //sg.printIndex();
        }

        //System.out.println(minX + " " + minY + ", " + maxX + " " + maxY);
        double rookstartTime = System.currentTimeMillis()/ 1000.0;
        System.out.println("Time for reading the file: " + (rookstartTime - startTime));
        SpatialGrid sg = new SpatialGrid(minX, minY, maxX, maxY);
        sg.createIndex(45, fList);
        sg.calculateContiguity(fList);
        //System.out.println(sg.getNeighbors(3));
        double rookendTime = System.currentTimeMillis()/ 1000.0;
        System.out.println("Rook time: " + (rookendTime - rookstartTime));
        /*for(int i = 0; i < fList.size(); i++){
            System.out.println(sg.getNeighbors(i));
            //arr[i] = i;
        }*/
        double dataLoadTime = System.currentTimeMillis()/ 1000.0;
        System.out.println(distAttr.size());
        //System.out.println(population.size());
        long [][] distanceMatrix = Tabu.pdist(distAttr);



        //RegionCollection rc = construction_phase_gene(population, income, 1, sg, idList,4000,Double.POSITIVE_INFINITY);

        RegionCollection rc = construction_phase_generalized(idList, distAttr, sg,
                minAttr,
                minAttrLow,
                minAttrHigh,

                maxAttr,
                maxAttrLow,
                maxAttrHigh,

                avgAttr,
                avgAttrLow,
                avgAttrHigh,

                sumAttr,
                sumAttrLow,
                sumAttrHigh,
                countLow, countHigh);
        double constructionTime = System.currentTimeMillis() / 1000.0;
        System.out.println("Time for construction phase:\n" + (constructionTime - rookendTime));

        int max_p = rc.getMax_p();
        System.out.println("MaxP: " + max_p);
        Map<Integer, Integer> regionSpatialAttr = rc.getRegionSpatialAttr();
        /*System.out.println("regionSpatialAttr after construction_phase:");
        for(Map.Entry<Integer, Integer> entry: regionSpatialAttr.entrySet()){
            Integer rid = entry.getKey();
            Integer rval = entry.getValue();
            System.out.print(rid + ": ");
            System.out.print(rval + " ");
            //System.out.println();
        }*/

        long totalWDS = Tabu.calculateWithinRegionDistance(rc.getRegionList(), distanceMatrix);
        System.out.println("totalWithinRegionDistance before tabu: \n" + totalWDS);
        int tabuLength = 10;
        int max_no_move = distAttr.size();
        //checkLabels(rc.getLabels(), rc.getRegionList());
        //To be updated

        TabuReturn tr = Tabu.performTabu(rc.getLabels(), rc.getRegionList(), sg, Tabu.pdist((distAttr)), tabuLength, max_no_move, minAttr, maxAttr, sumAttr, avgAttr);
        int[] labels = tr.labels;
        //int[] labels = SimulatedAnnealing.performSimulatedAnnealing(rc.getLabels(), rc.getRegionList(), sg, pdist((distAttr)), minAttr, maxAttr, sumAttr, avgAttr);
        double endTime = System.currentTimeMillis()/ 1000.0;
        //System.out.println("MaxP: " + max_p);
        System.out.println("Time for tabu(s): \n" + (endTime - constructionTime));
        System.out.println("total time: \n" +(endTime - startTime));
        File f = new File("data/AlgorithmTesting/out.txt");
        if(!file.exists()){
            file.createNewFile();
        }

        Writer w = new FileWriter(f);
        for( int i = 0; i < labels.length; i++){
            w.write(labels[i] + "\n");
        }
        w.close();

    }

    public static RegionCollection construction_phase_generalized(ArrayList<Integer> idList,
                                                                  ArrayList<Long> disAttr,
                                                                  SpatialGrid r,
                                                                  ArrayList<Long> minAttr,
                                                                  Double minLowerBound,
                                                                  Double minUpperBound,

                                                                  ArrayList<Long> maxAttr,
                                                                  Double maxLowerBound,
                                                                  Double maxUpperBound,

                                                                  ArrayList<Long> avgAttr,
                                                                  Double avgLowerBound,
                                                                  Double avgUpperBound,

                                                                  ArrayList<Long> sumAttr,
                                                                  Double sumLowerBound,
                                                                  Double sumUpperBound,

                                                                  Double countLowerBound,
                                                                  Double countUpperBound){
        //boolean debug = true;
        int maxIt = 100;
        //int maxIt = 10;
        Region.setRange(minLowerBound, minUpperBound, maxLowerBound, maxUpperBound, avgLowerBound, avgUpperBound, sumLowerBound, sumUpperBound, countLowerBound, countUpperBound);

        int max_p = 0;

        //long[][] distanceMatrix = pdist((disAttr));
        RegionCollection bestCollection = null;
        for (int it = 0; it < maxIt  ; it++) {
            //System.out.println("i: " + i + " max_p: " + max_p);
            ArrayList<Integer> areas = new ArrayList<Integer>();
            areas.addAll(idList);
            int min_unAssigned = maxAttr.size();
            int[] labels = new int[disAttr.size()];
            if (minLowerBound != -Double.POSITIVE_INFINITY || maxUpperBound != Double.POSITIVE_INFINITY || sumUpperBound != Double.POSITIVE_INFINITY) {
                Iterator<Integer> idIterator = areas.iterator();
                while (idIterator.hasNext()) {
                    Integer id = idIterator.next();
                    if (minAttr.get(id) < minLowerBound || maxAttr.get(id) > maxUpperBound || sumAttr.get(id) > sumUpperBound) {
                        idIterator.remove();
                        labels[id] = -2;
                    }
                }
            }

            ArrayList<Integer> seedAreas = new ArrayList<Integer>();
            if (minUpperBound != Double.POSITIVE_INFINITY || maxLowerBound != -Double.POSITIVE_INFINITY) {
                Iterator<Integer> idIterator = areas.iterator();
                while (idIterator.hasNext()) {
                    Integer id = idIterator.next();
                    if (minAttr.get(id) <= minUpperBound || maxAttr.get(id) >= maxLowerBound)//Changed from and to or
                        seedAreas.add(id);
                }
            } else {
                seedAreas.addAll(areas);
            }
            //RegionCollectionNew rc = construction_phase_average(avgAttr, disAttr, 1, r, idList, avgLowerBound, avgUpperBound, seedAreas);

            int cId;
            List<Integer> enclave = new ArrayList<Integer>();
            //Map<Integer, List<Integer>> regionList = new HashMap<Integer, List<Integer>>();
            Map<Integer, Integer> regionSpatialAttr = new HashMap<Integer, Integer>();
            Map<Integer, Region> regionList = new HashMap<Integer, Region>();
            List<Integer> unassignedLow = new ArrayList<Integer>();
            List<Integer> unassignedHigh = new ArrayList<Integer>();
            //List<> regionList = {};
            //Collections.shuffle(idList,new Random());
                /*for (int arr_index = 0; arr_index < threshold_attr.size(); arr_index++) {
                    if ((Integer)threshold_attr.get(arr_index) < lowerBound){
                        unassignedLow.add(arr_index);
                    }else if((Integer)threshold_attr.get(arr_index) > upperBound){
                        unassignedHigh.add(arr_index);
                    }else{
                        cId = regionList.size() + 1;
                        Region newRegion= new Region(upperBound, lowerBound, cId);
                        newRegion.addArea(arr_index, threshold_attr.get(arr_index), r);
                        regionList.put(cId, newRegion);
                        labels[arr_index] = cId;
                    }
                }*/
            for (int arr_index : seedAreas) {
                if (avgAttr.get(arr_index) < avgLowerBound) {
                    unassignedLow.add(arr_index);
                } else if (avgAttr.get(arr_index) > avgUpperBound) {
                    unassignedHigh.add(arr_index);
                } else {
                    cId = regionList.size() + 1;
                    Region newRegion = new Region(cId);
                    newRegion.addArea(arr_index, minAttr.get(arr_index), maxAttr.get(arr_index), avgAttr.get(arr_index), sumAttr.get(arr_index), r);
                    regionList.put(cId, newRegion);
                    labels[arr_index] = cId;
                }
            }
            /*for(Map.Entry<Integer, RegionNew> mapEntry: regionList.entrySet()){
                System.out.println(mapEntry.getKey() + " " + mapEntry.getValue().getId());
            }*/
            if(debug){
                System.out.println("Step 1:");
                System.out.println("UnassignedLow: " + unassignedLow.size());
                System.out.println("UnassignedHigh: " + unassignedHigh.size());
                System.out.println("Num of regions: " + regionList.size());
            }

                /*for(Map.Entry<Integer, Region> e: regionList.entrySet()){
                    System.out.println(e.getKey() + " " + e.getValue().getId() + " " + e.getValue().getRegionNeighborSet(labels));
                }*/
            boolean regionChange = true;
            Set<Integer> removedLow = new HashSet<Integer>();
            Set<Integer> removedHigh = new HashSet<Integer>();
            int count = 0;
            while (!(unassignedHigh.size() == removedLow.size()) && !(unassignedLow.size() == removedHigh.size()) && regionChange) {
                regionChange = false;
                Iterator<Integer> iteratorLow = unassignedLow.iterator();
                count++;
                if(debug)
                    System.out.println("Round " + count);
                while (iteratorLow.hasNext()) {
                    Integer lowArea = iteratorLow.next();
                    if (removedLow.contains(lowArea)) {
                        continue;
                    }
                    Region tr = new Region(-1);
                    removedLow.add(lowArea);
                    labels[lowArea] = -1;
                    tr.addArea(lowArea, minAttr.get(lowArea), maxAttr.get(lowArea), avgAttr.get(lowArea), sumAttr.get(lowArea), r);
                    boolean feasible = false;
                    boolean updated = true;
                    while (!feasible && updated) {
                        updated = false;
                        Set<Integer> neighborSet = tr.getAreaNeighborSet();
                        //Collections.shuffle((List<?>) neighborSet);
                        List<Integer> neighborList = new ArrayList<>(neighborSet);
                        Collections.shuffle(neighborList);

                        if (tr.getAverage() < avgLowerBound) {
                            for (Integer i : neighborList) {
                                if (labels[i] == 0 && avgAttr.get(i) > avgUpperBound && !removedHigh.contains(i)) {
                                    tr.addArea(i, minAttr.get(i), maxAttr.get(i), avgAttr.get(i), sumAttr.get(i), r);
                                    removedHigh.add(i);
                                    labels[i] = -1;
                                    updated = true;
                                    break;
                                }
                            }
                        } else if (tr.getAverage() > avgUpperBound) {
                            for (Integer i : neighborList) {
                                if (labels[i] == 0 && avgAttr.get(i) < avgLowerBound && !removedLow.contains(i)) {
                                    tr.addArea(i, minAttr.get(i), maxAttr.get(i), avgAttr.get(i), sumAttr.get(i), r);
                                    removedLow.add(i);
                                    labels[i] = -1;
                                    updated = true;
                                    break;
                                }
                            }
                        } else {
                            feasible = true;
                            regionChange = true;
                            if(debug){System.out.println("Region Change");}
                            labels = tr.updateId(regionList.size() + 1, labels);
                            regionList.put(regionList.size() + 1, tr);
                        }


                    }
                    if (!feasible) {

                        for (Integer area : tr.getAreaList()) {
                            labels[area] = 0;
                            if (avgAttr.get(area) < avgLowerBound) {
                                removedLow.remove(area);
                            } else {
                                removedHigh.remove(area);
                            }
                        }
                    }

                }
            }
            unassignedHigh.removeAll(removedHigh);
            unassignedLow.removeAll(removedLow);


            if(debug){
                System.out.println();
                System.out.println("Step 2:");
                System.out.println("UnassignedLow: " + unassignedLow.size());
                System.out.println("UnassignedHigh: " + unassignedHigh.size());
                System.out.println("Num of regions: " + regionList.size());

            }

            if (debug) {
                for (int i = 0; i < labels.length; i++) {
                    System.out.print(labels[i] + " ");
                }
                System.out.println();
                for (Map.Entry<Integer, Region> e : regionList.entrySet()) {

                    System.out.println("Id after step2:" + e.getValue().getId());
                    System.out.println("Min:" + e.getValue().getMin());
                    System.out.println("Max:" + e.getValue().getMax());
                    System.out.println("Avg:" + e.getValue().getAverage());
                    System.out.println("Sum:" + e.getValue().getSum());

                    System.out.println("Count:" + e.getValue().getCount());
                    System.out.println("Satisfiable:" + e.getValue().satisfiable());

                }
            }
            List<Integer> infeasibleLow = new ArrayList<Integer>();
            List<Integer> infeasibleHigh = new ArrayList<Integer>();
            List<Integer> unassignedAverage = new ArrayList<Integer>();
            for (int arr_index = 0; arr_index < avgAttr.size(); arr_index++) {
                if (seedAreas.contains(arr_index))
                    continue;
                if (labels[arr_index] > -1 && avgAttr.get(arr_index) < avgLowerBound) {
                    unassignedLow.add(arr_index);
                } else if (labels[arr_index] > -1 && avgAttr.get(arr_index) > avgUpperBound) {
                    unassignedHigh.add(arr_index);
                } else if (labels[arr_index] > -1) {
                    unassignedAverage.add(arr_index);
                }
            }
            Iterator<Integer> iteratorAvg = unassignedAverage.iterator();
            while (iteratorAvg.hasNext()) {
                Integer avgArea = iteratorAvg.next();
                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(avgArea));
                Collections.shuffle(neighborList);
                for (Integer neighborArea : neighborList) {
                    //if(labels[neighborArea] != 0){
                    if (labels[neighborArea] > 0) {
                        regionList.get(labels[neighborArea]).addArea(avgArea, minAttr.get(avgArea), maxAttr.get(avgArea), avgAttr.get(avgArea), sumAttr.get(avgArea), r);
                        labels[avgArea] = regionList.get(labels[neighborArea]).getId();
                        iteratorAvg.remove();
                        break;
                    }
                }
            }
            Iterator<Integer> iteratorLow = unassignedLow.iterator();
            while (iteratorLow.hasNext()) {
                Integer lowarea = iteratorLow.next();
                //Set<Integer> neighborRegionSet = new HashSet<Integer>();
                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(lowarea));
                Collections.shuffle(neighborList);
                for (Integer neighborArea : neighborList) {
                    //Set.add(labels[neighborArea]);
                    if (labels[neighborArea] > 0 && regionList.get(labels[neighborArea]).getAcceptLow() <= avgAttr.get(lowarea)) {
                        if (debug) {
                            System.out.println("Add low area " + lowarea + " to region " + labels[neighborArea]);
                        }
                        regionList.get(labels[neighborArea]).addArea(lowarea, minAttr.get(lowarea), maxAttr.get(lowarea), avgAttr.get(lowarea), sumAttr.get(lowarea), r);
                        iteratorLow.remove();
                        labels[lowarea] = regionList.get(labels[neighborArea]).getId();
                        break;
                    }
                }


            }
            Iterator<Integer> iteratorHigh = unassignedHigh.iterator();
            while (iteratorHigh.hasNext()) {
                Integer higharea = iteratorHigh.next();
                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(higharea));
                Collections.shuffle(neighborList);
                for (Integer neighborArea : neighborList) {
                    if (labels[neighborArea] > 0 && regionList.get(labels[neighborArea]).getAcceptHigh() >= avgAttr.get(higharea)) {
                        regionList.get(labels[neighborArea]).addArea(higharea, minAttr.get(higharea), maxAttr.get(higharea), avgAttr.get(higharea), sumAttr.get(higharea), r);
                        iteratorHigh.remove();
                        labels[higharea] = regionList.get(labels[neighborArea]).getId();
                        break;
                    }
                }
            }
            boolean merged = true;
            while (merged) {
                merged = false;
                Collections.shuffle(unassignedLow);
                for (Integer lowarea : unassignedLow) {
                    List<Region> tmpRegionList = new ArrayList<Region>();
                    Region tryR = new Region(-1);
                    Double lowestAcceptLow = Double.POSITIVE_INFINITY;
                    //System.out.println(r.getNeighbors(lowarea));
                    List<Integer> neighborList = new ArrayList<>(r.getNeighbors(lowarea));
                    Collections.shuffle(neighborList);
                    for (Integer neighborArea : neighborList) {
                        //System.out.print(labels[neighborArea] + " ");
                        if (labels[neighborArea] > 0) {
                            Region tmpR = regionList.get(labels[neighborArea]);
                            //System.out.println("Safe");
                            //System.out.print("Error at " + neighborArea + " " + labels[neighborArea] + " ");
                            //System.out.println(tmpR.getId());
                            if (tmpR.getAcceptLow()
                                    < lowestAcceptLow) {
                                tryR = regionList.get(labels[neighborArea]);
                                lowestAcceptLow = regionList.get(labels[neighborArea]).getAcceptLow();
                            }
                        } else {
                            continue;
                        }

                    }
                    //System.out.println();
                        /*
                        if(tryR.getId() == -1){

                            System.out.println("Low Area: " + lowarea + " faled to find neighbor!");
                        }*/
                    tmpRegionList.add(tryR);
                    boolean feasible = false;
                    while (!feasible) {
                        Region expandR = new Region(-1);

                        Double expandRacceptlowest = Double.POSITIVE_INFINITY;
                        //System.out.println("Neighbor Areas for " + lowarea + " " + tryR.getAreaNeighborSet());
                        List<Integer> tryRNeighborList = new ArrayList<>(tryR.getRegionNeighborSet(labels));
                        Collections.shuffle(tryRNeighborList);
                        for (Integer lr : tryRNeighborList) {
                            //System.out.print(lr + " ");
                            if (lr > 0 && regionList.get(lr).getAcceptLow() < expandRacceptlowest && !tmpRegionList.contains(regionList.get(lr))) {
                                expandR = regionList.get(lr);
                                expandRacceptlowest = expandR.getAcceptLow();
                            }
                        }
                        //System.out.println();

                        if (expandR.getId() == -1) {
                            break;
                        }
                        //System.out.println("Try " + expandR.getId() + " for " + lowarea);
                        tmpRegionList.add(expandR);
                        tryR = tryR.mergeWith(expandR, minAttr, maxAttr, avgAttr, sumAttr, r);
                        if (avgAttr.get(lowarea) >= tryR.getAcceptLow()) {
                            merged = true;
                            //System.out.println("Merged!");
                            tryR.addArea(lowarea, minAttr.get(lowarea), maxAttr.get(lowarea), avgAttr.get(lowarea), sumAttr.get(lowarea), r);
                            labels[lowarea] = expandR.getId();
                            labels = tryR.updateId(expandR.getId(), labels);
                            for (Region tr : tmpRegionList) {

                                regionList.remove(tr.getId());

                            }
                            feasible = true;
                            regionList.put(tryR.getId(), tryR);
                            if(debug){
                                System.out.println("Number of remaining regions(Min): " + regionList.size());
                            }
                        }
                    }
                    if (!feasible) {
                        infeasibleLow.add(lowarea);
                        //System.out.println("Low Area: " + lowarea + " fail to merge!");
                    }
                }
            }
            Collections.shuffle(unassignedHigh);
            for (Integer highArea : unassignedHigh) {
                List<Region> tmpRegionList = new ArrayList<Region>();
                Region tryR = new Region(-1);
                Double highestAcceptHigh = -Double.POSITIVE_INFINITY;

                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(highArea));
                Collections.shuffle(neighborList);

                for (Integer neighborArea : neighborList) {

                    if (labels[neighborArea] > 0 && regionList.get(labels[neighborArea]).getAcceptHigh() > highestAcceptHigh) {

                        tryR = regionList.get(labels[neighborArea]);
                        highestAcceptHigh = regionList.get(labels[neighborArea]).getAcceptHigh();
                    }
                }
                if (tryR.getId() == -1) {
                    if(debug)
                        System.out.println("HighArea: " + highArea + " faled to find neighbor!");
                }
                tmpRegionList.add(tryR);
                boolean feasible = false;
                while (!feasible) {
                    Region expandR = new Region(-1);
                    Double expandRacceptHighest = -Double.POSITIVE_INFINITY;
                    for (Integer lr : tryR.getRegionNeighborSet(labels)) {
                        if (lr > 0 && regionList.get(lr).getAcceptHigh() <= expandRacceptHighest && !tmpRegionList.contains(regionList.get(lr))) {
                            expandR = regionList.get(lr);
                            expandRacceptHighest = expandR.getAcceptHigh();
                        }
                    }
                    if (expandR.getId() == -1) {
                        break;
                    }
                    tmpRegionList.add(expandR);
                    tryR = tryR.mergeWith(expandR, minAttr, maxAttr, avgAttr, sumAttr, r);
                    if (avgAttr.get(highArea) <= tryR.getAcceptHigh()) {
                        tryR.addArea(highArea, minAttr.get(highArea), maxAttr.get(highArea), avgAttr.get(highArea), sumAttr.get(highArea), r);
                        labels[highArea] = expandR.getId();
                        labels = tryR.updateId(expandR.getId(), labels);
                        for (Region tr : tmpRegionList) {

                            regionList.remove(tr.getId());

                        }
                        feasible = true;
                        regionList.put(tryR.getId(), tryR);
                        if(debug){
                            System.out.println("Number of remaining regions(Max): " + regionList.size());
                        }
                    }
                }
                if (!feasible) {
                    infeasibleHigh.add(highArea);
                }
            }
            Collections.shuffle(unassignedAverage);
            Iterator<Integer> iteratorAvg2 = unassignedAverage.iterator();
            while (iteratorAvg2.hasNext()) {
                Integer avgArea = iteratorAvg2.next();
                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(avgArea));
                Collections.shuffle(neighborList);
                for (Integer neighborArea : r.getNeighbors(avgArea)) {
                    if (labels[neighborArea] > 0) {
                        regionList.get(labels[neighborArea]).addArea(avgArea, minAttr.get(avgArea), maxAttr.get(avgArea), avgAttr.get(avgArea), sumAttr.get(avgArea), r);
                        labels[avgArea] = regionList.get(labels[neighborArea]).getId();
                        iteratorAvg2.remove();
                        break;
                    }
                }
            }
            if(debug){
                System.out.println();
                System.out.println("Step 3:");
                System.out.println("Infeasible low: " + infeasibleLow);
                System.out.println("Infeasible high: " + infeasibleHigh);
                System.out.println("Unassigned average: " + unassignedAverage);
                System.out.println("Number of regions after step 3: " + regionList.size());
            }

            if (debug) {
                for (Map.Entry<Integer, Region> e : regionList.entrySet()) {

                    System.out.println("Id after step 3:" + e.getValue().getId());
                    System.out.println("Min:" + e.getValue().getMin());
                    System.out.println("Max:" + e.getValue().getMax());
                    System.out.println("Avg:" + e.getValue().getAverage());
                    System.out.println("Sum:" + e.getValue().getSum());

                    System.out.println("Count:" + e.getValue().getCount());
                    System.out.println("Areas: " + e.getValue().getAreaList());
                    System.out.println("Satisfiable:" + e.getValue().satisfiable());

                }
            }

            //checkLabels(labels, regionList);
            //Map<Integer, RegionNew> regionList = rc.getRegionList();

            //Add one more step to resolve Min and Max
            List<Integer> notMin = new ArrayList<Integer>();
            List<Integer> notMax = new ArrayList<Integer>();
            for (Map.Entry<Integer, Region> regionEntry : regionList.entrySet()) {
                Region region = regionEntry.getValue();
                if(region.getMin() > minUpperBound){
                    notMin.add(region.getId());
                }
                if(region.getMax() < maxLowerBound){
                    notMax.add(region.getId());
                }
            }
            Iterator<Integer> notMinIterator = notMin.iterator();

            while(notMinIterator.hasNext()){
                Integer notMinRegion = notMinIterator.next();

                Set<Integer> neighborSet = regionList.get(notMinRegion).getRegionNeighborSet(labels);
                List<Integer>regionNeighbor = new ArrayList<>(neighborSet);
                Collections.shuffle(regionNeighbor);
                for(Integer neighbor:regionNeighbor){
                    if(notMax.contains(neighbor)){
                        Region mergedRegion = regionList.get(notMinRegion).mergeWith(regionList.get(neighbor), minAttr, maxAttr, avgAttr, sumAttr, r);
                        mergedRegion.updateId(notMinRegion, labels);
                        regionList.remove(notMinRegion);
                        regionList.remove(neighbor);
                        regionList.put(notMinRegion, mergedRegion);
                        notMinIterator.remove();
                        notMax.remove(neighbor);
                        break;

                    }
                }
            }

            while(notMinIterator.hasNext()){
                Integer notMinRegion = notMinIterator.next();

                Set<Integer> neighborSet = regionList.get(notMinRegion).getRegionNeighborSet(labels);
                List<Integer>regionNeighbor = new ArrayList<>(neighborSet);
                Collections.shuffle(regionNeighbor);
                for(Integer neighbor:regionNeighbor){
                    if(!notMin.contains(neighbor)){
                        Region mergedRegion = regionList.get(notMinRegion).mergeWith(regionList.get(neighbor), minAttr, maxAttr, avgAttr, sumAttr, r);
                        mergedRegion.updateId(neighbor, labels);
                        regionList.remove(notMinRegion);
                        regionList.remove(neighbor);
                        regionList.put(neighbor, mergedRegion);
                        notMinIterator.remove();
                        //notMax.remove(neighbor);
                        break;

                    }
                }
            }
            Iterator<Integer> notMaxIterator = notMax.iterator();
            while(notMaxIterator.hasNext()){
                Integer notMaxRegion = notMaxIterator.next();

                Set<Integer> neighborSet = regionList.get(notMaxRegion).getRegionNeighborSet(labels);
                List<Integer>regionNeighbor = new ArrayList<>(neighborSet);
                Collections.shuffle(regionNeighbor);
                for(Integer neighbor:regionNeighbor){
                    if(!notMax.contains(neighbor)){
                        Region mergedRegion = regionList.get(notMaxRegion).mergeWith(regionList.get(neighbor), minAttr, maxAttr, avgAttr, sumAttr, r);
                        mergedRegion.updateId(neighbor, labels);
                        regionList.remove(notMaxRegion);
                        regionList.remove(neighbor);
                        regionList.put(neighbor, mergedRegion);
                        notMaxIterator.remove();
                        //notMax.remove(neighbor);
                        break;

                    }
                }
            }



            //Start sum and count
            if(debug){
                System.out.println("-------------");
                System.out.println("P after AVG" +regionList.size());
                System.out.println("-------------");
            }
            boolean updated = true;
            while (updated) {
                //checkLabels(labels, regionList);
                updated = false;
                List<Map.Entry<Integer, Region>> tmpList2 = new ArrayList<Map.Entry<Integer, Region>>(regionList.entrySet());
                Collections.shuffle(tmpList2);
                for (Map.Entry<Integer, Region> regionEntry : tmpList2) {
                    Region region = regionEntry.getValue();
                    if (region.getCount() < countLowerBound || region.getSum() < sumLowerBound) {
                        List<Integer> neighborList = new ArrayList<>(region.getAreaNeighborSet());
                        Collections.shuffle(neighborList);
                        for (Integer area : neighborList) {
                            if (labels[area] > 0 && regionList.get(labels[area]).removable(area, minAttr, maxAttr, avgAttr, sumAttr, r) && region.acceptable(area, minAttr, maxAttr, avgAttr, sumAttr)) {
                                regionList.get(labels[area]).removeArea(area, minAttr, maxAttr, avgAttr, sumAttr, r);
                                region.addArea(area, minAttr.get(area), maxAttr.get(area), avgAttr.get(area), sumAttr.get(area), r);
                                labels[area] = region.getId();
                                updated = true;
                                break;
                            }
                        }
                    }
                    if (region.getCount() > countUpperBound || region.getSum() > sumUpperBound) {
                        boolean removed = false;
                        //Iterator<Integer> it = region.areaList.iterator();
                        List<Integer> tmpList = new ArrayList<Integer>();
                        for (Integer area : region.getAreaList()) {
                            tmpList.add(area);
                        }
                        Collections.shuffle(tmpList);
                        for (Integer area : tmpList) {

                            if (region.removable(area, minAttr, maxAttr, avgAttr, sumAttr, r)) {
                                List<Integer> neighborList = new ArrayList<>(r.getNeighbors(area));
                                for (Integer neighbor : neighborList) {
                                    //存在未分配的area？- sumUpper, 一部分被移除的是0 -> 改成-2
                                    if (labels[neighbor] > 0 && regionList.get(labels[neighbor]).acceptable(area, minAttr, maxAttr, avgAttr, sumAttr)) {
                                        regionList.get(labels[neighbor]).addArea(area, minAttr.get(area), maxAttr.get(area), avgAttr.get(area), sumAttr.get(area), r);
                                        region.removeArea(area, minAttr, maxAttr, avgAttr, sumAttr, r);
                                        labels[area] = labels[neighbor];
                                        updated = true;
                                        removed = true;
                                        break;
                                    }

                                }
                                if (!removed) {
                                    region.removeArea(area, minAttr, maxAttr, avgAttr, sumAttr, r);
                                    labels[area] = -4;
                                    updated = true;
                                    removed = true;
                                }
                            }
                        }
                    }

                }
            }
            //checkLabels(labels, regionList);
            List<Integer> idToBeRemoved = new ArrayList<Integer>();
            for (Map.Entry<Integer, Region> regionEntry : regionList.entrySet()) {
                if (!regionEntry.getValue().satisfiable()) {
                    //idToBeRemoved.add(regionEntry.getValue().getId());
                    idToBeRemoved.add(regionEntry.getKey());
                    if (debug) {


                        System.out.println("Id to be removed:" + regionEntry.getValue().getId());
                        System.out.println("Min:" + regionEntry.getValue().getMin());
                        System.out.println("Max:" + regionEntry.getValue().getMax());
                        System.out.println("Avg:" + regionEntry.getValue().getAverage());
                        System.out.println("Sum:" + regionEntry.getValue().getSum());

                        System.out.println("Count:" + regionEntry.getValue().getCount());
                        System.out.println("Areas: " + regionEntry.getValue().getAreaList());
                        System.out.println("Satisfiable:" + regionEntry.getValue().satisfiable());
                    }
                    //System.out.print(regionEntry.getKey() + " ");

                }
            }
            //System.out.println("Not satisfiable region size:" + idToBeRemoved.size());
            //System.out.println();
            List<Integer> idMerged = new ArrayList<Integer>();
            updated = true;
            //checkLabels(labels, regionList);
            if (debug) {
                System.out.println("P in the middle of merge: " + regionList.size());
                System.out.println(idToBeRemoved);
            }

            while (updated) {

                updated = false;
                for (Integer region : idToBeRemoved) {
                    if (!idMerged.contains(region) && !regionList.get(region).satisfiable() && (regionList.get(region).getCount() < countLowerBound || regionList.get(region).getSum() < sumLowerBound)) {
                        List<Integer> regionMerged = new ArrayList<Integer>();
                        regionMerged.add(region);
                        //idMerged.add(region);//之前为啥注释掉了?idMerged表示因为merge消失的
                        List<Integer> neighborRegions = new ArrayList<>(regionList.get(region).getRegionNeighborSet(labels));

                        Region newRegion = regionList.get(region);

                        Collections.shuffle(neighborRegions);

                        for (Integer neighborRegion : neighborRegions) {
                            if (neighborRegion <= 0) {
                                continue;
                            }
                            if(regionList.get(neighborRegion).getSum() >= sumLowerBound && regionList.get(neighborRegion).getCount() >= countLowerBound){
                                continue;
                            }
                            if (sumUpperBound - regionList.get(neighborRegion).getSum() < newRegion.getSum() || countUpperBound - regionList.get(neighborRegion).getCount() < newRegion.getCount()) {
                                continue;
                            }
                            regionMerged.add(neighborRegion);
                            idMerged.add(neighborRegion);
                            //System.out.println("Region to be merged: " + neighborRegion);
                            if (!regionList.containsKey(region)) {
                                System.out.println(neighborRegion + "does not exist in regionList");
                                System.exit(123);
                            }
                            newRegion = newRegion.mergeWith(regionList.get(neighborRegion), minAttr, maxAttr, avgAttr, sumAttr, r);
                            labels = newRegion.updateId(region, labels);
                            if (newRegion.satisfiable()) {
                                break;
                            }
                        }
                        // Whether a feasible region will be merged and the new region is not feasible and needs to be removed?
                        labels = newRegion.updateId(region, labels);
                        if (regionMerged.size() > 1)
                            updated = true;
                        for (Integer regionRemoved : regionMerged) {
                            regionList.remove(regionRemoved);
                            //System.out.print(regionRemoved + " ");
                        }
                        //System.out.println(region + " added to RegionList");

                        regionList.put(region, newRegion);
                        if (!regionList.containsKey(region)) {
                            System.out.println(region + "???");
                            System.exit(124);
                        }
                    }
                }
            }
            //Assign Enclave:
            for (Integer region : idToBeRemoved) {
                if (!idMerged.contains(region) && !regionList.get(region).satisfiable() && (regionList.get(region).getCount() < countLowerBound || regionList.get(region).getSum() < sumLowerBound)) {
                    List<Integer> regionMerged = new ArrayList<Integer>();
                    regionMerged.add(region);

                    List<Integer> neighborRegions = new ArrayList<>(regionList.get(region).getRegionNeighborSet(labels));

                    Region newRegion = regionList.get(region);

                    Collections.shuffle(neighborRegions);

                    for (Integer neighborRegion : neighborRegions) {
                        if (neighborRegion <= 0) {
                            continue;
                        }

                        if (sumUpperBound - regionList.get(neighborRegion).getSum() < newRegion.getSum() || countUpperBound - regionList.get(neighborRegion).getCount() < newRegion.getCount()) {
                            continue;
                        }
                        regionMerged.add(neighborRegion);
                        idMerged.add(neighborRegion);
                        //System.out.println("Region to be merged: " + neighborRegion);
                        if (!regionList.containsKey(region)) {
                            System.out.println(neighborRegion + "does not exist in regionList");
                            System.exit(123);
                        }
                        newRegion = newRegion.mergeWith(regionList.get(neighborRegion), minAttr, maxAttr, avgAttr, sumAttr, r);
                        labels = newRegion.updateId(region, labels);
                        if (newRegion.satisfiable()) {
                            break;
                        }
                    }
                    // Whether a feasible region will be merged and the new region is not feasible and needs to be removed?
                    labels = newRegion.updateId(region, labels);
                    if (regionMerged.size() > 1)
                        updated = true;
                    for (Integer regionRemoved : regionMerged) {
                        regionList.remove(regionRemoved);
                        //System.out.print(regionRemoved + " ");
                    }
                    //System.out.println(region + " added to RegionList");

                    regionList.put(region, newRegion);
                    if (!regionList.containsKey(region)) {
                        System.out.println(region + "???");
                        System.exit(124);
                    }
                }
            }



            if (debug) {
                System.out.println("P returned after merge: " + regionList.size());
                for (int i = 0; i < labels.length; i++) {
                    System.out.print(labels[i] + " ");
                }
                for (Map.Entry<Integer, Region> e : regionList.entrySet()) {
                    System.out.println();
                    System.out.println("Id:" + e.getValue().getId());
                    System.out.println("Min:" + e.getValue().getMin());
                    System.out.println("Max:" + e.getValue().getMax());
                    System.out.println("Avg:" + e.getValue().getAverage());
                    System.out.println("Sum:" + e.getValue().getSum());

                    System.out.println("Count:" + e.getValue().getCount());
                    System.out.println("Areas: " + e.getValue().getAreaList());
                    System.out.println("Satisfiable:" + e.getValue().satisfiable());

                }
            }
            if (debug) {
                System.out.println("IdToBeRemoved: " + idToBeRemoved);
                System.out.println("IdMerged: " + idMerged);
                System.out.println("P returned before removal: " + regionList.size());
            }

            for (Integer id : idToBeRemoved) {
                if (!idMerged.contains(id) && !regionList.get(id).satisfiable()) {
                    regionList.get(id).updateId(-3, labels);
                    regionList.remove(id);
                }


            }
            if(debug){
                System.out.println("P returned after removal: " + regionList.size());
                System.out.println("-------------------------------------------------------------------------------");
            }


            /*for(Map.Entry<Integer, RegionNew> entry: regionList.entrySet()){
                System.out.println( entry.getValue().getSum() + " " + entry.getValue().getCount());
            }*/

            int unAssignedCount = 0;
            for(int i = 0; i < labels.length; i++){
                if (labels[i] <= 0)
                    unAssignedCount ++;
            }
            //System.out.println("Distance for this regionList" + calculateWithinRegionDistance(regionList, distanceMatrix));
            if(regionList.size() > max_p || (regionList.size() == max_p && unAssignedCount < min_unAssigned)){
                max_p = regionList.size();
                min_unAssigned = unAssignedCount;
                bestCollection = new RegionCollection(regionList.size(), labels, regionList);
            }

        }
        Map<Integer, Region> rcn = bestCollection.getRegionList();
        if(debug){
            for(Region rn: rcn.values()){
                if(!rn.satisfiable()){
                    System.out.println("Region " + rn.getId() + " not satisfiable!");
                    System.exit(125);
                }
            }
        }


        return bestCollection;
    }

    /*public static RegionCollectionNew construction_phase_average(ArrayList<Integer> threshold_attr, ArrayList dis_attr, int max_it, SpatialGrid r, ArrayList<Integer> idList, double lowerBound, double upperBound, ArrayList<Integer> seedAreas){
        //Map<Integer, Integer> labels_list = new HashMap<Integer, Integer>();

        for(int i = 0; i < idList.size(); i++){
            labels_list.put(idList.get(i), 0);
        }

        //Distance Matrix and Pairwise dist


        //}








    }*/

    public static void checkLabels(int[] labels, Map<Integer, Region> regionList){
        boolean consistent = true;
        for(int i = 0; i < labels.length; i++){
            if (labels[i] > 0){
                if(!regionList.get(labels[i]).getAreaList().contains(i)){
                    System.out.println("Area " + i + " not in region " + labels[i]);
                    consistent = false;
                }
            }
        }
        for(Map.Entry<Integer, Region> mapEntry: regionList.entrySet()){
            Region region = mapEntry.getValue();
            for(Integer area: region.getAreaList()){
                if(labels[area] != region.getId()){
                    System.out.println("Region " + region.getId() + " contains area " + area + " but labeled as in region " + labels[area]);
                    consistent = false;
                }
            }
        }
        if(!consistent){
            System.out.println("Label checking failed!");
            System.exit(126);
        }else{
            System.out.println("Pass label checking!");
        }


    }




}
