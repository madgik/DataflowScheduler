package Scheduler;

import utils.Pair;

import java.util.*;

import static utils.Plot.plotPlans;
import static utils.Plot.plotPoints;

/**
 * Created by johnchronis on 2/19/17.
 */
public class SolutionSpace implements Iterable<Plan> {

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

    public void addAll(SolutionSpace sp){
        results.addAll(sp.results);
    }

    public void setOptimizationTime(long optimizationTime_MS) {
        this.optimizationTime_MS = optimizationTime_MS;
    }

    public boolean isEmpty(){
        return results.isEmpty();
    }

    public int size(){
        return results.size();
    }

    public void print() {
//        System.out.println("////////////////Solutio");
        for(Plan p:results){
            p.printInfo();
        }
        System.out.println("/////////////////size: "+results.size());
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

    public void sort(){
        Collections.sort(results);
    }

    @Override public Iterator<Plan> iterator() {
        return results.iterator();
    }
}


