package main.java.utils;

import java.io.Serializable;

/**
 * Created by johnchronis on 2/18/17.
 */
public class PairPlot<A, B> implements Serializable {

    private static final long serialVersionUID = 1L;
    private long time ;

    public void setMoney(double money) {
        this.money = money;
    }

    private double money  ;

    public void setTime(long time) {
        this.time = time;
    }

    public PairPlot(long t, double m) {
        this.time = t;
        this.money = m;
    }


    public long getTime() {
        return time;
    }

    public double getMoney() {
        return money;
    }

}
