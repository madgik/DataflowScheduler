 package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.MultiplePlotInfo;
import utils.Pair;

import java.util.*;

import static utils.Plot.plotMultiple;

/**
 * Created by johnchronis on 2/19/17.
 */
public class paretoNoHomogen implements Scheduler {

    public SolutionSpace space;
    public Cluster cluster;
    public DAG graph;

    ///rankingks
//    private HashMap<Long, Double> b_rank = new HashMap<>(); //opidTobRank
//    private HashMap<Long, Double> t_rank = new HashMap<>();
//    private HashMap<Long, Double> sum_rank = new HashMap<>();
//    private HashMap<Long, Double> slacktime = new HashMap<>();
//    private LinkedList<Long> opsSumRankSorted = new LinkedList<>();
//    private Integer opsInfluential=-5;//10;//check from derivative ranking
//    private LinkedList<Long> opsBySlack = new LinkedList<>();
////    private HashMap<Long, Double> opSlack = new HashMap<>();
//    private HashMap<Long, Integer> opIDsInfluential= new HashMap<>();
    public static LinkedList<Long> opsSorted = new LinkedList<>();


    ///
    public int maxContainers = 100;


    private HashMap<Long, Integer> opLevel = new HashMap<>(); //opid->level


    public paretoNoHomogen(DAG graph,Cluster cl){
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
    }



