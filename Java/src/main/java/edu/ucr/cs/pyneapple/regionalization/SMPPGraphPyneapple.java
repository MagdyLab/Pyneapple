package edu.ucr.cs.pyneapple.regionalization;

import edu.ucr.cs.pyneapple.utils.SMPPUtils.*;


import java.nio.charset.Charset;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import java.util.*;
import java.util.concurrent.*;
import java.lang.Math;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.locks.ReentrantLock;

import com.opencsv.CSVWriter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureCollection;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.*;

import org.locationtech.jts.io.ParseException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;















class GrowRegionsGraphThread implements Callable<Partition> {

    double threshold;
    int itr;
    HashMap<Integer, HashMap<Integer, List>> neighbors;
    ArrayList<Double> household;
    ArrayList<Double> population;
    ArrayList<Geometry> polygons;
    ArrayList<DataPartition> dataPartitions;
    int cores;
    int random;

    /**
     * constructor for GrowRegionsThread
     * @param threshold threshold
     * @param itr maximum number of iterations for growing regions
     * @param neighbors neighbors graph
     * @param household extensive attribute
     * @param population dissimilarity attribute
     * @param dataPartitions data partitions
     * @param cores number of cores
     * @param random flag
     */

    public GrowRegionsGraphThread(double threshold,
                             int itr,
                             HashMap<Integer, HashMap<Integer, List>> neighbors,
                             ArrayList<Double> household,
                             ArrayList<Double> population,
                             ArrayList<DataPartition> dataPartitions,
                             int cores,
                             int random) {

        this.threshold = threshold;
        this.itr = itr;
        this.household = household;
        this.population = population;
        this.neighbors = neighbors;
        this.dataPartitions = dataPartitions;
        this.cores = cores;
        this.random = random;

    }

    /**
     * grows regions for each DataPartition in parallel
     * @return partition
     * @throws IOException
     */

