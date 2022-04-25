package edu.ucr.cs.pineapple.utils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.MultiPoint;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SpatialGrid{

    double minX, minY, maxX, maxY;
    private Map<Integer, Set<Integer>> grid_area;
    private Map<Integer, Set<Integer>> area_grid;
    Map<Integer, Set<Integer>> RookNeighbors;
    public SpatialGrid(double minx, double miny, double maxx, double maxy){
        minX = minx;
        minY = miny;
        maxX = maxx + 1;
        maxY = maxy + 1;
        grid_area = new HashMap<Integer, Set<Integer>>();
        area_grid = new HashMap<Integer, Set<Integer>>();
    }
    public SpatialGrid(){
        minX = -1;
        minY = -1;
        maxX = -1;
        maxY = -1;
        grid_area = new HashMap<Integer, Set<Integer>>();
        area_grid = new HashMap<Integer, Set<Integer>>();
    }
    public void creatreIndexWithGeometry(int n, List<Geometry> polygons){
        this.minX = Double.POSITIVE_INFINITY;
        this.minY = Double.POSITIVE_INFINITY;
        this.maxX = - Double.POSITIVE_INFINITY;
        this.maxY = -Double.POSITIVE_INFINITY;
        for(int i = 0; i < polygons.size(); i++){
            Geometry geometry = polygons.get(i);
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
        }
        double widthStep = (maxX - minX) / n;
        double heightStep = (maxY - minY) / n;
        int listLength = polygons.size();
        for(int i = 0; i < listLength; i++){
            //SimpleFeature feature = fList.get(i);
            Geometry geometry = polygons.get(i);
            double cminx = geometry.getEnvelope().getCoordinates()[0].getX();
            double cminy = geometry.getEnvelope().getCoordinates()[0].getY();
            double cmaxx = geometry.getEnvelope().getCoordinates()[2].getX();
            double cmaxy = geometry.getEnvelope().getCoordinates()[2].getY();
            if(cminx < minX || cmaxx > maxX || cminy < minY || cmaxy > maxY){
                System.out.println("Error!");
            }

            int cminGridIndexX = (int) ((cminx - minX) / widthStep);
            int cminGridIndexY = (int) ((cminy - minY) / heightStep);
            int cmaxGridIndexX = (int) ((cmaxx - minX) / widthStep);
            int cmaxGridIndexY = (int) ((cmaxy - minY) / heightStep);
            /*if(i == 545){
                System.out.println("545: " + ((cminx - minX) / widthStep) + " " + ((cminy - minY) / heightStep) + " " +((cmaxx - minX) / widthStep) + " " + ((cmaxy - minY) / heightStep));
            }*/
            for (int x = cminGridIndexX; x <= cmaxGridIndexX; x++){
                for(int y = cminGridIndexY; y <= cmaxGridIndexY; y++){
                    int key = y *n + x;
                    /*int id = Integer.parseInt(feature.getID().split("\\.")[1]) - 1;
                    if(id != i)
                        System.out.println("ID != I");*/
                    if(grid_area.containsKey(key)){
                        grid_area.get(key).add(i);
                    }else{
                        grid_area.put(key, new TreeSet<Integer>());
                        grid_area.get(key).add(i);
                    }
                    if (area_grid.containsKey(i)){
                        area_grid.get(i).add(key);
                    }else{
                        area_grid.put(i, new TreeSet<Integer>());
                        area_grid.get(i).add(key);
                    }
                }
            }
        }

    }
    public void createIndex(int n,List<SimpleFeature> fList){
        double widthStep = (maxX - minX) / n;
        double heightStep = (maxY - minY) / n;
        int listLength = fList.size();
        for(int i = 0; i < listLength; i++){
            SimpleFeature feature = fList.get(i);
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            double cminx = geometry.getEnvelope().getCoordinates()[0].getX();
            double cminy = geometry.getEnvelope().getCoordinates()[0].getY();
            double cmaxx = geometry.getEnvelope().getCoordinates()[2].getX();
            double cmaxy = geometry.getEnvelope().getCoordinates()[2].getY();
            if(cminx < minX || cmaxx > maxX || cminy < minY || cmaxy > maxY){
                System.out.println("Error!");
            }

            int cminGridIndexX = (int) ((cminx - minX) / widthStep);
            int cminGridIndexY = (int) ((cminy - minY) / heightStep);
            int cmaxGridIndexX = (int) ((cmaxx - minX) / widthStep);
            int cmaxGridIndexY = (int) ((cmaxy - minY) / heightStep);
            /*if(i == 545){
                System.out.println("545: " + ((cminx - minX) / widthStep) + " " + ((cminy - minY) / heightStep) + " " +((cmaxx - minX) / widthStep) + " " + ((cmaxy - minY) / heightStep));
            }*/
            for (int x = cminGridIndexX; x <= cmaxGridIndexX; x++){
                for(int y = cminGridIndexY; y <= cmaxGridIndexY; y++){
                    int key = y *n + x;
                    int id = Integer.parseInt(feature.getID().split("\\.")[1]) - 1;
                    if(id != i)
                        System.out.println("ID != I");
                    if(grid_area.containsKey(key)){
                        grid_area.get(key).add(id);
                    }else{
                        grid_area.put(key, new TreeSet<Integer>());
                        grid_area.get(key).add(id);
                    }
                    if (area_grid.containsKey(id)){
                        area_grid.get(id).add(key);
                    }else{
                        area_grid.put(id, new TreeSet<Integer>());
                        area_grid.get(id).add(key);
                    }
                }
            }
        }

    }
    public void RookWithGeometry(List<Geometry> geometries){
        creatreIndexWithGeometry(45, geometries);
        RookNeighbors = new HashMap<Integer, Set<Integer>>();
        int listLength = geometries.size();
        for(int i = 0; i < listLength; i++){
            //System.out.println(i== (Integer.parseInt(fList.get(i).getID().split("\\.")[1]) - 1));

            Set<Integer> areas = new TreeSet<Integer>();
            for(Integer g: area_grid.get(i)){
                if(g != i)
                    areas.addAll(grid_area.get(g));
            }
            Geometry pGeometry = geometries.get(i);
            for(Integer aid: areas){
                if(aid == i)
                    continue;
                Boolean intersects = pGeometry.intersects((Geometry)geometries.get(aid));
                Geometry intersection = pGeometry.intersection((Geometry)geometries.get(aid));
                if( intersects && !(intersection instanceof  Point || intersection instanceof  MultiPoint)){
                    //System.out.println(intersection.getClass().getName());
                    if (RookNeighbors.containsKey(i)) {
                        RookNeighbors.get(i).add(aid);
                    }
                    else{
                        RookNeighbors.put(i, new TreeSet<Integer>());
                        RookNeighbors.get(i).add(aid);
                    }

                    if(RookNeighbors.containsKey(aid))
                        RookNeighbors.get(aid).add(i);
                    else{
                        RookNeighbors.put(aid, new TreeSet<Integer>());
                        RookNeighbors.get(aid).add(i);
                    }

                }
            }
        }
    }

    public static ArrayList<List> RookWithGeometryNoGrid(ArrayList<Geometry> polygons) {

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
    }

    public void calculateContiguity(List<SimpleFeature> fList){
        RookNeighbors = new HashMap<Integer, Set<Integer>>();
        int listLength = fList.size();
        for(int i = 0; i < listLength; i++){
            //System.out.println(i== (Integer.parseInt(fList.get(i).getID().split("\\.")[1]) - 1));

            Set<Integer> areas = new TreeSet<Integer>();
            for(Integer g: area_grid.get(i)){
                if(g != i)
                    areas.addAll(grid_area.get(g));
            }
            Geometry pGeometry = (Geometry)fList.get(i).getDefaultGeometry();
            for(Integer aid: areas){
                if(aid == i)
                    continue;
                Boolean intersects = pGeometry.intersects((Geometry)fList.get(aid).getDefaultGeometry());
                Geometry intersection = pGeometry.intersection((Geometry)fList.get(aid).getDefaultGeometry());
                if( intersects && !(intersection instanceof  Point || intersection instanceof  MultiPoint)){
                    //System.out.println(intersection.getClass().getName());
                    if (RookNeighbors.containsKey(i)) {
                        RookNeighbors.get(i).add(aid);
                    }
                    else{
                        RookNeighbors.put(i, new TreeSet<Integer>());
                        RookNeighbors.get(i).add(aid);
                    }

                    if(RookNeighbors.containsKey(aid))
                        RookNeighbors.get(aid).add(i);
                    else{
                        RookNeighbors.put(aid, new TreeSet<Integer>());
                        RookNeighbors.get(aid).add(i);
                    }

                }
            }
        }
    }
    public Set<Integer> getNeighbors(Integer id){

        if(RookNeighbors.containsKey(id)){
            return RookNeighbors.get(id);
        }else{
            return new TreeSet<>();
        }

    }

    public void printIndex(){
        System.out.println("Grid-Area:");
        for(Map.Entry<Integer, Set<Integer>> entry: grid_area.entrySet()){
            System.out.print(entry.getKey() + ": ");
            for(Integer i: entry.getValue()){
                System.out.print(i + ", ");
            }
            System.out.println();
        }
        System.out.println("Area-Grid:");
        for(Map.Entry<Integer, Set<Integer>> entry: area_grid.entrySet()){
            System.out.print(entry.getKey() + ": ");
            for(Integer i: entry.getValue()){
                System.out.print(i + ", ");
            }
            System.out.println();
        }

    }
    public void setNeighbors(Map<Integer, Set<Integer>> n){
        this.RookNeighbors = n;
    }
    public static void main(String args[]) throws IOException {
        File file = new File("data/merged_noisland.shp");
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
        ArrayList<SimpleFeature> fList = new ArrayList<>();
        ArrayList<Integer> idList = new ArrayList<>();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = - Double.POSITIVE_INFINITY, maxY = -Double.POSITIVE_INFINITY;
        int minXA = -1, minYA = -1, maxXA = -1, maxYA = -1;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                int id = Integer.parseInt(feature.getID().split("\\.")[1]) - 1;
                //System.out.print(feature.getID());
                //System.out.print(": ");
                fList.add(feature);
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                double cminx = geometry.getEnvelope().getCoordinates()[0].getX();
                double cminy = geometry.getEnvelope().getCoordinates()[0].getY();
                double cmaxx = geometry.getEnvelope().getCoordinates()[2].getX();
                double cmaxy = geometry.getEnvelope().getCoordinates()[2].getY();
                if (minX > cminx){
                    minX = cminx;
                    minXA = id;
                }
                if (minY > cminy){
                    minY = cminy;
                    minYA = id;
                }
                if (maxX < cmaxx){
                    maxX = cmaxx;
                    maxXA = id;
                }
                if (maxY < cmaxy){
                    maxY = cmaxy;
                    maxYA = id;
                }
                idList.add(Integer.parseInt(feature.getID().split("\\.")[1]) - 1);
            }

            Geometry g1309 = (Geometry) fList.get(1309).getDefaultGeometry();
            Geometry g1302 = (Geometry) fList.get(1302).getDefaultGeometry();
            Geometry intersection = g1309.intersection(g1302);
            System.out.println(intersection.getClass().getName());
            //System.out.println(fList.get(1309).getAttribute("geoid"));
            //System.out.println(fList.get(1302).getAttribute("geoid"));
            //System.out.println(minXA + " " + minYA + ", " + maxXA + " " + maxYA);
            long startTime = System.currentTimeMillis();
            SpatialGrid sg = new SpatialGrid(minX, minY, maxX, maxY);
            sg.createIndex(50, fList);
            sg.calculateContiguity(fList);
            long endTime = System.currentTimeMillis();
            /*System.out.println("Rook time: " + (endTime - startTime));
            for(int i = 0; i < fList.size(); i++){
                System.out.println(sg.getNeighbors(i));
                //arr[i] = i;
            }*/
            FileWriter fw = new FileWriter("SG.txt", true);
            PrintWriter out = new PrintWriter(fw);
            for(int i = 0; i < fList.size(); i++){
                out.println(sg.getNeighbors(i));
                //arr[i] = i;
            }
            out.close();
            //sg.printIndex();
        }
    }
}
