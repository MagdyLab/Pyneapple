package org.geotools;

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



class DataPartition {

    int ID;
    int seed;
    ArrayList<Integer> areas;
    ReferencedEnvelope envelope;

    public DataPartition(int ID) {

        this.ID = ID;
        this.areas = new ArrayList<>();
    }

    public DataPartition(DataPartition dataPartition) {

        this.ID = dataPartition.ID;
        this.seed = dataPartition.seed;
        this.areas = new ArrayList<>(dataPartition.areas);
        this.envelope = new ReferencedEnvelope(dataPartition.envelope);
    }

    public void setEnvelope(ReferencedEnvelope envelope) {

        this.envelope = envelope;
    }

    public ReferencedEnvelope getEnvelope() {

        return envelope;
    }

    public void addArea(int area) {

        this.areas.add(area);
    }

    public ArrayList<Integer> getAreas() {

        return this.areas;
    }

    public int getID() {

        return this.ID;
    }
}


class BoundsFilter implements CoordinateFilter {

    double minx, miny, maxx, maxy;
    boolean first = true;

    public void filter(Coordinate c) {

        if (first) {

            minx = maxx = c.x;
            miny = maxy = c.y;
            first = false;
        } else {
            minx = Math.min(minx, c.x);
            miny = Math.min(miny, c.y);
            maxx = Math.max(maxx, c.x);
            maxy = Math.max(maxy, c.y);
        }
    }

    Rectangle2D getBounds() {

        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }
}


class Region {

    private ArrayList<Integer> areas;
    private int ID;
    private double regionalThreshold;
    private double dissimilarity;

    public Region(Region region) {

        this.areas = new ArrayList<>(region.areas);
        this.ID = region.ID;
        this.regionalThreshold = region.regionalThreshold;
        this.dissimilarity = region.dissimilarity;
    }

    public Region(int id) {

        this.areas = new ArrayList<>();
        this.ID = id;
        this.regionalThreshold = 0;
        this.dissimilarity = 0;
    }

    public void setID(int id) {

        this.ID = id;
    }

    public int getID() {

        return this.ID;
    }

    public void addArea(Integer area) {

        this.areas.add(area);
    }

    public void removeArea(Integer area) {

        this.areas.remove(area);
    }

    public ArrayList<Integer> getAreas() {

        return this.areas;
    }

    public void setRegionalThreshold(double threshold) {

        this.regionalThreshold = threshold;
    }

    public double getRegionalThreshold() {

        return this.regionalThreshold;
    }

    public void setDissimilarity(double dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    public double getDissimilarity() {

        return this.dissimilarity;
    }
}


class Partition {

    private int ID;
    private HashMap<Integer, Region> regions;
    private ArrayList<Integer> enclaves;
    private ArrayList<Integer> assignedAreas;
    private HashMap<Integer, Integer> areasWithRegions;
    private double dissimilarity;

    public Partition(Partition partition) {

        this.ID = partition.ID;
        this.regions = new HashMap<>(partition.regions);
        this.enclaves = new ArrayList<>(partition.enclaves);
        this.assignedAreas = new ArrayList<>(partition.assignedAreas);
        this.areasWithRegions = new HashMap<>(partition.areasWithRegions);
        this.dissimilarity = partition.dissimilarity;
    }

    public Partition(int ID, HashMap<Integer, Region> regions, double dissimilarity) {

        this.ID = ID;
        this.regions = new HashMap<>(regions);
        this.dissimilarity = dissimilarity;
    }

