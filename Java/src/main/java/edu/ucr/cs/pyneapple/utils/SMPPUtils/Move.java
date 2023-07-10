package edu.ucr.cs.pyneapple.utils.SMPPUtils;

/**
 * Move class describes the Move object used in the local search
 */
public class Move {

    private int recipientRegion;
    private int donorRegion;
    private int movedArea;
    private double donorRegionH;
    private double recipientRegionH;
    private double hetImprovement;

    /**
     * constructor for Move
     */

    public Move() {

        this.recipientRegion = 00;
        this.donorRegion = 00;
        this.movedArea = 00;
        this.donorRegionH = 00;
        this.recipientRegionH = 00;
        this.hetImprovement = 00;
    }

    /**
     * copies a Move object
     * @param move Move object
     */
    public Move(Move move) {

        this.recipientRegion = move.recipientRegion;
        this.donorRegion = move.donorRegion;
        this.movedArea = move.movedArea;
        this.donorRegionH = move.donorRegionH;
        this.recipientRegionH = move.recipientRegionH;
        this.hetImprovement = move.hetImprovement;
    }

    /**
     * sets the move's recipient region
     * @param region recipient region
     */
    public void setRecipientRegion(int region) {

        this.recipientRegion = region;
    }

    /**
     * sets the move's donor region
     * @param region donor region
     */

    public void setDonorRegion(int region) {

        this.donorRegion = region;
    }

    /**
     * sets the move's moved area
     * @param area moved area
     */

    public void setMovedArea(int area) {

        this.movedArea = area;
    }

    /**
     * sets the dissimilarity of the donor region
     * @param donorRegionH dissimilarity of the donor region
     */
    public void setDonorRegionH(double donorRegionH) {

        this.donorRegionH = donorRegionH;
    }

    /**
     * sets the dissimilarity of the recipient region
     * @param recipientRegionH dissimilarity of the recipient region
     */

    public void setRecipientRegionH(double recipientRegionH) {

        this.recipientRegionH = recipientRegionH;
    }

    /**
     * sets the dissimilarity improvement of the move
     * @param hetImprovement dissimilarity improvement of the move
     */
    public void setHetImprovement(double hetImprovement) {

        this.hetImprovement = hetImprovement;
    }

    /**
     * gets the recipient region of the move
     * @return recipient region
     */
    public int getRecipientRegion() {

        return this.recipientRegion;
    }

    /**
     * gets the donor region of the move
     * @return donor region
     */
    public int getDonorRegion() {

        return this.donorRegion;
    }

    /**
     * gets the moved area of the move
     * @return moved area
     */
    public int getMovedArea() {

        return this.movedArea;
    }

    /**
     * gets the the dissimilarity of the donor region
     * @return dissimilarity of the donor region
     */
    public double getDonorRegionH() {

        return this.donorRegionH;
    }

    /**
     * gets the dissimilarity of the recipient region
     * @return dissimilarity of the recipient region
     */
    public double getRecipientRegionH() {

        return this.recipientRegionH;
    }

    /**
     * get the improvement of the move
     * @return improvement of the move
     */
    public double getHetImprovement() {

        return this.hetImprovement;
    }
}

