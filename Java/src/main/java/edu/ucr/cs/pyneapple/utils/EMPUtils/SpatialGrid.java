package edu.ucr.cs.pyneapple.utils.EMPUtils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.io.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * The utility class for indexing the areas and computing the conguity relationship of the areas
 */
public class SpatialGrid{

    double minX, minY, maxX, maxY;
    private Map<Integer, Set<Integer>> grid_area;
    private Map<Integer, Set<Integer>> area_grid;
    Map<Integer, Set<Integer>> RookNeighbors;

    /**
     * The construction that initializes a bounding box that bounds the whole area set based on the coordinate of the four corners.
     * @param minx The minimum x coordinate.
     * @param miny The minimum y coordinate.
     * @param maxx The maximum x coordinate.
     * @param maxy The maximum y coordinate.
     */
    public SpatialGrid(double minx, double miny, double maxx, double maxy){
        minX = minx;
        minY = miny;
        maxX = maxx + 1;
        maxY = maxy + 1;
        grid_area = new HashMap<Integer, Set<Integer>>();
        area_grid = new HashMap<Integer, Set<Integer>>();
    }

    /**
     * The default constructor that initializes an empty spatial grid bounding box.
     */
    public SpatialGrid(){
        minX = -1;
        minY = -1;
        maxX = -1;
        maxY = -1;
        grid_area = new HashMap<Integer, Set<Integer>>();
        area_grid = new HashMap<Integer, Set<Integer>>();
    }

    /**
     * Compute the set of neighbors for each area
     * @param polygons The geometry polygons of each area
     * @return A hash map with the id of the areas as the key and the set of their neighbor areas as the value.
     */
    public static HashMap<Integer, Set<Integer>> calculateNeighbors(ArrayList<Geometry> polygons) {

        HashMap<Integer, Set<Integer>> neighborMap = new HashMap<>();

        for (int i = 0; i < polygons.size(); i++) {

            neighborMap.put(i, new TreeSet<>());
        }


        for (int i = 0; i < polygons.size(); i++) {

            for (int j = i + 1; j < polygons.size(); j++) {

                if (polygons.get(i).intersects(polygons.get(j))) {

                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));

                    if (intersection.getGeometryType() != "Point") {

                        neighborMap.get(i).add(j);
                        neighborMap.get(j).add(i);

                    } // end if
                } // end if
            } // end for
        } // end for

        return neighborMap;
    }

    void creatreIndexWithGeometry(int n, List<Geometry> polygons){
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
    void createIndex(int n,List<SimpleFeature> fList){
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
    void RookWithGeometry(List<Geometry> geometries){
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

    /**
     * The function for computing the Rook without specifying grids. Mainly used by the python interface.
     * @param polygons An arraylist of geometries
     * @return An arraylist of lists. Each list contains the neighbor area of the corresponding area
     */
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

    void calculateContiguity(List<SimpleFeature> fList){
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

    /**
     * Get the set of neighbor areas for the given area
     * @param id the id of the area
     * @return the set of neighbor areas for the given area
     */
    public Set<Integer> getNeighbors(Integer id){

        if(RookNeighbors.containsKey(id)){
            return RookNeighbors.get(id);
        }else{
            return new TreeSet<>();
        }

    }

    void printIndex(){
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

    /**
     * Set the neighbor map
     * @param n the new neighborhood relationship map
     */
    public void setNeighbors(Map<Integer, Set<Integer>> n){
        this.RookNeighbors = n;
    }

    /**
     * From SMPPythonInterface, interprets the geometry from a wktstring
     * @param wktString a string for the geometry
     * @return the geometry
     * @throws ParseException Exception when the WKTReader parses the string with a wrong format
     */
    public static Geometry stringToGeometry(String wktString) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geom = reader.read(wktString);
        return geom;
    }

    /**From SMPPythonInterface, get a list of geometry from the strings
     * @param wktStrings a list of strings for the geometry
     * @return a list of geometries
     * @throws ParseException Exception when the WKTReader parses the string with a wrong format
     */
    public static ArrayList stringListToGeometryList(ArrayList<String> wktStrings) throws ParseException {
        ArrayList<Geometry> polygons = new ArrayList<>();
        System.out.println(wktStrings.size());
        for (String wktString:wktStrings) {
            polygons.add(stringToGeometry(wktString));
        }
        return polygons;
    }
}