    @Override
    public SolutionSpace schedule() {
        long startCPU_MS = System.currentTimeMillis();
        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
        SolutionSpace skylinePlans = new SolutionSpace();

        SolutionSpace skylinePlans_INC = new SolutionSpace();
        SolutionSpace skylinePlans_DEC = new SolutionSpace();
        SolutionSpace skylinePlans_INCDEC = new SolutionSpace();

        SolutionSpace paretoPlans = new SolutionSpace();

        computeRankings();

        skylinePlans.clear();

        for(containerType cType: containerType.values()) {


            if (maxContainers == 1) {
                skylinePlans.add(onlyOneContainer());
            } else {

                if (cType.equals(containerType.getLargest())) {
                    skylinePlans_DEC.addAll(this.createAssignments("decreasing", cType));
//                    plotPlans("dec",skylinePlans);
//                    System.out.println("s1 "+skylinePlans.size());

                } else if (cType.equals(containerType.getSmallest())) {
                    skylinePlans_INC.addAll(this.createAssignments("increasing", cType));
//                    plotPlans("inc",skylinePlans);
//                    System.out.println("s2 "+skylinePlans.size());
                } else{
                    skylinePlans_INCDEC.addAll(this.createAssignments("increasing/decreasing", cType));
//                    plotPlans("inc,dec",skylinePlans);
//                    System.out.println("s3 "+skylinePlans.size());
                }
            }
        }

        System.out.println("//////////DEC///////");
//        skylinePlans_DEC.print();
//        skylinePlans_DEC.plot("DEC");
        mpinfo.add("DEC",skylinePlans_DEC.results);
        System.out.println("//////////INC///////");
//        skylinePlans_INC.print();
//        skylinePlans_INC.plot("INC");
        mpinfo.add("INC",skylinePlans_INC.results);
        System.out.println("//////////INCDEC///////");
//        skylinePlans_INCDEC.print();
//        skylinePlans_INCDEC.plot("INCDEC");
        mpinfo.add("INCDEC",skylinePlans_INCDEC.results);



        skylinePlans.addAll(skylinePlans_DEC.results);
        skylinePlans.addAll(skylinePlans_INC.results);
        skylinePlans.addAll(skylinePlans_INCDEC.results);


        paretoPlans.addAll(computeSkyline(skylinePlans));


        System.out.println("//////////PARETO///////");
//        paretoPlans.print();
//        paretoPlans.plot("pareto");
        mpinfo.add("pareto",paretoPlans.results);



        skylinePlans.clear();

        for(Plan pp: paretoPlans.results) {
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
//

        paretoPlans.clear();



        paretoPlans.addAll(homoToHetero(skylinePlans)); //returns only hetero


        paretoPlans.addAll(skylinePlans);


        space.addAll(computeSkyline(paretoPlans));

        long endCPU_MS = System.currentTimeMillis();
        space.setOptimizationTime(endCPU_MS - startCPU_MS);

        System.out.println("//////////RESULT///////");

        space.print();
        space.plot("space");
        mpinfo.add("space",space.results);

        plotMultiple(mpinfo);
        return space;

    }

    //input plan
    //output contSlack, contOps, opSlack
    private void computeSlack(Plan plan,HashMap<Long, Double> contSlack, HashMap<Long, Integer> contOps,HashMap<Long, Double> opSlack){
        contSlack.clear();
        contOps.clear();
        opSlack.clear();


        for(Long opId: opsSortedReversed()) {//topOrder.iteratorReverse()) //ranking reversed
            Double opSlackTime = Double.MAX_VALUE;
            if (graph.getChildren(opId).isEmpty()) //if exit node
                opSlackTime = (double) plan.stats.runtime_MS - plan.cluster.getContainer(plan.assignments.get(opId)).UsedUpTo_MS;
            for (Edge outEdge : graph.getChildren(opId)) {//successors at the dag
                Long opChildId = outEdge.to;
                if(!opSlack.containsKey(opChildId)){
                    System.out.println("Problem");
                }
                Double childSlack = opSlack.get(opChildId);

                Double opSpareTime = (double) plan.opIdtoStartEnd_MS.get(opChildId).a - plan.opIdtoStartEnd_MS.get(opId).b;//assumption: output data and communication cost computed in runtime
                opSlackTime = Math.min(childSlack + opSpareTime, opSlackTime);
            }
            long opContID = plan.assignments.get(opId);//consider successor at the container. If last op at container the returned bit (succStart) is -1
            long contEndTime_MS = plan.cluster.getContainer(opContID).UsedUpTo_MS;
            long opEndTime_MS = plan.opIdtoStartEnd_MS.get(opId).b;
            if(contEndTime_MS>opEndTime_MS) {
                long succStart = Long.MAX_VALUE;
                boolean succExists = false;
                for (Long opopId : plan.opIdtoStartEnd_MS.keySet()) {
                    Pair<Long, Long> pair = plan.opIdtoStartEnd_MS.get(opopId);
                    Long contcontId = plan.assignments.get(opopId);
                    long opopStartTime = plan.opIdtoStartEnd_MS.get(opopId).a;

                    if (contcontId == opContID && opopStartTime > opEndTime_MS) {
                        succStart = Math.min(succStart, opopStartTime);
                        succExists = true;
                    }

                }
                if (succExists){//(contEndTime_MS > opEndTime_MS) {//   System.out.println("succ at container starts at " + succStart + " while " + plan.activeAssignments.get(opIDtoAssignment.get(op.getopID())).end_SEC);
                    opSlackTime = Math.min(opSlackTime, succStart - opEndTime_MS);
                }
            }
            opSlack.put(opId, opSlackTime);

            double slackPerCont = opSlackTime;
            int opsPerCont = 1;
            if(contOps.containsKey(opContID))
            {
                slackPerCont+= contSlack.get(opContID);
                opsPerCont = contOps.get(opContID)+1;
            }

            contSlack.put(opContID, slackPerCont);
            contOps.put(opContID, opsPerCont);

        }

        //contSlack = avg opSlack //TODO: check this is needed here
        for(Long contId: contSlack.keySet()) {
            double contAvgSlack = contSlack.get(contId) /(double) contOps.get(contId);
            contSlack.put(contId, contAvgSlack);
        }
    }

    private SolutionSpace homoToHetero(SolutionSpace plans) {

        SolutionSpace plansInner = new SolutionSpace();//deepcopy of input
        for(Plan p:plans.results){
            plansInner.add(new Plan(p));
        }

        SolutionSpace result = new SolutionSpace();//keeps all the solutions at the current pareto

        for(Plan p:plans){
            result.add(new Plan(p));
        }
        //look at each plan and upgrade one by one the LARGE containers

        HashMap<Long, Double> opSlack = new HashMap<>();
        final HashMap<Long, Double> contSlack = new HashMap<>();
        final HashMap<Long, Integer> contOps = new HashMap<>();

        SolutionSpace skylinePlansNew = new SolutionSpace();
        //the set of plans from the newly modified plans (plans with upgraded/degraded vm types) that belong to the current pareto

        int updateSkyline = 1;

        while (updateSkyline == 1) {

            updateSkyline = 0;

            for (final Plan plan : plansInner) {

                LinkedList<Long> planContainersTobeModified = new LinkedList<>();

                //compute avg slack per container/VM
                computeSlack(plan,contSlack,contOps,opSlack);


                for (Long i: plan.cluster.containers.keySet()) {
                    Container cont  = plan.cluster.getContainer(i);

                    if (plan.vmUpgrading == null) {
                        System.out.println("bug line 254");
                        break;
                    }

                    if ((cont.contType == containerType.getLargest() && plan.vmUpgrading.equals("increasing"))
                        || (cont.contType == containerType.getSmallest() && plan.vmUpgrading.equals("decreasing"))) {
                        //the container has the largest vm type so it will be ignored as a candidate for upgrading
                        continue;
                    } else {//the container is a candidate for upgrading
                        planContainersTobeModified.add(i);
                        updateSkyline = 1;
                    }
                }

                //if the list of candidate containers for upgrading is empty then continue to the next plan
                if (planContainersTobeModified.size() == 0)
                    continue;


                Comparator<Long> contSlackComparator = new Comparator<Long>() {
                    @Override
                    public int compare(Long vm1, Long vm2) {
                        double s1 = contSlack.get(vm1)/(double)contOps.get(vm1);
                        double s2 = contSlack.get(vm2)/(double)contOps.get(vm2);
                        if (s1 > s2)//TODO: add precision error
                            return -1;
                        else if (s1 < s2)
                            return 1;
                        else
                            return 0;
                    }
                };

                if(plan.vmUpgrading.contains("decreasing"))
                    Collections.sort(planContainersTobeModified, contSlackComparator);
                else Collections.sort(planContainersTobeModified, Collections.reverseOrder(contSlackComparator));

                Plan newPlan = null;

                    for (Long k: planContainersTobeModified) {

                   if (plan.vmUpgrading == null) {
                        System.out.println("bug line 297");
                        break;
                    }

                   newPlan = new Plan(graph,new Cluster());
                   newPlan.vmUpgrading = plan.vmUpgrading;
                   for(Container contcont : plan.cluster.containersList){
                        newPlan.cluster.addContainer(contcont.contType);
                   }


                    Container cont = newPlan.cluster.containers.get(k);

                    if (plan.vmUpgrading.equals("increasing"))
                        newPlan.cluster.update(cont.id,containerType.getNextLarger(cont.contType));
                    else
                        newPlan.cluster.update(cont.id,containerType.getNextSmaller(cont.contType));



                    int opsAssigned=0;
                    HashSet<Long> opsAssignedSet = new HashSet<>();
                    HashSet<Long> readyOps = new HashSet<>();
                    for(Long opid:graph.operators.keySet()){
                        if( graph.getParents(opid).size()==0){
                            readyOps.add(opid);
                        }
                    }


                    while ( readyOps.size() > 0) {//iterate on the ready to schedule ops

                        opsAssigned++;
                        long nextOpID = nextOperator(readyOps);
                        Operator nextOp = graph.getOperator(nextOpID);
//                        System.out.println("\nHomoToHetero scheduling "+nextOpID + " "+readyOps.toString());


                        newPlan.assignOperator(nextOpID,plan.assignments.get(nextOpID));

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

                    //use Double.compare
                    if(newPlan.stats.money >= plan.stats.money && newPlan.stats.runtime_MS >= plan.stats.runtime_MS)//we could use a threshold. e.g. if savings less than 0.1%
                    {
                        //    System.out.println("breaks for k "+k);
                        break;
                    }
                    skylinePlansNew.add(newPlan);
//
//                     System.out.println("OLDPLAN ");
//                    plan.printInfo();
//                    System.out.println("NEWPLAN");
//                     newPlan.printInfo();
//

                    //   }


                }

            }

            plansInner.clear();
            plansInner.addAll(result.results);
            result.addAll(computeNewSkyline(plansInner.results, skylinePlansNew.results));

            plansInner.clear();

            plansInner.addAll(skylinePlansNew.results);


            skylinePlansNew.clear();
        }


        for (Plan plan : result) { //TODO ilia check if this is needed?
            if (plan == null) {
                continue;
            }

            HashMap<containerType, Double> avgOpTime = new HashMap<>();
            HashMap<containerType, Integer> opNumber = new HashMap<>();

            for (Long opId : plan.assignments.keySet()) {


                containerType cType = plan.cluster.getContainer(plan.assignments.get(opId)).contType;

                int ops = 0;
                double opProcessTime = plan.opIdtoStartEnd_MS.get(opId).b - plan.opIdtoStartEnd_MS.get(opId).a;

                if (opNumber.get(cType) == null) {
                    ops = 1;
                    avgOpTime.put(cType, opProcessTime);
                } else {
                    ops = opNumber.get(cType);
                    double processTime = avgOpTime.get(cType) * ops + opProcessTime;
                    ops++;
                    avgOpTime.put(cType, processTime / ops);

                }
                opNumber.put(cType, ops);
            }

        }


        return result;
    }


    private SolutionSpace createAssignments(String vmUpgrading, containerType cType) {


        Plan firstPlan = new Plan(graph, cluster);
        firstPlan.vmUpgrading = vmUpgrading;

        SolutionSpace allCandidates = new SolutionSpace();
        SolutionSpace plans = new SolutionSpace();
        plans.add(firstPlan);

        int opsAssigned=0;

        HashSet<Long> opsAssignedSet = new HashSet<>();
        HashSet<Long> readyOps = new HashSet<>();
        for(Long opid:graph.operators.keySet()){
            if( graph.getParents(opid).size()==0){
                readyOps.add(opid);
            }
        }

        int prevPrune=-1;

        while ( readyOps.size() > 0) {

            opsAssigned++;


            // Get the most expensive operator from the ready ones
            long nextOpID = nextOperator(readyOps);
            Operator nextOp = graph.getOperator(nextOpID);

         //   System.out.println("\nscheduling "+nextOpID + " "+readyOps.toString());

            allCandidates.clear();
            for (Plan plan : plans) {

                if (plan == null) {
                    continue;
                }

                System.out.println("\nnewly created plans");
                getCandidateContainers(nextOpID, plan, cType,allCandidates);//allCanditates is an out param
            }
            plans.clear();
//
            for(Plan p:allCandidates){
                p.printInfo();
            }

//            if(prevPrune+3 == opsAssigned){
//                plans = computeSkyline(allCandidates);
//                prevPrune=opsAssigned+3;
//            }else{
//                plans = new ArrayList<>(allCandidates);
//            }

            plans = computeSkyline(allCandidates);

            System.out.println("skyline");
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
         containerType contType,SolutionSpace planEstimations)
     {


        for(Long contId: plan.cluster.containers.keySet()){
            ///////////////
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId);
            planEstimations.add(newPlan);

            newPlan.printAssignments();
           // System.out.println(newPlan.assignments.);
            //////////////

        }
        if(plan.cluster.contUsed.size()<maxContainers){ //if no empty conts in plan add an empty and assign it
//            for(containerType ctype: contType) {//uncomment to add every ctype
                Plan newPlan = new Plan(plan);
                Long newContId = newPlan.cluster.addContainer(contType);
                newPlan.assignOperator(opId, newContId);
                planEstimations.add(newPlan);
                newPlan.printAssignments();
//            }
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

    public long nextOperator(HashSet<Long> readyOps) {

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

    public SolutionSpace computeSkyline(SolutionSpace plans){
        SolutionSpace skyline = new SolutionSpace();


        plans.sort(); // Sort by time breaking equality by sorting by money

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
            if(Math.abs(previous.stats.money - est.stats.money)>RuntimeConstants.precisionError) //TODO ji fix or check
                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
                    skyline.add(est);
                    previous = est;
                }
        }



        return skyline;
    }

    private ArrayList<Plan> computeHomoSkyline(ArrayList<Plan> skylinePlans) {

        ArrayList<Plan> candidates = new ArrayList<>();

        candidates.addAll(skylinePlans);

        // Sort by time breaking exec time equality by sorting by money and then containers used
        Collections.sort(candidates);

        // Keep only the skyline
        ArrayList<Plan> skyline = new ArrayList<>();
        Plan previous = null;


        for (Plan est : candidates) {
            // System.out.println("candidate "+est.estimator.getAssignments()+ est.estimator.activeContainersType);
            if (previous == null) {
                //    System.out.println("kept ");
                skyline.add(est);
                previous = est;
                continue;
            }
            if (previous.stats.runtime_MS == est.stats.runtime_MS){
                // Already sorted by money
                continue;
            }
            if (previous.stats.money > est.stats.money && Math.abs(previous.stats.money - est.stats.money)>0.000000001) {
                //   System.out.println("kept ");
                skyline.add(est);
                previous = est;
            }
        }
        return skyline;
    }

    private ArrayList<Plan> computeNewSkyline(ArrayList<Plan> skylinePlans, ArrayList<Plan> skylinePlansNew) {

        ArrayList<Plan> candidates = new ArrayList<>();

        candidates.addAll(skylinePlans);
        candidates.addAll(skylinePlansNew);

        // Sort by time breaking exec time equality by sorting by money and then containers used
        Collections.sort(candidates);

        // Keep only the skyline
        ArrayList<Plan> skyline = new ArrayList<>();
        Plan previous = null;


        for (Plan est : candidates) {
            // System.out.println("candidate "+est.estimator.getAssignments()+ est.estimator.activeContainersType);
            if (previous == null) {
                //    System.out.println("kept ");
                skyline.add(est);
                previous = est;
                continue;
            }
            if (previous.stats.runtime_MS == est.stats.runtime_MS){
                skylinePlansNew.remove(est);
                // Already sorted by money
                continue;
            }
            if (previous.stats.money > est.stats.money && Math.abs(previous.stats.money - est.stats.money)>0.000000001){                //   System.out.println("kept ");
                skyline.add(est);
                previous = est;
            }
            else
                skylinePlansNew.remove(est);
        }
        return skyline;
    }

    private void computeRankings(){

//        HashMap<Integer, Integer> opLevelperLevel = new HashMap <>();
//
        final TopologicalSorting topOrder = new TopologicalSorting(graph);
//        int numLevels=0;
//
//        for (Long opId : topOrder.iterator()) {
//            int level=0;
//            for (Edge parentEdge: graph.getParents(opId)) {
//
//                Integer plevel=opLevel.get(parentEdge.from);
//                level=Math.max(plevel+1, level);
//            }
//            opLevel.put(opId, level);
//            numLevels=Math.max(level+1, numLevels);
//            if(opLevelperLevel.containsKey(level))
//                opLevelperLevel.put(level, opLevelperLevel.get(level)+1);
//            else
//                opLevelperLevel.put(level, 1);
//
//             System.out.println("op "+ opId + " level " +level);
//
//        }
//
//
//        Double crPathLength=0.0;
//        for (Long opId : topOrder.iteratorReverse()) {
//            double maxRankChild=0.0;
//            for (Edge childEdge: graph.getChildren(opId)) {
//                double comCostChild = 0.0;
//                for(Edge parentofChildEdge: graph.getParents(childEdge.to)) {
//                    if(parentofChildEdge.from==opId) {
//                        comCostChild = Math.ceil(parentofChildEdge.data.size_B / RuntimeConstants.network_speed_B_MS);
//                    }
//                }
//                //assumptions for output data and communication cost
//                maxRankChild = Math.max(maxRankChild, comCostChild+b_rank.get(childEdge.to));
//            }
//
//            double wcur=0.0;
//            for(containerType contType: containerType.values())
//                wcur+=graph.getOperator(opId).getRunTime_MS()/contType.container_CPU; //TODO ji check if S or MS
//            int types= containerType.values().length;
//            double w=wcur/(double)types;//average execution cost for operator op
//            b_rank.put(opId, (w+maxRankChild));
//
//        }
//
//        for (Long opId : topOrder.iterator()) {
//            double maxRankParent=0.0;
//            for (Edge inLink: graph.getParents(opId)) {
////                ConcreteOperator opParent=graph.getOperator(inLink.from.getopID());
//                double comCostParent = Math.ceil(inLink.data.size_B / RuntimeConstants.network_speed_B_MS);
//                maxRankParent = Math.max(maxRankParent, comCostParent+t_rank.get(inLink.from));
//            }
//
//            double wcur=0.0;
//            for(containerType contType: containerType.values())
//                wcur+=graph.getOperator(opId).getRunTime_MS()/contType.container_CPU;
//            int types= containerType.values().length;
//            double w=wcur/(double)types;//average execution cost for operator op
//            t_rank.put(opId, (w+maxRankParent));
//            Double opRank=b_rank.get(opId) + t_rank.get(opId) -w;
//            sum_rank.put(opId, opRank);
//            crPathLength =Math.max(crPathLength, opRank);
//        }
//
//        for (Long op : topOrder.iterator()) {
//            opsSumRankSorted.add(op);
//            opsBySlack.add(op);
//            Double opRank=sum_rank.get(op);
//            double opSlacktime = crPathLength - opRank;
//            slacktime.put(op, opSlacktime);
//        }



        final HashMap<Long, Double> rankU = new HashMap<>();


        for (Long opId : topOrder.iteratorReverse()) {

            double maxRankChild=0.0;
            for (Edge outLink: graph.getChildren(opId)) {
                double comCostChild = Math.ceil(outLink.data.size_B / RuntimeConstants.network_speed_B_MS);
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



//        int cur_id=0;
//        for (Long cur_op : opsBySlack) {
//            //    System.out.println("inf op " + cur_op.getopID() + " slack " + slacktime.get(cur_op));
//            //    System.out.println("inf op " + cur_op.getopID() + " rank " +  sum_rank.get(cur_op) + " level " + opLevel.get(cur_op));
//            if (cur_id<opsInfluential) {
//                // System.out.println("added op " + cur_op.getopID() + " rank " +  sum_rank.get(cur_op) + " b_rank " + b_rank.get(cur_op) + " t_rank " + t_rank.get(cur_op));
//                cur_id++;
//                opIDsInfluential.put(cur_op, 1);
//            }
//        }///remove






    }


    public HashMap<Long, Long> computeLST(Plan plan) {

        HashMap <Long, Long> opLST = new HashMap<>();

        for(Long opId: opsSortedReversed()) {
            Long lst = Long.MAX_VALUE;
            Long opRuntime = plan.opIdToRuntimeAssigned_MS.get(opId);
            if (graph.getChildren(opId).isEmpty()) { //if exit node
                lst = plan.stats.runtime_MS - opRuntime;
            }else {
                for (Edge outEdge : graph.getChildren(opId)) {
                    Long succId = outEdge.to;

                    Long succStartTime =  plan.opIdtoStartEnd_MS.get(succId).a;

                    lst = Math.min(succStartTime - opRuntime, lst);
                }
            }
            opLST.put(opId, lst);
        }


        return opLST;
    }


    public HashMap<Long,Long> computeEST(Plan plan){
        HashMap <Long, Long> opEST = new HashMap<>();

        for(Long opId: opsSorted){
            Long est = Long.MIN_VALUE;
            if(graph.getParents(opId).isEmpty()){
                est = 0L;
            }else{
                for(Edge inEdge:graph.getParents(opId)){
                    Long preId = inEdge.from;

                    Long preEndTime = plan.opIdtoStartEnd_MS.get(preId).b;

                    est = Math.max(preEndTime,est);
                }
            }
            opEST.put(opId,est);
        }
        return opEST;
    }




    private void computeHomoRankings(containerType cType)
    {
        final TopologicalSorting topOrder = new TopologicalSorting(graph);

        final HashMap<Long, Double> rankU = new HashMap<>();

        for (Long opId : topOrder.iteratorReverse()) {

            double maxRankChild=0.0;
            for (Edge outLink: graph.getChildren(opId)) {
                double comCostChild = Math.ceil(outLink.data.size_B / RuntimeConstants.network_speed_B_MS);
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild+rankU.get(outLink.to));
            }

            double w=graph.getOperator(opId).getRunTime_MS()/cType.container_CPU;
            rankU.put(opId, (w+maxRankChild));

        }
        for (Long opId : topOrder.iterator())
            opsSorted.add(opId);

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
    }
    public Iterable<Long> opsSortedReversed() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return opsSorted.descendingIterator();
            }
        };
    }

}




//
//
//
//
//
//
//
//
//
//
//
