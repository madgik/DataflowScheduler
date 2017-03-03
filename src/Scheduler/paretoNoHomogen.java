package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by johnchronis on 2/19/17.
 */
public class paretoNoHomogen implements Scheduler {

//    public ArrayList<Plan> skylinePlans;
//    public ArrayList<Plan> paretoPlans;
    public SolutionSpace space;
    public Cluster cluster;
    public DAG graph;

    ///rankingks
    private HashMap<Long, Double> b_rank = new HashMap<>(); //opidTobRank
    private HashMap<Long, Double> t_rank = new HashMap<>();
    private HashMap<Long, Double> sum_rank = new HashMap<>();
    private HashMap<Long, Double> slacktime = new HashMap<>();
    private LinkedList<Long> opsSumRankSorted = new LinkedList<>();
    private Integer opsInfluential=-5;//10;//check from derivative ranking
    private LinkedList<Long> opsBySlack = new LinkedList<>();
    private HashMap<Long, Double> opSlack = new HashMap<>();
    private HashMap<Long, Integer> opIDsInfluential= new HashMap<>();
    public static LinkedList<Long> opsSorted = new LinkedList<>();


    ///
    public int maxContainers = 100;


    private HashMap<Long, Integer> opLevel = new HashMap<>(); //opid->level


    public paretoNoHomogen(DAG graph,Cluster cl){
//        skylinePlans = new ArrayList<>();
//        paretoPlans = new ArrayList<>();
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
    }