    public Partition(int id) {

        this.ID = id;
        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    public Partition() {

        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    public void addRegion(int regionID, Region region) {

        this.regions.put(regionID, region);
    }

    public HashMap<Integer, Region> getRegions() {

        return this.regions;
    }

    public void addEnclaves(ArrayList<Integer> enclave) {

        this.enclaves.addAll(enclave);
    }

    public ArrayList<Integer> getEnclaves() {

        return this.enclaves;
    }

    public void addAssignedAreas(ArrayList<Integer> assignedArea) {

        this.assignedAreas.addAll(assignedArea);
    }

    public ArrayList<Integer> getAssignedAreas() {

        return this.assignedAreas;
    }

    public void resetRegions(HashMap<Integer, Region> newRegions) {

        this.regions = new HashMap<>(newRegions);
    }

    public void setAreasWithRegions(HashMap<Integer, Integer> areasWithRegions) {

        this.areasWithRegions = areasWithRegions;
    }

    public HashMap<Integer, Integer> getAreasWithRegions() {

        return this.areasWithRegions;
    }

    public void updateAreasWithRegions(int newRegion, int area) {

        this.areasWithRegions.put(area, newRegion);
    }

    public void setDissimilarity(double dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    public double getDissimilarity() {

        return this.dissimilarity;
    }

    public double calculateDissimilarity() {

        double dissimilarity = 0;

        for (int regionID : this.regions.keySet()) {

            double regionDissimilarity = this.regions.get(regionID).getDissimilarity();
            dissimilarity = dissimilarity + regionDissimilarity;
        }

        return dissimilarity;
    }

    public int getPartitionID() {

        return this.ID;
    }
}


class Move {

    private int recipientRegion;
    private int donorRegion;
    private int movedArea;
    private double donorRegionH;
    private double recipientRegionH;
    private double hetImprovement;

    public Move() {

        this.recipientRegion = 00;
        this.donorRegion = 00;
        this.movedArea = 00;
        this.donorRegionH = 00;
        this.recipientRegionH = 00;
        this.hetImprovement = 00;
    }

    public Move(Move move) {

        this.recipientRegion = move.recipientRegion;
        this.donorRegion = move.donorRegion;
        this.movedArea = move.movedArea;
        this.donorRegionH = move.donorRegionH;
        this.recipientRegionH = move.recipientRegionH;
        this.hetImprovement = move.hetImprovement;
    }

    public void setRecipientRegion(int region) {

        this.recipientRegion = region;
    }

    public void setDonorRegion(int region) {

        this.donorRegion = region;
    }

    public void setMovedArea(int area) {

        this.movedArea = area;
    }

    public void setDonorRegionH(double donorRegionH) {

        this.donorRegionH = donorRegionH;
    }

    public void setRecipientRegionH(double recipientRegionH) {

        this.recipientRegionH = recipientRegionH;
    }

    public void setHetImprovement(double hetImprovement) {

        this.hetImprovement = hetImprovement;
    }

    public int getRecipientRegion() {

        return this.recipientRegion;
    }

    public int getDonorRegion() {

        return this.donorRegion;
    }

    public int getMovedArea() {

        return this.movedArea;
    }

    public double getDonorRegionH() {

        return this.donorRegionH;
    }

    public double getRecipientRegionH() {

        return this.recipientRegionH;
    }

    public double getHetImprovement() {

        return this.hetImprovement;
    }
}


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

    public static Random rand = SMPPPineapple.rand;

    double threshold;
    HashMap<Integer, List> neighbors;
    ArrayList<Double> household;
    ArrayList<Double> population;
    DataPartition dataPartition;
    int random;

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

    public Partition call() {

        //System.out.println("partition thread: " + System.currentTimeMillis());

        Partition partialPartition = growRegions(this.dataPartition);
        return partialPartition;
    }


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


class EnclavesAssignmentThread implements Callable<Partition> {

    ArrayList<Double> population;
    ArrayList<Double> household;
    ArrayList<Geometry> polygons;
    ArrayList<List> neighbors;
    Partition currentPartition;
    int i;

    public EnclavesAssignmentThread(ArrayList<Double> population,
                                    ArrayList<Double> household,
                                    ArrayList<Geometry> polygons,
                                    ArrayList<List> neighbors,
                                    Partition currentPartition,
                                    int i) {

        this.population = population;
        this.household = household;
        this.polygons = polygons;
        this.neighbors = neighbors;
        this.currentPartition = new Partition(currentPartition);
        this.i = i;
    }

    public Partition call() {

        Partition partition = enclavesAssignment();

        return partition;

    }

    public Partition enclavesAssignment() {

        ArrayList<Integer> assignedAreas = this.currentPartition.getAssignedAreas();
        ArrayList<Integer> enclaves = this.currentPartition.getEnclaves();
        HashMap<Integer, Region> regions = this.currentPartition.getRegions();
        HashMap<Integer, Integer> areasWithRegions = this.currentPartition.getAreasWithRegions();

        /*System.out.println("Thread " + i + "-Partition ID : " + partition.getPartitionID());
        System.out.println("Thread " + i + "-Enclaves size BEFORE enclave assignment: " + enclaves.size());
        System.out.println("Thread " + i + "-Assigned areas size BEFORE enclave assignment: " + assignedAreas.size());
        System.out.println("Thread " + i + "-Number of regions BEFORE enclave assignment: " + regions.size());*/

        while (!enclaves.isEmpty()) {

            Integer enclave = selectEnclave(assignedAreas, enclaves);

            HashSet<Region> neighboringRegions = new HashSet<>();

            List<Integer> enclaveNeighbors = this.neighbors.get(enclave);

            for (Integer neighbor : enclaveNeighbors) {

                if (areasWithRegions.containsKey(neighbor)) {

                    int regionID = areasWithRegions.get(neighbor);
                    Region region = regions.get(regionID);


                    neighboringRegions.add(region);

                }
            }

            Region similarRegion = getSimilarRegion(enclave, neighboringRegions);

            double enclaveHousehold = this.household.get(enclave);
            double regionThreshold = similarRegion.getRegionalThreshold();
            double updatedThreshold = enclaveHousehold + regionThreshold;

            similarRegion.setRegionalThreshold(updatedThreshold);
            similarRegion.addArea(enclave);
            areasWithRegions.put(enclave, similarRegion.getID());
            assignedAreas.add(enclave);
            enclaves.remove(enclave);

        } // end while

        /*System.out.println("Thread " + i + "-Enclaves size AFTER enclave assignment: " + partition.getEnclaves().size());
        System.out.println("Thread " + i + "-Assigned areas size AFTER enclave assignment: " + partition.getAssignedAreas().size());
        System.out.println("Thread " + i + "-Number of regions AFTER enclave assignment: " + partition.getRegions().size());*/

        return this.currentPartition;
    }


    public int selectEnclave(ArrayList<Integer> assignedAreas,
                             ArrayList<Integer> enclaves) {

        Integer enclave = null;

        loop:
        for (int i = 0; i < enclaves.size(); i++) {

            int currentEnclave = enclaves.get(i);

            for (int j = 0; j < assignedAreas.size(); j++) {

                List<Integer> areaNeighbors = this.neighbors.get(assignedAreas.get(j));

                if (areaNeighbors.contains(currentEnclave)) {

                    enclave = currentEnclave;
                    break loop;
                }
            }
        } // end for
        return enclave;
    }


    public Region getSimilarRegion(int enclave,
                                   HashSet<Region> neighboringRegions) {

        Region similarRegion = null;
        double minDissimilarity = Long.MAX_VALUE;
        double enclavePopulation = this.population.get(enclave);

        for (Region region : neighboringRegions) {

            ArrayList<Integer> areas = region.getAreas();
            double dissimilarity = calculateDissimilarity(enclavePopulation, areas);


            if (dissimilarity < minDissimilarity) {

                similarRegion = region;
                minDissimilarity = dissimilarity;
            }
        }

        return similarRegion;
    }

    public double calculateDissimilarity(double areaPopulation,
                                       ArrayList<Integer> regionAreas) {

        double dissimilarity = 0;

        for (int i = 0; i < regionAreas.size(); i++) {

            double regionAreaPopulation = this.population.get(regionAreas.get(i));

            dissimilarity += Math.abs(areaPopulation - regionAreaPopulation);
        }

        return dissimilarity;
    }
}






public class SMPPPineapple {


    public static Random rand = new Random();

    public static void readFiles(int dataSet,
                                 ArrayList<Double> household,
                                 ArrayList<Double> population,
                                 ArrayList<Geometry> polygons) throws IOException {

        String dbfFile = null;
        String shpFile = null;


        switch (dataSet) {

            case 1:
                dbfFile = "Datasets/Polygons15/Polygons15.dbf";
                shpFile = "Datasets/Polygons15/Polygons15.shp";
                break;

            case 2:
                dbfFile = "Datasets/Polygons57/Polygons57.dbf";
                shpFile = "Datasets/Polygons57/Polygons57.shp";

                break;

            case 3:
                dbfFile = "Datasets/2K/2K.dbf";
                shpFile = "Datasets/2K/2K.shp";
                break;

            case 4:
                dbfFile = "Datasets/5K/5K.dbf";
                shpFile = "Datasets/5K/5K.shp";
                break;

            case 5:
                dbfFile = "Datasets/10K/10K.dbf";
                shpFile = "Datasets/10K/10K.shp";
                break;

            case 6:
                dbfFile = "Datasets/20K/20K.dbf";
                shpFile = "Datasets/20K/20K.shp";
                break;

            case 7:
                dbfFile = "Datasets/30K/30K.dbf";
                shpFile = "Datasets/30K/30K.shp";
                break;

            case 8:
                dbfFile = "Datasets/40K/40K.dbf";
                shpFile = "Datasets/40K/40K.shp";
                break;

            case 9:
                dbfFile = "Datasets/50K/50K.dbf";
                shpFile = "Datasets/50K/50K.shp";
                break;

            case 10:
                dbfFile = "Datasets/60K/60K.dbf";
                shpFile = "Datasets/60K/60K.shp";
                break;

            case 11:
                dbfFile = "Datasets/70K/70K.dbf";
                shpFile = "Datasets/70K/70K.shp";
                break;

            case 13:
                dbfFile = "Datasets/cousub/cousub.dbf";
                shpFile = "Datasets/cousub/cousub.shp";
                break;

            case 14:
                dbfFile = "Datasets/LACity/LACity.dbf";
                shpFile = "Datasets/LACity/LACity.shp";
                break;

            case 15:
                dbfFile = "Datasets/mexico/mexicojoin.dbf";
                shpFile = "Datasets/mexico/mexicojoin.shp";
                break;

        }

        // read dbf file
        FileInputStream fis = new FileInputStream(dbfFile);
        DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(),
                false, Charset.forName("ISO-8859-1"));

        while (dbfReader.hasNext()) {
            final Object[] fields = dbfReader.readEntry();
            Double populationField = Double.MIN_VALUE;
            Double householdField = Double.MIN_VALUE;

            if (dataSet == 1 || dataSet == 2) {
                //householdField = ((Number)fields[112]).longValue();
                populationField = Double.valueOf((Integer) fields[50]);
                householdField = Double.valueOf((Integer) fields[49]);
            } else if (dataSet == 0 || dataSet == 3) {
                populationField = Double.valueOf((String) fields[15]);
                householdField = Double.valueOf((String) fields[13]);
            } else if (dataSet >= 4 && dataSet <= 12) {
                populationField = Double.valueOf((Long) fields[9]);
                householdField = Double.valueOf((Long) fields[8]);
            } else if (dataSet == 13) {
                populationField = Double.valueOf((Long) fields[15]);
                householdField = Double.valueOf((Long) fields[14]);
            }

            else if (dataSet == 15) {
                populationField = Double.valueOf((Double) fields[7]);
                householdField = Double.valueOf((Double) fields[4]);
            }

            population.add(populationField);
            household.add(householdField);

        }


        if (dbfReader != null)
            dbfReader.close();
        fis.close();

        // read shape file
        File file = new File(shpFile);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

        try (FeatureIterator<SimpleFeature> features = collection.features()) {

            while (features.hasNext()) {

                SimpleFeature feature = features.next();
                //System.out.print(feature.getID());
                //System.out.print(": ");
                //System.out.println(feature.getDefaultGeometryProperty().getValue());
                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                polygons.add(polygon);
            } // end while
        } // end

        dataStore.dispose();
    } // end readFiles


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


    public static void printNeighborsList(ArrayList<List> neighbors) {

        for (List<Integer> n1 : neighbors) {

            System.out.print(neighbors.indexOf(n1) + ": ");

            for (Integer n2 : n1) {
                System.out.print(n2 + ", ");
            } // end for

            System.out.println();
        } // end for
    } // end printNeighborsList


    public static ArrayList<DataPartition> partitionData(int nColumns,
                                                         int nRows,
                                                         ArrayList<Geometry> polygons,
                                                         ArrayList<Integer> areas,
                                                         ArrayList<List> neighbors) throws IOException {

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


    public static ArrayList<Integer> listTraversal(HashMap<Integer, Boolean> visitedAreas,
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


    public static void unassignedAssignment(ArrayList<List> neighbors,
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


    public static void writingDataPartitionsToFile(ArrayList<DataPartition> dataPartitions,
                                                   ArrayList<Geometry> polygons) throws IOException {

        for (DataPartition partition : dataPartitions) {

            FileWriter outputFile1 = new FileWriter(new File("/Users/hessah/Desktop/Research/Output/1DataPartitions/partition" + partition.getID() + ".csv"));
            CSVWriter csv1 = new CSVWriter(outputFile1);
            String[] header1 = {"Area ID", "Area Polygon in WKT"};
            csv1.writeNext(header1);

            ArrayList<Integer> partitionAreas = partition.getAreas();

            for (int i = 0; i < partitionAreas.size(); i++) {

                String areaID = partitionAreas.get(i).toString();

                String[] row = {areaID, polygons.get(partitionAreas.get(i)).toText()};
                csv1.writeNext(row);
            }
            csv1.close();
        }
    } // end writingDataPartitionsToFile


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


    public static void printPartitionsNeighborsList(HashMap<Integer, HashMap<Integer, List>> partitionsNeighbors) {

        for (Integer partitionID : partitionsNeighbors.keySet()) {

            HashMap<Integer, List> partitionNeighbors = partitionsNeighbors.get(partitionID);

            System.out.println("Partition (" + partitionID + ") neighbors");

            for (Integer area : partitionNeighbors.keySet()) {

                System.out.println(area + ": " + partitionNeighbors.get(area));
            }
        }
    } // end printPartitionsNeighborsList


    public static void writingPartitionsToFile(ArrayList<Partition> partitionsBeforeEnclaves,
                                               ArrayList<Geometry> polygons,
                                               String path) throws IOException {

        for (int i = 0; i < partitionsBeforeEnclaves.size(); i++) {
            FileWriter outputFile = new FileWriter(new File(path + partitionsBeforeEnclaves.get(i).getPartitionID() + ".csv"));
            CSVWriter csv = new CSVWriter(outputFile);

            String[] header = {"Region ID", "Area ID", "Area Polygon in WKT"};
            csv.writeNext(header);

            HashMap<Integer, Region> regionsList = partitionsBeforeEnclaves.get(i).getRegions();

            for (Integer ID : regionsList.keySet()) {

                ArrayList<Integer> areasList = regionsList.get(ID).getAreas();
                int integerID = regionsList.get(ID).getID();
                String regionID = String.valueOf(integerID);

                for (int area : areasList) {

                    String[] row = {regionID, String.valueOf(area), polygons.get(area).toText()};
                    csv.writeNext(row);
                }
            }
            csv.close();
        }

    } // end writingPartitionsToFile


    public static int selectRandomArea(ArrayList<Integer> areas) {

        return areas.get(rand.nextInt(areas.size()));

    } // end selectRandomArea


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


    public static boolean checkSpatialContiguity(ArrayList<Integer> areas,
                                                 ArrayList<List> neighbors) {

        boolean connected = true;

        int numOfRegions = 0;

        HashMap<Integer, Boolean> visitedAreas = new HashMap<>();

        // fill the hash map (key: area, value: false)
        for (int i = 0; i < areas.size(); i++) {

            visitedAreas.put(areas.get(i), false);

        } // end for

        for (Integer area : visitedAreas.keySet()) {

            if (visitedAreas.get(area) == false) {

                listTraversal(visitedAreas, area, areas, neighbors);
                numOfRegions++;

                if (numOfRegions > 1) {
                    connected = false;
                    break;
                } //end if
            } // end if
        } // end for

        //System.out.println("numOfRegions: " + numOfRegions);
        return connected;
    }


    public static void listTraversal(HashMap<Integer,
            Boolean> visitedAreas,
                                     int area,
                                     ArrayList<Integer> areas,
                                     ArrayList<List> neighbors) {

        visitedAreas.put(area, true);

        List<Integer> neighborsList = neighbors.get(area);

        for (Integer neighbor : neighborsList) {

            if (areas.contains(neighbor)) {

                if (visitedAreas.get(neighbor) == false) {

                    listTraversal(visitedAreas, neighbor, areas, neighbors);

                } // end if
            } // end if
        } // end for
    }


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

    public static void DFS(int u , int[] disc, int[] low, int[] parent, boolean[] articulation_label, int time,
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


    public static HashMap<Integer, Integer> parallel_search_movable_units(Partition currentP,
                                                                          ArrayList<List> neighbors,
                                                                          int cores) throws InterruptedException {

        HashMap<Integer, Region> regions = new HashMap<>(currentP.getRegions());
        HashMap<Integer, Integer> movableAreasWithRegions = new HashMap<>();
        ReentrantLock lock = new ReentrantLock();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);

        ArrayList<ParallelMovableUnitsSearch> tasks = new ArrayList<>();
        for (Integer id : regions.keySet()) {
            tasks.add(new ParallelMovableUnitsSearch(regions.get(id), movableAreasWithRegions, lock, neighbors));
        }

        for(ParallelMovableUnitsSearch task : tasks)
        {
            threadPool.execute(task);
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e) {}

        //System.out.println(currentP.getPartitionID()+" "+movableAreasWithRegions.keySet());

        return movableAreasWithRegions;
    }



    static class ParallelMovableUnitsSearch extends Thread
    {
        Region r;
        ReentrantLock lock;
        ArrayList<Integer> areas_in_r;
        int time;
        ArrayList<List> neighbors;
        HashMap<Integer, Integer> allMovableAreasWithRegions;

        public ParallelMovableUnitsSearch(Region r ,
                                          HashMap<Integer, Integer> allMovableAreasWithRegions,
                                          ReentrantLock lock,
                                          ArrayList<List> neighbors)
        {
            this.r = r;
            this.lock = lock;
            areas_in_r = r.getAreas();
            time = 0;
            this.neighbors = neighbors;
            this.allMovableAreasWithRegions = allMovableAreasWithRegions;

        }

        public void run()
        {
            ArrayList<Integer> r_articulation_pts = findAPs_Tarjan();

            ArrayList<Integer> r_non_articulation_pts = new ArrayList<>(r.getAreas());
            r_non_articulation_pts.removeAll(r_articulation_pts);
            HashMap<Integer, Integer> rMovableAreasWithRegions = new HashMap<>();

            for(Integer pt : r_non_articulation_pts){
                rMovableAreasWithRegions.put(pt, r.getID());
            }

            lock.lock();
            allMovableAreasWithRegions.putAll(rMovableAreasWithRegions);
            lock.unlock();
        }

        //tarjan's algorithm that finds all the articulation points(areas that do not disconnect the region)from a region
        private ArrayList<Integer> findAPs_Tarjan()
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
                    DFS(i , disc , low, parent , articulation_label);
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

        private void DFS(int u , int[] disc, int[] low, int[] parent, boolean[] articulation_label)
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
                        DFS(v , disc , low , parent , articulation_label);
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
    }


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


    public static void readData(String s_str,
                                String d_str,
                                String polygons_str,
                                ArrayList<Double> household,
                                ArrayList<Double> population,
                                ArrayList<Geometry> polygons) throws ParseException {

        String[] s_str_array = s_str.split("h");
        String[] d_str_array = d_str.split("h");
        String[] polygons_str_array = polygons_str.split("h");

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);

        for (int i = 0; i < s_str_array.length; i++)
        {

            household.add(Double.valueOf(s_str_array[i]));

            population.add(Double.valueOf(d_str_array[i]));

            Geometry polygon = (Geometry) reader.read(polygons_str_array[i]);
            polygons.add(polygon);
        }

    } // end readData


    public static ArrayList<List> readNeighborhoodGraph(String neighborhood_str) {

        ArrayList<List> neighbors = new ArrayList<>();

        String[] neighborhood_str_array = neighborhood_str.split("h");

        for (int i = 0 ; i < neighborhood_str_array.length; i++)
        {
            String[] key_value = neighborhood_str_array[i].split("=");
            int key = Integer.parseInt(key_value[0]);
            String[] values = key_value[1].split(",");
            List<Integer> neighborsList = new ArrayList<>();

            for (int j = 0; j < values.length; j++)
            {
                neighborsList.add(Integer.parseInt(values[j]));
            }

            neighbors.add(neighborsList);
        }

        //System.out.println(neighbors);
        return neighbors;
    } // end readNeighbrhoodGraph


    public static HashMap<Integer, Set<Integer>> execute_neighbors(String shpFile) throws IOException {

        ArrayList<Geometry> polygons = new ArrayList<>();


        File file = new File(shpFile);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

        try (FeatureIterator<SimpleFeature> features = collection.features()) {

            while (features.hasNext()) {

                SimpleFeature feature = features.next();
                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                polygons.add(polygon);
            } // end while
        } // end
        dataStore.dispose();


        ArrayList<List> neighbors = createNeighborsList(polygons);


        HashMap<Integer, Set<Integer>> neighbors_map = new HashMap<>();
        for (int i = 0; i < neighbors.size(); i++)
        {
            Set<Integer> neighbors_list = new HashSet<>(neighbors.get(i));
            neighbors_map.put(i, neighbors_list);
        }


        polygons.clear();
        neighbors.clear();


        return  neighbors_map;
    }


    public static Object[] execute_SMPP(int cores,
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
                                    ArrayList<String> polygons_strings) throws ParseException, IOException {

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
        Partition bestP;
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
        bestP = modifiedSA(lengthTabu, convSA, alpha, t, threshold, bestFeasiblePartition, household, population, neighbors);

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



    public static void main(String[] args) throws IOException, InterruptedException, ParseException {


        // arguments for the jar file execution

        /*int cores = Integer.parseInt(args[0]);
        int nRows = Integer.parseInt(args[1]);
        int nColumns = Integer.parseInt(args[2]);
        double threshold = Double.parseDouble(args[3]);
        int maxItr = Integer.parseInt(args[4]);
        int lengthTabu = Integer.parseInt(args[5]);
        double t = Integer.parseInt(args[6]);
        double alpha = Double.parseDouble(args[7]);
        int convSA = Integer.parseInt(args[8]);
        int random = Integer.parseInt(args[9]);
        String s_str = args[10];
        String d_str = args[11];
        String neighbrhood_str = args[12];
        String polygons_str = args[13];*/


        int maxItr = 40;
        double threshold = 250000000;
        //double threshold = 1;
        int lengthTabu = 100;
        double t = 1;
        int convSA = 90;
        double alpha = 0.9;
        int dataset = 11;
        int nRows = 2;
        int nColumns = 2;
        int cores = 4;
        int random = 0;



        System.out.println("MI: " + maxItr);
        System.out.println("Threshold: " + threshold);
        System.out.println("NI: " + convSA);
        System.out.println("alpha: " + alpha);
        //System.out.println("Dataset: " + dataset);
        System.out.println("Rows: " + nRows);
        System.out.println("Columns: " + nColumns);
        System.out.println("Cores: " + cores);
        System.out.println("Random: " + random);
        System.out.println();




        // ********************************************************************
        // **** READING FILES ****
        // ********************************************************************

        //System.out.println("-----------------------------------------------------------------------------------");
        //System.out.println("Reading files . . .");
        //System.out.println("-----------------------------------------------------------------------------------");

        long startReadingFile = System.currentTimeMillis();

        // dissimilarity attribute col#9 j AWATER
        ArrayList<Double> population = new ArrayList<>();
        // spatially extensive attribute col#8 i ALAND
        ArrayList<Double> household = new ArrayList<>();
        // areas as polygons
        ArrayList<Geometry> polygons = new ArrayList<>();

        readFiles(dataset, household, population, polygons);
        //readData(s_str, d_str, polygons_str, household, population, polygons);

        ArrayList<Integer> areas = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            areas.add(i);
        }

        //System.out.println("Number of areas: " + areas.size());

        long endReadingFile = System.currentTimeMillis();
        float totalReadingFile = (endReadingFile - startReadingFile) / 1000F;
        //System.out.println("Total time for Reading the Files is : " + totalReadingFile + "\n\n");


        // ********************************************************************
        // **** FINDING THE NEIGHBORS FOR EACH AREA ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Finding neighbors . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startFindingNeighbors = System.currentTimeMillis();

        ArrayList<List> neighbors = createNeighborsList(polygons);
        //ArrayList<List> neighbors = readNeighborhoodGraph(neighbrhood_str);

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

        ArrayList<DataPartition> dataPartitions = partitionData(nColumns, nRows, polygons, areas, neighbors);

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

        HashMap<Integer, HashMap<Integer, List>> partitionsNeighbors = createNeighborsList(neighbors, dataPartitions);

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
        Partition bestP;
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

        double improvement;
        double oldHeterogeneity;

        oldHeterogeneity = bestFeasiblePartition.getDissimilarity();
        bestP = modifiedSA(lengthTabu, convSA, alpha, t, threshold, bestFeasiblePartition, household, population, neighbors);

        double pH = bestP.getDissimilarity();

        long endSearch = System.currentTimeMillis();
        float totalSearch = (endSearch - startSearch) / 1000F;
        System.out.println("Total time for Local Search is : " + totalSearch);

        improvement = oldHeterogeneity - pH;
        float percentage = ((float)improvement/(float)oldHeterogeneity);

        //System.out.printf("Heterogeneity before local search: %.0f\n", oldHeterogeneity);
        System.out.printf("Heterogeneity after local search: %.0f\n" ,  pH);
        //System.out.printf("Improvement in heterogeneity: %.0f\n" , improvement);
        //System.out.println("Percentage of improvement: " + percentage);
        System.out.println("Max-p: " + maxP);


        float totalTime = totalFindingNeighbors + totalFindingNeighbors1 + totalPartitioning + totalGrowRegions + totalAssignEnclaves + totalSearch;
        //System.out.println("\nTotal time is : " + (totalTime));

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

        String list = "";
        for (int i = 0; i < area_region.length; i++)
        {
            if (!list.isEmpty())
                list = list + ",";
            list = list + area_region[i];
        }
        //System.out.print(list);


    } // end main
} // end class SMPPPinapple
