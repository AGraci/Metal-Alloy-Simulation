/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Client Side
 * GUI to display the alloy
 */
package csc375a04client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author angie
 */
public class Display extends JFrame {
    // Network Information:
    //private static final int portNumber = 12697;
    private static final int portNumber = 12692;
    private String[] hostNames;
    private Socket[] sockets;
    private ObjectOutputStream[] out;
    private ObjectInputStream[] in;
    // GUI Information:
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private ScheduledFuture<?>[] futures;
    private final Alloy alloy; // The "subject" of the drawing
    private JPanel alloyWindow;
    private BufferedImage drawing;
    private JLabel canvas;
    private long[][] tempBuffer;
    // Coloring Information:
    private long MAX_TEMP;
    private long MIN_TEMP;
    private long HIGH_TEMP;
    private long LOW_TEMP;
    private int maxPoint;
    private int minPoint;
    private int highPoint;
    private int lowPoint;
    private long[] tempRangesHigh;
    private long[] tempRangesLow;
    private long[] tempRangesMid;
    private int[] colorRangesHigh;
    private int[] colorRangesLow;
    private int[] colorRangesMid;

    public Display(Alloy alloy, String[] hn, String message) {
        super(message);
        this.alloy = alloy;
        this.hostNames = hn;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(alloy.getWidth() + 5, alloy.getHeight() + 36);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.establishCanvas();
        this.setColorBenchmarks();
        this.tempBuffer = new long[alloy.getWidth()][alloy.getHeight()];
        this.futures = new ScheduledFuture<?>[4];
        this.sockets = new Socket[4];
        this.out = new ObjectOutputStream[4];
        this.in = new ObjectInputStream[4];
    }