    @Override
    public SolutionSpace schedule() {
        long startCPU_MS = System.currentTimeMillis();

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        ArrayList<Plan> paretoPlans ;//= new ArrayList<>();

        computeRankings();
//        paretoPlans.clear();

        ArrayList<containerType> ctypes = new ArrayList<>();

        skylinePlans.clear();

        for(containerType cType: containerType.values()) {

            ctypes.clear();
            ctypes.add(containerType.LARGE);


            if (maxContainers == 1) {
                skylinePlans.add(onlyOneContainer());
            } else {

                if (cType.equals(containerType.getLargest())) {
                    skylinePlans.addAll(this.createAssigments("decreasing", ctypes));
                    System.out.println("s1 "+skylinePlans.size());
                } else if (cType.equals(containerType.getSmallest())) {
                    skylinePlans.addAll(this.createAssigments("increasing", ctypes));
                    System.out.println("s2 "+skylinePlans.size());
                } else{
                    skylinePlans.addAll(this.createAssigments("increasing/decreasing", ctypes));
                    System.out.println("s3 "+skylinePlans.size());
                }
            }
        }

        //all plans are in skyline plans

        paretoPlans = computeSkyline(skylinePlans);
        skylinePlans.clear();

        for(Plan pp: paretoPlans) {
            if (pp.vmUpgrading.equals("increasing/decreasing")) {

                pp.vmUpgrading = "increasing";
                skylinePlans.add(pp);

                Plan newpp = new Plan(pp);
                newpp.vmUpgrading="decreasing";
                skylinePlans.add(newpp);
            } else {
                skylinePlans.add(pp);
            }
        }

        paretoPlans.clear();
        paretoPlans.addAll(skylinePlans);

//        paretoPlans.addAll(homoToHetero()); //TODO j homotohetero

        space.addAllResults(paretoPlans);


        space.print();


        long endCPU_MS = System.currentTimeMillis();
        space.setOptimizationTime(endCPU_MS - startCPU_MS);
        return space;

    }

//    private void homoToHetero(ArrayList<Plan> skylinePlans) {
//
//
//
//        //look at each plan and upgrade one by one the LARGE containers
//
//        ArrayList<Plan> skylinePlansNew = new ArrayList<>();
//
//        int updateSkyline = 1;
//
//        while (updateSkyline == 1) {
//
//            updateSkyline = 0;
//
//            for (final Plan plan : skylinePlans) {
//
//
//                final HashMap<Long, Double> vmUtilisation = new HashMap<>();
//                final HashMap<Long, Double> contSlack = new HashMap<>();
//                final HashMap<Long, Integer> contOps = new HashMap<>();
//
//
//                LinkedList<Long> planContainers = new LinkedList<>();
//
//                HashMap<Long, Long> opIDtoCont = new HashMap<>();
//                HashMap<Long, Integer> opIDtoAssignment = new HashMap<>();
//
//                int ass=0;
//                for (Long opId: plan.assignments.keySet()) {
//
//                    opIDtoCont.put(opId, plan.assignments.get(opId));
//                    opIDtoAssignment.put(opId, ass);
//                    ass++;
//                } //save the assignment of each op for faster access
//
//                //check the ids of active container, simple container, activeop, simple op etc are the same
//
//                /////////////////////////////  separate the computation of slack per container to a computeSumSlackPerCont method
//                for(Long opid: opsSortedReversed()) {//topOrder.iteratorReverse()) //ranking reversed
//                    Double opSlackTime = Double.MAX_VALUE;
//
//                    //  System.out.println("\ncomputing slack for " + op.getopID());
//
//                    if (graph.getChildren(opid).isEmpty()) //if exit node
//                        opSlackTime = (double) plan.stats.runtime_MS  - plan.opIdtoStartEnd_MS.get(opid).b;
//
//                    for (Edge outLink : graph.getChildren(opid)) {//successors at the dag
//                        Long outLinkToId = outLink.to;
//                        Double childSlack = opSlack.get(outLinkToId);
//                        Double opSpareTime = (double) plan.opIdtoStartEnd_MS.get(outLinkToId).a - plan.opIdtoStartEnd_MS.get(opid).b;//assumption: output data and communication cost computed in runtime
//                        opSlackTime = Math.min(childSlack + opSpareTime, opSlackTime);
//                        // System.out.println("from parent " + op.getopID() + "child " + opChild.getopID() + " op tmp spare " + op.getopID() + " " + opSpareTime + " " + plan.activeAssignments.get(opIDtoAssignment.get(opChild.getopID())).start_SEC + " " + plan.activeAssignments.get(opIDtoAssignment.get(op.getopID())).end_SEC);
//                    }
//
//                    long opContID = opIDtoCont.get(opid);//consider successor at the container. If last op at container the returned bit (succStart) is -1
//                    long curBit = plan.opIdtoStartEnd_MS.get(opIDtoAssignment.get(opid)).b;
//                    long succStart = plan.cluster.getContainer(opContID).UsedUpTo_MS; //TODO ji check if correct
//                    if(succStart!=-1) // {   System.out.println("succ at container starts at " + succStart + " while " + plan.activeAssignments.get(opIDtoAssignment.get(op.getopID())).end_SEC);
//                        opSlackTime = Math.min(opSlackTime, succStart - plan.opIdtoStartEnd_MS.get(opid).b);//}
//
//                    opSlack.put(opid, opSlackTime);
//
//                    double slackPerCont = opSlackTime;
//                    int opsPerCont = 1;
//                    if(contOps.containsKey(plan.cluster.getContainer(opContID)))
//                    {
//                        slackPerCont+= contSlack.get(plan.cluster.getContainer(opContID));
//                        opsPerCont = contOps.get(plan.cluster.getContainer(opContID))+1;
//                    }
//
//                    contSlack.put(opContID, slackPerCont);
//                    contOps.put(opContID, opsPerCont);
//
//                }
//                ////////////////////////////
//
//                //compute avg slack per container/VM
//
//                for (int i = 0; i < plan.cluster.containersList.size(); ++i) {
//                    if(plan.cluster.contUsed.size() > 0) {
//                        if ((plan.cluster.containersList.get(i).contType == containerType.getSmallest() && plan.vmUpgrading.equals("increasing")) || (plan.cluster.containersList.get(i).contType == containerType.getLargest() && plan.vmUpgrading.equals("decreasing")))
//                            updateSkyline = 1;//there is at least one new configuration to be checked so the algorithm continues}
//                    }
//                    else
//                        continue;
//
//                    if ((plan.cluster.containersList.get(i).contType == containerType.getLargest() && plan.vmUpgrading.equals("increasing")) || (plan.cluster.containersList.get(i).contType == containerType.getSmallest() && plan.vmUpgrading.equals("decreasing"))) {//the container has the largest vm type so it will be ignored as a candidate for upgrading
//                        //System.out.printf("already largest\n");
//                        continue;
//                    } else {//the container is a candidate for upgrading
//                        //System.out.printf("not the largest\n");
//                        planContainers.add(i);
//                    }
//                }
//
//                if (planContainers.size() == 0)//if the list of candidate containers for upgrading is empty then continue to the next plan
//                    continue;
//
//
//                Comparator<Integer> contSlackComparator = new Comparator<Integer>() {
//                    @Override
//                    public int compare(Integer vm1, Integer vm2) {
//                        double s1 = contSlack.get(plan.cluster.containersList.get(vm1))/(double)contOps.get(plan.cluster.containersList.get(vm1));
//                        double s2 = contSlack.get(plan.cluster.containersList.get(vm2))/(double)contOps.get(plan.cluster.containersList.get(vm2));
//                        if (s1 > s2)//TODO: add precision error
//                            return -1;
//                        else if (s1 < s2)
//                            return 1;
//                        else
//                            return 0;
//                    }
//                };
//
//                if(plan.vmUpgrading.contains("decreasing"))
//                    Collections.sort(planContainers, contSlackComparator);
//                else Collections.sort(planContainers, Collections.reverseOrder(contSlackComparator));
//
//
//
//                Plan newPlan = null;
//
//                for (int k = 0; k < planContainers.size(); ++k) {
//
//                    if (plan.vmUpgrading == null) {
//                        System.out.println("bug");
//                        break;
//                    }
//
//                    HashMap<Integer, containerType> containersTypeTmp = new HashMap<>();
//                    int j = 0;
//                    for (Container aCont : plan.cluster.containersList) {
//                        containersTypeTmp.put(j++, aCont.contType);
//                    } //get the containers and types from the old plan
//
//                    if (plan.vmUpgrading.equals("increasing"))
//                        containersTypeTmp.put(k, containerType.getNextLarger(plan.cluster.containersList.get(k).contType)); //upgrade the chosen one//get next smaller
//                    else
//                        containersTypeTmp.put(k, containerType.getNextSmaller(plan.cluster.containersList.get(k).contType));//getnextSmaller
//
//
//                    newPlan = new ScheduleEstimator(graph, plan.activeContainers.size(), containersTypeTmp, runTimeParams);
//                    newPlan.vmUpgrading = plan.vmUpgrading;
//                    LinkedHashMap<Integer, ConcreteOperator> readyOpsTmp = new LinkedHashMap<>();
//                    BitSet readyOpsMaskTmp = new BitSet(graph.getMaxOpId());
//                    for (ConcreteOperator start : graph.getLeafOperators()) {
//                        readyOpsTmp.put(start.getopID(), start);
//                        readyOpsMaskTmp.set(start.getopID());
//                    } //find the ready to schedule ops
//
//                    BitSet assignedOperatorsTmp = new BitSet();
//
//                    while (readyOpsMaskTmp.cardinality() > 0) {//iterate on the ready to schedule ops
//
//                        int nextOpID = nextOperator(readyOpsTmp);
//                        ConcreteOperator op = readyOpsTmp.get(nextOpID);
//                        readyOpsTmp.remove(nextOpID);
//                        readyOpsMaskTmp.clear(nextOpID);
//
//                        int assignedCont = opIDtoCont.get(op.getopID());
//
//                        newPlan.addOperatorAssignment(op.getopID(), assignedCont, containersTypeTmp.get(assignedCont), graph); //add each op to the new plan
//
//                        assignedOperatorsTmp.set(op.getopID());
//
//                        for (Link outLink : graph.getOutputLinks(op.getopID())) {
//                            if (readyOpsMaskTmp.get(outLink.to.getopID()) || assignedOperatorsTmp.get(outLink.to.getopID())) {
//                                continue;
//                            }
//                            boolean allAssigned = true;
//                            for (Link inLink : graph.getInputLinks(outLink.to.getopID())) {
//                                if (assignedOperatorsTmp.get(inLink.from.getopID()) == false) {
//                                    allAssigned = false;
//                                    break;
//                                }
//                            }
//                            if (allAssigned) {
//                                readyOpsMaskTmp.set(outLink.to.getopID());
//                                readyOpsTmp.put(outLink.to.getopID(), outLink.to);
//                            }
//                        }
//
//
//                    }
//
//                    //use Double.compare
//                    if(newPlan.getScheduleStatistics().getMoneyInQuanta() >= plan.getScheduleStatistics().getMoneyInQuanta() && newPlan.getScheduleStatistics().getTime() >= plan.getScheduleStatistics().getTime())//we could use a threshold. e.g. if savings less than 0.1%
//                    {
//                        //    System.out.println("breaks for k "+k);
//                        break;
//                    }
//                    skylinePlansNew.add(newPlan);
//
//
//                }
//
//            }
//            skylinePlans.clear();
//            skylinePlans.addAll(paretoPlans);
//            paretoPlans = computeNewSkyline(skylinePlans, skylinePlansNew);
//
//
//            skylinePlans.clear();
//            skylinePlans.addAll(skylinePlansNew);
//
//
//            skylinePlansNew.clear();
//
//        }
//        //todo:compute new skyline and continue with the loop as long as pareto has changed
//
//        skylinePlans.clear();
//        skylinePlans.addAll(paretoPlans);
//
//        skylinePlansNew = null;
//
//        for (ScheduleEstimator plan : skylinePlans) {
//            if (plan == null) {
//                continue;
//            }
//
//            HashMap<containerType, Double> avgOpTime = new HashMap<>();
//            HashMap<containerType, Integer> opNumber = new HashMap<>();
//
//            for (int opAss = 0; opAss < plan.getAssignments().size(); opAss++) {
//
//                containerType cType = plan.getAssignments().get(opAss).contType;
//
//                int ops = 0;
//                double opProcessTime = plan.getAssignments().get(opAss).processTime;
//
//                if (opNumber.get(cType) == null) {
//                    ops = 1;
//                    avgOpTime.put(cType, opProcessTime);
//                } else {
//                    ops = opNumber.get(cType);
//                    double processTime = avgOpTime.get(cType) * ops + opProcessTime;
//                    ops++;
//                    avgOpTime.put(cType, processTime / ops);
//
//                }
//                opNumber.put(cType, ops);
//
//            }
//        }
//    }