    public Partition call() throws IOException {


        long start = System.currentTimeMillis();

        ExecutorService growPartitionExecutor =  Executors.newFixedThreadPool(1);
        List<GrowPartitionGraphThread> growThreads = new ArrayList<>();

        for (int i = 0; i < this.dataPartitions.size(); i++) {

            GrowPartitionGraphThread thread = new GrowPartitionGraphThread(this.threshold,
                    neighbors.get(dataPartitions.get(i).getID()), household, population, dataPartitions.get(i), random);
            growThreads.add(thread);
        }

        List<Future<Partition>> growResult = null;
        try {
            growResult = growPartitionExecutor.invokeAll(growThreads);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        growPartitionExecutor.shutdown();

        long end = System.currentTimeMillis();
        //System.out.println("iteration " + this.itr + " thread: " + (end - start));

        Partition partition = new Partition(this.itr);

        int ID = 1;

        for (int i = 0; i < growResult.size(); i++) {

            Future<Partition> future = growResult.get(i);

            Partition partialPartition = new Partition();

            try {
                partialPartition = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            // writing partial partitions and enclaves to a csv file
            /*FileWriter outputFile1 = new FileWriter(new File(
                    "/Users/hessah/Desktop/Research/Output/2GrowRegionsThreads/dataPartition" + this.itr + i + ".csv"));
            CSVWriter csv1 = new CSVWriter(outputFile1);
            String[] header1 = {"Area ID", "Area Polygon in WKT"};
            csv1.writeNext(header1);

            ArrayList<Integer> partitionAreas = partialPartition.getAssignedAreas();

            for (int ii = 0; ii < partitionAreas.size(); ii++) {

                String areaID = partitionAreas.get(ii).toString();

                String[] row = {areaID, polygons.get(partitionAreas.get(ii)).toText()};
                csv1.writeNext(row);
            }
            csv1.close();

            FileWriter outputFile2 = new FileWriter(new File(
                    "/Users/hessah/Desktop/Research/Output/2GrowRegionsThreads/enclaves" + this.itr + i + ".csv"));
            CSVWriter csv2 = new CSVWriter(outputFile2);
            String[] header2 = {"Area ID", "Area Polygon in WKT"};
            csv2.writeNext(header2);

            ArrayList<Integer> enclaves = partialPartition.getEnclaves();

            for (int ii = 0; ii < enclaves.size(); ii++) {

                String areaID = enclaves.get(ii).toString();

                String[] row = {areaID, polygons.get(enclaves.get(ii)).toText()};
                csv2.writeNext(row);
            }
            csv2.close();*/


            for (Integer area : partialPartition.getAssignedAreas()) {
                partition.getAssignedAreas().add(area);
            }

            for (Integer enclave : partialPartition.getEnclaves()) {
                partition.getEnclaves().add(enclave);
            }

            for (Integer regionID : partialPartition.getRegions().keySet()) {
                Region region = new Region(partialPartition.getRegions().get(regionID));
                region.setID(ID);
                partition.getRegions().put(ID, region);
                ID++;
            }

        }

        /*
        System.out.println("Thread " + itr + "-assigned areas: " + partition.getAssignedAreas().size());
        System.out.println("Thread " + itr + "-enclaves: " + partition.getEnclaves().size());
        System.out.println("Thread " + itr + "-regions: " + partition.getRegions().size());
        int total = partition.getAssignedAreas().size() + partition.getEnclaves().size();
        System.out.println("Thread " + itr + "-total: " + total);
        */

        return partition;
    }
}


class GrowPartitionGraphThread implements Callable<Partition> {

    /**
     * Random object
     */
    public static Random rand = SMPPGraphPyneapple.rand;

    double threshold;
    HashMap<Integer, List> neighbors;
    ArrayList<Double> household;
    ArrayList<Double> population;
    DataPartition dataPartition;
    int random;

    /**
     * constructor for GrowPartitionThread
     * @param threshold threshold
     * @param neighbors neighbors graph
     * @param household extensive attribute
     * @param population dissimilarity attribute
     * @param dataPartition data partition
     * @param random flag
     */
    public GrowPartitionGraphThread(double threshold,
                               HashMap<Integer, List> neighbors,
                               ArrayList<Double> household,
                               ArrayList<Double> population,
                               DataPartition dataPartition,
                               int random) {

        this.threshold = threshold;
        this.household = household;
        this.population = population;
        this.neighbors = neighbors;
        this.dataPartition = new DataPartition(dataPartition);
        this.random = random;

    }

    /**
     * grows regions for a DataPartition
     * @return data partition
     */
    public Partition call() {

        //System.out.println("partition thread: " + System.currentTimeMillis());

        Partition partialPartition = growRegions(this.dataPartition);
        return partialPartition;
    }

    /*public Partition growRegions(DataPartition dataPartition) {

        ArrayList<Integer> enclaves = new ArrayList<>();
        ArrayList<Integer> assignedAreas = new ArrayList<>();
        ArrayList<Integer> unassignedAreas = new ArrayList<>(dataPartition.getAreas());

        Partition partition = new Partition();
        int regionID = 0;

        while (!unassignedAreas.isEmpty()) {

            int seedArea = selectRandomArea(unassignedAreas);
            unassignedAreas.remove((Integer) seedArea);
            assignedAreas.add(seedArea);

            long spatiallyExtensiveAttribute = this.household.get(seedArea);

            if (spatiallyExtensiveAttribute >= this.threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);
                region.setRegionalThreshold(spatiallyExtensiveAttribute);
                partition.addRegion(regionID, region);

            } // end if

            else if (spatiallyExtensiveAttribute < this.threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);

                ArrayList<Integer> seedAreaNeighbors = new ArrayList<>(this.neighbors.get(seedArea));
                seedAreaNeighbors.removeAll(assignedAreas);

                int feasible = 1;
                long regionalThreshold = spatiallyExtensiveAttribute;

                while (regionalThreshold < this.threshold) {

                    if (!seedAreaNeighbors.isEmpty()) {

                        ArrayList<Integer> regionAreas = region.getAreas();
                        int similarArea = getSimilarArea(seedAreaNeighbors, regionAreas);
                        //int similarArea = selectRandomArea(seedAreaNeighbors);

                        region.addArea(similarArea);

                        List<Integer> similarAreaNeighbors = new ArrayList<>(this.neighbors.get(similarArea));
                        similarAreaNeighbors.removeAll(assignedAreas);
                        seedAreaNeighbors.remove((Integer)similarArea);
                        similarAreaNeighbors.removeAll(seedAreaNeighbors);
                        seedAreaNeighbors.addAll(similarAreaNeighbors);

                        regionalThreshold += this.household.get(similarArea);
                        unassignedAreas.remove((Integer)similarArea);
                        assignedAreas.add(similarArea);

                    } // end if

                    if (seedAreaNeighbors.isEmpty() && regionalThreshold < this.threshold) {

                        //ArrayList<Integer> regionAreas = new ArrayList<>(region.getAreas());
                        //regionAreas.removeAll(enclaves);
                        enclaves.addAll(region.getAreas());
                        assignedAreas.removeAll(region.getAreas());
                        feasible = 0;
                        break;
                    } // end if
                } // end while

                if (feasible == 1) {
                    region.setRegionalThreshold(regionalThreshold);
                    partition.addRegion(regionID, region);
                }
            } // end else if
            regionID++;
        } // end while

        partition.addEnclaves(enclaves);
        partition.addAssignedAreas(assignedAreas);

        return partition;
    }*/

    /**
     * grow regions
     * @param dataPartition data partition
     * @return partition
     */
    public Partition growRegions(DataPartition dataPartition) {

        ArrayList<Integer> enclaves = new ArrayList<>();
        HashSet<Integer> assignedAreas = new HashSet<>();
        ArrayList<Integer> unassignedAreas = new ArrayList<>(dataPartition.getAreas());

        Partition partition = new Partition();
        int regionID = 0;

        while (!unassignedAreas.isEmpty()) {

            int seedArea = selectRandomArea(unassignedAreas);

            unassignedAreas.remove((Integer)seedArea);
            assignedAreas.add(seedArea);

            double spatiallyExtensiveAttribute = this.household.get(seedArea);

            if (spatiallyExtensiveAttribute >= this.threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);
                region.setRegionalThreshold(spatiallyExtensiveAttribute);
                partition.addRegion(regionID, region);

            } // end if

            else if (spatiallyExtensiveAttribute < this.threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);

                HashSet<Integer> seedAreaNeighbors = new HashSet<>();
                List<Integer> seedNeighbors = neighbors.get(seedArea);
                for (int neighbor : seedNeighbors) {
                    if (!assignedAreas.contains(neighbor))
                        seedAreaNeighbors.add(neighbor);
                }

                int feasible = 1;
                double regionalThreshold = spatiallyExtensiveAttribute;

                while (regionalThreshold < this.threshold) {

                    if (!seedAreaNeighbors.isEmpty()) {

                        ArrayList<Integer> regionAreas = region.getAreas();
                        int similarArea;
                        if (this.random == 0)
                            similarArea = getSimilarArea(new ArrayList<>(seedAreaNeighbors), regionAreas);
                        else
                            similarArea = selectRandomArea(new ArrayList<>(seedAreaNeighbors));

                        region.addArea(similarArea);

                        List<Integer> similarAreaNeighbors = neighbors.get(similarArea);
                        seedAreaNeighbors.remove((Integer)similarArea);
                        for (int area : similarAreaNeighbors) {
                            if (!assignedAreas.contains(area))
                                seedAreaNeighbors.add(area);
                        }

                        regionalThreshold += this.household.get(similarArea);
                        unassignedAreas.remove((Integer)similarArea);
                        assignedAreas.add(similarArea);

                    } // end if

                    if (seedAreaNeighbors.isEmpty() && regionalThreshold < this.threshold) {

                        //ArrayList<Integer> regionAreas = new ArrayList<>(region.getAreas());
                        //regionAreas.removeAll(enclaves);
                        enclaves.addAll(region.getAreas());
                        assignedAreas.removeAll(region.getAreas());
                        feasible = 0;
                        break;
                    } // end if
                } // end while

                if (feasible == 1) {
                    region.setRegionalThreshold(regionalThreshold);
                    partition.addRegion(regionID, region);
                }
            } // end else if
            regionID++;
        } // end while

        partition.addEnclaves(enclaves);
        partition.addAssignedAreas(new ArrayList<>(assignedAreas));

        return partition;
    }

    public int selectRandomArea(ArrayList<Integer> areas) {

        return areas.get(this.rand.nextInt(areas.size()));
    }

    public Integer getSimilarArea(List<Integer> unassignedNeighbors,
                                  ArrayList<Integer> regionAreas) {

        double minDissimilarity = Long.MAX_VALUE;
        Integer similarArea = null;

        for (int i = 0; i < unassignedNeighbors.size(); i++) {

            int area = unassignedNeighbors.get(i);
            double areaPopulation = this.population.get(area);

            double dissimilarity = calculateDissimilarity(areaPopulation, regionAreas);

            if (dissimilarity < minDissimilarity) {

                minDissimilarity = dissimilarity;
                similarArea = unassignedNeighbors.get(i);

            }
        }
        return similarArea;
    }

