package Scheduler;

import utils.Pair;
import utils.Triple;

import java.util.*;

import static java.lang.Double.*;
import static java.lang.Double.compare;


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

    public long getFastestTime(){
        long runtime=Long.MAX_VALUE;
        for(Plan p:results){
            runtime = Math.min(runtime,p.stats.runtime_MS);
        }
        return runtime;

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

    public long getMinRuntime(){
        long runtime=Long.MAX_VALUE;
        for(Plan p:results){
            runtime = Math.min(runtime,p.stats.runtime_MS);
        }
        return runtime;
    }
    public double getMinCost(){
        double cost = Double.MAX_VALUE;
        for(Plan p:results){
            cost = Math.min(cost,p.stats.money);
        }
        return cost;
    }



    public Plan getMinRuntimePlan(){
        long runtime=Long.MAX_VALUE;
        Plan pp = null;
        for(Plan p:results){
            if (p.stats.runtime_MS < runtime) {
                runtime = p.stats.runtime_MS;
                pp = p;
            }
        }
        return pp;
    }

    public Plan getSlowest(){
        this.sort(true, false);
        return results.get(results.size()-1);
    }

    public  Plan getKnee(){
        Plan tp=null;
        double t= Double.MAX_VALUE;
        for(Plan p:results){
            double tt = p.stats.runtime_MS*0.5-0.5*p.stats.money;
            if( tt<t ){
                tp = p;
                t = tt;
            }
        }
        return tp;
    }

    public Plan getMaxUnfairnessPlan(boolean partialSolution){
        double unfairness=Double.MIN_VALUE;
        Plan temp = null;
        for(Plan p:results){
            if(partialSolution) {
                if (p.stats.partialUnfairness > unfairness) {
                    unfairness = p.stats.partialUnfairness;
                    temp = p;
                } }
                else {
                if (p.stats.unfairness > unfairness) {
                    unfairness = p.stats.unfairness;
                    temp = p;
                } }
        }
        return temp;
    }

    public double getMaxUnfairness(boolean partialSolution){
        Plan p = getMaxUnfairnessPlan(partialSolution);
        if (partialSolution) {
            return p.stats.partialUnfairness;
        }
        else{
            return p.stats.unfairness;
        }
    }

    public Plan getMinUnfairnessPlan(boolean partialSolution){
        double unfairness=Double.MAX_VALUE;
        Plan temp = null;
        for(Plan p:results){
            if(partialSolution) {
                if (p.stats.partialUnfairness < unfairness) {
                    unfairness = p.stats.partialUnfairness;
                    temp = p;
                } }
            else {
                if (p.stats.unfairness < unfairness) {
                    unfairness = p.stats.unfairness;
                    temp = p;
                } }
        }
        return temp;
    }

    public double getMinUnfairness(boolean partialSolution) {
        Plan p = getMinUnfairnessPlan(partialSolution);
        if (partialSolution) {
            return p.stats.partialUnfairness;
        }
        else{
                return p.stats.unfairness;
            }
    }


//    public double getMinUnfairness(boolean partialSolution){
//        double unfairness=Double.MAX_VALUE;
//        for(Plan p:results){
//
//            if(partialSolution)
//                unfairness = Math.min(unfairness,p.stats.partialUnfairness);
//            else
//                unfairness = Math.min(unfairness,p.stats.unfairness);
//
//
//        }
//        if(unfairness==Double.MAX_VALUE)
//            System.out.println("it is 1");
//        return unfairness;
//    }
//
//    public double getMaxUnfairness(boolean partialSolution){
//        double unfairness=Double.MIN_VALUE;
//        for(Plan p:results){
//            if(partialSolution)
//                unfairness = Math.max(unfairness,p.stats.partialUnfairness);
//            else
//                unfairness = Math.max(unfairness,p.stats.unfairness);
//        }
//        if(unfairness==Double.MIN_VALUE)
//            System.out.println("it is 2");
//        return unfairness;
//    }

    public void print() {
        System.out.println(this.toString());
    }

    public void smallPrint() {
        ArrayList<Triple<Long, Double, Double>> mtpairs = new ArrayList<>();
        for (Plan p: results){
            mtpairs.add(new Triple<>(p.stats.runtime_MS, p.stats.money, p.stats.unfairness));
        }
        Collections.sort(mtpairs, new Comparator<Triple<Long, Double, Double>>() {
            @Override
            public int compare(Triple<Long, Double, Double> o1, Triple<Long, Double, Double> o2) {
                return Long.compare(o1.a, o2.a);
            }
        });
        System.out.println("----------------");
        System.out.println(" Time  ----   Money");
        for (Triple<Long,Double, Double> p : mtpairs){
            System.out.println(p.a + " -- " + p.b + " --- " + p.c);
        }
        System.out.println("----------------");
    }


    public void clear(){
        results.clear();
        optimizationTime_MS = -1;
    }


    public void sort(boolean skyband){

        Comparator<Plan> planComparator = (Comparator<Plan>) (p1, p2) -> {//


            if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
                if (Math.abs(p1.stats.money - p2.stats.money) < RuntimeConstants.precisionError) {

                    if (Math.abs(p1.stats.partialUnfairness - p2.stats.partialUnfairness) < RuntimeConstants.precisionError)
                        return compare(p1.stats.contUtilization, p2.stats.contUtilization);//leave it as it is;
                    else if (p1.stats.partialUnfairness > p2.stats.partialUnfairness)
                        return 1;
                    else
                        return -1;
                }
                return compare(p1.stats.money, p2.stats.money);
            } else {
                return Long.compare(p1.stats.runtime_MS, p2.stats.runtime_MS);
            }


        };



                Collections.sort(results, planComparator);


    }


    public void sort(boolean isPareto, boolean multi){

        Comparator<Plan> ParetoPlanComparator = (Comparator<Plan>) (p1, p2) -> {
            if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
                if (Math.abs(p1.stats.money - p2.stats.money) < RuntimeConstants.precisionError) {
                    return Double.compare(p1.stats.contUtilization, p2.stats.contUtilization);

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




        Comparator<Plan> MultiParetoPlanComparator = (Comparator<Plan>) (p1, p2) -> {//


            if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
                if (Math.abs(p1.stats.money - p2.stats.money) < RuntimeConstants.precisionError) {

                    if (Math.abs(p1.stats.partialUnfairness - p2.stats.partialUnfairness) < RuntimeConstants.precisionError)
                        return compare(p1.stats.contUtilization, p2.stats.contUtilization);//leave it as it is;
                    else if (p1.stats.partialUnfairness > p2.stats.partialUnfairness)
                        return 1;
                    else
                        return -1;
                }
                return compare(p1.stats.money, p2.stats.money);
            } else {
                return Long.compare(p1.stats.runtime_MS, p2.stats.runtime_MS);
            }

//            double[] obj1= {p1.stats.runtime_MS, p1.stats.money};//, p1.stats.contUtilization};
//            double[] obj2= {p2.stats.runtime_MS, p2.stats.money};//, p2.stats.contUtilization};
//
//            solutionObjectives solution1 = new solutionObjectives(p1, obj1);
//            solutionObjectives solution2 = new solutionObjectives(p2, obj2);
//
//            boolean dominate1 = false;
//            boolean dominate2 = false;
//
//            for (int i = 0; i < solution1.getObjectives().length; i++) {
//                if ( (Math.abs(solution1.getObjective(i) - solution2.getObjective(i)) > RuntimeConstants.precisionError) && solution1.getObjective(i) < solution2.getObjective(i)) {
//                    dominate1 = true;
//
//                    if (dominate2) {
//                        return 0;
//                    }
//                } else if ((Math.abs(solution1.getObjective(i) - solution2.getObjective(i)) > RuntimeConstants.precisionError) &&  solution1.getObjective(i) > solution2.getObjective(i)) {
//                    dominate2 = true;
//
//                    if (dominate1) {
//                        return 0;
//                    }
//                }
//            }
//
//            if (dominate1 == dominate2) {
//                return 0;
//            } else if (dominate1) {
//                return -1;
//            } else {
//                return 1;
//            }

        };




        if(isPareto) {
            if(multi)
            {
                Collections.sort(results, MultiParetoPlanComparator);

//                System.out.println("sorted as");
//
//                for (int i = 0; i < results.size(); i++)
//                    System.out.println(i + " " + results.get(i).stats.runtime_MS + " " + results.get(i).stats.money + " " + results.get(i).stats.partialUnfairness);

            }
            else {
               // System.out.println("ppc here");

                Collections.sort(results, ParetoPlanComparator);
            }
        }else {
           // System.out.println("oups");
            Collections.sort(results, PlanComparator);
        }


    }



    @Override public Iterator<Plan> iterator() {
        return results.iterator();
    }

    public double getScoreElastic(){
        double maxTime=0,minTime= MAX_VALUE;
        double maxMoney=0,minMoney= MAX_VALUE;

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

    public  void computeSkyline(boolean isPareto, boolean multi){

        SolutionSpace skyline = new SolutionSpace();

        this.sort(isPareto, multi); // Sort by time breaking equality by sorting by money

        if(multi)
        {
            Plan previous = null;

            Plan previousFair = null;

            for (Plan est : results) {

                if (previous == null) {
                    skyline.add(est);
                    previous = est;
                    previousFair = est;
                    continue;
                }
                if (previous.stats.runtime_MS == est.stats.runtime_MS) {
                    // Already sorted by money

                    if (Math.abs(previousFair.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError) //TODO use fairness
                        if (previousFair.stats.partialUnfairness > est.stats.partialUnfairness) {//use Double.compare. at moheft as well or add precision error
                            skyline.add(est);
                            previousFair = est;
                        }

                    continue;
                }
                if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError) //TODO ji fix or check
                    if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                        skyline.add(est);
                        previous = est;
                        previousFair = est;
                        continue;
                    }

                if (Math.abs(previousFair.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError) //TODO use fairness
                    if (previousFair.stats.partialUnfairness > est.stats.partialUnfairness) {//use Double.compare. at moheft as well or add precision error
                        skyline.add(est);
                        previousFair = est;
                    }


            }
        }
            else{
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
                if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError) //TODO ji fix or check
                    if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                        skyline.add(est);
                        previous = est;
                    }
            }

        }
        results.clear();
        results.addAll(skyline.results);

    }

    public  ArrayList<Plan> getSkyline(boolean multi, boolean partialSolution){//todo: change

        SolutionSpace skyline = new SolutionSpace();


        this.sort(true, multi); // Sort by time breaking equality by sorting by money

//        System.out.println("\n\nsorted:");
//        for(Plan e: results)
//            System.out.println(e.stats.money+ " " + e.stats.runtime_MS+ " " + e.stats.partialUnfairness );
//        System.out.println(" \n");



//        Plan previous = null;
//        for (Plan est : results) {
//            if (previous == null) {
//                skyline.add(est);
//                previous = est;
//                continue;
//            }
//            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
//                // Already sorted by money
//                continue;
//            }
//            if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError) //TODO ji fix or check
//                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
//                    skyline.add(est);
//                    previous = est;
//                }
//        }

        
        if(multi) {//multi-objective includes fairness
            Plan previous = null;

            Plan previousFair = null;

            for (Plan est : results) {

//                System.out.println("looks for" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.contUtilization );
//                System.out.println("compares with" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.contUtilization );
//                System.out.println("compares with" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.contUtilization );

                if (previous == null) {
                    skyline.add(est);
                    previous = est;
                    previousFair = est;
                    continue;
                }
                if (previous.stats.runtime_MS == est.stats.runtime_MS) {
                    // Already sorted by money

                    if(partialSolution) {
                        if (Math.abs(previousFair.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError && Math.abs(previous.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError) //TODO use fairness
                            if (previousFair.stats.partialUnfairness > est.stats.partialUnfairness && previous.stats.partialUnfairness > est.stats.partialUnfairness) {//use Double.compare. at moheft as well or add precision error
                                skyline.add(est);
                                previousFair = est;
                                //      System.out.println("also added" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.partialUnfairness );
                            }
                    }else {
                        if (Math.abs(previousFair.stats.unfairness - est.stats.unfairness) > RuntimeConstants.precisionError && Math.abs(previous.stats.unfairness - est.stats.unfairness) > RuntimeConstants.precisionError) //TODO use fairness
                            if (previousFair.stats.unfairness > est.stats.unfairness && previous.stats.unfairness > est.stats.unfairness) {//use Double.compare. at moheft as well or add precision error
                                skyline.add(est);
                                previousFair = est;
                                //      System.out.println("also added" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.partialUnfairness );
                            }
                    }

                    continue;
                }
                if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError)
                    if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                        skyline.add(est);

                        previous = est;

                        if(partialSolution) {
                            if (Math.abs(previousFair.stats.partialUnfairness - previous.stats.partialUnfairness) > RuntimeConstants.precisionError)
                                if (previousFair.stats.partialUnfairness > previous.stats.partialUnfairness) {//use Double.compare. at moheft as well or add precision error
                                    previousFair = est;
                                }
                        }
                        else {
                            if (Math.abs(previousFair.stats.unfairness - previous.stats.unfairness) > RuntimeConstants.precisionError)
                                if (previousFair.stats.unfairness > previous.stats.unfairness) {//use Double.compare. at moheft as well or add precision error
                                    previousFair = est;
                                }
                        }

                        continue;
                    }

                    if(partialSolution) {
                        if (Math.abs(previousFair.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError && Math.abs(previous.stats.partialUnfairness - est.stats.partialUnfairness) > RuntimeConstants.precisionError) //TODO use fairness
                            if (previousFair.stats.partialUnfairness > est.stats.partialUnfairness && previous.stats.partialUnfairness > est.stats.partialUnfairness) {//use Double.compare. at moheft as well or add precision error
                                skyline.add(est);
                                previousFair = est;
                                //     System.out.println("also added" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.partialUnfairness );
                            }
                    }
                    else {
                        if (Math.abs(previousFair.stats.unfairness - est.stats.unfairness) > RuntimeConstants.precisionError && Math.abs(previous.stats.unfairness - est.stats.unfairness) > RuntimeConstants.precisionError) //TODO use fairness
                            if (previousFair.stats.unfairness > est.stats.unfairness && previous.stats.unfairness > est.stats.unfairness) {//use Double.compare. at moheft as well or add precision error
                                skyline.add(est);
                                previousFair = est;
                                //     System.out.println("also added" + est.stats.money+ " " + est.stats.runtime_MS+ " " + est.stats.partialUnfairness );
                            }
                    }


            }
        }//bi-objective for time-money
        else {
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
                if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError) //TODO ji fix or check
                    if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                        skyline.add(est);
                        previous = est;
                    }
            }
        }

