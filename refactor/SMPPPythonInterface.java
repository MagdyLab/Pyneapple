package edu.ucr.cs.pyneapple.regionalization;

//import com.opencsv.CSVWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SMPPPythonInterface implements RegionalizationMethod{
    private Partition bestP;
    private int maxItr = 100;
    //long threshold = 20000;
    private int lengthTabu = 10;
    private double t = 1;
    private int convSA = 90;
    private double alpha = 0.9;
    //private int dataset = 14;
    private int nRows = 2;
    private int nColumns = 2;
    private int cores = 4;
    private int random = 0;
    private ArrayList<Geometry> geometries;
    public void setGeometry(ArrayList<Geometry> geometries){
        this.geometries = geometries;
    }
    public void setGeometryStrings(ArrayList<String> geometryStrings) throws ParseException {
        this.geometries = stringListToGeometryList(geometryStrings);
    }
    public void setGeometries(ArrayList<Geometry> geometries) {
        this.geometries = geometries;
    }
    public void setNRows(int nRows){
        this.nRows = nRows;
    }

    public void setT(double t) {
        this.t = t;
    }

    public void setNColumns(int nColumns) {
        this.nColumns = nColumns;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public void setConvSA(int convSA) {
        this.convSA = convSA;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setLengthTabu(int lengthTabu) {
        this.lengthTabu = lengthTabu;
    }

    public void setMaxItr(int maxItr) {
        this.maxItr = maxItr;
    }

    public void setRandom(int random) {
        this.random = random;
    }
    @Override
    public int getP(){
        return bestP.getP();
    }
    public int[] getRegionList(){
        int noOfArea = bestP.getAreasWithRegions().size();
        int[] regionList = new int[noOfArea];
        for (Integer key : bestP.getAreasWithRegions().keySet()){
            regionList[key] = bestP.getAreasWithRegions().get(key);
        }
        return regionList;
    }
    @Override
    public void execute_regionalization(Map<Integer, Set<Integer>> neighbor, ArrayList<Long> disAttr, ArrayList<Long> sumAttr, Long threshold) throws ParseException, IOException {


        ArrayList<Geometry> polygons = new ArrayList<>();
        polygons = this.geometries; // If not initialzied throw exception
        ArrayList<Long> population = disAttr;
        // spatially extensive attribute col#8 i ALAND
        ArrayList<Long> household =sumAttr;
        ArrayList<Integer> areas = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            areas.add(i);
        }

        System.out.println("Number of areas: " + areas.size());



        // ********************************************************************
        // **** FINDING THE NEIGHBORS FOR EACH AREA ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Finding neighbors . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startFindingNeighbors = System.currentTimeMillis();

        ArrayList<List> neighbors = SMPP.createNeighborsList(polygons);

        long endFindingNeighbors = System.currentTimeMillis();
        float totalFindingNeighbors = (endFindingNeighbors - startFindingNeighbors) / 1000F;
        System.out.println("Total time for Finding the Neighbors is: " + totalFindingNeighbors + "\n\n");

        // print neighbors list
        //printNeighborsList(neighbors);


        // ********************************************************************
        // **** CREATING DATA PARTITIONS ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Creating data partitions . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startPartitioning = System.currentTimeMillis();

        ArrayList<DataPartition> dataPartitions = SMPP.partitionData(nColumns, nRows, polygons, areas, neighbors);

        long endPartitioning = System.currentTimeMillis();
        float totalPartitioning = (endPartitioning - startPartitioning) / 1000F;
        System.out.println("Total time for Creating Data Partitions is: " + totalPartitioning + "\n\n");

        // writing the final data partitions to a CSV file
        //writingDataPartitionsToFile(dataPartitions, polygons);


        // ********************************************************************
        // **** FINDING THE NEIGHBORS WITHIN PARTITIONS ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Finding neighbors within each partition . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startFindingNeighbors1 = System.currentTimeMillis();

        HashMap<Integer, HashMap<Integer, List>> partitionsNeighbors = SMPP.createNeighborsList(neighbors, dataPartitions);

        long endFindingNeighbors1 = System.currentTimeMillis();
        float totalFindingNeighbors1 = (endFindingNeighbors1 - startFindingNeighbors1) / 1000F;
        System.out.println("Total time for Finding the Neighbors Within Partitions is: " + totalFindingNeighbors1 + "\n");

        // print partitions neighbors list
        //printPartitionsNeighborsList(partitionsNeighbors);

        float total = totalPartitioning + totalFindingNeighbors1;
        System.out.println("Total time for Data Partitioning Phase: " + total + "\n\n");


        // ********************************************************************
        // **** CONSTRUCTION PHASE ****
        // ********************************************************************

        int maxP = 0;
        //Partition bestP;
        ArrayList<Partition> partitionsBeforeEnclaves = new ArrayList<>();
        Partition bestFeasiblePartition = new Partition();

        // **** GROW REGIONS ****

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Growing the regions . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startGrowRegions = System.currentTimeMillis();

        ExecutorService growRegionsExecutor = Executors.newFixedThreadPool(cores);

        List<GrowRegionsThread> growThreads = new ArrayList<>();
        for (int itr = 0; itr < maxItr; itr++) {

            GrowRegionsThread thread = new GrowRegionsThread(threshold, itr, partitionsNeighbors, household, population, polygons, dataPartitions, cores, random);
            growThreads.add(thread);
        }

        List<Future<Partition>> growResult = null;
        try {
            growResult = growRegionsExecutor.invokeAll(growThreads);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        growRegionsExecutor.shutdown();

        for (int i = 0; i < growResult.size(); i++) {

            Future<Partition> future = growResult.get(i);

            Partition partition = new Partition();

            try {
                partition = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            int p = partition.getRegions().size();

            if (p > maxP) {

                HashMap<Integer, Integer> areasWithRegions = SMPP.createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.clear();
                partitionsBeforeEnclaves.add(partition);
                maxP = p;

            } else if (p == maxP) {

                HashMap<Integer, Integer> areasWithRegions = SMPP.createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.add(partition);

            } else if (p < maxP) {

                // pass
            }
        }

        System.out.println("MaxP: " + maxP);
        System.out.println("Number of partitions after growing the regions using threads: " + partitionsBeforeEnclaves.size());

        long endGrowRegions = System.currentTimeMillis();
        float totalGrowRegions = (endGrowRegions - startGrowRegions) / 1000F;
        System.out.println("Total time for Growing the Regions using threads is : " + totalGrowRegions + "\n\n");

        // writing the grow regions phase output to a CSV file
        //writingPartitionsToFile(partitionsBeforeEnclaves, polygons, "");


        // **** ENCLAVES ASSIGNMENT ****

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Enclaves Assignment . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startAssignEnclaves = System.currentTimeMillis();

        /*long minHet = Long.MAX_VALUE;

        ExecutorService enclavesAssignmentExecutor = Executors.newFixedThreadPool(cores);

        List<EnclavesAssignmentThread> enclavesThreads = new ArrayList<>();
        for (int i = 0; i < partitionsBeforeEnclaves.size(); i++) {

            Partition currentPartition = partitionsBeforeEnclaves.get(i);
            EnclavesAssignmentThread thread = new EnclavesAssignmentThread(population, household, polygons, neighbors, currentPartition, i);
            enclavesThreads.add(thread);
            //System.out.println(currentPartition.getPartitionID() + " before: " +currentPartition.getAssignedAreas().size() + " -- " + currentPartition.getEnclaves().size());
        }

        List<Future<Partition>> enclavesResult = null;
        try {
            enclavesResult = enclavesAssignmentExecutor.invokeAll(enclavesThreads);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        enclavesAssignmentExecutor.shutdown();

        for (int i = 0; i < enclavesResult.size(); i++) {

            Future<Partition> future = enclavesResult.get(i);

            Partition feasiblePartition = new Partition();

            try {
                feasiblePartition = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            //System.out.println(feasiblePartition.getPartitionID() + " after: " +feasiblePartition.getAssignedAreas().size() + " -- " + feasiblePartition.getEnclaves().size());

            feasiblePartitions.add(feasiblePartition);
            long heterogeneity = calculatePartitionH(feasiblePartition, population);

            if (heterogeneity < minHet) {
                minHet = heterogeneity;
                bestFeasiblePartition = feasiblePartition;
            }
        }*/

        long minHet = Long.MAX_VALUE;

        for (int i = 0; i < partitionsBeforeEnclaves.size(); i++) {

            Partition currentPartition = partitionsBeforeEnclaves.get(i);
            Partition feasiblePartition = SMPP.enclavesAssignment(population, household, neighbors, currentPartition);
            long heterogeneity = SMPP.calculatePartitionH(feasiblePartition, population);
            feasiblePartition.setDissimilarity(heterogeneity);
            if (heterogeneity < minHet) {
                bestFeasiblePartition = feasiblePartition;
                minHet = heterogeneity;
            }
            //feasiblePartition.setDissimilarity(heterogeneity);
            //feasiblePartitions.add(feasiblePartition);
        }

        // writing enclaves assignment output to a cvs file
        //writingPartitionsToFile(feasiblePartitions, polygons, "/Users/hessah/Desktop/Output/enclaves");

        long endAssignEnclaves = System.currentTimeMillis();
        float totalAssignEnclaves = (endAssignEnclaves - startAssignEnclaves) / 1000F;
        System.out.println("Total time for Assign Enclaves is : " + totalAssignEnclaves + "\n\n");


        // ********************************************************************
        // **** LOCAL SEARCH PHASE ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Local Search . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startSearch = System.currentTimeMillis();

        long improvement;
        long oldHeterogeneity;

        oldHeterogeneity = bestFeasiblePartition.getDissimilarity();
        bestP = SMPP.modifiedSA(lengthTabu, convSA, alpha, t, threshold, bestFeasiblePartition, household, population, neighbors);

        long pH = bestP.getDissimilarity();

        long endSearch = System.currentTimeMillis();
        float totalSearch = (endSearch - startSearch) / 1000F;
        System.out.println("Total time for Local Search is : " + totalSearch);

        improvement = oldHeterogeneity - pH;
        float percentage = ((float)improvement/(float)oldHeterogeneity);
        System.out.println("Heterogeneity before local search: " + oldHeterogeneity);
        System.out.println("Heterogeneity after local search: " + pH);
        System.out.println("Improvement in heterogeneity: " + improvement);
        System.out.println("Percentage of improvement: " + percentage);
        System.out.println("Max-p: " + maxP);

        float totalTime = totalFindingNeighbors + totalFindingNeighbors1 + totalPartitioning + totalGrowRegions + totalAssignEnclaves + totalSearch;
        System.out.println("\nTotal time is : " + (totalTime));



        //csv.writeNext(row);

        //csv.close();
        //return maxP;
        bestP.setP(maxP);

    }

    public static Geometry stringToGeometry(String wktString) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geom = reader.read(wktString);
        return geom;
    }
    public static ArrayList stringListToGeometryList(ArrayList<String> wktStrings) throws ParseException {
        ArrayList<Geometry> polygons = new ArrayList<>();
        System.out.println(wktStrings.size());
        for (String wktString:wktStrings) {
            polygons.add(stringToGeometry(wktString));
        }
        return polygons;
    }


}