    // Create the canvas, and initialize color to purple!
    private void establishCanvas() {
        alloyWindow = new JPanel();
        alloyWindow.setLayout(new BorderLayout());
        drawing = new BufferedImage(alloy.getWidth(), alloy.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int color = (Color.decode("#770077")).getRGB();
        for (int x = 0; x < drawing.getWidth(); x++) {
            for (int y = 0; y < drawing.getHeight(); y++) {
                drawing.setRGB(x, y, color);
            }
        }
        canvas = new JLabel();
        canvas.setLayout(new BorderLayout());
        canvas.setIcon(new ImageIcon(drawing));
        JScrollPane sp = new JScrollPane(canvas);
        alloyWindow.add(sp, BorderLayout.CENTER);
        this.add(alloyWindow, BorderLayout.CENTER);
    }

    // Schedule re-drawing of the alloy:
    public void draw() {
        System.out.println("Starting GUI client.");
        // Establish the sockets:
        try {
            for (int i = 0; i < 4; i++) {
                this.sockets[i] = new Socket(hostNames[i], portNumber);
                out[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                in[i] = new ObjectInputStream(sockets[i].getInputStream());
            }
            // Create cyclic redraw barrier:
            CyclicBarrier cb = new CyclicBarrier(4, new Runnable() {
                @Override
                public void run() {
                    repaint();
                }
            });
            // Schedule future updates:
            for (int i = 0; i < 4; i++) {
                futures[i] = scheduler.scheduleAtFixedRate(new ReDrawer(i, i * alloy.getHeight(), cb),
                        0, 100, MILLISECONDS);
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Assign colors to the various ranges:
    private void setColorBenchmarks() {
        findExtremes();
        this.tempRangesHigh = new long[4];
        tempRangesHigh[1] = (HIGH_TEMP + MAX_TEMP) >>> 1; // mid-point
        tempRangesHigh[0] = (HIGH_TEMP + tempRangesHigh[1]) >>> 1; // lower quartile
        tempRangesHigh[2] = (MAX_TEMP + tempRangesHigh[1]) >>> 1; // upper quartile
        tempRangesHigh[3] = MAX_TEMP; // roof
        this.tempRangesLow = new long[4];
        tempRangesLow[1] = (LOW_TEMP + MIN_TEMP) >>> 1; // mid-point
        tempRangesLow[0] = (MIN_TEMP + tempRangesLow[1]) >>> 1; // lower quartile
        tempRangesLow[2] = (LOW_TEMP + tempRangesLow[1]) >>> 1; // upper quartile
        tempRangesLow[3] = LOW_TEMP; // roof
        this.tempRangesMid = new long[4];
        tempRangesMid[1] = (LOW_TEMP + HIGH_TEMP) >>> 1; // mid-point
        tempRangesMid[0] = (LOW_TEMP + tempRangesMid[1]) >>> 1; // lower quartile
        tempRangesMid[2] = (HIGH_TEMP + tempRangesMid[1]) >>> 1; // upper quartile
        tempRangesMid[3] = HIGH_TEMP; // roof
        this.colorRangesHigh = new int[4];
        colorRangesHigh[0] = Color.decode("#FF4400").getRGB();
        colorRangesHigh[1] = Color.decode("#FF7700").getRGB();
        colorRangesHigh[2] = Color.decode("#FF0000").getRGB();
        colorRangesHigh[3] = Color.decode("#FF00FF").getRGB();
        this.colorRangesLow = new int[4];
        colorRangesLow[0] = Color.decode("#0000FF").getRGB();
        colorRangesLow[1] = Color.decode("#0044FF").getRGB();
        colorRangesLow[2] = Color.decode("#0077FF").getRGB();
        colorRangesLow[3] = Color.decode("#004477").getRGB();
        this.colorRangesMid = new int[4];
        colorRangesMid[0] = Color.decode("#00FFFF").getRGB();
        colorRangesMid[1] = Color.decode("#00FF44").getRGB();
        colorRangesMid[2] = Color.decode("#00FF00").getRGB();
        colorRangesMid[3] = Color.decode("#FFFF00").getRGB();
    }

    // Find the pivot points to scale the coloring:
    private void findExtremes() {
        this.MAX_TEMP = Long.MAX_VALUE;
        this.MIN_TEMP = Long.MIN_VALUE;
        if (alloy.getS() < alloy.getT()) {
            this.HIGH_TEMP = alloy.getT();
            this.LOW_TEMP = alloy.getS();
        } else {
            this.HIGH_TEMP = alloy.getT();
            this.LOW_TEMP = alloy.getS();
        }
        if (alloy.getInitialTemp() > this.HIGH_TEMP) {
            this.HIGH_TEMP = alloy.getInitialTemp();
        } else if (alloy.getInitialTemp() < this.LOW_TEMP) {
            this.LOW_TEMP = alloy.getInitialTemp();
        }
        this.maxPoint = (Color.decode("#cccccc")).getRGB();
        this.minPoint = (Color.decode("#444444")).getRGB();
        this.highPoint = (Color.WHITE).getRGB();
        this.lowPoint = (Color.BLACK).getRGB();
    }
    
    private class ReDrawer implements Runnable {
        private int id;
        private int offset;
        private CyclicBarrier cb;
        
        public ReDrawer(int id, int offset, CyclicBarrier cb) {
            this.id = id;
            this.offset = offset;
            this.cb = cb;
        }

        @Override
        public void run() {
         try {
            Object rawInput;
            long[][] input;
            // get fourth section:
            out[id].writeObject("Update please.");
            out[id].flush();
            out[id].reset();
            rawInput = in[id].readObject();
            if (rawInput == null) {
                return;
            }
            input = (long[][]) rawInput;
            // Color the pixels:
            for (int i = 0; i < input.length; i++) {
                for (int j = 0; j < input[0].length; j++) {
                    // determine color:
                    colorCode(i+offset, j, input[i][j]);
                }
            }
            // wait to redraw:
            cb.await();
        } catch (IOException | ClassNotFoundException | InterruptedException | BrokenBarrierException ex) {
            Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (alloy.isFinished()) {
                try {
                    // send quit messages to the servers:
                    out[id].writeObject("Bye.");
                    out[id].flush();
                    out[id].reset();
                } catch (IOException ex) {
                    Logger.getLogger(Display.class.getName()).log(Level.SEVERE, null, ex);
                }
                // stop future tasks:
                futures[id].cancel(true);
            }
        }
    }

    // Change the color of the pixel corresponding to the given point:
    private void colorCode(int i, int j, long temp) {
        if (temp >= HIGH_TEMP) {
            colorHighTemp(i, j, temp);
        } else if (temp <= LOW_TEMP) {
            colorLowTemp(i, j, temp);
        } else {
            colorMidTemp(i, j, temp);
        }
    }
        // Assign color in the high-range:
    private void colorHighTemp(int i, int j, long temp) {
        if (temp == MAX_TEMP) {
            drawing.setRGB(i, j, maxPoint);
            return;
        }
        if (temp == HIGH_TEMP) {
            drawing.setRGB(i, j, highPoint);
            return;
        }
        for (int k = 0; k < tempRangesHigh.length; k++) {
            if (temp < tempRangesHigh[k]) {
                drawing.setRGB(i, j, colorRangesHigh[k]);
                break;
            }
        }
    }

    // Assign a color in the low-range"
    private void colorLowTemp(int i, int j, long temp) {
        if (temp == MIN_TEMP) {
            drawing.setRGB(i, j, minPoint);
            return;
        }
        if (temp == LOW_TEMP) {
            drawing.setRGB(i, j, lowPoint);
            return;
        }
        for (int k = 0; k < tempRangesLow.length; k++) {
            if (temp < tempRangesLow[k]) {
                drawing.setRGB(i, j, colorRangesLow[k]);
                break;
            }
        }
    }

    // Assign a color in the mid-range:
    private void colorMidTemp(int i, int j, long temp) {
        for (int k = 0; k < tempRangesMid.length; k++) {
            if (temp < tempRangesMid[k]) {
                drawing.setRGB(i, j, colorRangesMid[k]);
                break;
            }
        }
    }
    }
}