    /**
     * calculates the dissimilarity
     * @param areaPopulation dissimilarity attribute
     * @param regionAreas region areas
     * @return dissimilarity
     */
    public double calculateDissimilarity(double areaPopulation,
                                         ArrayList<Integer> regionAreas) {

        double dissimilarity = 0;

        for (int i = 0; i < regionAreas.size(); i++) {

            double regionAreaPopulation = this.population.get(regionAreas.get(i));

            dissimilarity += Math.abs(areaPopulation - regionAreaPopulation);
        }
        return dissimilarity;
    }
} // end GrowPartitionThread




/**
 * executes the SMP algorithm
 */
public class SMPPGraphPyneapple implements RegionalizationMethod{


    /**
     * default constructor for SMPPGraphPyneapple class
     */
    public SMPPGraphPyneapple() {

    }
    Partition bestP;
    /**
     * Random object
     */
    public static Random rand = new Random();


    /**
     * partitions the dataset
     * @param nColumns number of columns
     * @param nRows number of rows
     * @param areasIDs list pf areas' IDs
     * @param neighbors neighborhood graph
     * @return data partitions
     */
    public static ArrayList<DataPartition> coarsening(int nColumns,
                                                      int nRows,
                                                      ArrayList<Integer> areasIDs,
                                                      Map<Integer, Set<Integer>> neighbors) {

        ArrayList<DataPartition> partitions = new ArrayList<>();

        // construct graph
        Graph graph = new Graph(0);
        for (int ID : areasIDs) {
            Area area = new Area(ID);
            //area.set_polygon(polygon);
            area.add_original_subarea_ID(ID);
            area.set_neighbors(new ArrayList<>(neighbors.get(ID)));

            Set<Integer> area_neighbors = neighbors.get(ID);
            HashMap<Integer, Integer> neighbors_degrees = new HashMap<>();
            for (Integer neighbor : area_neighbors)
            {
                neighbors_degrees.put(neighbor, 1);
            }
            area.set_neighbors2(neighbors_degrees);

            graph.add_area(ID, area);
        }



        ArrayList<Graph> coarsest_graphs = new ArrayList<>();
        HashMap<Integer,Area> areas = graph.copy_graph_areas();
        int current_graph_ID = 1;
        int new_starting_index = 0;
        ArrayList<Integer> previous_graph_unmatched = new ArrayList<>(); // areas not matched in the previous graph


        while (areas.size() > (nColumns * nRows))
        //for (int i = 0; i < 2; i++)
        {

            new_starting_index = new_starting_index + areas.size();
            int area_ID = new_starting_index;
            ArrayList<Integer> unmatched = new ArrayList<>(areas.keySet());
            ArrayList<Integer> current_graph_unmatched = new ArrayList<>();


            while (!unmatched.isEmpty()) {

                if (areas.size() == (nColumns * nRows))
                    break;
                Area matched_area = new Area(area_ID);

                // select area to match
                int area;
                if (!previous_graph_unmatched.isEmpty()) {
                    area = previous_graph_unmatched.get(previous_graph_unmatched.size()-1);
                    previous_graph_unmatched.remove((Integer) area);
                }
                else
                    area = selectRandomArea(unmatched);

                int matched_neighbor = find_connected_area(areas, unmatched, area);
                //int matched_neighbor = find_any_area(unmatched, areas.get(area).get_neighbors())


                // set subareas_IDs for the new matched area
                ArrayList<Integer> subareas_IDs = new ArrayList<>();
                subareas_IDs.add(area);
                if (matched_neighbor != -1)
                    subareas_IDs.add(matched_neighbor);
                matched_area.set_subareas_IDs(subareas_IDs);

                // set original_subareas_IDs for the new matched area
                ArrayList<Integer> original_subareas_IDs = new ArrayList<>();
                original_subareas_IDs.addAll(areas.get(area).get_original_subareas_IDs());
                if (matched_neighbor != -1)
                    original_subareas_IDs.addAll(areas.get(matched_neighbor).get_original_subareas_IDs());
                matched_area.get_original_subareas_IDs().addAll(original_subareas_IDs);

                // set subareas (contains areas as Area objects) for the new matched area
                /*
                ArrayList<Area> subareas = new ArrayList<>();
                for (Integer subarea : subareas_IDs)
                    subareas.add(areas.get(subarea));
                matched_area.set_subareas(subareas);
                */

                // set the degree for the new matched area
                int degree = areas.get(area).get_degree();
                if (matched_neighbor != -1)
                    degree = degree + areas.get(matched_neighbor).get_degree();
                matched_area.set_degree(degree);


                // set the neighbors list for the new matched area
                if (matched_neighbor != -1)
                {
                    //----
                    ArrayList<Integer> common_neighbors = new ArrayList<>(areas.get(area).get_neighbors());
                    common_neighbors.retainAll(areas.get(matched_neighbor).get_neighbors());
                    common_neighbors.removeAll(subareas_IDs);

                    HashMap<Integer, Integer> matched_area_neighbors = new HashMap<>();
                    for (int common_neighbor : common_neighbors) {
                        int new_degree = areas.get(area).get_neighbors2().get(common_neighbor)
                                + areas.get(matched_neighbor).get_neighbors2().get(common_neighbor);
                        matched_area_neighbors.put(common_neighbor, new_degree);
                    }
                    //----



                    HashSet<Integer> merged_neighbors = new HashSet<>(areas.get(area).get_neighbors());
                    merged_neighbors.addAll(areas.get(matched_neighbor).get_neighbors());
                    merged_neighbors.removeAll(subareas_IDs);
                    matched_area.set_neighbors(new ArrayList<>(merged_neighbors));



                    //----
                    for (int neighbor : areas.get(area).get_neighbors())
                    {
                        if (!common_neighbors.contains(neighbor))
                            matched_area_neighbors.put(neighbor, areas.get(area).get_neighbors2().get(neighbor));
                    }

                    for (int neighbor : areas.get(matched_neighbor).get_neighbors())
                    {
                        if (!common_neighbors.contains(neighbor))
                            matched_area_neighbors.put(neighbor, areas.get(matched_neighbor).get_neighbors2().get(neighbor));
                    }

                    matched_area.set_neighbors2(matched_area_neighbors);
                    //----


                }
                else
                {
                    matched_area.set_neighbors(new ArrayList<>(areas.get(area).get_neighbors()));

                    //----
                    matched_area.set_neighbors2(new HashMap<>(areas.get(area).get_neighbors2()));
                    //----
                }


                // update the neighbors list for the neighbors of the matched area
                for (int neighbor_area : matched_area.get_neighbors())
                {
                    ArrayList<Integer> neighbor_area_neighbors = areas.get(neighbor_area).get_neighbors();
                    neighbor_area_neighbors.removeAll(subareas_IDs);
                    neighbor_area_neighbors.add(area_ID);


                    //----
                    HashMap neighbor_area_neighbors2 = areas.get(neighbor_area).get_neighbors2();
                    for (int subarea : subareas_IDs)
                        neighbor_area_neighbors2.remove(subarea);
                    neighbor_area_neighbors2.put(area_ID, areas.get(neighbor_area).get_neighbors2().get(area_ID));
                    //----
                }


                //------------------------------------
                // set edge degree

                HashMap<Integer, Integer> matched_area_neighbors = new HashMap<>();
                for (int neighbor : matched_area.get_neighbors())
                {
                    matched_area_neighbors.put(neighbor, matched_area.get_neighbors2().get(neighbor));
                    areas.get(neighbor).get_neighbors2().put(area_ID, matched_area.get_neighbors2().get(neighbor));
                }
                matched_area.set_neighbors2(matched_area_neighbors);
                //------------------------------------

                // mark as matched
                unmatched.removeAll(subareas_IDs);

                // remove matched areas from the graph
                for (int subarea : subareas_IDs)
                    areas.remove(subarea);

                // add the new matched area to the graph
                areas.put(area_ID, matched_area);

                // add the unmatched area to the current_graph_unmatched
                if (matched_neighbor == -1)
                    current_graph_unmatched.add(area_ID);

                area_ID++;
            }



            Graph new_graph = new Graph(current_graph_ID);
            new_graph.set_starting_index(new_starting_index);
            new_graph.set_graph_areas(new HashMap(areas));
            coarsest_graphs.add(new_graph);

            previous_graph_unmatched.addAll(current_graph_unmatched);

            current_graph_ID++;

        }

        graph.set_coarsest_graphs(coarsest_graphs);

        int i = 0;
        for (Area a : graph.get_coarsest_graphs().get(graph.get_coarsest_graphs().size()-1).get_graph_areas().values()) {
            DataPartition dp = new DataPartition(i);
            dp.addAreas(a.get_original_subareas_IDs());
            partitions.add(dp);
            i++;
        }

        /*for (Area a : graph.get_coarsest_graphs().get(graph.get_coarsest_graphs().size()-1).get_graph_areas().values()) {
            System.out.println(a.get_degree());
            //System.out.println(a.get_original_subareas_IDs());
        }*/

        return partitions;
    } //end coarsening


