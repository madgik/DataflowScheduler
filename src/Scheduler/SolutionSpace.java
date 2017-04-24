package Scheduler;

import org.apache.commons.collections.ArrayStack;
import utils.Check;
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
    public long getMaxRuntime(){
        long runtime=0;
        for(Plan p:results){
            runtime = Math.max(runtime,p.stats.runtime_MS);
        }
        return runtime;
    }
    public double getMaxCost(){
        double cost = 0;
        for(Plan p:results){
            cost = Math.max(cost,p.stats.money);
        }
        return cost;
    }

    public Plan getSlowest(){
        this.sort(true);
        return results.get(results.size()-1);
    }

    public  Plan getKnee(){
        Plan tp=null;
        double t= Double.MAX_VALUE;
        for(Plan p:results){
            double tt = p.stats.runtime_MS*0.5+0.5*p.stats.money;
            if(tt<t){
                tp = p;
                t=tt;
            }
        }
        return tp;
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

    HashMap<Integer,Plan> sortedPrunned = new HashMap<>();

    static int steps=0;
    static int aaa = 0;
    public void computeSkyline(boolean pruneEnabled, int k, boolean keepWhole, String method){
        ArrayList<Plan> skyline = getSkyline();

        if(!pruneEnabled){
            this.results.clear();
            this.results.addAll(skyline);
            return;
        }else {
            if (keepWhole) {
                k = results.size();
            }
            if (k > skyline.size()) {
                this.results.clear();
                this.results.addAll(skyline);
                return;
            }

            if (method.equals("valkanas")) {
                HashSet<Plan> retset  = new HashSet<>();
                valkanasPruning(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("valkanas1and2")){
                HashSet<Plan> retset = new HashSet<>();
                valkanasPruning1and2(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingScoreDist")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceScoreNormalized(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingScoreDist2")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceScoreNormalized2(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("crowdingMoney")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceMoney(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingRuntime")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceRuntime(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingSimpleDist")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceSimpleDist(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowding")) {
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistance(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("crowdingMaxMoney")) {
                HashSet<Plan> retset = new HashSet<Plan>();
                crowdingMaxMoney(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("newall")){
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceScoreNormalized(skyline,k/2,retset);
                crowdingDistanceRuntime(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("newall2")){
                HashSet<Plan> retset = new HashSet<>();
                crowdingDistanceScoreNormalized(skyline,k/2,retset);
                crowdingDistanceMoney(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("scoreDist+maxMoney")){
                HashSet<Plan> retset = new HashSet<>();
                crowdingMaxMoney(skyline,k/2+1,retset);
                crowdingDistanceScoreNormalized(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else{
                System.out.println("houston we have a problem");
            }
        }
    }

    private HashSet<Plan> valkanasPruning1and2(ArrayList<Plan> skyline, int k,HashSet<Plan> retset) {
        //valkPruning and add the 10 lowest in dom Size
//        HashSet<Plan> retset = new HashSet<>();

        retset.add(skyline.get(0));
        retset.add(skyline.get(skyline.size()-1));

        HashMap<Plan, HashSet<Plan>> spToDom = new HashMap<>();
        HashMap<Plan, Integer> spToDomSize = new HashMap<>();
        ///////
        //for each point calculate how many points it dominates
        for (Plan sp : skyline) {
            HashSet<Plan> t = new HashSet<>();
            for (Plan p : results) {
                if (sp.stats.runtime_MS < p.stats.runtime_MS && sp.stats.money < p.stats.money
                    && Math.abs(sp.stats.money - p.stats.money) > RuntimeConstants.precisionError) {
                    t.add(p);
                }
            }
            spToDom.put(sp, t);
            spToDomSize.put(sp, t.size());
        }
        /////////////
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


        ArrayList<Pair<Plan,Integer>> sorted = new ArrayList<>();
        for(Plan sp:spToDomSize.keySet()){
            sorted.add(new Pair<Plan,Integer>(sp,spToDomSize.get(sp)));
        }
        Collections.sort(sorted, new Comparator<Pair<Plan, Integer>>() {
            @Override
            public int compare(Pair<Plan, Integer> o1, Pair<Plan, Integer> o2) {
                return o2.b - o1.b;
            }
        });

        for(int i=0;i<10;++i){
            retset.add(sorted.get(i).a);

        }

        return retset;




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

    public double jaccardDist(Plan a, Plan b,HashMap<Plan, HashSet<Plan>> spToDom){
        HashSet<Plan> intersection = new HashSet<>();
        intersection.addAll(spToDom.get(a));
        intersection.retainAll(spToDom.get(b));
        if(intersection.size() == (spToDom.get(a).size()+spToDom.get(b).size())){
            return 0;
        }
        return 1.0 - (intersection.size())/(spToDom.get(a).size()+spToDom.get(b).size()-intersection.size());

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
        ArrayList<Plan> sk = getSkyline();

        if(results.size() == 0) {
            System.out.println("empty");
            return;
        }
        HashSet<Plan> t = new HashSet<>();
        t.add(sk.get(0));
        t.add(sk.get(sk.size()-1));

        if (k>sk.size()){
            k = sk.size();
        }
        results.clear();
        int c=0;
        while(t.size()<k){
            if(!t.contains(sk.get(c))) {
                t.add(sk.get(c));
            }
            c++;
        }
        results.addAll(t);
    }

//    private HashSet<Plan>  valkanasPruning2(ArrayList<Plan> skyline, int k, HashSet<Plan> retset>) {
//
//        HashMap<Plan, HashSet<Plan>> spToDom = new HashMap<>();
//        HashMap<Plan, Integer> spToDomSize = new HashMap<>();
//        ///////
//        //for each point calculate how many points it dominates
//        for (Plan sp : skyline) {
//            HashSet<Plan> t = new HashSet<>();
//            for (Plan p : results) {
//                if (sp.stats.runtime_MS < p.stats.runtime_MS && sp.stats.money < p.stats.money
//                    && Math.abs(sp.stats.money - p.stats.money) > RuntimeConstants.precisionError) {
//                    t.add(p);
//                }
//            }
//            spToDom.put(sp, t);
//            spToDomSize.put(sp, t.size());
//        }
//        /////////////
//        Plan maxp = null;
//        int maxdom = -1;
//        for (Plan sp : skyline) {
//            if (spToDomSize.get(sp) > maxdom) {
//                maxp = sp;
//                maxdom = spToDomSize.get(sp);
//            }
//        }
//        retset.add(maxp);
//
//        ArrayList<Pair<Plan,Integer>> sorted = new ArrayList<>();
//        for(Plan sp:spToDomSize.keySet()){
//            sorted.add(new Pair<Plan,Integer>(sp,spToDomSize.get(sp)));
//        }
//        Collections.sort(sorted, new Comparator<Pair<Plan, Integer>>() {
//            @Override
//            public int compare(Pair<Plan, Integer> o1, Pair<Plan, Integer> o2) {
//                return o2.b - o1.b;
//            }
//        });
//
//        for(int i=0;i<k;++i){
//         retset.add(sorted.get(i).a);
//
//        }
//
//        return retset;
//    }

    public double getScore(Plan p, long longest, double maxcost){
        return (0.5*(p.stats.money/maxcost))+(0.5*(p.stats.runtime_MS/longest));
    }


    public HashSet<Plan> valkanasPruning(ArrayList<Plan> skyline, int k, HashSet<Plan> retset){
//        HashSet<Plan> retset = new HashSet<>();

        retset.add(skyline.get(0));
        retset.add(skyline.get(skyline.size()-1));

        HashMap<Plan, HashSet<Plan>> spToDom = new HashMap<>();
        HashMap<Plan, Integer> spToDomSize = new HashMap<>();
        ///////
        //for each point calculate how many points it dominates
        for (Plan sp : skyline) {
            HashSet<Plan> t = new HashSet<>();
            for (Plan p : results) {
                if (sp.stats.runtime_MS < p.stats.runtime_MS && sp.stats.money < p.stats.money
                    && Math.abs(sp.stats.money - p.stats.money) > RuntimeConstants.precisionError) {
                    t.add(p);
                }
            }
            spToDom.put(sp, t);
            spToDomSize.put(sp, t.size());
        }
        /////////////
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


        return retset;
    }

    public HashSet<Plan> crowdingDistanceScoreNormalized(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {


//        HashSet<Plan> ret = new HashSet<>();
        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

        HashMap<Plan,Double> planToScore = new HashMap<>();
        long maxRuntime = getMaxRuntime();
        double maxcost = getMaxCost();
        for(Plan p:results){
            planToScore.put(p,getScore(p,maxRuntime,maxcost));
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(planToScore.get(o1),planToScore.get(o2));
            }
        });

        this.sort(true);
        ret.add(results.get(0));
        ret.add(results.get(results.size()-1));

        int i=0;
        while(i<skylinePlans.size() && ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }

        return ret;
    }

    public HashSet<Plan> crowdingDistanceScoreNormalized2(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {


        //        HashSet<Plan> ret = new HashSet<>();
        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

        HashMap<Plan,Double> planToScore = new HashMap<>();
        long maxRuntime = getMaxRuntime();
        double maxcost = getMaxCost();
        for(Plan p:results){
            planToScore.put(p,getScore(p,maxRuntime,maxcost));
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(planToScore.get(o1),planToScore.get(o2));
            }
        });

        this.sort(true);
        ret.add(results.get(0));
        ret.add(results.get(results.size()-1));

        int i=0;
        while(i<skylinePlans.size() && ret.size()<skylinePlansToKeep-3){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }

        i=skylinePlans.size()-1;
        while(i<skylinePlans.size() && ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i--;
        }


        return ret;
    }


    public HashSet<Plan> crowdingDistanceRuntime(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {
        //uses runtime to sort crowded spaces

//        HashSet<Plan> ret = new HashSet<>();
        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
            }
        });

        this.sort(true);
        ret.add(results.get(0));
        ret.add(results.get(results.size()-1));

        int i=0;
        while(ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }


        return ret;
    }

    public HashSet<Plan> crowdingDistanceMoney(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {
        //uses runtime to sort crowded spaces

//        HashSet<Plan> ret = new HashSet<>();
        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });

        this.sort(true);
        ret.add(results.get(0));
        ret.add(results.get(results.size()-1));

        int i=0;
        while(ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }


        return ret;
    }

    public HashSet<Plan> crowdingDistanceSimpleDist(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {

//        HashSet<Plan> ret = new HashSet<>();
        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);


        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });

        this.sort(true);
        ret.add(results.get(0));
        ret.add(results.get(results.size()-1));

        int i=0;
        while(ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }


        return ret;
    }

    public HashSet<Plan> crowdingDistance(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret){
//        HashSet<Plan> ret = new HashSet<>();
        ret.add(donotchange.get(0));
        ret.add(donotchange.get(donotchange.size()-1));

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);
        skylinePlansToKeep-=2;

        int schedulesKept = 0;
        final HashMap<Plan, Double> planDistance = new HashMap<>();

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
            }
        });
        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p == 0 || p == skylinePlans.size() - 1) {
                planDistance.put(skylinePlans.get(p), 0.0);
            } else {
                long makespan_prev = skylinePlans.get(0).stats.runtime_MS;
                long makespan_next = skylinePlans.get(p).stats.runtime_MS;
                planDistance.put(skylinePlans.get(p),
                    Math.pow((makespan_next - makespan_prev) / makespan_prev, 2));
            }
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });
        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p == 0 || p == skylinePlans.size() - 1) {
                planDistance.put(skylinePlans.get(p), 0.0);
            } else {
                Double money_prev = skylinePlans.get(0).stats.money;
                Double money_next = skylinePlans.get(p).stats.money;
                planDistance.put(skylinePlans.get(p),
                    planDistance.get(skylinePlans.get(p)) + Math
                        .pow((double) ((money_next - money_prev) / money_prev), 2));
            }
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(Math.sqrt(planDistance.get(o1)),
                    Math.sqrt(planDistance.get(o2)));
            }
        });



        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p < skylinePlansToKeep) {
                ++schedulesKept;
                sortedPrunned.put(p,skylinePlans.get(p));
            } else
                skylinePlans.set(p, null);
        }

        for(Plan p:skylinePlans){
            if(p!=null){
                ret.add(p);
            }
        }

        return ret;
    }

    public HashSet<Plan> crowdingMaxMoney(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret){
        //        HashSet<Plan> ret = new HashSet<>();
//        Collections.sort(donotchange, new Comparator<Plan>() {
//            @Override public int compare(Plan o1, Plan o2) {
//                return (int) (o1.stats.runtime_MS  - o2.stats.runtime_MS);
//            }
//        });
//
//        ret.add(donotchange.get(0));
//        ret.add(donotchange.get(donotchange.size()-1));

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);
        skylinePlansToKeep-=2;

        int schedulesKept = 0;
        final HashMap<Plan, Double> planDistance = new HashMap<>();

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });
        ArrayList<Pair<Plan,Double>> sortedList = new ArrayList<>();

        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p == 0 || p == skylinePlans.size() - 1) {
               sortedList.add(new Pair<>(skylinePlans.get(p),Double.MAX_VALUE));
            } else {
                double mon = skylinePlans.get(p).stats.money;
                double mon1 = skylinePlans.get(p+1).stats.money;
                double mon2 = skylinePlans.get(p-1).stats.money;

                double dist = Math.max(Math.abs(mon-mon1),Math.abs(mon-mon2));
                sortedList.add(new Pair<>(skylinePlans.get(p),dist));
            }
        }


        Collections.sort(sortedList, new Comparator<Pair<Plan,Double>>() {
            @Override public int compare(Pair<Plan,Double> o1, Pair<Plan,Double> o2) {
                return Double.compare(o1.b , o2.b);
            }
        });

        int i=0;
        while(i<sortedList.size() && ret.size()<skylinePlansToKeep){
            if(!ret.contains(sortedList.get(i))){
                ret.add(sortedList.get(i).a);
            }
            ++i;
        }
        return ret;
    }


    public void retainAllAndKeep(SolutionSpace skylinePlansNew, int pruneSkylineSize) {
        HashSet<Plan> tointe = new HashSet<>();
        tointe.addAll(skylinePlansNew.results);

        ArrayList<Plan> t = new ArrayList<Plan>();
        for(int i=0;i<results.size() && t.size()<pruneSkylineSize;++i){
            if(tointe.contains(results.get(i))){
                t.add(results.get(i));
            }
        }
        results=t;
    }
}