    private ArrayList<Plan> createAssigments(String vmUpgrading, ArrayList<containerType> cTypes) {


        Plan firstPlan = new Plan(graph, cluster);
        firstPlan.vmUpgrading = vmUpgrading;

        ArrayList<Plan> allCandidates = new ArrayList<>();
        ArrayList<Plan> plans = new ArrayList<>();
        plans.add(firstPlan);

        int opsAssigned=0;

        HashSet<Long> opsAssignedSet = new HashSet<>();
        HashSet<Long> readyOps = new HashSet<>();
        for(Long opid:graph.operators.keySet()){
            if( graph.getParents(opid).size()==0){
                readyOps.add(opid);
            }
        }



        while ( readyOps.size() > 0) {

            opsAssigned++;


            // Get the most expensive operator from the ready ones
            long nextOpID = nextOperator(firstPlan, readyOps);
            Operator nextOp = graph.getOperator(nextOpID);

            System.out.println("scheduling "+nextOpID + " "+readyOps.toString());

            allCandidates.clear();
            for (Plan plan : plans) {

                if (plan == null) {
                    continue;
                }
                getCandidateContainers(nextOpID, plan, vmUpgrading, cTypes,allCandidates);//allCanditates is an out param
            }
            plans.clear();

            for(Plan p:allCandidates){
                p.printInfo();
            }

            plans = computeSkyline(allCandidates);

            for(Plan p:plans){
                p.printInfo();
            }


            opsAssignedSet.add(nextOpID);
            readyOps.remove(nextOpID);
//            assignedOperators.set(nextOpID);



            Boolean allAssigned;               //find new readyops
            readyOps.remove(nextOpID);
            for (Edge childEdge : graph.getChildren(nextOpID)) {
                Long childopId = childEdge.to;
                allAssigned = true;
                for (Edge ParentChildEdge : graph.getParents(childopId)) {
                    Long ParentChildOpId = ParentChildEdge.from;
                    if (!opsAssignedSet.contains(ParentChildOpId)) {
                        allAssigned = false;
                    }
                }
                if (allAssigned) {
                    readyOps.add(childopId);
                }
            }

        }

        return plans;
    }