    /**
     * picks the area to merge during partitioning
     * @param areas list of areas
     * @param unmatched list of unmerged areas
     * @param area area to be merged
     * @return area
     */
    public static int find_connected_area(HashMap<Integer, Area> areas,
                                          ArrayList<Integer> unmatched,
                                          int area) {

        ArrayList<Integer> area_neighbors = areas.get(area).get_neighbors();

        int matched_neighbor = -1;
        int best_degree = 0;

        for (int neighbor : area_neighbors)
        {
            if (unmatched.contains(neighbor))
            {
                if (areas.get(neighbor).get_degree() > best_degree)
                {
                    matched_neighbor = neighbor;
                    best_degree = areas.get(neighbor).get_degree();
                }

            }
        }

        return matched_neighbor;

    } //end find_connected_area


    /**
     * creates the neighborhood graph for each data partition
     * @param neighbors original neighbrhood graph
     * @param dataPartitions data partitions
     * @return neighborhood graph for each data partition
     */
    public static HashMap<Integer, HashMap<Integer, List>> createNeighborsList(Map<Integer, Set<Integer>> neighbors,
                                                                               ArrayList<DataPartition> dataPartitions) {

        HashMap<Integer, HashMap<Integer, List>> partitionsNeighbors = new HashMap<>();

        for (DataPartition partition : dataPartitions) {

            HashMap<Integer, List> partitionNeighbors = new HashMap<>();

            ArrayList<Integer> partitionAreas = partition.getAreas();

            for (Integer area : partitionAreas) {

                List<Integer> areaNeighbors = new ArrayList<>(neighbors.get(area));
                ArrayList<Integer> difference = new ArrayList<>(neighbors.get(area));
                difference.removeAll(partitionAreas);
                areaNeighbors.removeAll(difference);

                partitionNeighbors.put(area, areaNeighbors);
            }

            partitionsNeighbors.put(partition.getID(), partitionNeighbors);
        }
        return partitionsNeighbors;
    } // end createNeighborsList


    /**
     * selects a random area from a list of areas
     * @param areas list of areas
     * @return area
     */
    public static int selectRandomArea(ArrayList<Integer> areas) {

        return areas.get(rand.nextInt(areas.size()));

    } // end selectRandomArea


    /**
     * calculates the dissimilarity increase when adding an area
     * @param areaPopulation area dissimilarity attribute
     * @param regionAreas region areas
     * @param population dissimilarity attributes for the areas
     * @return dissimilarity increase
     */
    public static double calculateDissimilarity(Double areaPopulation,
                                                ArrayList<Integer> regionAreas,
                                                ArrayList<Double> population) {

        double dissimilarity = 0;

        for (int i = 0; i < regionAreas.size(); i++) {

            Double regionAreaPopulation = population.get(regionAreas.get(i));

            dissimilarity += Math.abs(areaPopulation - regionAreaPopulation);
        }

        return dissimilarity;
    } // end calculateDissimilarity


    /**
     * selects an enclave area
     * @param assignedAreas list of assigned areas
     * @param enclaves list of enclaves
     * @param neighbors neighborhood graph
     * @return enclave
     */
    public static int selectEnclave(ArrayList<Integer> assignedAreas,
                                    ArrayList<Integer> enclaves,
                                    Map<Integer, Set<Integer>> neighbors) {

        Integer enclave = null;

        loop:
        for (int i = 0; i < enclaves.size(); i++) {

            int currentEnclave = enclaves.get(i);

            for (int j = 0; j < assignedAreas.size(); j++) {

                Set<Integer> areaNeighbors = neighbors.get(assignedAreas.get(j));

                if (areaNeighbors.contains(currentEnclave)) {

                    enclave = currentEnclave;
                    break loop;
                }
            }
        } // end for

        return enclave;
    } // end selectEnclave


    /**
     * checks if a move is in the tabulist
     * @param move move object
     * @param tabuList tabu list
     * @return true or false
     */
    public static boolean isTabu(Move move, ArrayList<Move> tabuList) {

        boolean isTabu = false;

        int donorRegionID = move.getDonorRegion();
        int recipientRegionID = move.getRecipientRegion();
        int areaID = move.getMovedArea();

        for (int i = 0; i < tabuList.size(); i++) {

            Move tabuMove = tabuList.get(i);

            int donorRegionTabuID = tabuMove.getRecipientRegion();
            int recipientRegionTabuID = tabuMove.getDonorRegion();
            int areaTabuID = tabuMove.getMovedArea();

            if ((donorRegionID == donorRegionTabuID)
                    && (recipientRegionID == recipientRegionTabuID)
                    && (areaID == areaTabuID)) {

                isTabu = true;
                //System.out.println("tabu move is found.");
                break;
            }

        }

        return isTabu;
    }


