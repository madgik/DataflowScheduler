/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package Scheduler;


import java.rmi.RemoteException;

/**
 *
 * @author heraldkllapi
 */
public class SimpleScheduleDerivative {
    double[][] scores = null;
    double baselineScore = 0.0;

    private final int minIdx = 0;
    private final int maxIdx = 1;

    public SimpleScheduleDerivative() {
    }

    
    public void reset(int operators, int containers) throws RemoteException {
        scores = new double[operators][2];
        for (int op = 0; op < scores.length; ++op) {
            scores[op][minIdx] = Double.MAX_VALUE;
            scores[op][maxIdx] = Double.MIN_VALUE;
        }
    }

    
    public void setBaselineScore(double score) throws RemoteException {
        baselineScore = score;
    }

    
    public void addScore(int operator,
        int container,
        double score) throws RemoteException {
        if (scores[operator][minIdx] > score) {
            scores[operator][minIdx] = score;
        }
        if (scores[operator][maxIdx] < score) {
            scores[operator][maxIdx] = score;
        }
    }

    
    public double[] computeOperatorDerivatives() throws RemoteException {
        double[] derivatives = new double[scores.length];
        for (int op = 0; op < scores.length; ++op) {
            derivatives[op] = scores[op][maxIdx] - scores[op][minIdx];
        }
        return derivatives;
    }
}
