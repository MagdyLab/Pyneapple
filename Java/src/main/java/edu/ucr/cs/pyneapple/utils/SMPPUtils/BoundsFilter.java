package edu.ucr.cs.pyneapple.utils.SMPPUtils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateFilter;

import java.awt.geom.Rectangle2D;


/**
 * BoundsFilter class describes the BoundsFilter object for determining the bounds for a data partition
 */
public class BoundsFilter implements CoordinateFilter {

    double minx, miny, maxx, maxy;
    boolean first = true;


    /**
     * constructor for the BoundsFilter
     */
    public BoundsFilter() {

    }

    /**
     * overrides the filter method
     * @param c coordinates
     */

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

    /**
     * get the bounds for a data partition
     * @return bounds for a data partition
     */
    public Rectangle2D getBounds() {

        return new Rectangle2D.Double(minx, miny, maxx - minx, maxy - miny);
    }
}