    /**
     * creates a list of area IDs with their region IDs
     * @param feasiblePartition partition object
     * @return list of area IDs with their region IDs
     */
    public static HashMap<Integer, Integer> createAreasWithRegions(Partition feasiblePartition) {

        // create a list of areas with their region ID
        HashMap<Integer, Integer> areasWithRegions = new HashMap<>();
        HashMap<Integer, Region> partitionRegions = feasiblePartition.getRegions();
        for (Integer regionID : partitionRegions.keySet()) {

            Region region = partitionRegions.get(regionID);
            ArrayList<Integer> areas = region.getAreas();

            for (int j = 0; j < areas.size(); j++) {
                areasWithRegions.put(areas.get(j), regionID);
            } // end for
        } // end for

        return areasWithRegions;
    }


    /**
     * calculates the partition dissimilarity
     * @param partition partition object
     * @param population list of dissimilarity attributes
     * @return partition dissimilarity
     */
    public static double calculatePartitionH(Partition partition,
                                             ArrayList<Double> population) {

        double H = 0;

        HashMap<Integer, Region> regions = partition.getRegions();

        for (Integer regionID : regions.keySet()) {

            H += calculateRegionH(regions.get(regionID), population);

        }

        partition.setDissimilarity(H);

        return H;
    }

    /**
     * calculates the region dissimilarity
     * @param region region object
     * @param population list of dissimilarity attributes
     * @return region dissimilarity
     */

    public static double calculateRegionH(Region region,
                                          ArrayList<Double> population) {

        double H = 0;

        ArrayList<Integer> areas = region.getAreas();


        for (int i = 0; i < areas.size(); i++) {

            for (int j = i + 1; j < areas.size(); j++) {

                H += Math.abs(Math.abs(population.get(areas.get(i))) - Math.abs(population.get(areas.get(j))));

            }
        }

        region.setDissimilarity(H);

        return H;
    }

    /**
     * local search algorithm (simulated annealing) to improve the solution
     * @param lengthTabu length of the tabulist
     * @param max_no_improve maximum number of iterations for the local search
     * @param alpha alpha value
     * @param t temperature value
     * @param threshold threshold value
     * @param feasiblePartition partition
     * @param household list of extensive attributes
     * @param population list of dissimilarity attributes
     * @param neighbors neighbrhood graph
     * @return optimized partition
     */

    public static Partition modifiedSA(int lengthTabu,
                                       int max_no_improve,
                                       double alpha,
                                       double t,
                                       double threshold,
                                       Partition feasiblePartition,
                                       ArrayList<Double> household,
                                       ArrayList<Double> population,
                                       Map<Integer, Set<Integer>> neighbors) {

        // p = feasiblePartition
        Partition p = new Partition(feasiblePartition);
        //currentP = feasiblePartition
        Partition currentP = new Partition(feasiblePartition);
        double pDissimilarity = p.getDissimilarity();
        double currentPDissimilarity = currentP.getDissimilarity();

        ArrayList<Move> tabuList = new ArrayList<>();
        ArrayList<Integer> movable_units = new ArrayList<>();
        int no_improving_move = 0;

        while (no_improving_move < max_no_improve) {

            //System.out.println("c = " + c);

            if (movable_units.isEmpty()) {
                movable_units = search_movable_units(currentP, neighbors);
                if (movable_units.isEmpty()) {
                    break;
                }
            } // end if

            Move move = selectRandomMove(movable_units, household, population, currentP, threshold, neighbors);

            if (move == null)
                continue;

            int area_to_move = move.getMovedArea();
            int donor = move.getDonorRegion();
            int receiver = move.getRecipientRegion();
            double improvement = move.getHetImprovement();

            //System.out.println(improvement);
            boolean moveFlag;

            double newPDissimilarity = currentPDissimilarity - improvement; // subtraction because positive improvement means decrease in het and  negative improvement means increase in het
            if (improvement > 0) {

                //System.out.println(newPDissimilarity + " < "+ pDissimilarity);

                makeMove(currentP, move, household.get(move.getMovedArea()), population, newPDissimilarity);
                currentPDissimilarity = newPDissimilarity;
                moveFlag = true;
                movable_units.remove((Integer)area_to_move);

                if (tabuList.size() == lengthTabu)
                    tabuList.remove(0);

                tabuList.add(move);


                if (currentPDissimilarity < pDissimilarity) {

                    no_improving_move = 0;
                    p.resetRegions(currentP.getRegions());
                    pDissimilarity = newPDissimilarity;
                }

                else
                {
                    no_improving_move ++;
                }
            } // end if

            else {

                //System.out.println(newPDissimilarity + " > "+ pDissimilarity);

                no_improving_move ++;

                double probability =  Math.pow(Math.E, (improvement / t));

                if (probability > Math.random())
                {
                    //System.out.println("prob > random");

                    if (isTabu(move, tabuList)) {

                        moveFlag = false;
                        movable_units.remove((Integer)area_to_move);
                    }

                    else {

                        makeMove(currentP, move, household.get(move.getMovedArea()), population, newPDissimilarity);
                        currentPDissimilarity = newPDissimilarity;
                        moveFlag = true;
                        movable_units.remove((Integer)area_to_move);

                    }

                } // end if

                else {
                    moveFlag = false;
                    movable_units.remove((Integer)area_to_move);

                } // end else
            } // end else

            if (moveFlag) {

                /*ArrayList<Integer> toRemove = new ArrayList<>(currentP.getRegions().get(donor).getAreas());
                toRemove.removeAll(currentP.getRegions().get(receiver).getAreas());
                toRemove.addAll(currentP.getRegions().get(receiver).getAreas());*/

                for (int area : currentP.getRegions().get(donor).getAreas())
                    movable_units.remove((Integer)area);
                for (int area : currentP.getRegions().get(receiver).getAreas())
                    movable_units.remove((Integer)area);

            } //end if

            t = t * alpha;

        } // end while

        p.setDissimilarity(pDissimilarity);
        return p;
    } // end modifiedSA


