package edu.ucr.cs.pyneapple.utils.PRUCUtils;

/**
 * This class implements the basic operation of moving an area from a region to another region
 */

public class Move{
    Area area;
    Region donor;
    Region receiver;

    /**
     * The Move of an area from a donor region to a receiver region
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

    /**
     * The area to be moved
     * @return the area to be moved
     */
    public Area get_area()
    {
        return area;
    }

    /**
     * Get the donor region where the area to be moved comes from
     * @return the donor region
     */
    public Region get_donor()
    {
        return donor;
    }

    /**
     * Get the receiver region where the area is going to be reassigned to
     * @return the receiver region
     */
    public Region getReceiver()
    {
        return receiver;
    }


    public boolean equals(Object obj) {
        Move m = (Move)obj;
        return (area.equals(m.get_area())) && (receiver.equals(m.getReceiver())) && (donor.equals(m.get_donor()));
    }

}