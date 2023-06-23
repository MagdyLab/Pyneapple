package edu.ucr.cs.pyneapple.utils.PRUCUtils;

/**
 * This class implements the basic operation of moving an area from a region to another region
 */

public class Move{
    Area area;
    Region donor;
    Region receiver;

    /**
     *
     * @param area The area to be moved
     * @param donor The region that donates the area
     * @param receiver The region that receives the area
     */
    public Move(Area area, Region donor, Region receiver)
    {
        this.area = area;
        this.donor = donor;
        this.receiver = receiver;
    }

    public Area get_area()
    {
        return area;
    }

    public Region get_donor()
    {
        return donor;
    }

    public Region getReceiver()
    {
        return receiver;
    }


    public boolean equals(Object obj) {
        Move m = (Move)obj;
        return (area.equals(m.get_area())) && (receiver.equals(m.getReceiver())) && (donor.equals(m.get_donor()));
    }

}