    /**
     * identifies the moveable areas
     * @param currentP partition
     * @param neighbors neighborhood graph
     * @return  moveable areas
     */
    public static ArrayList<Integer> search_movable_units(Partition currentP,
                                                          Map<Integer, Set<Integer>> neighbors) {

        HashMap<Integer, Region> regions = new HashMap<>(currentP.getRegions());
        ArrayList<Integer> movable_units = new ArrayList<>();

        for (int ID : regions.keySet()) {

            ArrayList<Integer> r_articulation_pts = findAPs_Tarjan(regions.get(ID).getAreas(), neighbors);
            ArrayList<Integer> r_non_articulation_pts = new ArrayList<>(regions.get(ID).getAreas());
            r_non_articulation_pts.removeAll(r_articulation_pts);
            movable_units.addAll(r_non_articulation_pts);
        }

        return movable_units;

    }

    /**
     * finds the articulation areas in a region
     * @param areas_in_r region areas
     * @param neighbors neighbrhood graph
     * @return articulation areas
     */
    public static ArrayList<Integer> findAPs_Tarjan(ArrayList<Integer> areas_in_r,
                                                    Map<Integer, Set<Integer>> neighbors)
    {
        ArrayList<Integer> r_articulation_points = new ArrayList<>();

        int size = areas_in_r.size();

        int[] disc = new int[size];
        Arrays.fill(disc , -1);

        int[] low = new int[size];
        Arrays.fill(low , -1);

        int[] parent = new int[size];
        Arrays.fill(parent , -1);

        boolean[] articulation_label = new boolean[size];
        Arrays.fill(articulation_label , false);

        for(int i = 0 ; i < size ; i++)
        {
            if(disc[i] == -1)
            {
                DFS(i , disc , low, parent , articulation_label, 0, areas_in_r, neighbors);
            }
        }

        for(int i = 0 ; i < size ; i++)
        {
            if(articulation_label[i])
            {
                r_articulation_points.add(areas_in_r.get(i));
            }
        }

        return r_articulation_points;

    }


    private static void DFS(int u , int[] disc, int[] low, int[] parent, boolean[] articulation_label, int time,
                           ArrayList<Integer> areas_in_r,
                           Map<Integer, Set<Integer>> neighbors)
    {
        disc[u] = low[u] = time;
        time += 1;
        int children = 0;

        Set<Integer> neigh_areas = neighbors.get(areas_in_r.get(u));

        for(Integer neigh_area : neigh_areas)
        {
            if(areas_in_r.contains(neigh_area))
            {
                int v = areas_in_r.indexOf(neigh_area);

                if(disc[v] == -1)
                {
                    children += 1;
                    parent[v] = u;
                    DFS(v , disc , low , parent , articulation_label, time, areas_in_r, neighbors);
                    low[u] = Math.min(low[u] , low[v]);

                    if(parent[u] == -1 && children > 1)
                    {
                        articulation_label[u] = true;
                    }

                    if(parent[u] != -1 && low[v] >= disc[u])
                    {
                        articulation_label[u] = true;
                    }
                }

                else if(v != parent[u])
                {
                    low[u] = Math.min(low[u] , disc[v]);
                }
            }
        }
    }


    /**
     * selects a random move for local search
     * @param movable_units list of moveable areas
     * @param household extensive attribute
     * @param population dissimilarity attribute
     * @param currentP partition
     * @param threshold threshold value
     * @param neighbors neighborhood graph
     * @return move
     */
    public static Move selectRandomMove(ArrayList<Integer> movable_units,
                                        ArrayList<Double> household,
                                        ArrayList<Double> population,
                                        Partition currentP,
                                        double threshold,
                                        Map<Integer, Set<Integer>> neighbors) {

        Move move = new Move();

        int area = selectRandomArea(movable_units);
        HashMap<Integer, Integer> areasWithRegions = currentP.getAreasWithRegions();
        HashMap<Integer, Region> regions = currentP.getRegions();
        int areaRegionID = areasWithRegions.get(area);
        Region areaRegion = regions.get(areaRegionID);

        if (areaRegion.getRegionalThreshold() - household.get(area) < threshold) {
            movable_units.remove((Integer)area);
            return null;
        }

        HashSet<Integer> neighboringRegions = new HashSet<>();
        Set<Integer> areaNeighbors = neighbors.get(area);

        for (int neighbor : areaNeighbors) {

            int neighborRegionID = areasWithRegions.get(neighbor);

            if (neighborRegionID != areaRegionID) {

                neighboringRegions.add(neighborRegionID);

            }
        }

        if (neighboringRegions.isEmpty()) {
            movable_units.remove((Integer)area);
            return null;
        }

        double optimal_hetero_decre = Long.MIN_VALUE;
        double optimal_r1Hetero = 0;
        double optimal_r2Hetero = 0;
        int best_region = -1;
        for (Integer r : neighboringRegions)
        {

            double r1Hetero = 0;
            ArrayList<Integer> r1_areas = areaRegion.getAreas();
            for (int area1 : r1_areas) {
                if (area1 != area)
                    r1Hetero += Math.abs(population.get(area) - population.get(area1));
            }

            double r2Hetero = 0;
            ArrayList<Integer> r2_areas = regions.get(r).getAreas();
            for (int area2 : r2_areas) {
                r2Hetero += Math.abs(population.get(area) - population.get(area2));
            }

            double hetero_decre = r1Hetero - r2Hetero;//heteroChange(areaRegion, regions.get(r), population, area, move);
            if(hetero_decre > optimal_hetero_decre)
            {
                optimal_hetero_decre = hetero_decre;
                best_region = r;
                optimal_r1Hetero = r1Hetero;
                optimal_r2Hetero = r2Hetero;
            }

        }

        move.setRecipientRegion(best_region);
        move.setDonorRegion(areaRegion.getID());
        move.setMovedArea(area);
        move.setHetImprovement(optimal_hetero_decre);
        move.setDonorRegionH(optimal_r1Hetero);
        move.setRecipientRegionH(optimal_r2Hetero);

        return move;
    } // end selectRandomMove

    /**
     * moves an area in the local search
     * @param currentP partition
     * @param move move objcet
     * @param movedAreaHousehold extensive attributed for the moved area
     * @param population dissimilarity attribute
     * @param newPDissimilarity partition new dissimilarity
     */

    public static void makeMove(Partition currentP,
                                Move move,
                                Double movedAreaHousehold,
                                ArrayList<Double> population,
                                double newPDissimilarity) {

        //Partition newP = new Partition(currentP);

        HashMap<Integer, Region> regions = currentP.getRegions();
        Region donorRegion = regions.get(move.getDonorRegion());
        Region recipientRegion = regions.get(move.getRecipientRegion());
        int movedArea = move.getMovedArea();

        double newDonorThreshold = Math.abs(donorRegion.getRegionalThreshold() - movedAreaHousehold);
        double newRecipientThreshold = Math.abs(recipientRegion.getRegionalThreshold() + movedAreaHousehold);
        donorRegion.setRegionalThreshold(newDonorThreshold);
        recipientRegion.setRegionalThreshold(newRecipientThreshold);

        donorRegion.removeArea((Integer)movedArea);
        //regions.put(donorRegion.getID(), donorRegion);

        recipientRegion.addArea(movedArea);
        //regions.put(recipientRegion.getID(), recipientRegion);

        double donorHet = calculateRegionH(donorRegion, population);
        double recipientHet = calculateRegionH(recipientRegion, population);
        donorRegion.setDissimilarity(donorHet);
        recipientRegion.setDissimilarity(recipientHet);

        currentP.getAreasWithRegions().put(movedArea, recipientRegion.getID());
        currentP.setDissimilarity(newPDissimilarity);

        //return newP;
    }


