/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Client Side
 */
package csc375a04client;

import javax.swing.SwingUtilities;

/**
 *
 * @author angie
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] hostNames = initializeHostNames();
        
        // Parse the parameters (fill gaps with defaults):
        Alloy alloy = parseInput(args, hostNames);
        
        // Run stuff:
        buildAndRunGui(alloy, hostNames); 
        alloy.runSimulation();
        
        // Exit:
        System.out.println("Good-bye!");
    }

    private static Alloy parseInput(String[] args, String[] hn) {
        // Initialize the alloy according to the parameters (or defaults):
        Alloy result;
        // Establish some defaults:
        long[] longValues = new long[3];
        longValues[0] = 100000;   // S -- 1
        longValues[1] = 450000000;   // T -- 2
        longValues[2] = 0;    // InitialTemp -- 8*
        double[] doubleValues = new double[3];
        doubleValues[0] = .75;      // C1 -- 3
        doubleValues[1] = 1.00;     // C2 -- 4
        doubleValues[2] = 1.25;     // C3 -- 5
        int[] intValues = new int[2];
        intValues[0] = 300;      // Size -- 6
        intValues[1] = 100;      // Threshold -- 7
        int numParams = longValues.length + doubleValues.length + intValues.length;
        if (args.length < numParams) {
            System.out.println("Too few arguments; filling unspecified with defaults.");
        } else if (args.length > numParams) {
            System.out.println("Too many arguments.");
        }
        // Get the longs (s,t):
        for (int i = 0; (i < 2) && (i < args.length); i++) {
            try {
                longValues[i] = Long.parseLong(args[i]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter format: " + i
                        + ", using default.");
            }
        }
        if (args.length >= 8) {
            try {
                longValues[2] = Long.parseLong(args[7]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter format: " + 7
                        + ", using default.");
            }
        }
        // Get the doubles (c1, c2, c3):
        for (int i = 2; (i < 5) && (i < args.length); i++) {
            try {
                doubleValues[i - 2] = Double.parseDouble(args[i]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter format: " + i
                        + ", using default.");
            }
        }
        // Get the ints (x,threshold,initialTemp):
        for (int i = 5; (i < 7) && (i < args.length); i++) {
            try {
                intValues[i - 5] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter format: " + i
                        + ", using default.");
            }
        }
        displayParams(longValues, doubleValues, intValues);
        result = new Alloy(longValues, doubleValues, intValues[0], intValues[1], hn);
        return result;
    }

    // Display the simulation parameters to the user:
    private static void displayParams(long[] longValues, double[] doubleValues,
            int[] intValues) {
        System.out.println("Settings:");
        System.out.println("  s: " + longValues[0]);
        System.out.println("  t: " + longValues[1]);
        System.out.println("  c1: " + doubleValues[0]);
        System.out.println("  c2: " + doubleValues[1]);
        System.out.println("  c3: " + doubleValues[2]);
        System.out.println("  dimension: " + intValues[0]);
        System.out.println("  threshold: " + intValues[1]);
        System.out.println("  initial temp: " + longValues[2]);
    }
    
    private static String[] initializeHostNames() {
        String[] hostNames = new String[4];
        hostNames[0] = "pi.cs.oswego.edu";
        hostNames[1] = "rho.cs.oswego.edu";
        hostNames[2] = "wolf.cs.oswego.edu";
        hostNames[3] = "lambda.cs.oswego.edu";
        return hostNames;
    }

    private static void buildAndRunGui(final Alloy alloy, final String[] hn) {
        SwingUtilities.invokeLater(new Runnable() { // (dispatch thread code)
            @Override
            public void run() {
                Display display = new Display(alloy, hn, "Assignment 3: Alloy Simulation");
                display.setVisible(true);
                display.draw();
            }
        }); // (/dispatch thread code)
    }
}
