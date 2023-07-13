package edu.ucr.cs.pyneapple.utils.EMPUtils;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The utility class for reading the shapefile and loading the attributes for the EMP problem
 */
public class ShapefileReader {
    private ArrayList<Long> minAttr;
    private ArrayList<Long> maxAttr;
    private ArrayList<Long> avgAttr;
    private ArrayList<Long> sumAttr;
    private ArrayList<Long> distAttr;
    private Map<Integer, Set<Integer>> neighborMap;

    /**
     * Construct a reader to read the attributes from the shapefile
     * @param directory directory to the shapefile
     * @param minAttrName the name of the min attribute
     * @param maxAttrName the name of the max attribute
     * @param avgAttrName the name of the avg attribute
     * @param sumAttrName the name of the sum attribute
     * @param distAttrName the name of the dissimilarity attribute
     * @throws IOException exception when file not found
     */
    public ShapefileReader(String directory, String minAttrName, String maxAttrName, String avgAttrName, String sumAttrName, String distAttrName) throws IOException {
        File file = new File(directory);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
        minAttr = new ArrayList<>();
        maxAttr = new ArrayList<>();
        avgAttr = new ArrayList<>();
        sumAttr = new ArrayList<>();
        distAttr = new ArrayList<>();

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
        ArrayList<Geometry> geometryList = new ArrayList<>();
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
                if (Long.parseLong(feature.getAttribute(sumAttrName).toString()) < sumMin){
                    sumMin = Long.parseLong(feature.getAttribute(sumAttrName).toString());
                }
                /*if (Long.parseLong(feature.getAttribute(avgAttrName).toString()) < 0){
                    System.out.println("AVG attribute contains negative value(s)");
                    return;
                }
                if (Long.parseLong(feature.getAttribute(sumAttrName).toString()) < 0){
                    System.out.println("SUM attribute contains negative value(s)");
                    return;
                }*/
                if (Long.parseLong(feature.getAttribute(minAttrName).toString()) < minAttrMin){
                    minAttrMin = Double.parseDouble(feature.getAttribute(minAttrName).toString());
                }
                if (Long.parseLong(feature.getAttribute(minAttrName).toString()) > minAttrMax){
                    minAttrMax = Double.parseDouble(feature.getAttribute(minAttrName).toString());
                }
                if(Long.parseLong(feature.getAttribute(maxAttrName).toString()) > maxAttrMax){
                    maxAttrMax = Double.parseDouble(feature.getAttribute(maxAttrName).toString());
                }
                if(Long.parseLong(feature.getAttribute(maxAttrName).toString()) < maxAttrMin){
                    maxAttrMin = Double.parseDouble(feature.getAttribute(maxAttrName).toString());
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
                geometryList.add(geometry);
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
        /*if(minAttrMin > minAttrHigh|| minAttrMax < minAttrLow|| maxAttrMin > maxAttrHigh || maxAttrMax < maxAttrLow||  sumMin > sumAttrHigh || sumTotal < sumAttrLow || count < countLow){
            System.out.println("The constraint settings are infeasible. The program will terminate immediately.");
            System.exit(1);
        }
        if(minAttrMin > minAttrHigh){
            System.out.println("There is no area satisfying the MIN <=. The program will terminate immediately.");
            System.exit(1);
        }else if(minAttrMax < minAttrLow){
            System.out.println("There is no area satisfying the MIN >=. The program will terminate immediately.");
            System.exit(1);
        }*/
        //SpatialGrid sg = new SpatialGrid(minX, minY, maxX, maxY);
        //sg.createIndex(45, fList);
        //sg.calculateContiguity(fList);
        neighborMap = SpatialGrid.calculateNeighbors(geometryList);
        //sg.setNeighbors(neighborMap);
    }

    /**
     * Get the list of min attribute values
     * @return the list of min attribute values
     */
    public ArrayList<Long> getMinAttr(){return this.minAttr;}

    /**
     * Get the list of max attribute values
     * @return the list of max attribute values
     */
    public ArrayList<Long> getMaxAttr(){return this.maxAttr;}

    /**
     * Get the list of avg attribute values
     * @return the list of avg attribute values
     */
    public ArrayList<Long> getAvgAttr(){return this.avgAttr;}

    /**
     * Get the list of sum attribute values
     * @return the list of sum attribute values
     */
    public ArrayList<Long> getSumAttr() {
        return sumAttr;
    }

    /**
     * Get the list of dissimilarity attribute values
     * @return the list of dissimilarity attribute values
     */
    public ArrayList<Long> getDistAttr() {
        return distAttr;
    }

    /**
     * Get the set of neighbor areas for each area
     * @return a map with the area id as the key and the set of neighbor areas as the value
     */
    public Map<Integer, Set<Integer>> getNeighborMap() {
        return neighborMap;
    }

}
