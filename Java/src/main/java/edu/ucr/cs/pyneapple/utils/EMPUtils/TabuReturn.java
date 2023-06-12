package edu.ucr.cs.pyneapple.utils.EMPUtils;

/**
 * A utility calss for recording the return of the tabu.
 */
public class TabuReturn{

    /**
     * The default constructor for the EMPTabu class. The class is used like a tuple.
     */
    public TabuReturn(){}
    /**
     * The labels of the areas
     */
    public int labels[];
    /**
     * The with-in region dissimilarity value after the tabu search
     */
    public long WDS;
}
