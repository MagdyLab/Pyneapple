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
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import org.locationtech.jts.geom.*;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;














class GrowRegionsThread implements Callable<Partition> {

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
     * @param polygons areas' polygons
     * @param dataPartitions data partitions
     * @param cores number of cores
     * @param random flag
     */
    public GrowRegionsThread(double threshold,
                             int itr,
                             HashMap<Integer, HashMap<Integer, List>> neighbors,
                             ArrayList<Double> household,
                             ArrayList<Double> population,
                             ArrayList<Geometry> polygons,
                             ArrayList<DataPartition> dataPartitions,
                             int cores,
                             int random) {

        this.threshold = threshold;
        this.itr = itr;
        this.household = household;
        this.population = population;
        this.neighbors = neighbors;
        this.polygons = polygons;
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
        List<GrowPartitionThread> growThreads = new ArrayList<>();

        for (int i = 0; i < this.dataPartitions.size(); i++) {

            GrowPartitionThread thread = new GrowPartitionThread(this.threshold,
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

        return partition;
    }
}


class GrowPartitionThread implements Callable<Partition> {

    /**
     * Random object
     */
    public static Random rand = SMPPPyneapple.rand;

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
    public GrowPartitionThread(double threshold,
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

    /**
     *
     * @param areas areas
     * @return random area
     */
    public int selectRandomArea(ArrayList<Integer> areas) {

        return areas.get(this.rand.nextInt(areas.size()));
    }

    /**
     * get the similar area
     * @param unassignedNeighbors neighbors
     * @param regionAreas region areas
     * @return area
     */
    public Integer getSimilarArea(List<Integer> unassignedNeighbors,
                                  ArrayList<Integer> regionAreas) {

        double minDissimilarity = Double.MAX_VALUE;
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
public class SMPPPyneapple implements RegionalizationMethod {


    /**
     * default constructor for SMPPPyneapple class
     */
    public SMPPPyneapple() {

    }

    /**
     * Random object
     */
    public static Random rand = new Random();
    Partition bestP;


    /**
     * partitions the dataset
     * @param nColumns number of columns
     * @param nRows number of rows
     * @param polygons areas polygons
     * @param areas list pf areas' IDs
     * @param neighbors neighborhood graph
     * @return data partitions
     */
    public static ArrayList<DataPartition> partitionData(int nColumns,
                                                         int nRows,
                                                         ArrayList<Geometry> polygons,
                                                         ArrayList<Integer> areas,
                                                         ArrayList<List> neighbors) {

        ArrayList<Integer> unassignedAreas = new ArrayList<>(areas);
        ArrayList<Integer> assignedAreas = new ArrayList<>();
        ArrayList<Integer> enclaves = new ArrayList<>();
        ArrayList<DataPartition> partitionsBoundaries;
        ArrayList<DataPartition> dataPartitions = new ArrayList<>();

        BoundsFilter boundsFilter = new BoundsFilter();
        for (Geometry area : polygons) {
            area.apply(boundsFilter);
        }

        Rectangle2D MBR = boundsFilter.getBounds();

        double minX = MBR.getMinX();
        double minY = MBR.getMinY();
        double maxX = MBR.getMaxX();
        double maxY = MBR.getMaxY();

        ReferencedEnvelope envelope =
                new ReferencedEnvelope(minX, maxX, minY, maxY, DefaultGeographicCRS.WGS84);

        partitionsBoundaries = createPartitionsBoundaries(nColumns, nRows, envelope);

        for (int i = 0; i < areas.size(); i++) {

            Envelope areaEnvelope = polygons.get(i).getEnvelopeInternal();
            boolean assigned = false;

            for (DataPartition partition : partitionsBoundaries) {

                ReferencedEnvelope partitionEnvelope = partition.getEnvelope();

                if (isWithin(areaEnvelope, partitionEnvelope)) {

                    partition.addArea(i);
                    unassignedAreas.remove((Integer) i);
                    assignedAreas.add(i);
                    assigned = true;
                    break;
                } // end if
            } // end for

            if (!assigned) {
                enclaves.add(i);
            }
        } // end for

        for (DataPartition partition : partitionsBoundaries) {

            if (!partition.getAreas().isEmpty()) {

                ArrayList<ArrayList> components = connectedComponents(partition.getAreas(), neighbors);
                if (components.size() > 1) {

                    int max = Integer.MIN_VALUE;
                    int index = 0;

                    for (int i = 0; i < components.size(); i++) {

                        if (components.get(i).size() > max) {
                            max = components.get(i).size();
                            index = i;
                        }
                    }

                    partition.getAreas().clear();
                    partition.getAreas().addAll(components.get(index));
                    components.remove(index);

                    for (ArrayList<Integer> component : components) {
                        enclaves.addAll(component);
                        assignedAreas.removeAll(component);
                    }
                }
                dataPartitions.add(partition);
            }
        }

        unassignedAssignment(neighbors, dataPartitions, enclaves, assignedAreas);

        long t = 0;
        for (DataPartition db : dataPartitions) {
            t += db.getAreas().size();
        }
        //System.out.println("total: " + t);

        return dataPartitions;
    } // end partitionData


    /**
     * creates the boundaries for the data partitions
     * @param nColumns number of columns
     * @param nRows number of rows
     * @param envelope areas envelope
     * @return  boundaries for the data partitions
     */
    public static ArrayList<DataPartition> createPartitionsBoundaries(int nColumns,
                                                                      int nRows,
                                                                      ReferencedEnvelope envelope) {

        ArrayList<DataPartition> partitionsBoundaries = new ArrayList<>();

        double minX = envelope.getMinX();
        double minY = envelope.getMinY();
        double maxX = envelope.getMaxX();
        double maxY = envelope.getMaxY();
        double rowWidth = (maxY - minY) / nRows;
        double columnWidth = (maxX - minX) / nColumns;
        int ID = 1;

        for (int i = 0; i < nRows; i++) {

            double minY1 = minY + (rowWidth * i);
            double maxY1 = minY + (rowWidth * (i + 1));

            for (int j = 0; j < nColumns; j++) {

                double minX1 = minX + (columnWidth * j);
                double maxX1 = minX + (columnWidth * (j + 1));

                DataPartition partition = new DataPartition(ID);
                ReferencedEnvelope envelope1 =
                        new ReferencedEnvelope(minX1, maxX1, minY1, maxY1, DefaultGeographicCRS.WGS84);
                partition.setEnvelope(envelope1);
                partitionsBoundaries.add(partition);
                ID++;

            }
        }

        return partitionsBoundaries;
    } // end createPartitionsBoundaries


    /**
     * checks if an area is inside a partition boundary
     * @param areaEnvelope area envelope
     * @param partitionEnvelope partition envelope
     * @return if an area is inside a partition boundary
     */
    public static boolean isWithin(Envelope areaEnvelope,
                                   ReferencedEnvelope partitionEnvelope) {

        double areaMinX = areaEnvelope.getMinX();
        double areaMinY = areaEnvelope.getMinY();
        double areaMaxX = areaEnvelope.getMaxX();
        double areaMaxY = areaEnvelope.getMaxY();

        double partitionMinX = partitionEnvelope.getMinX();
        double partitionMinY = partitionEnvelope.getMinY();
        double partitionMaxX = partitionEnvelope.getMaxX();
        double partitionMaxY = partitionEnvelope.getMaxY();

        if (areaMinX >= partitionMinX && areaMaxX <= partitionMaxX
                && areaMinY >= partitionMinY && areaMaxY <= partitionMaxY) {
            return true;
        } else {
            return false;
        }
    } // end isWithin


    /**
     * calculates the connected components for the the data partition
     * @param areas data partition areas
     * @param neighbors neighborhood graph
     * @return connected components for the the data partition
     */
    public static ArrayList<ArrayList> connectedComponents(ArrayList<Integer> areas,
                                                           ArrayList<List> neighbors) {

        ArrayList<ArrayList> connectedComponents = new ArrayList<>();

        HashMap<Integer, Boolean> visitedAreas = new HashMap<>();

        // fill the hash map (key: area, value: false)
        for (int i = 0; i < areas.size(); i++) {

            visitedAreas.put(areas.get(i), false);

        } // end for

        for (Integer area : visitedAreas.keySet()) {

            if (visitedAreas.get(area) == false) {

                ArrayList<Integer> component = new ArrayList<>();
                connectedComponents.add(listTraversal(visitedAreas, area, areas, neighbors, component));

            } // end if
        } // end for

        return connectedComponents;
    } // end connectedComponents


    private static ArrayList<Integer> listTraversal(HashMap<Integer, Boolean> visitedAreas,
                                                   int area,
                                                   ArrayList<Integer> areas,
                                                   ArrayList<List> neighbors,
                                                   ArrayList<Integer> component) {
        visitedAreas.put(area, true);

        component.add(area);

        List<Integer> neighborsList = neighbors.get(area);

        for (Integer neighbor : neighborsList) {

            if (areas.contains(neighbor)) {

                if (visitedAreas.get(neighbor) == false) {

                    listTraversal(visitedAreas, neighbor, areas, neighbors, component);

                } // end if
            } // end if
        } // end for
        return component;
    } // end listTraversal


    private static void unassignedAssignment(ArrayList<List> neighbors,
                                            ArrayList<DataPartition> partitions,
                                            ArrayList<Integer> enclaves,
                                            ArrayList<Integer> assignedAreas) {

        while (!enclaves.isEmpty()) {

            int min = Integer.MAX_VALUE;

            Integer enclave = selectEnclave(assignedAreas, enclaves, neighbors);

            ArrayList<DataPartition> neighboringPartitions = new ArrayList<>();

            for (int k = 0; k < partitions.size(); k++) {

                DataPartition partition = partitions.get(k);

                ArrayList<Integer> areas = partition.getAreas();

                for (int m = 0; m < areas.size(); m++) {

                    List<Integer> areaNeighbors = neighbors.get(areas.get(m));

                    if (areaNeighbors.contains(enclave)) {

                        neighboringPartitions.add(partition);
                        break;

                    } // end if
                } // end for m
            } // end for k

            int index = 0;

            for (int i = 0; i < neighboringPartitions.size(); i++) {

                if (neighboringPartitions.get(i).getAreas().size() < min) {
                    min = neighboringPartitions.get(i).getAreas().size();
                    index = i;
                }
            }

            neighboringPartitions.get(index).getAreas().add(enclave);
            assignedAreas.add(enclave);
            enclaves.remove(enclave);

        } // end while
    } // end unassignedAssignment




    /**
     * creates the neighborhood graph for each data partition
     * @param neighbors original neighbrhood graph
     * @param dataPartitions data partitions
     * @return neighborhood graph for each data partition
     */
    public static HashMap<Integer, HashMap<Integer, List>> createNeighborsList(ArrayList<List> neighbors,
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
    public static double calculateDissimilarity(double areaPopulation,
                                                ArrayList<Integer> regionAreas,
                                                ArrayList<Double> population) {

        double dissimilarity = 0;

        for (int i = 0; i < regionAreas.size(); i++) {

            double regionAreaPopulation = population.get(regionAreas.get(i));

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
                                    ArrayList<List> neighbors) {

        Integer enclave = null;

        loop:
        for (int i = 0; i < enclaves.size(); i++) {

            int currentEnclave = enclaves.get(i);

            for (int j = 0; j < assignedAreas.size(); j++) {

                List<Integer> areaNeighbors = neighbors.get(assignedAreas.get(j));

                if (areaNeighbors.contains((Integer)currentEnclave)) {

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
                                       ArrayList<List> neighbors) {

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
                                                          ArrayList<List> neighbors) {

        HashMap<Integer, Region> regions = new HashMap<>(currentP.getRegions());
        ArrayList<Integer> movable_units = new ArrayList<>();


        for (int ID : regions.keySet()) {

            if (regions.get(ID).getAreas().size() == 1)
                continue;

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
                                                    ArrayList<List> neighbors)
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
                           ArrayList<List> neighbors)
    {
        disc[u] = low[u] = time;
        time += 1;
        int children = 0;

        List<Integer> neigh_areas = neighbors.get(areas_in_r.get(u));

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
                                        ArrayList<List> neighbors) {

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
        List<Integer> areaNeighbors = neighbors.get(area);

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

        double optimal_hetero_decre = Double.NEGATIVE_INFINITY;
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
                                double movedAreaHousehold,
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
                                               ArrayList<List> neighbors,
                                               Partition currentPartition) {

        ArrayList<Integer> assignedAreas = currentPartition.getAssignedAreas();
        ArrayList<Integer> enclaves = currentPartition.getEnclaves();
        HashMap<Integer, Region> regions = currentPartition.getRegions();
        HashMap<Integer, Integer> areasWithRegions = currentPartition.getAreasWithRegions();

        while (!enclaves.isEmpty()) {

            Integer enclave = selectEnclave(assignedAreas, enclaves, neighbors);

            HashSet<Region> neighboringRegions = new HashSet<>();

            List<Integer> enclaveNeighbors = neighbors.get(enclave);

            for (Integer neighbor : enclaveNeighbors) {

                if (areasWithRegions.containsKey(neighbor)) {

                    int regionID = areasWithRegions.get(neighbor);
                    Region region = regions.get(regionID);

                    neighboringRegions.add(region);

                }
            }

            Region similarRegion = getSimilarRegion(enclave, population, neighboringRegions);

            double enclaveHousehold = household.get(enclave);
            double regionThreshold = similarRegion.getRegionalThreshold();
            double updatedThreshold = enclaveHousehold + regionThreshold;

            similarRegion.setRegionalThreshold(updatedThreshold);
            similarRegion.addArea(enclave);
            areasWithRegions.put(enclave, similarRegion.getID());
            assignedAreas.add(enclave);
            enclaves.remove((Integer)enclave);

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
        double enclavePopulation = population.get(enclave);

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
     * @param polygons_strings polygons strings for the areas
     * @throws ParseException parse exception for reading the polygons
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
                                        ArrayList<List> neighbors,
                                        ArrayList<String> polygons_strings) throws ParseException {

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

        ArrayList<Geometry> polygons = new ArrayList<>();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);

        for (int i = 0; i < polygons_strings.size(); i++)
        {

            Geometry polygon = (Geometry) reader.read(polygons_strings.get(i));
            polygons.add(polygon);
        }

        ArrayList<Integer> areas = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++)
        {
            areas.add(i);
        }



        // ********************************************************************
        // **** CREATING DATA PARTITIONS ****
        // ********************************************************************

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Creating data partitions . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        ArrayList<DataPartition> dataPartitions = partitionData(nColumns, nRows, polygons, areas, neighbors);


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

        double pH = bestP.getDissimilarity();

        improvement = oldHeterogeneity - pH;
        float percentage = ((float)improvement/(float)oldHeterogeneity);


        HashMap<Integer, Region> regionsList = bestP.getRegions();
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
    } // end execute_SMPP


    /**
     * overrides the  execute_regionalization function
     * @param neighborSet A hashmap. The key is the index of each area. The value is the set of indices of the neighbor areas of the given area. The area index is assumed to be 0 to the number of areas that correspond to the attribute lists.
     * @param disAttr The list of dissimilarity attributes
     * @param sumAttr The list of attributes for the summation constraint.
     * @param threshold the threshold value
     */
    @Override
    public void execute_regionalization(Map<Integer, Set<Integer>> neighborSet, ArrayList<Long> disAttr, ArrayList<Long> sumAttr, Long threshold) {

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
    public static ArrayList<List> createNeighborsList(ArrayList<Geometry> polygons) {

        ArrayList<List> neighbors = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {

            neighbors.add(new ArrayList());
        }


        for (int i = 0; i < polygons.size(); i++) {

            for (int j = i + 1; j < polygons.size(); j++) {

                if (polygons.get(i).intersects(polygons.get(j))) {

                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));

                    if (intersection.getGeometryType() != "Point") {

                        neighbors.get(i).add(j);
                        neighbors.get(j).add(i);

                    } // end if
                } // end if
            } // end for
        } // end for

        return neighbors;
    } // end createNeighborsList


} // end class SMPPPinapple

