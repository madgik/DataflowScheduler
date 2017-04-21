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

    HashMap<Integer,Plan> sortedPrunned = new HashMap<>();

    static int steps=0;
    static int aaa = 0;
    public void computeSkyline(boolean pruneEnabled, int k, boolean keepWhole, String method){
        steps++;boolean aa= false;aaa++;
//        System.out.println("enter "+steps+ " "+aaa+" " +k+ " "+ getSkyline().size());
        if(steps>50) {
            aa=true;
            steps=0;
//            System.out.println(steps);
//                            plotUtility plot = new plotUtility();
//                            plot.plotPlans("StepAll:" + aaa, results);
                        }
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
                HashSet<Plan> retset = valkanasPruning(skyline,k);
                //                boolean newGreedy = false;
                //                if (newGreedy) {
                //
//                    Plan lastAddition = maxp;
//
//                    while (retset.size() < k) {
//                        int maxDOM = -1;
//                        double maxDIST = -1;
//                        Plan toadd = null;
//
//                        for (Plan sp : skyline) {
//                            if (retset.contains(sp)) {
//                                continue;
//                            }
//                            double jacdist = jaccardDist(sp, lastAddition, spToDom);
//
//                            if (jacdist >= maxDIST) {
//                                if (jacdist > maxDIST) {
//                                    toadd = sp;
//                                    maxDIST = jacdist;
//                                    maxDOM = spToDomSize.get(sp);
//                                } else if (maxDOM < spToDomSize.get(toadd)) {
//                                    toadd = sp;
//                                    maxDIST = jacdist;
//                                    maxDOM = spToDomSize.get(sp);
//                                }
//
//                            }
//
//                        }
//                        if (toadd != null) {
//                            retset.add(toadd);
//                            lastAddition = toadd;
//                        }
//
//                    }
//                }
                this.results.clear();
                this.results.addAll(retset);

            }else if (method.equals("crowding")) {
                HashSet<Plan> retset = crowdingDistance(skyline, k);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("a")){
                HashSet<Plan> retset = valkanasPruning(skyline,k);

                retset.addAll(valkanasPruning2(skyline,k));

                this.results.clear();
                this.results.addAll(retset);


                this.computeSkyline(false,k,false,"crowding");

            } else if (method.equals("all")){
                HashSet<Plan> retset = valkanasPruning1and2(skyline,k);



                this.results.clear();
                this.results.addAll(retset);


            }else{
                System.out.println("houston we have a problem");
            }
        }
        if(keepWhole && sortedPrunned.size()==0){
//            System.out.println("a");
        }
        if(aa) {
//            plotUtility plot = new plotUtility();
//            plot.plotPlans("StepPruned:" + aaa, results);
        }

    }

    private HashSet<Plan> valkanasPruning1and2(ArrayList<Plan> skyline, int k) {

        HashSet<Plan> retset = new HashSet<>();

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


    private HashSet<Plan>  valkanasPruning2(ArrayList<Plan> skyline, int k) {

        HashSet<Plan> retset = new HashSet<>();


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

        for(int i=0;i<k;++i){
         retset.add(sorted.get(i).a);

        }

        return retset;
    }

    public HashSet<Plan> valkanasPruning(ArrayList<Plan> skyline, int k){
        sortedPrunned.clear();
        HashSet<Plan> retset = new HashSet<>();

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
        sortedPrunned.put(retset.size(),maxp);

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

            sortedPrunned.put(retset.size(),tempSkylineCovP);

            skylineCoverage = tempSkylineCoverage;
        }


        return retset;
    }

    public HashSet<Plan> crowdingDistance(ArrayList<Plan> donotchange, int skylinePlansToKeep){
        sortedPrunned.clear();
        HashSet<Plan> ret = new HashSet<>();
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


