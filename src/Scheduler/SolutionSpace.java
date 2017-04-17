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

    public Plan getFastest(){
        this.sort(true);
        return results.get(0);
    }
    public Plan getSlowest(){
        this.sort(true);
        return results.get(results.size()-1);
    }


    public void print() {
        System.out.println(this.toString());
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

    public  ArrayList<Plan> getSkyline(){

        SolutionSpace skyline = new SolutionSpace();


        this.sort(true); // Sort by time breaking equality by sorting by money

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


     return skyline.results;

    }


    @Override public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Plan p : results) {
            sb.append(p.toString());
        }

       sb.append("/////////////////size: " + results.size()).append("\n");

        return sb.toString();
    }


    public void computeSkyline(boolean pruneEnabled, int k, boolean keepWhole){
        ArrayList<Plan> skyline = getSkyline();

        if(!pruneEnabled){
            this.results.clear();
            this.results.addAll(skyline);
            return;
        }else {
            if(keepWhole){
                k = results.size();
            }
            if (k > skyline.size()) {
                this.results.clear();
                this.results.addAll(skyline);
                return;
            }
            //        System.out.println("enter prune");

            HashMap<Plan, HashSet<Plan>> spToDom = new HashMap<>();
            HashMap<Plan, Integer> spToDomSize = new HashMap<>();
            ///////
            //for each point calculate how many points it dominates
            for (Plan sp : skyline) {
                HashSet<Plan> t = new HashSet<>();
                for (Plan p : results) {
                    if (sp.stats.runtime_MS < p.stats.runtime_MS &&   sp.stats.money < p.stats.money && Math.abs(sp.stats.money - p.stats.money)
                        > RuntimeConstants.precisionError) {
                        t.add(p);
                    }
                }
                spToDom.put(sp, t);
                spToDomSize.put(sp, t.size());
            }
            /////////////
            HashSet<Plan> retset = new HashSet<>();
            Plan maxp = null;
            int maxdom = -1;
            for (Plan sp : skyline) {
                if (spToDomSize.get(sp) > maxdom) {
                    maxp = sp;
                    maxdom = spToDomSize.get(sp);
                }
            }
            retset.add(maxp);

            int skylineCoverage = maxdom;
            int tempSkylineCoverage = -1;
            Plan tempSkylineCovP = null;

            while (retset.size() < k) {
                tempSkylineCovP = null;
                tempSkylineCoverage = -1;

                for (Plan sp : skyline) {
                    if (retset.contains(sp)) {
                        continue;
                    }
                    int newSkylineCov = calcNewCoverage(retset, sp, spToDom);
                    if (newSkylineCov > tempSkylineCoverage) {
                        tempSkylineCoverage = newSkylineCov;
                        tempSkylineCovP = sp;
                    }
                }
                retset.add(tempSkylineCovP);
                skylineCoverage = tempSkylineCoverage;
            }
            ////reset contains the result
            this.results.clear();
            this.results.addAll(retset);
            //        System.out.println("leave prune");
        }
    }

    public int calcNewCoverage(HashSet<Plan> retset,Plan newp,HashMap<Plan,HashSet<Plan>> spToDom){
        HashSet<Plan> t = new HashSet<>();
        for(Plan p:retset){
            if(!spToDom.containsKey(p)){
                System.out.println("aaaa");
            }
            if(spToDom.get(p).size()>0) {
                t.addAll(spToDom.get(p));
            }
        }
        t.addAll(spToDom.get(newp));

        return t.size();
    }


    public int calcJaccard(HashSet<Plan> a,HashSet<Plan> b){
        HashSet<Plan> intersection = new HashSet<>(a);
        a.retainAll(b);
        int inter = a.size();
        int jacc = inter/(a.size()+b.size() - inter);
        return jacc;
    }

    public void retainAll(SolutionSpace skylinePlansNew) {
        HashSet<Plan> t = new HashSet<>();
        t.addAll(results);
        t.retainAll(skylinePlansNew.results);
        this.results.clear();
        this.results.addAll(t);


    }

    public void keepK(int k) {
        if(results.size() == 0) {
            System.out.println("empty");
            return;
        }
        ArrayList<Plan> t = new ArrayList<>();
        t.addAll(results);
        if (k>results.size()){
            k = results.size();
        }
        results.clear();
        System.out.println(t.size()+" "+k);
        results.addAll(t.subList(0,k-1));
    }
}


