package Scheduler;

import utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static utils.Plot.plotPoints;

/**
 * Created by johnchronis on 2/19/17.
 */
public class SolutionSpace {

    private List<Plan> results = null;
    public double optimizationTime_SEC;


    public SolutionSpace(){
        results = new ArrayList<>();
    }

    public void addResult(Plan p){
        results.add(p);
    }

    public void addAllResults(Collection<Plan> p){
        results.addAll(p);
    }

    public void setOptimizationTime(long optimizationTime_MS) {
        optimizationTime_SEC = optimizationTime_MS/1000;
    }

    public void print() {
        for(Plan p:results){
            p.printInfo();
        }
    }

    public void plot(){
        ArrayList<Pair<Long,Double>> a = new ArrayList<>();
        for(Plan p: results){
            a.add(new Pair<Long, Double>(p.stats.runtime_MS,p.stats.money));
        }
        plotPoints(a);
    }
}


