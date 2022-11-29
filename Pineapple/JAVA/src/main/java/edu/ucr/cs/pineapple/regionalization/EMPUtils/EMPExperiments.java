package edu.ucr.cs.pineapple.regionalization.EMPUtils;

import edu.ucr.cs.pineapple.regionalization.EMP;

public class EMPExperiments {
    static boolean debug = false;
    static boolean labelCheck = true;
    static int numOfIts = 3;
    static int randFlag[] = {1,1};
    static int rand[] = {0, 1, 2};
    static String rands[] = {"S", "R", "B"};
    static int mergeLimit = 3;
    static String testName = "RandWithMergeLimit";

    static double minTime = 0;
    static double avgTime = 0;
    static double sumTime = 0;
    static Double[] minlower = {-Double.POSITIVE_INFINITY, 2000.0,3500.0,5000.0, 2500.0, 2000.0, 1500.0, 1000.0, 500.0, 1500.0, 2500.0, 3500.0};
    static Double[] minupper = {Double.POSITIVE_INFINITY, 2000.0,3500.0, 5000.0, 3500.0, 4000.0, 4500.0, 5000.0, 2500.0, 3500.0, 4500.0, 5500.0};
    static Double[] avglower = {2500.0, 2000.0, 1500.0, 1000.0};
    static Double[] avgupper = {3500.0, 4000.0, 4500.0, 5000.0};
    static Double[] sumlower = {1000.0, 10000.0, 20000.0, 30000.0, 40000.0, 50000.0, 15000.0, 10000.0, 5000.0};
    static Double[] sumupper = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 25000.0, 30000.0, 35000.0};

    static String lac = "data/LACounty/La_county_noisland.shp";
    static String files[] = {"data/LACity/LACity.shp", "data/LACounty/La_county_noisland.shp", "data/SCA/SouthCal_noisland.shp", "data/CA/Cal_noisland.shp"};
    static String fileNames[] = {"LACity", "LACounty", "SCA", "CA"};

    static void experimentMinUpper() throws Exception{
        System.out.println("Experiment 1: MIN with a open lower bound:");

        System.out.println("M:");
        for(int i = 1; i < 4; i++){
            System.out.println("(-inf, " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 1; i < 4; i++){
            System.out.println("(-inf, " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 1; i < 4; i++){
            System.out.println("(-inf, " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 1; i < 4; i++){
            System.out.println("(-inf, " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void experimentMinLower() throws Exception{
        System.out.println("Experiment 2: MIN with a open upper bound:");

        System.out.println("M:");
        for(int i = 1; i < 4; i++){
            System.out.println("(" + minlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 1; i < 4; i++){
            System.out.println("(" + minlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 1; i < 4; i++){
            System.out.println("(" + minlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 1; i < 4; i++){
            System.out.println("(" + minlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }
    static void experimentMinRangeSize() throws Exception{
        System.out.println("Experiment 3: MIN with changing range length:");

        System.out.println("M:");
        for(int i = 4; i < 8; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 4; i < 8; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 4; i < 8; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 4; i < 8; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }
    static void experimentMinRangePos() throws Exception{
        System.out.println("Experiment 4: MIN with changing range midpoint:");

        System.out.println("M:");
        for(int i = 8; i < 12; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 8; i < 12; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 8; i < 12; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i] + ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 8; i < 12; i++){
            System.out.println("(" + minlower[i] + ", " + minupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    minlower[i],
                    minupper[i],
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }
    static void experimentSumLower() throws Exception{
        System.out.println("Experiment 5: SUM with an open upper bound:");

        System.out.println("S:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + sumlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    sumlower[i],
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + sumlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    sumlower[i],
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("AS:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + sumlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    sumlower[i],
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + sumlower[i] + ", inf)");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    sumlower[i],
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }
    static void experimentSumRange() throws Exception{
        System.out.println("Experiment 6: SUM with different range lengths:");

        System.out.println("S:");
        for(int i = 6; i < 9; i++){
            System.out.println("(" + sumlower[i] + ", " +  sumupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    sumlower[i],
                    sumupper[i],
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MS:");
        for(int i = 6; i < 9; i++){
            System.out.println("(" + sumlower[i] + ", " +  sumupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    sumlower[i],
                    sumupper[i],
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("AS:");
        for(int i = 6; i < 9; i++){
            System.out.println("(" + sumlower[i] + ", " +  sumupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    sumlower[i],
                    sumupper[i],
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 6; i < 9; i++){
            System.out.println("(" + sumlower[i] + ", " +  sumupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    sumlower[i],
                    sumupper[i],
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void experimentAvgRange() throws Exception{
        System.out.println("Experiment 7: AVG with different range lengths:");

        System.out.println("A:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + avglower[i] + ", " +  avgupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    avglower[i],
                    avgupper[i],
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }

        System.out.println("MA:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + avglower[i] + ", " +  avgupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    avglower[i],
                    avgupper[i],
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("AS:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + avglower[i] + ", " +  avgupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    avglower[i],
                    avgupper[i],
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 0; i < 4; i++){
            System.out.println("(" + avglower[i] + ", " +  avgupper[i]+ ")");
            EMP.set_input(lac,
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    avglower[i],
                    avgupper[i],
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void scalibilityWithoutAvg() throws Exception{
        System.out.println("Experiment 8: Scability for constraints without AVG:");

        System.out.println("M:");
        for(int i = 0; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("S:");
        for(int i = 0; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MS:");
        for(int i = 0; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void scalibilityWithAvgCA() throws Exception{
        System.out.println("Experiment 8: Scability for constraints without AVG:");

        System.out.println("A:");
        for(int i = 3; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input_construct(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 3; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input_construct(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("AS:");
        for(int i = 3; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input_construct(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 3; i < 4; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input_construct(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void scalibilityWithAvg() throws Exception{
        System.out.println("Experiment 8.5: Scability for constraints without AVG with CA:");

        System.out.println("A:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MA:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("AS:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MAS:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
    }

    static void scalibility3V5() throws Exception{

        System.out.println("MAS:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    1000.0,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    50000.0,
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "households"
            );
        }
        System.out.println("MMASC:");
        for(int i = 0; i < 3; i++){
            System.out.println(fileNames[i] + ": ");
            EMP.set_input(files[i],
                    "pop_16up",
                    -Double.POSITIVE_INFINITY,
                    3000.0,
                    "unemployed",
                    -Double.POSITIVE_INFINITY,
                    400.0,
                    "employed",
                    2000.0,
                    4000.0,
                    "pop2010",
                    20000.0,
                    Double.POSITIVE_INFINITY,
                    -Double.POSITIVE_INFINITY,
                    30.0,
                    "households"
            );
        }
    }

    static void selectionCriteria() throws Exception{
        System.out.println("Experiment 9: Area selection criteria");

        for(int j = 0; j < 3; j++){
            System.out.println(rands[j] + "R: ");
            randFlag[0] = rand[j];
            for(int i = 0; i < 3; i++){
                System.out.println(fileNames[i] + ": ");
                EMP.set_input(files[i],
                        "pop_16up",
                        -Double.POSITIVE_INFINITY,
                        3000.0,
                        "unemployed",
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "employed",
                        2000.0,
                        4000.0,
                        "pop2010",
                        -Double.POSITIVE_INFINITY,
                        20000.0,
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "households"
                );
            }
        }

        randFlag[0] = 1;
        for(int j = 0; j < 3; j++){
            System.out.println("R" + rands[j] + ": ");
            randFlag[1] = rand[j];
            for(int i = 0; i < 3; i++){
                System.out.println(fileNames[i] + ": ");
                EMP.set_input(files[i],
                        "pop_16up",
                        -Double.POSITIVE_INFINITY,
                        3000.0,
                        "unemployed",
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "employed",
                        2000.0,
                        4000.0,
                        "pop2010",
                        -Double.POSITIVE_INFINITY,
                        20000.0,
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "households"
                );
            }
        }

    }

    static void mergeLimit() throws Exception{
        System.out.println("Experiment 10: Merge limit");
        for (int j = 0; j < 4; j++){
            System.out.println("AVG range: (" + avglower[j] + ", " + avgupper[j] + ")");
            for(int i = 0; i < 10; i++){
                mergeLimit = i;
                System.out.println("Merge Limit = " + mergeLimit);
                randFlag[1] = rand[i];
                EMP.set_input(lac,
                        "pop_16up",
                        -Double.POSITIVE_INFINITY,
                        3000.0,
                        "unemployed",
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "employed",
                        avglower[j],
                        avgupper[j],
                        "pop2010",
                        -Double.POSITIVE_INFINITY,
                        20000.0,
                        -Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY,
                        "households"
                );
            }
        }

    }
    public static void largeDatasetTest() throws Exception {
        String files[] = {"data/10K/10K.shp", "data/20K/20K.shp", "data/30K/30K.shp"};
        for(int i = 0; i < files.length; i++){
            EMP.set_input_construct(files[i], "AWATER",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "AWATER",
                    -Double.POSITIVE_INFINITY,
                    200000.0,
                    "ALAND",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    "AWATER",
                    -Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    10.0,
                    Double.POSITIVE_INFINITY,
                    "ALAND");
        }
    }
    public static void main(String[] args) throws Exception {
        //experimentMinUpper();
        //experimentMinLower();
        //experimentMinRangeSize();
        // experimentMinRangePos();
        //experimentAvgRange();
        //experimentSumLower();
        //experimentSumRange();
        //scalibilityWithoutAvg();
        //scalibilityWithAvg();
        // selectionCriteria();
        //mergeLimit();
        //scalibility3V5();
        //System.out.println("End of execution for EMP");
        //largeDatasetTest();
        scalibilityWithAvgCA();

    }
}
