/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Server Side
 * Smallest unit of the alloy
 */
package csc375a04server;

import java.util.ArrayList;

/**
 *
 * @author angie
 */
public class Region {
    private final ArrayList<Region> neighbors = new ArrayList();
    private double[] metalPercentages;
    private double[] thermalConstants;
    private boolean constantTemp;
    private int indexI;
    private int indexJ;
    private Alloy sa;

    // Constructor:
    public Region(long initialTemp, double[] tc,
            int i, int j, Alloy sa) {
        this.sa = sa;
        sa.temps[0][i][j] = initialTemp;
        sa.temps[1][i][j] = initialTemp;
        thermalConstants = tc;
        calculatePercentages();
        constantTemp = false;
        this.indexI = i;
        this.indexJ = j;
    }

    // Randomizes percentages of metals in regions:
    private void calculatePercentages() {
        metalPercentages = new double[3];
        double point1, point2, larger, smaller;
        point1 = Math.random();
        point2 = Math.random();
        if (point1 < point2) {
            larger = point2;
            smaller = point1;
        } else {
            larger = point1;
            smaller = point2;
        }
        metalPercentages[0] = smaller;
        metalPercentages[1] = larger - smaller;
        metalPercentages[2] = 1.00 - larger;
    }

    // Set a temperature at which the region is constantly heated:
    public void setConstantTemperature(long constantTemp) {
        this.constantTemp = true;
        sa.temps[0][indexI][indexJ] = constantTemp;
        sa.temps[1][indexI][indexJ] = constantTemp;
        System.out.println(this.getTemp());
    }

    // Add a neighbor to the list (2-4 total):
    public void addNeighbor(Region ar) {
        this.neighbors.add(ar);
    }

    // Get the percentage of a given metal in this region of the alloy:
    private double getMetalPercentage(int i) {
        if ((i >= 0) && (i < this.thermalConstants.length)) {
            return this.metalPercentages[i];
        } else {
            System.out.println("ERROR: invalid metal percentage requested.");
            return 0;
        }
    }

    // Return the stable temperature value:
    public long getTemp() {
        return sa.temps[sa.stable][indexI][indexJ];
    }
    
    // Reset the stable temperature value:
    public void setTemp(long t) {
        sa.temps[sa.stable][indexI][indexJ] = t;
        sa.temps[sa.updating][indexI][indexJ] = t;
    }

    // Calculate the new temperature based on neighbors:
    public void calculateTemp() {
        if (this.constantTemp) {
            return;
        }
        long result = 0;
        long tmp = 0;
        for (int i = 0; i < thermalConstants.length; i++) {
            for (int j = 0; j < neighbors.size(); j++) {
                tmp += (neighbors.get(j).getTemp() * (neighbors.get(j).getMetalPercentage(i)));
            }
            tmp = ((((long) (thermalConstants[i] * 100)) * tmp) / 100) / neighbors.size();
            result += tmp;
            tmp = 0;
        }
        sa.temps[sa.updating][indexI][indexJ] = result;
    }
}
