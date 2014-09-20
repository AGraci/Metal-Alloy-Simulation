/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Package for initialization data
 */
package csc375a04;

import java.io.Serializable;

/**
 *
 * @author angie
 */
public class InitPack implements Serializable {
    private int id;
    private double[] tc;
    private int t, w, h;
    private long[] initialTemps;
    
    public InitPack(double[] tc, int t, int w, int h, int id, long[] i) {
        this.tc = tc;
        this.t = t;
        this.w = w;
        this.h = h;
        this.initialTemps = i;
        this.id = id;
    }
    
    public int getWidth() {
        return this.w;
    }
    
    public int getHeight() {
        return this.h;
    }
    
    public int getThreshold() {
        return this.t;
    }
    
    public double[] getThermConst() {
        return this.tc;
    }
    
    public long[] getInitTemps() {
        return this.initialTemps;
    }
    
    public int getID() {
        return this.id;
    }
}
