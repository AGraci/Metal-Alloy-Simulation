/*
 * Angie Graci
 * CSC 375
 * Dr. Lea
 * Assignment 04
 * 
 * Server Side
 * Recursive parallel algorithm to divide-and-conquer temperature updates
 */
package csc375a04server;

import static java.util.concurrent.ForkJoinTask.invokeAll;
import java.util.concurrent.RecursiveAction;

/**
 *
 * @author angie
 */
public final class UpdateTask extends RecursiveAction {

    Region[][] array;
    int hij, loj, hii, loi, t, v;
    boolean irregular;

    public UpdateTask(Region[][] array, int hij, int loj, int hii, int loi,
            int threshold, boolean irregular) {
        this.array = array;
        this.hij = hij;
        this.loj = loj;
        this.hii = hii;
        this.loi = loi;
        this.t = threshold;
        this.irregular = irregular;
    }

    @Override
    protected void compute() {
        if (((hii - loi) * (hij - loj)) < t) {
            sequentiallyUpdate(array, hij, loj, hii, loi);
        } else {
            divideTask(array, hij, loj, hii, loi, t, irregular);
        }
    }

    private void sequentiallyUpdate(Region[][] array, int hij, int loj, int hii, int loi) {
        for (int i = loi; i < hii; i++) {
            for (int j = loj; j < hij; j++) {
                array[i][j].calculateTemp();
            }
        }
    }

    private void divideTask(Region[][] array, int hij, int loj, int hii, int loi,
            int t, boolean irregular) {
        if (irregular) {
            int midh2 = (loi + hii) >>> 1;
            int midh1 = (loi + midh2) >>> 1;
            int midh3 = (hii + midh2) >>> 1;
            invokeAll(new UpdateTask(array, hij, loj, midh1, loi, t, false),
                    new UpdateTask(array, hij, loj, midh2, midh1, t, false),
                    new UpdateTask(array, hij, loj, midh3, midh2, t, false),
                    new UpdateTask(array, hij, loj, hii, midh3, t, false));
        } else {
            int midh = (loi + hii) >>> 1;
            int midv = (loj + hij) >>> 1;
            invokeAll(new UpdateTask(array, hij, midv, hii, midh, t, false),
                    new UpdateTask(array, hij, midv, midh, loi, t, false),
                    new UpdateTask(array, midv, loj, hii, midh, t, false),
                    new UpdateTask(array, midv, loj, midh, loi, t, false));
        }
    }
}