    /**
     * assigned the enclaves to regions
     * @param population dissimilarity attribute
     * @param household extensive attribute
     * @param neighbors neighborhood graph
     * @param currentPartition partition
     * @return partition
     */
    public static Partition enclavesAssignment(ArrayList<Double> population,
                                               ArrayList<Double> household,
                                               Map<Integer, Set<Integer>> neighbors,
                                               Partition currentPartition) {

        ArrayList<Integer> assignedAreas = currentPartition.getAssignedAreas();
        ArrayList<Integer> enclaves = currentPartition.getEnclaves();
        HashMap<Integer, Region> regions = currentPartition.getRegions();
        HashMap<Integer, Integer> areasWithRegions = currentPartition.getAreasWithRegions();

        while (!enclaves.isEmpty()) {

            Integer enclave = selectEnclave(assignedAreas, enclaves, neighbors);

            HashSet<Region> neighboringRegions = new HashSet<>();

            Set<Integer> enclaveNeighbors = neighbors.get(enclave);

            for (Integer neighbor : enclaveNeighbors) {

                if (areasWithRegions.containsKey(neighbor)) {

                    int regionID = areasWithRegions.get(neighbor);
                    Region region = regions.get(regionID);

                    neighboringRegions.add(region);

                }
            }

            Region similarRegion = getSimilarRegion(enclave, population, neighboringRegions);

            Double enclaveHousehold = household.get(enclave);
            double regionThreshold = similarRegion.getRegionalThreshold();
            double updatedThreshold = enclaveHousehold + regionThreshold;

            similarRegion.setRegionalThreshold(updatedThreshold);
            similarRegion.addArea(enclave);
            areasWithRegions.put(enclave, similarRegion.getID());
            assignedAreas.add(enclave);
            enclaves.remove(enclave);

        } // end while

        return currentPartition;
    }


    /**
     * selects a region to add the enclave to
     * @param enclave enclave area
     * @param population extensive attribute
     * @param neighboringRegions enclave neighboring regions
     * @return region
     */
    public static Region getSimilarRegion(int enclave,
                                          ArrayList<Double> population,
                                          HashSet<Region> neighboringRegions) {

        Region similarRegion = null;
        double minDissimilarity = Long.MAX_VALUE;
        Double enclavePopulation = population.get(enclave);

        for (Region region : neighboringRegions) {

            ArrayList<Integer> areas = region.getAreas();
            double dissimilarity = calculateDissimilarity(enclavePopulation, areas, population);


            if (dissimilarity < minDissimilarity) {

                similarRegion = region;
                minDissimilarity = dissimilarity;
            }
        }

        return similarRegion;
    } // end getSimilarRegion

    /**
     * executes the SMP regionalization
     * @param cores number of cores
     * @param nRows number of rows
     * @param nColumns number of columns
     * @param threshold threshold value
     * @param maxItr maximum iterations for finding the maximum p
     * @param lengthTabu length of the tabulist
     * @param t temperature
     * @param alpha alpha
     * @param convSA number of iterations for the local search
     * @param random flag for selecting the area in the region growing
     * @param population dissimilarity attribute
     * @param household extensive attribute
     * @param neighbors neighbrhood graph
     * @return p, areas and their region ID
     */

    public Object[] execute_regionalization(int cores,
                                             int nRows,
                                             int nColumns,
                                             double threshold,
                                             int maxItr,
                                             int lengthTabu,
                                             double t,
                                             double alpha,
                                             int convSA,
                                             int random,
                                             ArrayList<Double> population,
                                             ArrayList<Double> household,
                                             Map<Integer, Set<Integer>> neighbors){


        /*System.out.println("cores: " + cores);
        System.out.println("nRows: " + nRows);
        System.out.println("nColumns: " + nColumns);
        System.out.println("threshold: " + threshold);
        System.out.println("maxItr: " + maxItr);
        System.out.println("lengthTabu: " + lengthTabu);
        System.out.println("t: " + t);
        System.out.println("alpha: " + alpha);
        System.out.println("convSA: " + convSA);
        System.out.println("random: " + random);
        System.out.println("d: " + population);
        System.out.println("s: " + household);
        System.out.println("poly: " + polygons_strings);
        System.out.println("neighbors: " + neighbors);*/

        ArrayList<Integer> areas = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            areas.add(i);
        }


        // ********************************************************************
        // **** CREATING DATA PARTITIONS ****
        // ********************************************************************

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Creating data partitions . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        ArrayList<DataPartition> dataPartitions = coarsening(nColumns, nRows, areas, neighbors);


        // ********************************************************************
        // **** FINDING THE NEIGHBORS WITHIN PARTITIONS ****
        // ********************************************************************

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Finding neighbors within each partition . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        HashMap<Integer, HashMap<Integer, List>> partitionsNeighbors = createNeighborsList(neighbors, dataPartitions);



        // ********************************************************************
        // **** CONSTRUCTION PHASE ****
        // ********************************************************************

        int maxP = 0;
        //Partition bestP;
        ArrayList<Partition> partitionsBeforeEnclaves = new ArrayList<>();
        Partition bestFeasiblePartition = new Partition();

        // **** GROW REGIONS ****

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Growing the regions . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        ExecutorService growRegionsExecutor = Executors.newFixedThreadPool(cores);

