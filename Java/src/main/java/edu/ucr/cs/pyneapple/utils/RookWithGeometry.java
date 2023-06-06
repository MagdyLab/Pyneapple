package edu.ucr.cs.pyneapple.utils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static edu.ucr.cs.pyneapple.regionalization.SMPPPythonInterface.stringListToGeometryList;

public class RookWithGeometry {
    public static void main(String args[]) throws IOException, ParseException {
        //SGWithGeoString();
        //SGWithShpfile();
    }
    static void SGWithGeoString() throws IOException, ParseException {
        String filePath = "C:/Users/50476/Documents/Pineapple/data/gStrings.txt";
        File file = new File(filePath);
        Scanner myReader = new Scanner(file);
        ArrayList<String> gString = new ArrayList<>();
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            gString.add(data);
        }
        ArrayList<Geometry> polygons;
        polygons = stringListToGeometryList(gString);
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = - Double.POSITIVE_INFINITY, maxY = -Double.POSITIVE_INFINITY;
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
        System.out.println(polygons.size());
        System.out.println(minX + " " + minY + " " + maxX  + " " + maxY);
        //System.out.println(polygons.get(212));
        SpatialGrid sg = new SpatialGrid(minX, minY, maxX, maxY);
        sg.creatreIndexWithGeometry(45, polygons);
        sg.RookWithGeometry(polygons);
        //sg.printIndex();
    }
    static void SGWithShpfile() throws IOException {
        String fileName = "C:/Users/50476/Documents/Pineapple/data/LACounty/la_county_noisland.shp";
        //SpatialGrid sg = new SpatialGrid();
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
        Double minAttrMin = Double.POSITIVE_INFINITY;
        Double minAttrMax = -Double.POSITIVE_INFINITY;
        Double maxAttrMax = -Double.POSITIVE_INFINITY;
        Double maxAttrMin = Double.POSITIVE_INFINITY;
        double sumMin = Double.POSITIVE_INFINITY;
        int count = 0;
        Double avgTotal = 0.0;
        Double sumTotal = 0.0;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                //System.out.print(feature.getID());
                //System.out.print(": ");


                //System.out.println(feature.getID());
                idList.add(Integer.parseInt(feature.getID().split("\\.")[1]) - 1);
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
            features.close();

            //sg.printIndex();
        }
        dataStore.dispose();
        avgTotal = avgTotal / count;


        //Feasibility checking
        // (1)The situation for AVG will change after removing infeasible areas
        // (2) Even when avgTotal does not lie with in avgAttrMin and avgAttrMax, the algorithm will


        //System.out.println(minX + " " + minY + ", " + maxX + " " + maxY);
        double rookstartTime = System.currentTimeMillis()/ 1000.0;
        //System.out.println("Time for reading the file: " + (rookstartTime - startTime));

        System.out.println(minX + " " + minY + " " + maxX  + " " + maxY);
        //System.out.println(geometries.size());
        SpatialGrid sg = new SpatialGrid(minX, minY, maxX, maxY);
        ArrayList<Geometry> geometries = new ArrayList<>();
        for(int i = 0; i < fList.size(); i++){
            geometries.add((Geometry)fList.get(i).getDefaultGeometry());

        }
        System.out.println(geometries.size());
        System.out.println(geometries.get(212));
        sg.creatreIndexWithGeometry(45, geometries);
        sg.RookWithGeometry(geometries);
        //sg.printIndex();

    }
}