//        System.out.println("skyline is:");
//        for(Plan e: skyline)
//        System.out.println(e.stats.money+ " " + e.stats.runtime_MS+ " " + e.stats.partialUnfairness + " " + e.stats.unfairness);

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

    //computeSkylineSkyband1: computation of skyband with k points with min unfairness:
    public void computeSkylineSkyband1(boolean pruneEnabled, int k, boolean keepWhole, String method, boolean multi, boolean partialSolution) {
// if in skyband of C-T not unf

       double bandSize = 0.05;

        ArrayList<Plan> skylineCT = getSkyline(false, false);//todo: use unfairness when equal
        ArrayList<Plan> skyBand = new ArrayList<Plan>();
        this.sort(true, false);//may not be required
        skyBand.addAll(this.results);//correct?

        Iterator <Plan> skyBandIterator = skyBand.iterator();

        Iterator <Plan> skylineCTIterator = skylineCT.iterator();



        Plan nextSK = skylineCTIterator.next();
        Plan prevSK =nextSK;

        Plan prevSB=null;
        Plan nextSB = null;

        ArrayList<Plan> plans = new ArrayList<>();

        while(skyBandIterator.hasNext())
        {
            prevSB =nextSB;
            nextSB = skyBandIterator.next();

            if(prevSB!=null) {//if same as previously kept point remove it. should also check if same in C-T and unfairness larger remove it
                double timeDif = Math.abs(nextSB.stats.runtime_MS- prevSB.stats.runtime_MS);
                double moneyDif = Math.abs(nextSB.stats.money- prevSB.stats.money);
                double unfDif = prevSB.stats.unfairness- nextSB.stats.unfairness;
                if(partialSolution)
                    unfDif = prevSB.stats.partialUnfairness- nextSB.stats.partialUnfairness;
                if (timeDif<1e-12 && moneyDif<1e-12 && unfDif<1e-12)
                    continue;
            }

            double timeDif = Math.abs(nextSB.stats.runtime_MS- prevSK.stats.runtime_MS)/(double)prevSK.stats.runtime_MS;//correct?
            double moneyDif = Math.abs(nextSB.stats.money- prevSK.stats.money)/(double)prevSK.stats.money;
            double unfDif = Math.abs(nextSB.stats.unfairness- prevSK.stats.unfairness)/(double)prevSK.stats.unfairness;
            if(partialSolution)
                unfDif = Math.abs(nextSB.stats.partialUnfairness- prevSK.stats.partialUnfairness)/(double)prevSK.stats.partialUnfairness;

            if(nextSB.equals(nextSK)) {//skylinePoint so keep it and continue
                 prevSK = nextSK;
                plans.add(nextSB);

                if(skylineCTIterator.hasNext()) {
                    nextSK = skylineCTIterator.next();
                }

                continue;
            }

            if(timeDif<1e-12 && moneyDif<1e-12 && unfDif<1e-12)
                continue;//skyBandIterator.remove(nextSB);//duplicate so remove it from the list
            else
            if(timeDif > bandSize || moneyDif > bandSize)
                continue;//skyBandIterator.remove(nextSB);//not within the skyband range so remove it from the list

           plans.add(nextSB);



        }

//sort by unfairness and keep first k
        Collections.sort(plans, new Comparator<Plan>() {
            @Override public int compare(Plan p1, Plan p2) {
                if(partialSolution)
                    return Double.compare(p1.stats.partialUnfairness, p2.stats.partialUnfairness);
                else
                return Double.compare(p1.stats.unfairness, p2.stats.unfairness);
            }
        });


        this.results.clear();

//    if(k>plans.size())
//        this.results.addAll(plans);
//    else
        for(int i=0; i<Math.min(k, plans.size()); i++)
            this.results.add(plans.get(i));


//        for(int i=0; i<plans.size(); i++)
//           System.out.println("kept " + plans.get(i).stats.money + " " + plans.get(i).stats.runtime_MS + " " + plans.get(i).stats.unfairness + " " +plans.get(i).stats.partialUnfairness);

    }



    public void computeSkylineSkyband2(boolean pruneEnabled, int k, boolean keepWhole, String method, boolean multi, boolean partialSolution){//a second way sth like that?

        double bandSize=0.05;
        SolutionSpace skyBand = new SolutionSpace();

        Plan prevSB=null;

        this.sort(true); // Sort by time breaking equality by sorting by money

        System.out.println("\n\nsorted:");
        for(Plan e: results)
            System.out.println(e.stats.money+ " " + e.stats.runtime_MS+ " " + e.stats.partialUnfairness );
        System.out.println(" \n");

        Plan previous = null;
        for (Plan est : results) {


            double timeDif = 0;
            double moneyDif = 0;
            double unfDif = 0;

            if(prevSB!=null)////add it if all the difs within skyband and diif from prevskyband<0.05
            {
                timeDif = Math.abs(prevSB.stats.runtime_MS- est.stats.runtime_MS)/(double)prevSB.stats.runtime_MS;
                moneyDif = Math.abs(prevSB.stats.money- est.stats.money)/(double)prevSB.stats.money;
                unfDif = Math.abs(prevSB.stats.unfairness- est.stats.unfairness)/(double)prevSB.stats.unfairness;
                if(partialSolution)
                    unfDif = Math.abs(prevSB.stats.partialUnfairness- est.stats.partialUnfairness)/(double)prevSB.stats.partialUnfairness;

            }

            if (previous == null) {
                prevSB=est;
                skyBand.add(est);
                previous = est;
                continue;

            }
            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
                // Already sorted by money

                if (timeDif<1e-12 && moneyDif<1e-12 && unfDif<1e-12)
                    continue;
                else if(timeDif > bandSize || moneyDif > bandSize)
                    continue;//skyBandIterator.remove(nextSB);//not within the skyband range so remove it from the list
                else
                {
                    prevSB=est;
                    skyBand.add(est);
                }



                continue;
            }
            if (Math.abs(previous.stats.money - est.stats.money) > RuntimeConstants.precisionError) //TODO ji fix or check
                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                    skyBand.add(est);
                    prevSB=est;
                    previous = est;
                }
                else if (timeDif<1e-12 && moneyDif<1e-12 && unfDif<1e-12)
                    continue;
                else if(timeDif > bandSize || moneyDif > bandSize)
                    continue;//skyBandIterator.remove(nextSB);//not within the skyband range so remove it from the list
                else
                {
                    prevSB=est;
                    skyBand.add(est);
                }



        }

        ArrayList<Plan> plans = new ArrayList<>();
        plans.addAll(skyBand.results);

        //sort by unfairness and keep first k
        Collections.sort(plans, new Comparator<Plan>() {
            @Override public int compare(Plan p1, Plan p2) {
                if(partialSolution)
                    return Double.compare(p1.stats.partialUnfairness, p2.stats.partialUnfairness);
                else
                    return Double.compare(p1.stats.unfairness, p2.stats.unfairness);
            }
        });


        this.results.clear();