        List<GrowRegionsGraphThread> growThreads = new ArrayList<>();
        for (int itr = 0; itr < maxItr; itr++) {

            GrowRegionsGraphThread thread = new GrowRegionsGraphThread(threshold, itr, partitionsNeighbors, household, population, dataPartitions, cores, random);
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

                HashMap<Integer, Integer> areasWithRegions = createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.clear();
                partitionsBeforeEnclaves.add(partition);
                maxP = p;

            } else if (p == maxP) {

                HashMap<Integer, Integer> areasWithRegions = createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.add(partition);

            } else if (p < maxP) {

                // pass
            }
        }

        //System.out.println("MaxP: " + maxP);
        //System.out.println("Number of partitions after growing the regions using threads: " + partitionsBeforeEnclaves.size());



        // **** ENCLAVES ASSIGNMENT ****

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Enclaves Assignment . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        double minHet = Long.MAX_VALUE;

        for (int i = 0; i < partitionsBeforeEnclaves.size(); i++) {

            Partition currentPartition = partitionsBeforeEnclaves.get(i);
            Partition feasiblePartition = enclavesAssignment(population, household, neighbors, currentPartition);
            double heterogeneity = calculatePartitionH(feasiblePartition, population);
            feasiblePartition.setDissimilarity(heterogeneity);
            if (heterogeneity < minHet) {
                bestFeasiblePartition = feasiblePartition;
                minHet = heterogeneity;
            }
            //feasiblePartition.setDissimilarity(heterogeneity);
            //feasiblePartitions.add(feasiblePartition);
        }



        // ********************************************************************
        // **** LOCAL SEARCH PHASE ****
        // ********************************************************************

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Local Search . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        double improvement;
        double oldHeterogeneity;

        oldHeterogeneity = bestFeasiblePartition.getDissimilarity();
        this.bestP = modifiedSA(lengthTabu, convSA, alpha, t, threshold, bestFeasiblePartition, household, population, neighbors);

        double pH = this.bestP.getDissimilarity();

        improvement = oldHeterogeneity - pH;
        float percentage = ((float)improvement/(float)oldHeterogeneity);


        HashMap<Integer, Region> regionsList = this.bestP.getRegions();
        int[] area_region = new int[areas.size()];
        for (Integer regionID : regionsList.keySet())
        {

            ArrayList<Integer> areasList = regionsList.get(regionID).getAreas();
            //System.out.println("region " + regionID + ":" + areasList);
            for (int area : areasList)
            {
                area_region[area] = regionID;
            }
        }


        return new Object[]{area_region, bestP.getRegions().size(), pH};

    } // end execute_SMPPGraph


    /**
     * overrides the  execute_regionalization function
     * @param neighborSet A hashmap. The key is the index of each area. The value is the set of indices of the neighbor areas of the given area. The area index is assumed to be 0 to the number of areas that correspond to the attribute lists.
     * @param disAttr The list of dissimilarity attributes
     * @param sumAttr The list of attributes for the summation constraint.
     * @param thr the threshold value
     */
    @Override
    public void execute_regionalization(Map<Integer, Set<Integer>> neighborSet, ArrayList<Long> disAttr, ArrayList<Long> sumAttr, Long thr){

        int cores = 4;
        int nRows = 4;
        int nColumns = 4;
        double threshold = (double) thr;
        int maxItr = 40;
        int lengthTabu = 100;
        double t = 1;
        double alpha = 0.9;
        int convSA = 50;
        int random = 0;

        ArrayList<Double> population = new ArrayList<>();
        ArrayList<Double> household = new ArrayList<>();
        for (long l : disAttr)
            population.add((double)l);
        for (long l : sumAttr)
            household.add((double)l);

        execute_regionalization(cores, nRows, nColumns, threshold, maxItr, lengthTabu, t, alpha, convSA, random, population, household, neighborSet);


    }


    @Override
    public int getP() {
        return this.bestP.getRegions().size();
    }


    public int[] getRegionLabels() {

        int size = 0;
        for (int id : this.bestP.getRegions().keySet())
            size = size + this.bestP.getRegions().get(id).getAreas().size();

        HashMap<Integer, Region> regionsList = this.bestP.getRegions();
        int[] area_region = new int[size];
        for (Integer regionID : regionsList.keySet())
        {

            ArrayList<Integer> areasList = regionsList.get(regionID).getAreas();
            //System.out.println("region " + regionID + ":" + areasList);
            for (int area : areasList)
            {
                area_region[area] = regionID;
            }
        }
        return area_region;
    }

    /**
     * reads the shape files
     * @param shp_file shape file path
     * @param string_polygons to store polygon strings
     * @param areas_polygons to store polygons
     * @param areas_IDs to store area IDs
     * @param dissimilarity_attr to store dissimilarity attributes
     * @param ex_attr to store extensive attributes
     * @param dissimilarity_attr_col dissimilarity attribute column name
     * @param ex_attr_col extensive attribute column name
     * @throws IOException throws IOException when unable to read the shape file
     */
    public static void read_files(String shp_file,
                                  ArrayList<String> string_polygons,
                                  ArrayList<Geometry> areas_polygons,
                                  ArrayList<Integer> areas_IDs,
                                  ArrayList<Double> dissimilarity_attr,
                                  ArrayList<Double> ex_attr,
                                  String dissimilarity_attr_col,
                                  String ex_attr_col) throws IOException {



        // reading shape file
        File file = new File(shp_file);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

        try (FeatureIterator<SimpleFeature> features = collection.features()) {

            int ID = 0;

            while (features.hasNext()) {

                SimpleFeature feature = features.next();
                //System.out.print(feature.getID());
                //System.out.print(": ");
                //System.out.println(feature.getDefaultGeometryProperty().getValue());
                double attr1 = Double.parseDouble(feature.getAttribute(dissimilarity_attr_col).toString());
                dissimilarity_attr.add(attr1);
                double attr2 = Double.parseDouble(feature.getAttribute(ex_attr_col).toString());
                ex_attr.add(attr2);
                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                areas_polygons.add(polygon);
                string_polygons.add(feature.getDefaultGeometryProperty().getValue().toString());
                areas_IDs.add(ID);
                ID++;
            } // end while
        } // end try

        dataStore.dispose();
    } // end reading_files

    /**
     * creates the neighborhood graph
     * @param polygons polygons list
     * @return neighborhood graph
     */
    public static Map<Integer, Set<Integer>> createNeighborsList(ArrayList<Geometry> polygons) {

        Map<Integer, Set<Integer>> neighbors = new HashMap<>();

        for (int i = 0; i < polygons.size(); i++) {

            for (int j = i + 1; j < polygons.size(); j++) {

                if (polygons.get(i).intersects(polygons.get(j))) {

                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));

                    if (intersection.getGeometryType() != "Point") {

                        if (neighbors.containsKey(i))
                            neighbors.get(i).add(j);
                        else {
                            Set<Integer> neighborList = new HashSet<>();
                            neighborList.add(j);
                            neighbors.put(i, neighborList);
                        }

                        if (neighbors.containsKey(j))
                            neighbors.get(j).add(i);
                        else {
                            Set<Integer> neighborList = new HashSet<>();
                            neighborList.add(i);
                            neighbors.put(j, neighborList);
                        }

                    } // end if
                } // end if
            } // end for
        } // end for

        return neighbors;
    } // end createNeighborsList

} // end class SMPPGraphPineapple
