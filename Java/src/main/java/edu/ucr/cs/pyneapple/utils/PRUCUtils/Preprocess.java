package edu.ucr.cs.pyneapple.utils.PRUCUtils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class preprocess the data by reading the data from the shapefile, creating the area class and build the neighboring relations
 */
public class Preprocess {


    /**
     * extracts the centroids of the areas, get the neighboring relationship of areas
     * @param dataset the shapefile dataset to be processed
     * @return the neighboring relationship of the polygons
     * the list of dissimilarity attributes
     * the list of extensive attributes
     * the list of centroids of areas
     * @throws IOException IOException when reading the shapefiles
     */

    /**
     * The default constructor
     */
    public Preprocess(){

    }

    /**
     * Build the neighboring relationship and centroids
     * @param dataset the input polygons
     * @return the neighborhood of each area and the centroids of areas
     * @throws IOException IOException when reading the input polygons
     */
    public static Object[] GeoSetBuilder(String dataset) throws IOException {
        ArrayList<Area> areas = new ArrayList<>();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = preprocess(dataset);
        Object[] ret = initial_construct(collection , areas, dataset);
        ArrayList<Geometry> polygons = (ArrayList<Geometry>)ret[0];
        ArrayList<Long> sumAttr = (ArrayList<Long>) ret[1];
        ArrayList<Long> disAttr = (ArrayList<Long>) ret[2];
        ArrayList<double[]> centroids  = (ArrayList<double[]>) ret[3];

        Map<Integer, Set<Integer>> neighborSet = setNeighbors(polygons);
        return new Object[]{neighborSet,disAttr, sumAttr, centroids};
    }

    private static FeatureCollection<SimpleFeatureType, SimpleFeature> preprocess(String dataset) throws IOException {

        File file = null;
        switch (dataset) {
            case "data/LACity/LACity.shp":
                file = new File("data/LACity/LACity.shp");
                break;
            case "data/LACity_negative_attr/LACity.shp":
                file = new File("data/LACity_negative_attr/LACity.shp");
                break;
        }
        //System.out.println(file.getTotalSpace());
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        dataStore.dispose();
        return source.getFeatures(filter);
    }

    private static Object[] initial_construct(FeatureCollection<SimpleFeatureType, SimpleFeature> collection , ArrayList<Area> areas, String dataset)
    {
        //initialize a set of collections class in the following

        ArrayList<Long> sumAttr = new ArrayList<>();
        ArrayList<Long> disAttr = new ArrayList<>();

        ArrayList<Geometry> polygons = new ArrayList<>();
        ArrayList<double[]> centroids = new ArrayList<>();
        int geo_index = 0;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                long extensive_attr ;
                long internal_attr;

                if(dataset.equals("data/LACity/LACity.shp"))
                {
                    extensive_attr = Long.parseLong((feature.getAttribute("pop2010").toString()));
                    internal_attr  = Long.parseLong(feature.getAttribute("households").toString());
                }

                else if(dataset.equals("data/LACity_negative_attr/LACity.shp"))
                {
                    extensive_attr = Long.parseLong((feature.getAttribute("pop2010").toString()));
                    internal_attr = Long.parseLong((feature.getAttribute("households").toString()));

                }

                else
                {
                    extensive_attr = Long.parseLong((feature.getAttribute("ALAND").toString()));
                    internal_attr  = Long.parseLong(feature.getAttribute("AWATER").toString());
                }

                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                polygons.add(polygon);
                Coordinate[] coor_array = polygon.getCoordinates();
                double total_x = 0.0;
                double total_y = 0.0;
                for (Coordinate coordinate : coor_array) {
                    total_x += coordinate.getX();
                    total_y += coordinate.getY();
                }
                double ave_x = total_x / coor_array.length;
                double ave_y = total_y / coor_array.length;

                sumAttr.add(extensive_attr);
                disAttr.add(internal_attr);
                centroids.add(new double[]{ave_x,ave_y});
                //Area newArea = new Area(geo_index , internal_attr , extensive_attr , new double[]{ave_x,ave_y});
                geo_index ++;
                //areas.add(newArea);
            }
        }

        return new Object[]{polygons, sumAttr, disAttr, centroids};

    }

    private static Map<Integer, Set<Integer>> setNeighbors(ArrayList<Geometry> polygons)
    {
        Map<Integer, Set<Integer>> neighborSet = new HashMap<>();
        for (int i = 0; i < polygons.size(); i++) {

            for (int j = i + 1; j < polygons.size(); j++) {

                if (polygons.get(i).intersects(polygons.get(j))) {

                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));




                    if (intersection.getGeometryType() != "Point")
                    {
                        if(neighborSet.containsKey(i))
                        {
                            neighborSet.get(i).add(j);
                        }

                        else
                        {
                            Set<Integer> s = new HashSet<>();
                            s.add(j);
                            neighborSet.put(i, s);
                        }



                        if(neighborSet.containsKey(j))
                        {
                            neighborSet.get(j).add(i);
                        }

                        else
                        {
                            Set<Integer> s = new HashSet<>();
                            s.add(i);
                            neighborSet.put(j, s);
                        }
                    }
                }
            }
        }

        return neighborSet;



    }
}