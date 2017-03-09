package Scheduler;

import utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static utils.Plot.plotPlans;
import static utils.Plot.plotPoints;

/**
 * Created by johnchronis on 2/19/17.
 */
public class SolutionSpace {

    public ArrayList<Plan> results = null;
    public double optimizationTime_MS;


    public SolutionSpace(){
        results = new ArrayList<>();
    }

    public void add(Plan p){
        results.add(p);
    }

    public void addAll(Collection<Plan> p){
        results.addAll(p);
    }

    public void setOptimizationTime(long optimizationTime_MS) {
        this.optimizationTime_MS = optimizationTime_MS;
    }

    public void print() {
        System.out.println("////////////////Space");
        for(Plan p:results){
            p.printInfo();
        }
        System.out.println("/////////////////Space size: "+results.size());
    }

    public void plot(String n){
        ArrayList<Pair<Long,Double>> a = new ArrayList<>();
        for(Plan p: results){
            a.add(new Pair<Long, Double>(p.stats.runtime_MS,p.stats.money));
        }
        plotPoints(n,a);
    }


    public void clear(){
        results.clear();
        optimizationTime_MS = -1;
    }
}


