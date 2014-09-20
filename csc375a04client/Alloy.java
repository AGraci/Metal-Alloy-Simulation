/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Client Side
 * Centralized alloy data
 */
package csc375a04client;

import csc375a04.InitPack;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angie
 */
public class Alloy {
    // Network Information:
    //private static final int portNumber = 2697;
    private static final int portNumber = 2691;
    private String[] hostNames;
    // Metal Information:
    private static final int dimensionRatio = 4;
    private long[][][] commonRegions;
    private long[] initTemps; // S || T || initial
    private double[] constants; // thermal constants (c1, c2, c3)
    private int threshold;
    private int size;
    private int stable;   // stable temp version index
    private int updating; // updating temp version index
    private static final int DURATION = 7000;
    private int cycles; // temps have converged
    
    
    public Alloy(long[] params1, double[] params2, int x, int t, String[] hn) {
        this.initTemps = params1;
        this.constants = params2;
        this.threshold = t;
        stable = 0;
        updating = 1;
        this.hostNames = hn;
        initializeCommonRegions(x);
        this.size = x;
    }

    private void initializeCommonRegions(int x) {
        this.commonRegions = new long[2][6][x];
        for(int i=0; i<this.commonRegions.length; i++) {
            for(int j=0; j<this.commonRegions[0].length; j++) {
                for(int k=0; k<this.commonRegions[0][0].length; k++) {
                    this.commonRegions[i][j][k] = this.initTemps[2];
                }
            }
        }
    }
    
    public boolean isFinished() {
        return (this.cycles >= DURATION);
    }
    
    public int getWidth() {
        return this.size * dimensionRatio;
    }
    
    public int getHeight() {
        return this.size;
    }
                
    public long getS() {
        return initTemps[0];
    }  
    
    public long getT() {
        return initTemps[1];
    }
    
    public long getInitialTemp() {
        return initTemps[2];
    }

    // Main loop:
    void runSimulation() {
        CyclicBarrier cb = new CyclicBarrier(4, new ClientSynchronizer());
        
        ClientThread[] clients = new ClientThread[dimensionRatio];
        for (int i=0; i<dimensionRatio; i++) {
            clients[i] = new ClientThread(i,cb);
        }
        for (int i=0; i<dimensionRatio; i++) {
            clients[i].start();
        }
    }
    
    // Updates the temperatures by sending requests to a server:
    private class ClientThread extends Thread {
        private int id;
        private int[] req;
        private int[] res;
        private CyclicBarrier barrier;
    
        public ClientThread(int id, CyclicBarrier barrier) {
            this.id = id;
            // determine where to push/pull info:
            if(id == 0) {
                req = new int[1];
                req[0] = 1;
                res = new int[1];
                res[0] = 0;
            } else if(id == 1) {
                req = new int[2];
                req[0] = 0;
                req[1] = 3;
                res = new int[2];
                res[0] = 1;
                res[1] = 2;
            } else if(id == 2) {
                req = new int[2];
                req[0] = 2;
                req[1] = 5;
                res = new int[2];
                res[0] = 3;
                res[1] = 4;
            } else {
                req = new int[1];
                req[0] = 4;
                res = new int[1];
                res[0] = 5;
            }
            // Set up the barrier for synchronization:
            this.barrier = barrier;
        }
    
        @Override
        public void run() {
            System.out.println("Starting client " + id);
            try (
                 Socket socket = new Socket(hostNames[id], portNumber);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            ) {    
                long[] buffer = new long[size * req.length];
                Object input; // store the input
                // initialize (& wait):
                out.writeObject((Object) new InitPack(constants, threshold, size+req.length,
                        size, id, initTemps));
                out.flush();
                out.reset();
                input = in.readObject();
                // calculations loop (wait for each):
                while(cycles != DURATION) {
                    // 1. make array copy
                    int k=0;
                    for(int i=0; i<req.length; i++) {
                        for(int j=0; j<size; j++) {
                            buffer[k] = commonRegions[stable][req[i]][j];
                            k++;
                        }
                    }
                    // 2. send it to server
                    out.writeObject(buffer);
                    out.flush();
                    out.reset();
                    // 3. process reply (full/partial?)
                    input = (long[]) in.readObject();
                    buffer = (long[]) input;
                    k=0;
                    for(int i=0; i<res.length; i++) {
                        for(int j=0; j<size; j++) {
                            commonRegions[updating][res[i]][j] = buffer[k];
                            k++;
                        }
                    }
                    // 4. wait
                    this.barrier.await();
                }
                // exit:
                out.writeObject("bye.");
                out.flush();
                out.reset();
            } catch (UnknownHostException ex) {
                Logger.getLogger(Alloy.class.getName()).log(Level.SEVERE, null, ex);
            } catch ( IOException | ClassNotFoundException ex ) {
                Logger.getLogger(Alloy.class.getName()).log(Level.SEVERE, null, ex);
            } catch (    InterruptedException | BrokenBarrierException ex) {
                Logger.getLogger(Alloy.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Ending client " + id);
        }
    }
    
    private class ClientSynchronizer implements Runnable {
        @Override
        public void run() {
            // BARRIER ACTION!
            if(stable == 0) {
                stable = 1;
                updating = 0;
            } else {
                stable = 0;
                updating = 1;
            }
            // Increment counter:
            cycles++;
        }
    }
}
