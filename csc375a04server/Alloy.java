/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Server Side
 * Alloy section data
 */
package csc375a04server;

import csc375a04.InitPack;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author angie
 */
public class Alloy {
    private ForkJoinPool fjp;
    private int sectionID;
    public long[][][] temps;
    private Region[][] submesh;
    private int threshold;
    private int width, height;
    public int stable, updating; // temp version index
    private boolean initialized;
    
    // Constructor:
    public Alloy() {
        this.fjp = new ForkJoinPool();
        this.initialized = false;
    }
    
    // Ready to be used?
    public boolean isInitialized() {
       return initialized;
    }
    
    // Initialize the alloy based on params from client:
    void Initialize(InitPack ip) {
        this.sectionID = ip.getID();
        System.out.println(ip.getID());
        this.stable = 0;
        this.updating = 1;
        this.threshold = ip.getThreshold();
        this.width = ip.getWidth();
        this.height = ip.getHeight();
        this.temps = new long[2][width][height];
        initializeMesh(width, height, ip.getInitTemps(), ip.getThermConst());
        this.initialized = true;
    }
    
    // Initialize the mesh:
    private void initializeMesh(int width, int height, long[] it, double[] tc) {
        this.submesh = new Region[width][height];
        for(int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
                this.submesh[i][j] = new Region(it[2], tc, i, j, this);
            }
        }
        if(this.sectionID == 0) {
            this.submesh[0][0].setConstantTemperature(it[0]);
        } else if(this.sectionID == 3) {
            this.submesh[width-1][height-1].setConstantTemperature(it[1]);
        }
        linkMesh();
    }
    
    // Link the regions together:
    private void linkMesh() {
        for (int i = 0; i < (width); i++) { // row
            for (int j = 0; j < height; j++) { // col
                if (j != 0) submesh[i][j].addNeighbor(this.submesh[i][j - 1]); // North
                if (j != height - 1) submesh[i][j].addNeighbor(this.submesh[i][j + 1]); // South
                if (i != width - 1) submesh[i][j].addNeighbor(this.submesh[i + 1][j]); // East
                if (i != 0) submesh[i][j].addNeighbor(this.submesh[i - 1][j]); // West
            }
        }
    }

    // Do calculations (recursively):
    public long[] Calculate(long[] l) {
        updateNeighbors(l);
        RecursiveAction ut = new UpdateTask(submesh, height, 0, width, 0, threshold, true);
        fjp.invoke(ut);
        ut.reinitialize();
        if(this.stable == 0) {
            this.stable = 1;
            this.updating = 0;
        } else {
            this.stable = 0;
            this.updating = 1;
        }
        return getCommonRegions();
    }
    
    // Update neighbor values:
    private void updateNeighbors(long[] l) {
        if((this.sectionID == 1) || (this.sectionID == 2)) {
            for(int i=0; i<height; i++) submesh[0][i].setTemp(l[i]);
            for(int i=0; i<height; i++) submesh[width-1][i].setTemp(l[i+height]);
        } else if(this.sectionID == 3) {
            for(int i=0; i<height; i++) submesh[0][i].setTemp(l[i]);
        } else {
            for(int i=0; i<height; i++) submesh[width-1][i].setTemp(l[i]);
        }
    }
    
    // Share regions that neighbor other sections:
    private long[] getCommonRegions() {
        long[] result;
        if((this.sectionID == 1) || (this.sectionID == 2)) {
            result = new long[height*2];
            for(int i=0; i<height; i++) result[i] = submesh[1][i].getTemp();
            for(int i=0; i<height; i++) result[i+height] = submesh[width-2][i].getTemp();
        } else if(this.sectionID == 3) {
            result = new long[height];
            for(int i=0; i<height; i++) result[i] = submesh[1][i].getTemp();
        } else {
            result = new long[height];
            for(int i=0; i<height; i++) {
                result[i] = submesh[width-2][i].getTemp();
            }
        }
        return result;
    }
    
    // Return a snapshot of the values:
    public long[][] getMesh() {
        if(!initialized) {
            return null;
        }
        long[][] result;
        if((this.sectionID == 1) || (this.sectionID == 2)) {
            result = new long[width-2][height];
            for(int i=0; i<result.length; i++) {
                for(int j=0; j<result[0].length; j++) {
                    result[i][j] = temps[stable][i+1][j];
                }
            }
        } else if(this.sectionID == 3) {
            result = new long[width-1][height];
            for(int i=0; i<result.length; i++) {
                for(int j=0; j<result[0].length; j++) {
                    result[i][j] = temps[stable][i+1][j];
                }
            }
        } else {
            result = new long[width-1][height];
            for(int i=0; i<result.length; i++) {
                for(int j=0; j<result[0].length; j++) {
                    result[i][j] = temps[stable][i][j];
                }
            }
        }
        return result;
    }
}
