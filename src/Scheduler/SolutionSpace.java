package Scheduler;

import utils.Pair;
import utils.plotUtility;

import java.util.*;


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
        plotUtility plot = new plotUtility();

        plot.plotPoints(n,a);
    }

    public void clear(){
        results.clear();
        optimizationTime_MS = -1;
    }






    public void sort(boolean isPareto){

        Comparator<Plan> ParetoPlanComparator = (Comparator<Plan>) (p1, p2) -> {
            if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
                if (Math.abs(p1.stats.money - p2.stats.money) < RuntimeConstants.precisionError) {
                    return Double.compare(p1.stats.contUtilization, p2.stats.contUtilization);
                    //return Long.compare(stats.quanta, p2.stats.quanta);
                    //return Long.compare(stats.containersUsed, p2.stats.containersUsed);
                    // TODO: if containers number the same add a criterion e.g fragmentation, #idle slots, utilization etc
                }
                return Double.compare(p1.stats.money, p2.stats.money);
            } else {
                return Long.compare(p1.stats.runtime_MS, p2.stats.runtime_MS);
            }
        };


        Comparator<Plan> PlanComparator = (Comparator<Plan>) (p1, p2) -> {
            if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
                return Double.compare(p1.stats.money, p2.stats.money);
            } else {
                return Long.compare(p1.stats.runtime_MS, p2.stats.runtime_MS);
            }
        };



        if(isPareto) {
            Collections.sort(results, ParetoPlanComparator);
        }else {
            Collections.sort(results, PlanComparator);
        }


    }



    @Override public Iterator<Plan> iterator() {
        return results.iterator();
    }

    public double getScoreElastic(){
        double maxTime=0,minTime= Double.MAX_VALUE;
        double maxMoney=0,minMoney= Double.MAX_VALUE;

        for(Plan p: results){
            maxMoney = (long) Math.max(maxMoney,p.stats.money);
            minMoney = (long) Math.min(minMoney,p.stats.money);
            maxTime =  Math.max(maxTime,p.stats.runtime_MS);
            minTime =  Math.min(minTime,p.stats.runtime_MS);
        }

        double score = ((maxTime - minTime)/maxTime) / ((maxMoney-minMoney)/maxMoney);

        return score;
    }

    public void sortDist() {
        Collections.sort(results, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money,o2.stats.money);
            }
        });
    }

    public  void computeSkyline(boolean isPareto){

        SolutionSpace skyline = new SolutionSpace();


        this.sort(isPareto); // Sort by time breaking equality by sorting by money

        Plan previous = null;
        for (Plan est : results) {
            if (previous == null) {
                skyline.add(est);
                previous = est;
                continue;
            }
            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
                // Already sorted by money
                continue;
            }
            if(Math.abs(previous.stats.money - est.stats.money)>RuntimeConstants.precisionError) //TODO ji fix or check
                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                    skyline.add(est);
                    previous = est;
                }
        }


        results.clear();
        results.addAll(skyline.results);

    }

}