    private void getCandidateContainers(Long opId , Plan plan, //assume that not empty containers exist
         String vmUpgrading, ArrayList<containerType> contTypes,ArrayList<Plan> planEstimations)
     {


        long runtime = graph.getOperator(opId).getRunTime_MS();

        for(Long contId: plan.cluster.containers.keySet()){
            Container curCont = plan.cluster.getContainer(contId);
            ///////////////
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId);
            planEstimations.add(newPlan);
            //////////////

        }
        if(plan.cluster.contUsed.size()<maxContainers){ //if no empty conts in plan add an empty and assign it
            for(containerType ctype: contTypes) {
                Plan newPlan = new Plan(plan);
                Long newContId = newPlan.cluster.addContainer(ctype);
                newPlan.assignOperator(opId, newContId);
                planEstimations.add(newPlan);
            }
        }


        //return planEstimations;
    }






    public Plan onlyOneContainer() {
        containerType contType= containerType.getSmallest();//maybe check for every container later
        Plan plan = new Plan(graph,cluster);


        for (Operator op : graph.getOperators()) {
            plan.assignOperator(op.getId(), plan.cluster.getContainer(0L).id);
        }
        return plan;
    }


    public long nextOperator(Plan plan, HashSet<Long> readyOps) {

        long minRankOpID = 0;
        Integer minRank = Integer.MAX_VALUE;
        for (Long opId : readyOps) {
            Integer opRank = opsSorted.indexOf(opId);
            if (opRank < minRank) {
                minRankOpID = opId;
                minRank = opRank;
            }
        }

        return minRankOpID;
    }


    public ArrayList<Plan> computeSkyline(ArrayList<Plan> plans){
        ArrayList<Plan> skyline = new ArrayList<>();


        Collections.sort(plans); // Sort by time breaking equality by sorting by money

        Plan previous = null;
        for (Plan est : plans) {
            if (previous == null) {
                skyline.add(est);
                previous = est;
                continue;
            }
            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
                // Already sorted by money
                continue;
            }
            if(Math.abs(previous.stats.money - est.stats.money)>0.000000000001) //TODO ji fix or check
                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                    skyline.add(est);
                    previous = est;
                }
        }



        return skyline;
    }
    private void computeRankings(){

        HashMap<Integer, Integer> opLevelperLevel = new HashMap <>();

        final TopologicalSorting topOrder = new TopologicalSorting(graph);
        int numLevels=0;

        for (Long opId : topOrder.iterator()) {
            int level=0;
            for (Edge parentEdge: graph.getParents(opId)) {

                Integer plevel=opLevel.get(parentEdge.from);
                level=Math.max(plevel+1, level);
            }
            opLevel.put(opId, level);
            numLevels=Math.max(level+1, numLevels);
            if(opLevelperLevel.containsKey(level))
                opLevelperLevel.put(level, opLevelperLevel.get(level)+1);
            else
                opLevelperLevel.put(level, 1);

             System.out.println("op "+ opId + " level " +level);

        }


        Double crPathLength=0.0;
        for (Long opId : topOrder.iteratorReverse()) {
            double maxRankChild=0.0;
            for (Edge childEdge: graph.getChildren(opId)) {
                double comCostChild = 0.0;
                for(Edge parentofChildEdge: graph.getParents(childEdge.to)) {
                    if(parentofChildEdge.from==opId) {
                        comCostChild = Math.ceil(parentofChildEdge.data.size_B / RuntimeConstants.network_speed__B_SEC);
                    }
                }
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild+b_rank.get(childEdge.to));
            }

            double wcur=0.0;
            for(containerType contType: containerType.values())
                wcur+=graph.getOperator(opId).getRunTime_MS()/contType.container_CPU; //TODO ji check if S or MS
            int types= containerType.values().length;
            double w=wcur/(double)types;//average execution cost for operator op
            b_rank.put(opId, (w+maxRankChild));

        }

        for (Long opId : topOrder.iterator()) {
            double maxRankParent=0.0;
            for (Edge inLink: graph.getParents(opId)) {
//                ConcreteOperator opParent=graph.getOperator(inLink.from.getopID());
                double comCostParent = Math.ceil(inLink.data.size_B / RuntimeConstants.network_speed__B_SEC);
                maxRankParent = Math.max(maxRankParent, comCostParent+t_rank.get(inLink.from));
            }

            double wcur=0.0;
            for(containerType contType: containerType.values())
                wcur+=graph.getOperator(opId).getRunTime_MS()/contType.container_CPU;
            int types= containerType.values().length;
            double w=wcur/(double)types;//average execution cost for operator op
            t_rank.put(opId, (w+maxRankParent));
            Double opRank=b_rank.get(opId) + t_rank.get(opId) -w;
            sum_rank.put(opId, opRank);
            crPathLength =Math.max(crPathLength, opRank);
        }

        for (Long op : topOrder.iterator()) {
            opsSumRankSorted.add(op);
            opsBySlack.add(op);
            Double opRank=sum_rank.get(op);
            double opSlacktime = crPathLength - opRank;
            slacktime.put(op, opSlacktime);
        }



        final HashMap<Long, Double> rankU = new HashMap<>();


        for (Long opId : topOrder.iteratorReverse()) {
            // System.out.println("reversed ID is " + op.getopID());

            double maxRankChild=0.0;
            for (Edge outLink: graph.getChildren(opId)) {
                double comCostChild = Math.ceil(outLink.data.size_B / RuntimeConstants.network_speed__B_SEC);
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild+rankU.get(outLink.to));
            }

            double wcur=0.0;
            for(containerType contType: containerType.values()) {
                long mst = graph.getOperator(opId).getRunTime_MS();
                double cput = contType.container_CPU;
                wcur += graph.getOperator(opId).getRunTime_MS() / contType.container_CPU;
            }
            int types= containerType.values().length;
            double w=wcur/(double)types;//average execution cost for operator op
            rankU.put(opId, (w+maxRankChild));

        }

        for (Long op : topOrder.iterator())
            opsSorted.add(op);

        Comparator<Long> rankComparator = new Comparator<Long>() {
            @Override
            public int compare(Long op1, Long op2) {
                double r1 = rankU.get(op1);
                double r2 = rankU.get(op2);
                if (r1 > r2)
                    return -1;
                else if (r1 < r2)
                    return 1;
                else
                    return 0;
            }
        };
        Collections.sort(opsSorted, rankComparator);



        int cur_id=0;
        for (Long cur_op : opsBySlack) {
            //    System.out.println("inf op " + cur_op.getopID() + " slack " + slacktime.get(cur_op));
            //    System.out.println("inf op " + cur_op.getopID() + " rank " +  sum_rank.get(cur_op) + " level " + opLevel.get(cur_op));
            if (cur_id<opsInfluential) {
                // System.out.println("added op " + cur_op.getopID() + " rank " +  sum_rank.get(cur_op) + " b_rank " + b_rank.get(cur_op) + " t_rank " + t_rank.get(cur_op));
                cur_id++;
                opIDsInfluential.put(cur_op, 1);
            }
        }///remove






    }
    public Iterable<Long> opsSortedReversed() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return opsSorted.descendingIterator();
            }
        };
    }




//    private class WhatIfEstimation implements Comparable<WhatIfEstimation>{
//        long opId;
//        long contId;
//        Statistics before;
//        Statistics after;
//
//
//        public WhatIfEstimation(Long oid, Long cid, Statistics b, Statistics af){
//            opId = oid;
//            contId = cid;
//            before = b;
//            after = af;
//        }
//
//        public double getScore(Double alphaPar, Double mCost, Double mTime, Double k){
//            return (1.0-alphaPar)*after.quanta+alphaPar*(after.runtime_MS);
//        }
//
//        @Override public int compareTo(WhatIfEstimation other) {
//            if (after.runtime_MS == other.after.runtime_MS) {
//                if (Math.abs(after.money - other.after.money)<=0.000000001) {// if (moneyQuanta == other.moneyQuanta) {
//
//                        // Keep the one with the least number of containers
//                        return Long.compare(after.containersUsed, other.after.containersUsed);
//                } else {
//                    // Order by money
//                    return Double.compare(after.money, other.after.money);
//                }
//            } else {
//                // Order by time
//                return Long.compare(after.runtime_MS, other.after.runtime_MS);
//            }
//        }
//
//    }


}















