package Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}