//    if(k>plans.size())
//        this.results.addAll(plans);
//    else
        for(int i=0; i<Math.min(k, plans.size()); i++)
            this.results.add(plans.get(i));

        // return skyBand.results;

    }

    public void computeSkyline(boolean pruneEnabled, int k, boolean keepWhole, String method, boolean multi,
                               boolean partialSolution, int constraint_mode, double money_constraint, long time_constraint ){

        ArrayList<Plan> skyline = new ArrayList<>();
        if (constraint_mode == 1) {
            // return the schedule that maximizes fairness given the constraints.
            HashSet<Plan> retset  = new HashSet<>();
            keepPlanWithinConstraints(money_constraint, time_constraint);
            if (results.size() > 0) {
                retset.add(getMinUnfairnessPlan(partialSolution));
            }
            this.results.clear();
            this.results.addAll(retset);
            return;
        } else if (constraint_mode == 2) {
            // find all plans that satisfy the constraints and then use pruning.
            HashSet<Plan> retset  = new HashSet<>();
            keepPlanWithinConstraints(money_constraint, time_constraint);
        }
        // the rest of the code works on the skyline
        skyline = getSkyline(multi, partialSolution);

        if(!pruneEnabled){

            System.out.println("pruneEnabled false");
            this.results.clear();
            this.results.addAll(skyline);
            return;
        }else {
            if (keepWhole) {
                k = results.size();
            }
            if (k >= skyline.size()) {
                this.results.clear();
                this.results.addAll(skyline);
                return;
            }
            HashSet<Plan> retset  = new HashSet<>();

            if (method.equals("valkanas")) {
                valkanasPruning(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("valkanas1and2")){
                valkanasPruning1and2(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingScoreDist")) {
                crowdingDistanceScoreNormalized(skyline, k, retset);
                this.results.clear();
                this.results.addAll(retset);
            } else if (method.equals("crowdingDistanceScoreNormalizedMin")) {
                    crowdingDistanceScoreNormalizedMin(skyline,k,retset);
                    this.results.clear();
                    this.results.addAll(retset);
            }
            else if (method.equals("crowdingScoreDist2")) {
                crowdingDistanceScoreNormalized2(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("crowdingMoney")) {
                crowdingDistanceMoney(skyline,k,retset, false);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingRuntime")) {
                crowdingDistanceRuntime(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowdingMaxDist")) {
                crowdingDistanceMaxDist(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("crowding")) {
                crowdingDistance(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("crowdingMaxMoney")) {
                crowdingMaxMoney(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("newall")) {
                crowdingDistanceScoreNormalized(skyline, k / 2, retset);
                crowdingDistanceRuntime(skyline, k, retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("Knee")) {
             //   if(skyline.size()>k) {
                    //System.out.println("skyline size is more than k " + skyline.toString());
                    Knee(skyline, k, retset, multi, partialSolution);
                    this.results.clear();
                    this.results.addAll(retset);
                    //System.out.println("results are" + this.results.toString());

            //    }
            //    else {
                    //for(int j=0; j<skyline.size(); j++)
                  //  System.out.println("skyline size is less than k " + skyline.get(j).);
                    //for(int j=0; j<skyline.size(); j++)
                    //    System.out.println("results are" + this.results.get(j));
            //    }
            }
            else if (method.equals("newall2")){
                crowdingDistanceScoreNormalized(skyline,k/2,retset);
                crowdingDistanceMoney(skyline,k,retset, false);
                this.results.clear();
                this.results.addAll(retset);
            }else if (method.equals("scoreDist+maxMoney")){
                crowdingMaxMoney(skyline,k/2+1,retset);
                crowdingDistanceScoreNormalized(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else if (method.equals("crowdandScore")){
                crowdingDistanceScoreNormalized(skyline,5*k/7,retset);
                crowdingDistance(skyline,k,retset);
                this.results.clear();
                this.results.addAll(retset);
            }
            else{
                System.out.println("houston we have a problem: "+ method);
            }
        }

//        System.out.println("\n\nafter pruning:");
//        for(Plan e: results) {
//            System.out.println(e.stats.money + " " + e.stats.runtime_MS + " " + e.stats.partialUnfairness + " " + e.stats.unfairness);

//        }
//
//        System.out.println(" \n");

    }

    private HashSet<Plan> valkanasPruning1and2(ArrayList<Plan> skyline, int k,HashSet<Plan> retset) {
        //valkPruning and add the 10 lowest in dom Size
//        HashSet<Plan> retset = new HashSet<>();

        addExtremes(skyline,retset);


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

        while (retset.size() < k-4) {
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

        int i=0;
        while (i<sorted.size()-1 && retset.size() < k) {
            retset.add(sorted.get(i).a);
            ++i;
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

    public void keepK(int k, boolean multi, boolean partialSolution) {
        ArrayList<Plan> sk = getSkyline(multi, partialSolution);

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

    public double getScore(Plan p, long longest, double maxcost){
        return (0.5*(p.stats.money/maxcost))+(0.5*(p.stats.runtime_MS/longest));
    }

    public double getScoreMin(Plan p, long longest, double maxcost){
        return Math.abs((0.5*(p.stats.money/maxcost))-(0.5*(p.stats.runtime_MS/longest)));
    }

    public HashSet<Plan> valkanasPruning(ArrayList<Plan> skyline, int k, HashSet<Plan> retset){
//        HashSet<Plan> retset = new HashSet<>();

       addExtremes(skyline,retset);

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


        addExtremes(donotchange,ret);


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


        int i=0;
        while(i<skylinePlans.size()){// && ret.size()<skylinePlansToKeep){//
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }

        return ret;
    }

    public HashSet<Plan> crowdingDistanceScoreNormalizedMin(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {


        addExtremes(donotchange,ret);

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
            planToScore.put(p,getScoreMin(p,maxRuntime,maxcost));
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(planToScore.get(o2),planToScore.get(o1));
            }
        });

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


        addExtremes(donotchange,ret);

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

    addExtremes(donotchange,ret);
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



        int i=0;
        while(ret.size()<skylinePlansToKeep){
            if(!ret.contains(skylinePlans.get(i))){
                ret.add(skylinePlans.get(i));
            }
            i++;
        }


        return ret;
    }

    public HashSet<Plan> crowdingDistanceMoney(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret, boolean multi) {
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

        this.sort(true, multi);
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

    public HashSet<Plan> crowdingDistanceMaxDist(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret) {


        if(donotchange.size()<=skylinePlansToKeep){
            ret.addAll(donotchange);
            return ret;
        }

        addExtremes(donotchange,ret);

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

//        double dist1= Double.MAX_VALUE;
//        double dist2 = Double.MAX_VALUE;

        final HashMap<Plan, Double> planDistance = new HashMap<>();

        ArrayList<Double> sortedList = new ArrayList<>();

//        dist1 = Math.min(calculateEuclidean(donotchange.get(2),donotchange.get(0)),calculateEuclidean(donotchange.get(2),donotchange.get(1)));
//        dist2 = Math.max(calculateEuclidean(donotchange.get(2),donotchange.get(0)),calculateEuclidean(donotchange.get(2),donotchange.get(1)));

        for(Plan p:donotchange){
//            dist1= Double.MAX_VALUE;
//            dist2 = Double.MAX_VALUE;
            sortedList.clear();
            for(int i=0;i<donotchange.size();++i){
                Plan pp = donotchange.get(i);
                if(p == pp)continue;
                sortedList.add(calculateEuclidean(p,pp));
            }
            Collections.sort(sortedList);
            planDistance.put(p,Math.max(sortedList.get(0),sortedList.get(1)));

        }


        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });

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

        addExtremes(donotchange,ret);


        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);
        int schedulesKept = 0;
        final HashMap<Plan, Double> planDistance = new HashMap<>();

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
            }
        });
        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p == 0 || p == skylinePlans.size() - 1) {
                planDistance.put(skylinePlans.get(p), MAX_VALUE);
            } else {
                long makespan_prev = skylinePlans.get(0).stats.runtime_MS;
                long makespan_next = skylinePlans.get(p).stats.runtime_MS;
                planDistance.put(skylinePlans.get(p),
                    (double)Math.abs(makespan_next - makespan_prev));
            }
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });
        for (int p = 0; p < skylinePlans.size(); ++p) {
            if (p == 0 || p == skylinePlans.size() - 1) {
                planDistance.put(skylinePlans.get(p), MAX_VALUE);
            } else {
                Double money_prev = skylinePlans.get(0).stats.money;
                Double money_next = skylinePlans.get(p).stats.money;
                planDistance.put(skylinePlans.get(p),
                    planDistance.get(skylinePlans.get(p)) * Math
                        .abs(money_next - money_prev));
            }
        }

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(planDistance.get(o1),
                    planDistance.get(o2));
            }
        });

        int i=0;
        while(i<skylinePlans.size()-1 && ret.size()<skylinePlansToKeep){
                ret.add(skylinePlans.get(i));
                ++i;
        }
        return ret;
    }

    public HashSet<Plan> crowdingMaxMoney(ArrayList<Plan> donotchange, int skylinePlansToKeep, HashSet<Plan> ret){

        addExtremes(donotchange,ret);
        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.addAll(donotchange);

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
               sortedList.add(new Pair<>(skylinePlans.get(p), MAX_VALUE));
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

    public HashSet<Plan> Knee(ArrayList<Plan> donotchange,int k,HashSet<Plan> ret, boolean multi, boolean partialSolution){//TODO: if plans number > k then keep all plans

        addExtremes(donotchange,ret);

//        Collections.sort(donotchange, new Comparator<Plan>() {
//            @Override public int compare(Plan o1, Plan o2) {
//                return Long.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
//            }
//        });
//
//        Plan slowest = donotchange.get(donotchange.size()-1);
//        Plan fastest = donotchange.get(0);
//
//
//        ArrayList<Plan> skylinePlans = new ArrayList<>();
//
//        Collections.sort(results, new Comparator<Plan>() {
//            @Override public int compare(Plan o1, Plan o2) {
//                return Double.compare(o1.stats.money,o2.stats.money);
//            }
//        });
//        ArrayList<Pair<Plan,Double>> tosort = new ArrayList<>();
//
//        for(int i=0;i<size()-1;++i){
//            tosort.add(new Pair( results.get(i),costPerTime(results.get(i),results.get(i+1)) ) );
//        }
//        Collections.sort(tosort, new Comparator<Pair<Plan, Double>>() {
//            @Override
//            public int compare(Pair<Plan, Double> o1, Pair<Plan, Double> o2) {
//                return (int)Double.compare(o1.b,o2.b);
//            }
//        });
//
////        Plan knee = tosort.get(0).a;
////
////        long dist1 = slowest.stats.runtime_MS - knee.stats.runtime_MS;
////        long dist2 = knee.stats.runtime_MS - fastest.stats.runtime_MS;
     //   System.out.println("ret is " + ret.size());
        HashMap<Plan,Double> secondDer = new HashMap<>();
        ArrayList<Plan> knees = getKneess(donotchange,secondDer, multi,partialSolution);


        ArrayList<Pair<Plan,Double>> finalMetric = new ArrayList<>();

        double maxSecondDer = 0.0;
        for(Double der:secondDer.values()){
            maxSecondDer = Math.max(maxSecondDer,der);
        }
        double maxDist = calculateEuclideanMulti(getMaxCostPlan(), getMinCostPlan(), multi, partialSolution);


        for(int j=1;j<donotchange.size()-1;++j){
            Plan p = donotchange.get(j);
            if(knees.contains(p)){
                finalMetric.add(new Pair<Plan, Double>(p, MAX_VALUE));
            }else{
                finalMetric.add(new Pair<Plan, Double>(p,((0.5*secondDer.get(p))/maxSecondDer)*((0.5*minDist(p,knees, multi, partialSolution))/maxDist ) ));//ignore 0.5...

            }
        }

        Collections.sort(finalMetric, new Comparator<Pair<Plan, Double>>() {
                        @Override
                        public int compare(Pair<Plan, Double> o1, Pair<Plan, Double> o2) {
                            return (int)Double.compare(o2.b,o1.b);
                        }
                    });

        int i=0;

        while(ret.size()<k && finalMetric.size()>i+1){

            ret.add(finalMetric.get(i).a);
            ++i;
        }

        return ret;
    }

    // returns the second der and the knees
    public ArrayList<Plan> getKneess(ArrayList<Plan> pp,HashMap<Plan,Double> planMetric, boolean multi, boolean partialSolution){
        //sort by one dimension just to get an orderding
        Collections.sort(pp,new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                //System.out.println("done");

//               // if(multi) {
//                    if (o1.stats.runtime_MS == o2.stats.runtime_MS) {
//                        return Double.compare(o2.stats.money, o1.stats.money);
//                    } else {
//                        return Long.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
//                    }
//               // }
//               // else  return Long.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);

                if(multi) {
                    if (o1.stats.money == o2.stats.money) {
                        if (Math.abs(o1.stats.runtime_MS - o2.stats.runtime_MS) < RuntimeConstants.precisionError) {

                            if (Math.abs(o1.stats.partialUnfairness - o2.stats.partialUnfairness) < RuntimeConstants.precisionError)
                                return Double.compare(o1.stats.contUtilization, o2.stats.contUtilization);//leave it as it is;
                            else if (o1.stats.partialUnfairness > o2.stats.partialUnfairness)
                                return 1;
                            else
                                return -1;
                        }
                        return Long.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
                    } else {
                        return Double.compare(o1.stats.money, o2.stats.money);
                    }
                }
                else{
                    if (o1.stats.money  == o2.stats.money) {
                        if (Math.abs(o1.stats.runtime_MS - o2.stats.runtime_MS) < RuntimeConstants.precisionError) {

                            return Double.compare(o1.stats.contUtilization, o2.stats.contUtilization);//leave it as it is;

                        }
                        return Long.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
                    } else {
                        return Double.compare(o1.stats.money, o2.stats.money );
                    }
                }





            }
        });


        SolutionSpace plans = new SolutionSpace();
        ArrayList<Plan> knees = new ArrayList<>();
        plans.addAll(pp);
        int i=1;
        double d = 0.0;
        for(;i<plans.size()-1;++i){
            Plan p0 = plans.results.get(i-1);
            Plan p1 = plans.results.get(i);
            Plan p2 = plans.results.get(i+1);

        //    System.out.println("next is " + " " + p1.stats.money +  " " + p1.stats.runtime_MS +  " "+ p1.stats.unfairness);

            double secder=0.0;
          //  if(multi)
                secder = plans.getDerMulti(p0,p1,p2, multi, plans,partialSolution);
            //    else
                 //       secder = plans.getDer(p0,p1,p2, multi);
            d+=secder;
            planMetric.put(p1,secder);
        }
        i=1;
        double secder_Avg = d/(plans.results.size()-2);

        for(Plan p:planMetric.keySet()){

            if(planMetric.get(p)>secder_Avg){
                knees.add(p);
            }
        }
        return knees;
    }

    public double minDist(Plan p,ArrayList<Plan> knees, boolean multi, boolean partialSolution){
        Plan r = null;
        double dist = MAX_VALUE;
        double td;
        for(Plan pp:knees){
            td = calculateEuclideanMulti(p,pp, multi, partialSolution);
            if( td<dist ) {
                dist = td;
                r = pp;
            }
        }
        return dist;
    }

    public void addExtremes(ArrayList<Plan> plans,HashSet<Plan> ret){
        Plan maxCost = null, slowest = null;
        long slowestTime = 0;
        double maxCostCost = 0.0;

        for(Plan p:plans){
            if(p.stats.runtime_MS>slowestTime){
                slowest=p;
                slowestTime = p.stats.runtime_MS;
            }
            if(p.stats.money>maxCostCost){
                maxCost = p;
                maxCostCost = p.stats.money;
            }
        }
        ret.add(slowest);
        ret.add(maxCost);
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

    public double calculateEuclidean(Plan a,Plan b){
        double x = a.stats.runtime_MS - b.stats.runtime_MS;
        double y = a.stats.money - b.stats.money;
        return Math.sqrt((x*x)+(y*y));
    }

    public double calculateEuclideanMulti(Plan a,Plan b, boolean multi, boolean partialSolution){
        double x = a.stats.runtime_MS - b.stats.runtime_MS;
        double y = a.stats.money - b.stats.money;
        double z = a.stats.partialUnfairness - b.stats.partialUnfairness;
        if(!partialSolution)
            z = a.stats.unfairness - b.stats.unfairness;
        if(!multi)
            z=0.0;
        return Math.sqrt((x*x)+(y*y)+(z*z));
    }

    public Plan getMaxCostPlan() {
        double cost = 0;
        Plan pl = null;

        for(Plan p:results){
            if(p.stats.money>cost){
                cost = p.stats.money;
                pl = p;
            }

        }
        return pl;
    }
    public Plan getMinCostPlan() {
        double cost = MAX_VALUE;
        Plan pl = null;

        for(Plan p:results){
            if(p.stats.money<cost){
                cost = p.stats.money;
                pl = p;
            }

        }
        return pl;
    }

    public  double costPerTime(Plan p0, Plan p1){
        //sort by money first
        double moneyImprov = ((p1.stats.money - p0.stats.money) *100 )/ p0.stats.money;
        double timeImprov = ((double)(Math.abs(p1.stats.runtime_MS - p0.stats.runtime_MS))*100 )/ (double)p0.stats.runtime_MS;

        return timeImprov/moneyImprov;

    }

    public double getDer(Plan p0, Plan p1, Plan p2, Boolean multi, boolean partialSolution){
        //sort by money first
        Statistics p0Stats = p0.stats;
        Statistics p1Stats = p1.stats;
        Statistics p2Stats = p2.stats;

        double aR = p1Stats.money - p0Stats.money;
        double bR = p1Stats.runtime_MS - p0Stats.runtime_MS;

        double aL = p2Stats.money - p1Stats.money;
        double bL = p2Stats.runtime_MS - p1Stats.runtime_MS;


        double thetaL = bR/aR;
        double thetaR = bL/aL;
        double theta2P1 = Math.abs(thetaL - thetaR);


        return theta2P1;
    }

    public double normalize(double range, double min, double value){
        if( range == 0) {return 0;}
        return (value - min) / range;
    }
    public long normalize(long range, long min, long value){
        if( range == 0) {return 0;}
        return (value - min) / range;
    }

    //YC changes
    public double getDerMulti(Plan p0, Plan p1, Plan p2, boolean multi, SolutionSpace plans, boolean partialSolution){//getDerMultiYC_combined
        Statistics p0Stats = p0.stats;
        Statistics p1Stats = p1.stats;
        Statistics p2Stats = p2.stats;

        if ( p2.stats.money < p1.stats.money || p1.stats.money < p0.stats.money){
            System.out.println("VALUES ARE NOT SORTED");
            System.exit(0);
        }

        double costRange = plans.getMaxCost() - plans.getMinCost();
        long timeRange = plans.getMaxRuntime() - plans.getMinRuntime();
        double unfairRange = plans.getMaxUnfairness(partialSolution) - plans.getMinUnfairness(partialSolution);

        double minCost = plans.getMinCost();
        long minTime = plans.getMinRuntime();
        double minUnfair = plans.getMinUnfairness(partialSolution);

        double m01 = normalize(costRange, minCost, p1Stats.money) - normalize(costRange, minCost,p0Stats.money);
        double t01 = normalize(timeRange, minTime, p1Stats.runtime_MS) - normalize(timeRange, minTime, p0Stats.runtime_MS);
        double u01 = normalize(unfairRange, minUnfair, p1Stats.partialUnfairness) - normalize(unfairRange, minUnfair, p0Stats.partialUnfairness);
        if(!partialSolution)
            u01 = normalize(unfairRange, minUnfair, p1Stats.unfairness) - normalize(unfairRange, minUnfair, p0Stats.unfairness);

        double m12 = normalize(costRange, minCost, p2Stats.money) - normalize(costRange, minCost, p1Stats.money);
        double t12 = normalize(timeRange, minTime, p2Stats.runtime_MS) - normalize(timeRange, minTime, p1Stats.runtime_MS);
        double u12 = normalize(unfairRange, minUnfair, p2Stats.partialUnfairness) - normalize(unfairRange, minUnfair, p1Stats.partialUnfairness);
        if(!partialSolution)
            u12 = normalize(unfairRange, minUnfair, p2Stats.unfairness) - normalize(unfairRange, minUnfair, p1Stats.unfairness);



        if(!multi)
        {
            u01=0.0;
            u12=0.0;
        }


        double theta01;
        if(Math.abs(m01)<1e-12) {
           // System.out.println("m01 " + m01);
            theta01 = 0.0;
        }else {
            theta01 = ( t01 + u01 ) / (m01);
        }
        double theta12;
        if(Math.abs(m12)<1e-12) {
            theta12 = 0.0;
        }else{
            theta12 = ( t12 + u12 ) / (m12);
        }

        return theta12 - theta01;  // I am not taking absolutes because I am not sure if they affect the result
        /* for example: in the two dimensional problem we knew that by sorting by money we sort execution time
        in the opposite way, if this did not hold our points would not be in the pareto set.
        But now when we sort by money we do not know what the other two dimension improve as we pay more money so we need to keep the
        signs (prosima?)
         */

    }

    public double getDerMultiIP(Plan p0, Plan p1, Plan p2, boolean multi, SolutionSpace plans, boolean partialSolution){
        //sort by money first
        Statistics p0Stats = p0.stats;
        Statistics p1Stats = p1.stats;
        Statistics p2Stats = p2.stats;

        double mR = p1Stats.money - p0Stats.money;
        double rR = p1Stats.runtime_MS - p0Stats.runtime_MS;
        double uR = p1Stats.partialUnfairness - p0Stats.partialUnfairness;

        double mL = p2Stats.money - p1Stats.money;
        double rL = p2Stats.runtime_MS - p1Stats.runtime_MS;
        double uL = p2Stats.partialUnfairness - p1Stats.partialUnfairness;




        //compute for each two-dimensional space the partial derivative as the difference between the left and right point (the start is the middle point)
        //and add the partial derivatives (i think it is not needed as it is directly the norm...)
        //change the euclidean distance for three dimensions

        double thetaL_t = rR/mR;
        if(Math.abs(mR)<1e-12)
            thetaL_t=0.0;
        double thetaR_t = rL/mL;
        if(Math.abs(mL)<1e-12)
            thetaR_t=0.0;
        double thetaL_u = uR/mR;
        if(Math.abs(mR)<1e-12)
            thetaL_u=0.0;
        double thetaR_u = uL/mL;
        if(Math.abs(mL)<1e-12)
            thetaR_u=0.0;
        double theta2P1 = Math.abs(thetaL_t - thetaR_t)*Math.abs(thetaL_u - thetaR_u);


        if(!multi)
            theta2P1 = Math.abs(thetaL_t - thetaR_t);
//
////        double timeDif = Math.pow(p2Stats.runtime_MS-p0Stats.runtime_MS, 2);
////        double moneyDif = Math.pow(p2Stats.money-p0Stats.money, 2);
////        double unfDif = Math.pow(p2Stats.partialUnfairness-p0Stats.partialUnfairness, 2);
////
////        if(multi)
//////        theta2P1 = Math.sqrt(timeDif + moneyDif + unfDif);
//
//        ///////////////////
//
//        double normaL = Math.sqrt(Math.pow(p0Stats.money,2) + Math.pow(p0Stats.runtime_MS,2) + Math.pow(p0Stats.partialUnfairness,2));
//        double normaM = Math.sqrt(Math.pow(p1Stats.money,2) + Math.pow(p1Stats.runtime_MS,2) + Math.pow(p1Stats.partialUnfairness,2));
//        double normaR = Math.sqrt(Math.pow(p2Stats.money,2) + Math.pow(p2Stats.runtime_MS,2) + Math.pow(p2Stats.partialUnfairness,2));
//
//
//        double productLM = p0Stats.money*p1Stats.money + p0Stats.runtime_MS*p1Stats.runtime_MS +p0Stats.partialUnfairness*p1Stats.partialUnfairness;//eswteriko ginomeno
//        double productMR = p2Stats.money*p1Stats.money + p2Stats.runtime_MS*p1Stats.runtime_MS +p2Stats.partialUnfairness*p1Stats.partialUnfairness;;
//
//        double thetaL = productLM/(normaL*normaM);
//        double thetaR = productMR/(normaR*normaM);
//       if(multi)
//         theta2P1 = thetaL*thetaR;
//        /////////////////////////////////////
//
//        double p0p1money= p1Stats.money-p0Stats.money;
//        double p0p1runtime= p1Stats.runtime_MS-p0Stats.runtime_MS;
//        double p0p1unf=p1Stats.partialUnfairness-p0Stats.partialUnfairness;
//
//        double p2p1money= p2Stats.money-p1Stats.money;
//        double p2p1runtime= p2Stats.runtime_MS-p1Stats.runtime_MS;
//        double p2p1unf=p2Stats.partialUnfairness-p1Stats.partialUnfairness;
//
//        double productDif = p0p1money*p2p1money + p0p1runtime*p2p1runtime + p0p1unf*p2p1unf;
//        double normap0p1 =  Math.sqrt(Math.pow(p0p1money,2) + Math.pow(p0p1runtime,2) + Math.pow(p0p1unf,2));
//        double normap2p1 =  Math.sqrt(Math.pow(p2p1money,2) + Math.pow(p2p1runtime,2) + Math.pow(p2p1unf,2));

        if(multi)

        {

            double costRange = plans.getMaxCost() - plans.getMinCost();
            long timeRange = plans.getMaxRuntime() - plans.getMinRuntime();
            double unfairRange = plans.getMaxUnfairness(partialSolution) - plans.getMinUnfairness(partialSolution);

            double minCost = plans.getMinCost();
            long minTime = plans.getMinRuntime();
            double minUnfair = plans.getMinUnfairness(partialSolution);

            double m01 = normalize(costRange, minCost, p1Stats.money) - normalize(costRange, minCost,p0Stats.money);
            double t01 = normalize(timeRange, minTime, p1Stats.runtime_MS) - normalize(timeRange, minTime, p0Stats.runtime_MS);
            double u01 = normalize(unfairRange, minUnfair, p1Stats.partialUnfairness) - normalize(unfairRange, minUnfair, p0Stats.partialUnfairness);
            if(!partialSolution)
                u01 = normalize(unfairRange, minUnfair, p1Stats.unfairness) - normalize(unfairRange, minUnfair, p0Stats.partialUnfairness);

            double m12 = normalize(costRange, minCost, p2Stats.money) - normalize(costRange, minCost, p1Stats.money);
            double t12 = normalize(timeRange, minTime, p2Stats.runtime_MS) - normalize(timeRange, minTime, p1Stats.runtime_MS);
            double u12 = normalize(unfairRange, minUnfair, p2Stats.partialUnfairness) - normalize(unfairRange, minUnfair, p1Stats.partialUnfairness);
            if(!partialSolution)
                u12 = normalize(unfairRange, minUnfair, p2Stats.unfairness) - normalize(unfairRange, minUnfair, p1Stats.unfairness);




            thetaL_t = t12/m12;
            if(Math.abs(m12)<1e-12)
                thetaL_t=0.0;

            thetaR_t = t01/m01;
            if(Math.abs(m01)<1e-12)
                thetaR_t=0.0;

            thetaL_u = u12/m12;
            if(Math.abs(m12)<1e-12)
                thetaL_u=0.0;

            thetaR_u = u01/m01;
            if(Math.abs(m01)<1e-12)
                thetaR_u=0.0;



            theta2P1 = Math.abs(thetaL_t - thetaR_t) * Math.abs(thetaL_u - thetaR_u);
        }
        //theta2P1 = productDif/(normap0p1*normap2p1);

        //p_1p_2 = (p2Stats.money-p1Stats.money, p2Stats.runtime_MS-p1Stats.runtime_MS, p2Stats.partialUnfairness-p1Stats.partialUnfairness)






        //use theta2PIu for unfairness cost and use product of theta2PI and theta2PIu



        return theta2P1;
    }


    public void keepPlanWithinConstraints(double money_constraint, long time_constraint) {
        HashSet<Plan> retset  = new HashSet<>();
        for(Plan p : results) {
            if (p.stats.money <= money_constraint && p.stats.runtime_MS <= time_constraint) {
                retset.add(p);
            }
        }
        this.results.clear();
        this.results.addAll(retset);
    }


}


