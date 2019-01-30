package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Check;
import utils.MultiplePlotInfo;

import java.util.*;

/**
 * Created by johnchronis on 2/19/17.
 */
public class hhdsEnsemble implements Scheduler {

    public SolutionSpace space;
    public Cluster cluster;


    public DAG graph;

    public ArrayList<DAG> ensemble;


    //public String newDir = "";
    public String rankingMethod = "";//"dagMerge";//commonEntry:default, perDag, dagMerge

    public LinkedList<Long> opsSorted ;

    public int pruneSkylineSize = 10;//20;
    public int homoPlanstoKeep = 10;//80;


    public int maxContainers = 100000000;

    public boolean backfilling = false;
    public boolean backfillingUpgrade = false;

    public boolean heteroEnabled = true;
    public boolean pruneEnabled = false;
    public String PruneMethod = "";
    public boolean homotohetero = false;
    public boolean heteroStartEnabled = false;

    public boolean multi=false;

    private HashMap<Long, Integer> opLevel;

    int constraint_mode;
    double money_constraint;
    long time_constraint;

    public hhdsEnsemble(DAG graph, Cluster cl, boolean prune, String PruneMethod, String rankingMethod, Boolean multi,
                        int pruning_k, int constraint_mode, double money_constraint, long time_constraint) {
        this.rankingMethod = rankingMethod;
        this.pruneEnabled = prune;
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
        this.opsSorted = new LinkedList<>();
        opLevel = new HashMap<>();
        this.PruneMethod = PruneMethod;
        this.multi = multi;
        this.pruneSkylineSize = pruning_k;
        this.constraint_mode = constraint_mode;
        this.money_constraint = money_constraint;
        this.time_constraint = time_constraint;
        // this.newDir = dir;
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

        if (heteroEnabled){

            for (containerType cType : containerType.values()) {

                if (maxContainers == 1) {
                    skylinePlans.add(onlyOneContainer());
                } else {
                    ////INC DEC/////
 //                    System.out.println("calc "+cType.name);
                    if (cType.equals(containerType.getLargest())) {
                        ArrayList<containerType> cTypes = new ArrayList<>();
                        cTypes.add(cType);

                        skylinePlans_DEC.addAll(this.createAssignments("decreasing", cTypes, true));
                        //                    plotPlans("dec",skylinePlans);
                        //                    System.out.println("s1 "+skylinePlans.size());

                    } else if (cType.equals(containerType.getSmallest())) {
                        ArrayList<containerType> cTypes = new ArrayList<>();
                        cTypes.add(cType);

                        skylinePlans_INC.addAll(this.createAssignments("increasing", cTypes, true));
                        //                    plotPlans("inc",skylinePlans);
                        //                    System.out.println("s2 "+skylinePlans.size());
                    } else {
                        ArrayList<containerType> cTypes = new ArrayList<>();
                        cTypes.add(cType);


                        skylinePlans_INCDEC
                                .addAll(this.createAssignments("increasing/decreasing", cTypes, true));
                        //                    plotPlans("inc,dec",skylinePlans);
                        //                    System.out.println("s3 "+skylinePlans.size());
                    }

                }

            }

            skylinePlans.addAll(skylinePlans_DEC.results);
            skylinePlans.addAll(skylinePlans_INC.results);
            skylinePlans.addAll(skylinePlans_INCDEC.results);


        } else{

            // for (containerType cType : containerType.values()) {

            containerType cType = containerType.getSmallest();
            if (maxContainers == 1) {
                skylinePlans.add(onlyOneContainer());
            } else {

                ArrayList<containerType> cTypes = new ArrayList<>();
                cTypes.add(cType);

                skylinePlans_INCDEC.addAll(this.createAssignments("increasing", cTypes, true));

            }

            // }
            skylinePlans.addAll(skylinePlans_INCDEC.results);


        }

        for(int pc=0; pc<skylinePlans.results.size(); pc++)
            System.out.println("plan " + skylinePlans.results.get(pc).stats.money+" "+skylinePlans.results.get(pc).stats.runtime_MS );

        paretoPlans.addAll(skylinePlans.results);

        // compute the skyline for all the homogeneous plans
        System.out.println("After homo: " + paretoPlans.size());
        paretoPlans.computeSkyline(pruneEnabled, homoPlanstoKeep, false, PruneMethod, multi,
                false, 0, money_constraint, time_constraint);
        System.out.println("After homo+computeskyline: " + paretoPlans.size());
        mpinfo.add("pareto", paretoPlans.results);

        long homoEnd = System.currentTimeMillis();
        System.out.println("Pare homoEnd: " + (homoEnd - startCPU_MS));

        skylinePlans.clear();

        for (Plan pp : paretoPlans.results) {
            if (pp.vmUpgrading.equals("increasing/decreasing")) {

                pp.vmUpgrading = "increasing";
                skylinePlans.add(pp);

                Plan newpp = new Plan(pp);
                newpp.vmUpgrading = "decreasing";
                skylinePlans.add(newpp);
            } else {
                skylinePlans.add(pp);
            }
        }

        paretoPlans.clear();
        space.smallPrint();

        if (heteroEnabled) {
            paretoPlans.addAll(homoToHetero(skylinePlans)); //returns only hetero
            System.out.println("After hetero: " + paretoPlans.size());
        }

//        System.out.println("Pare homoToHetero End: " + (System.currentTimeMillis() - homoEnd));

        paretoPlans.addAll(skylinePlans);

        space.addAll(paretoPlans);

        long endCPU_MS = System.currentTimeMillis();
        space.setOptimizationTime(endCPU_MS - startCPU_MS);

        mpinfo.add("final space", space.results);

        System.out.println("before final computeSkyline: " + space.size());
        System.out.println("minmoney: "+space.getMinCostPlan().stats.money + " "+ space.getMinCostPlan().stats.runtime_MS);
        System.out.println("mintime: "+space.getMinRuntimePlan().stats.runtime_MS + " " + space.getMinRuntimePlan().stats.money);

        System.out.println("money_constraint: "+money_constraint);
        System.out.println("time_constraint: "+time_constraint );

        space.smallPrint();

        if (constraint_mode == 1 || constraint_mode == 2  || constraint_mode == 3) {
            // by fixing constraint mode to 1 only one plan is returned. This happens only when constraints are applied.
            space.computeSkyline(pruneEnabled, pruneSkylineSize, false, PruneMethod, multi, false,
                    1, money_constraint, time_constraint);
        } else {
            space.computeSkyline(pruneEnabled, pruneSkylineSize, false, PruneMethod, multi, false,
                    0, money_constraint, time_constraint);
        }
        System.out.println("After final computeSkyline: " + space.size());
        space.smallPrint();
        return space;

    }
//    @Override
//    public SolutionSpace schedule() {
//
//        //  for(DAG graph:ensemble)
//        // {
//        long startCPU_MS = System.currentTimeMillis();
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//        SolutionSpace skylinePlans = new SolutionSpace();
//
//        SolutionSpace skylinePlans_INC = new SolutionSpace();
//        SolutionSpace skylinePlans_DEC = new SolutionSpace();
//        SolutionSpace skylinePlans_INCDEC = new SolutionSpace();
//
//        SolutionSpace paretoPlans = new SolutionSpace();
//
//        computeRankings();
//
//        skylinePlans.clear();
//
////        if (heteroStartEnabled) {
////
////            ArrayList<containerType> cTypes = new ArrayList<>();
////            cTypes.clear();
////            cTypes.add(containerType.A);
////            cTypes.add(containerType.H);
////            skylinePlans.addAll(this.createAssignments("increasing/decreasing", cTypes));
////            skylinePlans.addAll(this.createAssignments("increasing/decreasing", cTypes));
////
////            cTypes.clear();
////            cTypes.add(containerType.C);
////            cTypes.add(containerType.G);
////            skylinePlans.addAll(this.createAssignments("increasing/decreasing", cTypes));
////
////        }
//
//        if (!heteroStartEnabled){
//
//            for (containerType cType : containerType.values()) {
//
//
//                if (maxContainers == 1) {
//                    skylinePlans.add(onlyOneContainer());
//                } else {
//
//                    ////INC DEC/////
////                    System.out.println("calc "+cType.name);
//                    if (cType.equals(containerType.getLargest())) {
//                        ArrayList<containerType> cTypes = new ArrayList<>();
//                        cTypes.add(cType);
//
//                        skylinePlans_DEC.addAll(this.createAssignments("decreasing", cTypes));
//                        //                    plotPlans("dec",skylinePlans);
//                        //                    System.out.println("s1 "+skylinePlans.size());
//
//                    } else if (cType.equals(containerType.getSmallest())) {
//                        ArrayList<containerType> cTypes = new ArrayList<>();
//                        cTypes.add(cType);
//
//                        skylinePlans_INC.addAll(this.createAssignments("increasing", cTypes));
//                        //                    plotPlans("inc",skylinePlans);
//                        //                    System.out.println("s2 "+skylinePlans.size());
//                    } else {
//                        ArrayList<containerType> cTypes = new ArrayList<>();
//                        cTypes.add(cType);
//
//
//                        skylinePlans_INCDEC
//                                .addAll(this.createAssignments("increasing/decreasing", cTypes));
//
//                    }
//
//                }
//
//            }
//        }
//
//        skylinePlans.addAll(skylinePlans_DEC.results);
//        skylinePlans.addAll(skylinePlans_INC.results);
//        skylinePlans.addAll(skylinePlans_INCDEC.results);
//
//
//        paretoPlans.addAll(skylinePlans.results);
//
//
//        if(homotohetero) {
//
//            paretoPlans.computeSkyline(pruneEnabled,homoPlanstoKeep,false,PruneMethod, multi, true);
//
//            mpinfo.add("pareto",paretoPlans.results);
//
//            long homoEnd = System.currentTimeMillis();
//            System.out.println("Pare homoEnd: "+(homoEnd-startCPU_MS));
//
//            skylinePlans.clear();
//
//            for(Plan pp: paretoPlans.results) {
//                if (pp.vmUpgrading.equals("increasing/decreasing")) {
//
//                    pp.vmUpgrading = "increasing";
//                    skylinePlans.add(pp);
//
//                    Plan newpp = new Plan(pp);
//                    newpp.vmUpgrading="decreasing";
//                    skylinePlans.add(newpp);
//                } else {
//                    skylinePlans.add(pp);
//                }
//            }
//
//            paretoPlans.clear();
//
//
//            paretoPlans.addAll(homoToHetero(skylinePlans)); //returns only hetero
//
//            System.out.println("Pare homoToHetero End: "+(System.currentTimeMillis() - homoEnd));
//
//            paretoPlans.addAll(skylinePlans);
//
//        }
//        space.addAll(paretoPlans);
//
//        //        space.computeSkyline(pruneEnabled,pruneSkylineSize,false,PruneMethod);
//        long endCPU_MS = System.currentTimeMillis();
//        space.setOptimizationTime(endCPU_MS - startCPU_MS);
//
//
//        mpinfo.add("final space",space.results);
//
//
//        return space;
//
//    }

    //input plan
    //output contSlack, contOps, opSlack
    private void computeSlack(Plan plan,HashMap<Long, Double> contSlack, HashMap<Long, Integer> contOps, HashMap<Long, Long> opSlack, ArrayList<Long> opSortedBySlack) {
        contSlack.clear();
        contOps.clear();
        opSlack.clear();
        opSortedBySlack.clear();

        computeSlackOps(plan, opSlack, opSortedBySlack);

        for(Long opId: opsSortedReversed()) {
            double slackPerCont = opSlack.get(opId);
            long opContID = plan.assignments.get(opId);
            int opsPerCont = 1;
            if(contOps.containsKey(opContID)){
                slackPerCont += contSlack.get(opContID);
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
        if( plans.isEmpty() ) { return plans; }

        SolutionSpace plansInner = new SolutionSpace();//deepcopy of input
        for (Plan p : plans.results) {
            plansInner.add(new Plan(p));
        }

        SolutionSpace result = new SolutionSpace();//keeps all the solutions at the current pareto

        for (Plan p : plans) {
            result.add(new Plan(p));
        }
        //look at each plan and upgrade one by one the LARGE containers

        HashMap<Long, Long> opSlack = new HashMap<>();

        SolutionSpace skylinePlansNew = new SolutionSpace();
        //the set of plans from the newly modified plans (plans with upgraded/degraded vm types) that belong to the current pareto

        int updateSkyline = 1;

        int loop = 0;

        while (updateSkyline == 1) {
            loop++;
            updateSkyline = 0;
            int innerloop = 0;
            for (final Plan plan : plansInner) {                                                                         //for every plan
                innerloop++;
                LinkedList<Long> planContainersTobeModified = new LinkedList<>();
//                System.out.println(loop + ": plan" + innerloop);
                ArrayList<Long> opSortedBySlack = new ArrayList<>();
                //compute avg slack per container/VM

                HashMap<Long, Double> contSlack = new HashMap<>();
                HashMap<Long, Integer> contOps = new HashMap<>();

                computeSlack(plan, contSlack, contOps, opSlack, opSortedBySlack);

                for (Long i : plan.cluster.containers.keySet()) {                                                           // for each cont change it
                    Container cont = plan.cluster.getContainer(i);

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
                        double s1;
                        double s2;
                        if (!contSlack.containsKey(vm1)) {
                            s1 = Double.MAX_VALUE;
                        } else {
                            s1 = contSlack.get(vm1);///(double)contOps.get(vm1);
                        }

                        if (!contSlack.containsKey(vm2)) {
                            s2 = Double.MAX_VALUE;//TODO check asap
                        } else {
                            s2 = contSlack.get(vm2);///(double)contOps.get(vm1);
                        }
                        if (s1 > s2)//TODO: add precision error
                            return -1;
                        else if (s1 < s2)
                            return 1;
                        else
                            return 0;
                    }
                };

                if (plan.vmUpgrading.contains("decreasing")) {
//                    System.out.println("ss " + planContainersTobeModified.size()+" "+planContainersTobeModified);
                    Collections.sort(planContainersTobeModified, contSlackComparator);
                } else Collections.sort(planContainersTobeModified, Collections.reverseOrder(contSlackComparator));

                Plan newPlan = null;

                for (Long k : planContainersTobeModified) {            //for every cont that can be modified, create a new plan

                    if (plan.vmUpgrading == null) {
                        System.out.println("bug line 297");
                        break;
                    }

                    newPlan = new Plan(graph, new Cluster());
                    newPlan.vmUpgrading = plan.vmUpgrading;
                    for (Container contcont : plan.cluster.containersList) {
                        newPlan.cluster.addContainer(contcont.contType);
                    }


                    Container cont = newPlan.cluster.containers.get(k);

                    if (plan.vmUpgrading.equals("increasing"))                                      //modify the container -- TODO check if we could add all smaller and bigger conts
                        newPlan.cluster.update(cont.id, containerType.getNextLarger(cont.contType));
                    else
                        newPlan.cluster.update(cont.id, containerType.getNextSmaller(cont.contType));


                    int opsAssigned = 0;                                                              //assign all the ops again to the new plan
                    HashSet<Long> opsAssignedSet = new HashSet<>();
                    HashSet<Long> readyOps = new HashSet<>();

                    findRoots(readyOps);

                    while (readyOps.size() > 0) {//iterate on the ready to schedule ops

                        opsAssigned++;
                        long nextOpID = nextOperator(readyOps);
                        Operator nextOp = graph.getOperator(nextOpID);
//                        System.out.println("\nHomoToHetero scheduling "+nextOpID + " "+readyOps.toString());

                        newPlan.assignOperator(nextOpID, plan.assignments.get(nextOpID), backfillingUpgrade);

                        findNextReadyOps(readyOps, opsAssignedSet, nextOpID);
                    }

                    //use Double.compare
                    if (newPlan.stats.money >= plan.stats.money && newPlan.stats.runtime_MS >= plan.stats.runtime_MS)//we could use a threshold. e.g. if savings less than 0.1%
                    {
                        break; //no more containers for this plan are going to be modified. it breaks
                    }
                    skylinePlansNew.add(newPlan);
                }

            }

            plansInner.clear();
            plansInner.addAll(result.results);


            plansInner.addAll(skylinePlansNew);

            plansInner.computeSkyline(pruneEnabled, pruneSkylineSize, true, PruneMethod, multi,
                    false, constraint_mode, money_constraint, time_constraint);

            plansInner.retainAllAndKeep(skylinePlansNew, pruneSkylineSize);

            result.addAll(plansInner);

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
                double opProcessTime = plan.opIdtoStartEndProcessing_MS
                        .get(opId).b - plan.opIdtoStartEndProcessing_MS.get(opId).a;

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

    private void findRoots(HashSet<Long> readyOps) {
        for (Long opid : graph.operators.keySet()) {
            if (graph.getParents(opid).size() == 0) {
                readyOps.add(opid);
            }
        }
    }

    private void findNextReadyOps(HashSet<Long> readyOps, HashSet<Long> opsAssignedSet, Long justScheduledOpId) {
        Boolean allAssigned;               //find new readyops
        readyOps.remove(justScheduledOpId);
        opsAssignedSet.add(justScheduledOpId);
        for (Edge childEdge : graph.getChildren(justScheduledOpId)) {
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

    private void findNextReadyOps(HashSet<Long> readyOps, HashSet<Long> readyOpsInner, HashSet<Long> opsAssignedSet, Plan plan) {


        plan.calculateOrderingofOperatorsInContainers();

        if (readyOps.size() == 0) {
            findRoots(readyOpsInner);
        } else {
            for (Long opId : readyOps) {
                opsAssignedSet.add(opId);
                findNextReadyOps(readyOpsInner, opsAssignedSet, opId);
            }
            readyOps.clear();
        }
        //find the ready operators from plan container ordering


        ArrayList<Long> firstUnscheduledOp = new ArrayList<>();

        for (Stack<Long> s : plan.contIdToSortedOps.values()) { //update the sorted operators
            if (s.size() > 0) {
                if (opsAssignedSet.contains(s.peek())) {
                    s.pop();
                }
            }
        }
        //find the ready operators from plan container ordering
        for (Stack<Long> s : plan.contIdToSortedOps.values()) { //update the sorted operators
            if (s.size() > 0) {
                firstUnscheduledOp.add(s.peek());
            }
        }

        //intersect firsUnscheduled with readyOps

        for (Long opId : firstUnscheduledOp) {
            if (readyOpsInner.contains(opId)) {
                readyOps.add(opId);
            }
        }

    }

    private SolutionSpace createAssignments(String vmUpgrading, ArrayList<containerType> cTypes, boolean homo) {

        int constraint_mode_local = constraint_mode;
        if (homo) { constraint_mode_local = 0; }
//        System.out.println("createass start");
        Plan firstPlan = new Plan(graph, cluster);
        firstPlan.vmUpgrading = vmUpgrading;

        SolutionSpace allCandidates = new SolutionSpace();
        SolutionSpace plans = new SolutionSpace();
        plans.add(firstPlan);

        int opsAssigned = 0;

        HashSet<Long> opsAssignedSet = new HashSet<>();
        HashSet<Long> readyOps = new HashSet<>();

        findRoots(readyOps);

        int prevPrune = -1;

        while (readyOps.size() > 0) {

            opsAssigned++;

            // Get the most expensive operator from the ready ones
            long nextOpID = nextOperator(readyOps);
            Operator nextOp = graph.getOperator(nextOpID);
//            System.out.println(cTypes.get(0).name + ". Next:" + nextOpID + ". Assigned " + opsAssigned + " ops");
//               System.out.println("scheduling "+nextOpID + " "+readyOps.toString());

            allCandidates.clear();
            for (Plan plan : plans) {

                if (plan == null) {
                    continue;
                }

//                System.out.println("\nnewly created plans");
                scheduleToCandidateContainers(nextOpID, plan, cTypes, allCandidates);//allCanditates is an out param

            }
            plans.clear();

            plans = new SolutionSpace();
            plans.addAll(allCandidates.results);
            plans.computeSkyline(pruneEnabled, pruneSkylineSize, false, PruneMethod, multi, true,
                    0, money_constraint, time_constraint);
            if (plans.isEmpty()) { return plans; }

            findNextReadyOps(readyOps, opsAssignedSet, nextOpID);

        }
//        System.out.println("createass end");

        return plans;
    }

    private void scheduleToCandidateContainers(Long opId, Plan plan, ArrayList<containerType> contTypes, SolutionSpace planEstimations) {
        //assume that not empty containers exist

        for (Long contId : plan.cluster.containers.keySet()) { //add to every existing container
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId, backfilling);
            planEstimations.add(newPlan);
        }
        if (plan.cluster.contUsed.size() < maxContainers) {  //add a nwe container of contType and assign the op to that
            for (containerType contType : contTypes) {//uncomment to add every ctype
                Plan newPlan = new Plan(plan);
                Long newContId = newPlan.cluster.addContainer(contType);
                newPlan.assignOperator(opId, newContId, backfilling);
                planEstimations.add(newPlan);

            }
        }

    }

    public Plan onlyOneContainer() {
        containerType contType = containerType.getSmallest();//maybe check for every container later
        Plan plan = new Plan(graph, cluster);

        plan.cluster.addContainer(contType.getSmallest());
        plan.vmUpgrading = "increasing";

        cluster.addContainer(contType);

        for (Operator op : graph.getOperators()) {
            plan.assignOperator(op.getId(), plan.cluster.getContainer(0L).id, backfilling);
        }
        //  plan.printAssignments();
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


    public SolutionSpace ComputeOnePerNumberofVmsSkyline(SolutionSpace plans) {
        SolutionSpace skyline = new SolutionSpace();

        HashMap<Integer, Plan> vmCountToFastestPlan = new HashMap<>();

        for (Plan p : plans) {
            int numberofConts = p.cluster.contUsed.size();
            Plan tplan = p;
            if (vmCountToFastestPlan.containsKey(numberofConts)) {
                tplan = getFastest(tplan, vmCountToFastestPlan.get(numberofConts));
            }
            vmCountToFastestPlan.put(numberofConts, tplan);

        }

        skyline.addAll(vmCountToFastestPlan.values());
        return skyline;

    }


    public Plan getFastest(Plan p1, Plan p2) {
        if (p1.stats.runtime_MS == p2.stats.runtime_MS) {
            if (p1.stats.money == p2.stats.money) {
                if (p1.cluster.contUsed.size() < p1.cluster.contUsed.size()) {
                    return p1;
                } else {
                    return p2;
                }
            } else if (p1.stats.money < p2.stats.money) {
                return p1;
            } else {
                return p2;
            }
        } else {
            if (p1.stats.runtime_MS < p2.stats.runtime_MS) {
                return p1;
            } else {
                return p2;
            }
        }
    }


    public void findDominanceRelations(ArrayList<Plan> plans, HashMap<Plan, ArrayList<Plan>> dominatedSet, HashMap<Plan, ArrayList<Plan>> dominanceSet, ArrayList<Plan> skyline) {
        for (int cur = 0; cur < plans.size(); cur++) {
            Plan curPlan = plans.get(cur);

            //finding dominatedSet
            for (int next = cur + 1; next < plans.size(); next++) {
                Plan nextPlan = plans.get(next);
                if (nextPlan.stats.runtime_MS > curPlan.stats.runtime_MS && nextPlan.stats.money < curPlan.stats.money) {
                    dominatedSet.get(curPlan).add(nextPlan);


                } else
                    break;
            }

            //finding dominanceSet
            for (int previous = cur - 1; previous >= 0; previous--) {

                Plan previousPlan = plans.get(previous);
                if (previousPlan.stats.runtime_MS <= curPlan.stats.runtime_MS && previousPlan.stats.money <= curPlan.stats.money) {
                    if (skyline.contains(previousPlan))
                        dominanceSet.get(curPlan).add(previousPlan);
                } else
                    break;

            }

        }
    }


    public HashMap<Plan, Double> computeDominanceScore(ArrayList<Plan> plans, HashMap<Plan, ArrayList<Plan>> dominatedSet, HashMap<Plan, ArrayList<Plan>> dominanceSet, ArrayList<Plan> skyline) {
        //dominatedSet: set of plans dominated by the key plan
        //dominanceSet: set of skyline plans the key plan is dominated by

        HashMap<Plan, Double> dominanceScore = new HashMap<>();

        for (int curPlan = 0; curPlan < skyline.size(); curPlan++) {//for(int curPlan = 0; curPlan<plans.size(); curPlan++) {
            Double domScore = 0.0;
            if (dominatedSet.containsKey(skyline.get(curPlan))) {//if (dominatedSet.containsKey(plans.get(curPlan))) {
                //     System.out.println("not empty");
                for (Plan p : dominatedSet.get(plans.get(curPlan))) {
                    //     System.out.println(dominatedSet.get(p).size() + " " + dominanceSet.get(p).size());

                    Double dp_p_sp = 1.0 / dominatedSet.get(plans.get(curPlan)).size();//Double dp_p_sp = 1.0 / dominatedSet.get(p).size();
                    if (dominatedSet.get(plans.get(curPlan)).size() == 0)
                        dp_p_sp = Double.MAX_VALUE;
                    Double idp_p = Math.log((double) skyline.size() / (double) dominanceSet.get(p).size());
                    if (dominanceSet.get(p).size() == 0)
                        idp_p = Double.MAX_VALUE;
                    domScore += (dp_p_sp * idp_p);

                }

            }
            dominanceScore.put(skyline.get(curPlan), domScore);//  dominanceScore.put(plans.get(curPlan), domScore);

        }

        return dominanceScore;

    }

//    public  SolutionSpace computeSkyline(SolutionSpace plans){
//        SolutionSpace skyline = new SolutionSpace();
//
//
//
//        plans.sort(true, multi); // Sort by time breaking equality by sorting by money
//
//        HashMap<Plan, ArrayList <Plan>> dominatedSet=new HashMap<>();
//        HashMap<Plan, ArrayList <Plan>> dominanceSet=new HashMap<>();
//        for(Plan p : plans){
//            dominanceSet.put(p,new ArrayList<>());
//            dominatedSet.put(p,new ArrayList<>());
//        }
//
//
//        Plan previous = null;
//        for (Plan est : plans) {
//            if (previous == null) {
//                skyline.add(est);
//                previous = est;
//                continue;
//            }
//            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
//                // Already sorted by money
//                continue;
//            }
//            if(Math.abs(previous.stats.money - est.stats.money)>RuntimeConstants.precisionError) //TODO ji fix or check
//                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
//                    skyline.add(est);
//                    previous = est;
//                }
//        }
//
//        findDominanceRelations(plans.results, dominatedSet,  dominanceSet, skyline.results);
//
//        HashMap<Plan, Double> domScore  = computeDominanceScore(plans.results, dominatedSet,  dominanceSet, skyline.results);
//
//        SolutionSpace skylinePruned = new SolutionSpace();
//
//        return skyline;
//    }

    private SolutionSpace pruneSkylineByDominanceScore(SolutionSpace skylinePlans, HashMap<Plan, Double> domScore) {

        SolutionSpace skylineNew = new SolutionSpace();

        int skylinePlansToKeep = 20;

        if (skylinePlans.size() > skylinePlansToKeep) {
            // Keep only some schedules in the skyline according to their crowding distance

            int schedulesKept = 0;
            final HashMap<Plan, Double> planDistance = new HashMap<>();

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(o1.stats.runtime_MS, o2.stats.quanta);
                }
            });

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(o1.stats.money, o2.stats.money);
                }
            });

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(domScore.get(o1),
                            domScore.get(o2));
                }
            });

            for (int p = 0; p < skylinePlans.size(); ++p) {
                if (p < skylinePlansToKeep) {
                    ++schedulesKept;
                } else
                    skylinePlans.results.set(p, null);
            }

            Check.True(schedulesKept <= skylinePlansToKeep + 1,
                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
        }
        for (Plan p : skylinePlans) {
            if (p != null)
                skylineNew.add(p);
        }

        return skylineNew;
    }


    private SolutionSpace pruneSkylineByCrowdDist(SolutionSpace skylinePlans) {

        SolutionSpace skylineNew = new SolutionSpace();

        int skylinePlansToKeep = 20;

        if (skylinePlans.size() > skylinePlansToKeep) {
            // Keep only some schedules in the skyline according to their crowding distance

            int schedulesKept = 0;
            final HashMap<Plan, Double> planDistance = new HashMap<>();

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
                }
            });
            for (int p = 0; p < skylinePlans.size(); ++p) {
                if (p == 0 || p == skylinePlans.size() - 1) {
                    planDistance.put(skylinePlans.results.get(p), 0.0);
                } else {
                    long makespan_prev = skylinePlans.results.get(0).stats.runtime_MS;
                    long makespan_next = skylinePlans.results.get(p).stats.runtime_MS;
                    planDistance.put(skylinePlans.results.get(p),
                            Math.pow((makespan_next - makespan_prev) / makespan_prev, 2));
                }
            }

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(o1.stats.money, o2.stats.money);
                }
            });
            for (int p = 0; p < skylinePlans.size(); ++p) {
                if (p == 0 || p == skylinePlans.size() - 1) {
                    planDistance.put(skylinePlans.results.get(p), 0.0);
                } else {
                    Double money_prev = skylinePlans.results.get(0).stats.money;
                    Double money_next = skylinePlans.results.get(p).stats.money;
                    planDistance.put(skylinePlans.results.get(p),
                            planDistance.get(skylinePlans.results.get(p)) + Math
                                    .pow((double) ((money_next - money_prev) / money_prev), 2));
                }
            }

            Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    return Double.compare(Math.sqrt(planDistance.get(o1)),
                            Math.sqrt(planDistance.get(o2)));
                }
            });

            for (int p = 0; p < skylinePlans.size(); ++p) {
                if (p < skylinePlansToKeep) {
                    ++schedulesKept;
                } else
                    skylinePlans.results.set(p, null);
            }

            Check.True(schedulesKept <= skylinePlansToKeep + 1,
                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
        }
        for (Plan p : skylinePlans) {
            if (p != null)
                skylineNew.add(p);
        }

        return skylineNew;
    }


//    private SolutionSpace computeHomoSkyline(SolutionSpace skylinePlans) {
//
//        SolutionSpace candidates = new SolutionSpace();
//
//        skylinePlans.sort(true, multi);
//
//        // Sort by time breaking exec time equality by sorting by money and then containers used
//        candidates.sort(true, multi);
//
//        // Keep only the skyline
//        SolutionSpace skyline = new SolutionSpace();
//        Plan previous = null;
//
//
//        for (Plan est : candidates) {
//            // System.out.println("candidate "+est.estimator.getAssignments()+ est.estimator.activeContainersType);
//            if (previous == null) {
//                //    System.out.println("kept ");
//                skyline.add(est);
//                previous = est;
//                continue;
//            }
//            if (previous.stats.runtime_MS == est.stats.runtime_MS){
//                // Already sorted by money
//                continue;
//            }
//            if (previous.stats.money > est.stats.money && Math.abs(previous.stats.money - est.stats.money)>0.000000001) {
//                //   System.out.println("kept ");
//                skyline.add(est);
//                previous = est;
//            }
//        }
//        return skyline;
//    }

//    private SolutionSpace computeNewSkyline(SolutionSpace skylinePlans, SolutionSpace skylinePlansNew) {
//
//        SolutionSpace candidates = new SolutionSpace();
//
//        candidates.addAll(skylinePlans);
//        candidates.addAll(skylinePlansNew);
//
//        // Sort by time breaking exec time equality by sorting by money and then containers used
//        candidates.sort(true, multi);
//
//        // Keep only the skyline
//        SolutionSpace skyline = new SolutionSpace();
//        Plan previous = null;
//
//
//        for (Plan est : candidates) {
//            // System.out.println("candidate "+est.estimator.getAssignments()+ est.estimator.activeContainersType);
//            if (previous == null) {
//                //    System.out.println("kept ");
//                skyline.add(est);
//                previous = est;
//                continue;
//            }
//            if (previous.stats.runtime_MS == est.stats.runtime_MS){
//                skylinePlansNew.results.remove(est);
//                // Already sorted by money
//                continue;
//            }
//            if (previous.stats.money > est.stats.money && Math.abs(previous.stats.money - est.stats.money)>0.000000001){                //   System.out.println("kept ");
//                skyline.add(est);
//                previous = est;
//            }
//            else
//                skylinePlansNew.results.remove(est);
//        }
//        return skyline;
//    }

    private void computeRankings() {
        final HashMap<Long, Double> b_rank = new HashMap<>(); //opidTobRank
        final HashMap<Long, Double> t_rank = new HashMap<>();
        final HashMap<Long, Double> w_mean = new HashMap<>();
        final HashMap<Long, Double> sum_rank = new HashMap<>();
        // final HashMap<Long, Double> slacktime = new HashMap<>();
        final LinkedList<Long> opsSumRankSorted = new LinkedList<>();
        final LinkedList<Long> opsBySlack = new LinkedList<>();
///   private HashMap<Long, Double> opSlack = new HashMap<>();

        final TopologicalSorting topOrder = new TopologicalSorting(graph);

        HashMap<Integer, Integer> opLevelperLevel = new HashMap<>();
        HashMap<Integer, ArrayList<Long>> opLevelList = new HashMap<>();


        //TODO:initalize b_rank and t_rank!!

        int numLevels = 0;
        for (int i = 0; i < 20; ++i) {
            opLevelList.put(i, new ArrayList<Long>());
        }

        for (Long opId : topOrder.iterator()) {
            int level = 0;
            for (Edge parentEdge : graph.getParents(opId)) {

                Integer plevel = opLevel.get(parentEdge.from);
                level = Math.max(plevel + 1, level);
            }
            opLevel.put(opId, level);
            numLevels = Math.max(level + 1, numLevels);
            if (opLevelperLevel.containsKey(level))
                opLevelperLevel.put(level, opLevelperLevel.get(level) + 1);
            else
                opLevelperLevel.put(level, 1);

            opLevelList.get(level).add(opId);

            //  System.out.println("op "+ opId + " level " +level);

        }


        //Double crPathLength=0.0;
        for (Long opId : topOrder.iteratorReverse()) {
            double maxRankChild = 0.0;
            for (Edge childEdge : graph.getChildren(opId)) {
                double comCostChild = 0.0;
                for (Edge parentofChildEdge : graph.getParents(childEdge.to)) {
                    if (parentofChildEdge.from.equals(opId)) {// if((long)parentofChildEdge.from==(long)opId) {//
                        comCostChild = Math.ceil(parentofChildEdge.data.size_B / RuntimeConstants.network_speed_B_MS);
                    }
                }
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild + b_rank.get(childEdge.to));
            }

            double wcur = 0.0;
            for (containerType contType : containerType.values())
                wcur += graph.getOperator(opId).getRunTime_MS() / contType.container_CPU; //TODO ji check if S or MS
            int types = containerType.values().length;
            double w = wcur / (double) types;//average execution cost for operator op
            b_rank.put(opId, (w + maxRankChild));//b_rank.put(opId, (w+maxRankChild));
            w_mean.put(opId, w);

        }

        for (Long opId : topOrder.iterator()) {
            double maxRankParent = 0.0;
            for (Edge inLink : graph.getParents(opId)) {
//                Operator opParent=graph.getOperator(inLink.from.getopID());
                double comCostParent = Math.ceil(inLink.data.size_B / RuntimeConstants.network_speed_B_MS);
                maxRankParent = Math.max(maxRankParent, comCostParent + t_rank.get(inLink.from) + w_mean.get(inLink.from));
            }

            double wcur = 0.0;
            for (containerType contType : containerType.values())
                wcur += graph.getOperator(opId).getRunTime_MS() / contType.container_CPU;
            int types = containerType.values().length;
            double w = wcur / (double) types;//average execution cost for operator op
            t_rank.put(opId, (maxRankParent));
            Double opRank = b_rank.get(opId) + t_rank.get(opId);// -w;
            sum_rank.put(opId, opRank);
            //  crPathLength =Math.max(crPathLength, opRank);
        }

        for (Long op : topOrder.iterator()) {
            opsSumRankSorted.add(op);
            opsBySlack.add(op);
            Double opRank = sum_rank.get(op);
            //  double opSlacktime = crPathLength - opRank;
            //  slacktime.put(op, opSlacktime);
        }

        final HashMap<Long, Double> rankU = new HashMap<>();

        for (Long opId : topOrder.iteratorReverse()) {

            double maxRankChild = 0.0;
            for (Edge outLink : graph.getChildren(opId)) {
                double comCostChild = Math.ceil(outLink.data.size_B / RuntimeConstants.network_speed_B_MS);
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild + rankU.get(outLink.to));
            }

            double wcur = 0.0;
            for (containerType contType : containerType.values()) {
                long mst = graph.getOperator(opId).getRunTime_MS();
                double cput = contType.container_CPU;
                wcur += graph.getOperator(opId).getRunTime_MS() / contType.container_CPU;
            }
            int types = containerType.values().length;
            double w = wcur / (double) types;//average execution cost for operator op
            rankU.put(opId, (w + maxRankChild));

        }

        for (Long op : topOrder.iterator()) {
            //     System.out.println(op + " sumrank " +  sum_rank.get(op));
            opsSorted.add(op);
        }


//        Comparator<Long> rankComparator = new Comparator<Long>() {
//            @Override
//            public int compare(Long op1, Long op2) {
//                double r1 = rankU.get(op1);
//                double r2 = rankU.get(op2);
//                if (r1 > r2)
//                    return -1;
//                else if (r1 < r2)
//                    return 1;
//                else
//                    return 0;
//            }
//        };

        //    Collections.sort(opsSorted, rankComparator);

        Comparator<Long> sumrankComparator = new Comparator<Long>() {//sumrank for tasks of a single dag (superdag based) instead the opposite order for crpathlength-sumrank
            @Override
            public int compare(Long op1, Long op2) {
                double r1 = sum_rank.get(op1);
                double r2 = sum_rank.get(op2);
                if (r1 > r2)//TODO: add precision error
                    return -1;
                else if (r1 < r2)
                    return 1;
                else
                    return 0;
            }
        };
        Collections.sort(opsSorted, sumrankComparator);

        Comparator<Long> levelRankComparator = new Comparator<Long>() {
            @Override
            public int compare(Long op1, Long op2) {
                double r1 = opLevel.get(op1);
                double r2 = opLevel.get(op2);
                if (r1 < r2)//TODO: add precision error
                    return -1;
                else if (r1 > r2)
                    return 1;
                else
                    return 0;
            }
        };
        Collections.sort(opsSorted, levelRankComparator);  //should it be the same as 297????


        Comparator<Long> dagIdComparator = new Comparator<Long>() {
            @Override
            public int compare(Long op1, Long op2) {
                Long d1 = graph.getOperator(op1).dagID;
                Long d2 = graph.getOperator(op2).dagID;
                if (d1 < d2)
                    return -1;
                else if (d1 > d2)
                    return 1;
                else
                    return 0;
            }
        };

        //default: commonentry which has by slack and level
        if (rankingMethod.equals("perDag"))
            Collections.sort(opsSorted, dagIdComparator);


        if (rankingMethod.equals("dagMerge")) {

            Comparator<Long> subdagComparator = new Comparator<Long>() {
                @Override
                public int compare(Long op1, Long op2) {
                    Integer r1 = opsSorted.indexOf(op1);
                    Integer r2 = opsSorted.indexOf(op2);
                    if (r1 < r2)
                        return -1;
                    else if (r1 > r2)
                        return 1;
                    else
                        return 0;
                }
            };

            LinkedList<Long> opsToSchedule = new LinkedList<>();
            HashMap<Long, LinkedList<Long>> subdagOpsList = new HashMap<>();
            HashMap<Long, Long> subdagNext = new HashMap<>();
            HashMap<Long, ListIterator<Long>> iteratorPerSubdag = new HashMap<>();

            for (DAG subdag : graph.superDAG.subDAGList.values()) {
                LinkedList<Long> opsSubdag = new LinkedList();
                //  System.out.println("subdagid " + subdag.dagId);
                Long subdagId = subdag.dagId;
                for (Operator op : subdag.getOperators())
                    opsSubdag.add(graph.superDAG.subdagToDagOpIds.get(subdag.dagId).get(op.getId()));//op.getId());
                Collections.sort(opsSubdag, subdagComparator);
                subdagOpsList.put(subdagId, opsSubdag);
                iteratorPerSubdag.put(subdagId, subdagOpsList.get(subdagId).listIterator());
                Long opc = iteratorPerSubdag.get(subdagId).next();
                subdagNext.put(subdagId, opc);
            }

            //     int opsAdded = 0;

//        System.out.println("initially subdagNext \n");
//        for(long opnext: subdagNext.values())
//            System.out.println(opnext);
//        System.out.println("\n");

            while (opsToSchedule.size() < graph.getOperators().size()) {
                double maxSumrank = 0.0;
                double minSlack = Double.MAX_VALUE;
                double maxPriority = 0.0;
                long nextToAdd = -1L;
                for (long opnext : subdagNext.values()) {

                    //long opId= subdagNext.get()
                    //    System.out.println("opnext" + " " + opnext + " "+ graph.getOperator(opnext).dagID + " " + graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).dagId);
                    double crPathLength = graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).computeCrPathLength(containerType.values());

                    HashMap<Long, Double> pathToExit = graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).computePathToExit(containerType.values());
                    HashMap<Long, Double> maxPath = graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).computePath(containerType.values());
                    Double cpSubdag = graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).computeMaxPath(containerType.values());


                    Long idsub = graph.superDAG.dagToSubdagOpIds.get(opnext);
                    //graph.superDAG.subdagToDagOpIds.get(graph.getOperator(opnext).dagID).g(opnext);
                    ////   subdagOpsList.get(graph.getOperator(opnext).dagID);

                    // Long idsub= graph.superDAG.subdagToDagOpIds.get(graph.getOperator(opnext).dagID).get(opnext);
//               System.out.println("looks for: " + opnext + " pathToExit " + pathToExit.get(idsub) + " " +idsub);

                    double tasksScheduledPerc = (iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex() + 1) / (double) graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size();
                    double taskWeight = w_mean.get(opnext) / crPathLength;
                    double taskSlack = crPathLength - sum_rank.get(opnext);//only slack based

                    double tasksUnScheduledPerc = ((double) graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size() - iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex()) / (double) graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size();


                    // double c=crPathLength/sum_rank.get(opnext);//tasksScheduledPerc;//taskSlack*tasksScheduledPerc;///taskWeight;
//add level/levels per subdag?

                    //     double c =taskSlack/(w_mean.get(opnext)/crPathLength);//(w_mean.get(opnext)*taskSlack)
                    //  double c =(w_mean.get(opnext)*taskSlack)/crPathLength*tasksScheduledPerc;
                    //  double c = (sum_rank.get(opnext) / crPathLength) * (iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex() + 1) / graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size();

                    //  double c = crPathLength - sum_rank.get(opnext);//only slack based

                    //    double c =taskSlack*tasksScheduledPerc;

                    //     double c =taskSlack*tasksScheduledPerc/taskWeight;

                    //   double c =taskSlack/tasksUnScheduledPerc;


//                    taskSlack = cpSubdag - maxPath.get(graph.superDAG.dagToSubdagOpIds.get(opnext));//only slack based
//
//                    double c =taskSlack*tasksScheduledPerc;
//
//                    if (c <= minSlack) {
//                        nextToAdd = opnext;
//                        minSlack = c;
//
//                    }


                    double br = pathToExit.get(graph.superDAG.dagToSubdagOpIds.get(opnext));
                    double c = (br / crPathLength) * tasksUnScheduledPerc;

                    if (c >= maxPriority) {
                        //if equal select hte one with the smallest level
                        nextToAdd = opnext;
                        maxPriority = c;

                    }


                }

                opsToSchedule.addLast(nextToAdd);

//            System.out.println(opsAdded+". adds " + opsToSchedule.getLast()+ " " + nextToAdd + " of ");

                long dagToUse = graph.getOperator(nextToAdd).dagID;//or directly from row structure

//            System.out.println(opsAdded+". adds " + opsToSchedule.getLast()+ " " + nextToAdd + " of "+ dagToUse);

                if (!iteratorPerSubdag.get(dagToUse).hasNext())
                    subdagNext.remove(dagToUse);
                else {
                    subdagNext.put(dagToUse, iteratorPerSubdag.get(dagToUse).next());
                    //int indexNext = subdagOpsList.get(dagToUse).indexOf(nextToAdd);
                }


                //    opsAdded++;
//            System.out.println("subdagNext have");
//            for(long op: subdagNext.values())
//                System.out.println(op + " dagId " + graph.getOperator(op).dagID + " level " +opLevel.get(op) + " rank " + sum_rank.get(op));

            }
//

//        System.out.println("\n\n opstoschedule");

            opsSorted.clear();
            opsSorted.addAll(opsToSchedule);


        }
//        for(Long op: opsToSchedule)
//            System.out.println(op + " dagId " + graph.getOperator(op).dagID + " level " +opLevel.get(op) + " rank " + sum_rank.get(op));

//        System.out.println("sorted ops");
//        for(Long op: opsSorted)
//            System.out.println(op + " dagId " + graph.getOperator(op).dagID + " level " +opLevel.get(op) + " rank " + sum_rank.get(op));

    }


    //                if(sum_rank.get(opnext)>maxSumrank)
//                {
//                    nextToAdd = opnext;
//                    maxSumrank = sum_rank.get(opnext);
//
//                }


//                double c=(crPathLength-sum_rank.get(opnext))/(graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size()/(iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex()+1));
//
//
//                if(c<minSlack)
//                {
//                    nextToAdd = opnext;
//                    minSlack = c;
//
//                }


//                double c=(sum_rank.get(opnext))/(iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex()+1)/graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size();
//
//
//                if(c>maxSumrank)
//                {
//                    nextToAdd = opnext;
//                    maxSumrank = c;
//
//                }

//                double c=(sum_rank.get(opnext))*(iteratorPerSubdag.get(graph.getOperator(opnext).dagID).previousIndex()+1)/graph.superDAG.getSubDAG(graph.getOperator(opnext).dagID).getOperators().size();
//                if( c <= minSlack)
//                {
//                    nextToAdd = opnext;
//                    minSlack =c;
//
//                }


    public HashMap<Long, Long> computeLST(Plan plan) {

        HashMap<Long, Long> opLST = new HashMap<>();

        for (Long opId : opsSortedReversed()) {
            Long lst = Long.MAX_VALUE;


            //also checks succ at container
            long succStartTime = Long.MAX_VALUE;
            long templst = Long.MAX_VALUE;
            Long succId = null;
            Long contId = plan.assignments.get(opId);
//            plan.printInfo();
            for (Long nextOpId : plan.contAssignments.get(contId)) {
                if (plan.opIdtoStartEndProcessing_MS.get(nextOpId).a < succStartTime && plan.opIdtoStartEndProcessing_MS.get(nextOpId).a > plan.opIdtoStartEndProcessing_MS.get(opId).a) {
                    succStartTime = plan.opIdtoStartEndProcessing_MS.get(nextOpId).a;

                    templst = succStartTime - (plan.calculateDelayDistributedStorage(opId, succId));//plan.opIdToBeforeDTDuration_MS.get(succId);
                    succId = nextOpId;
                }
            }

            if (succId != null)
                lst = Math.min(templst - plan.calculateDelayDistributedStorage(opId, succId) - plan.opIdToProcessingTime_MS.get(opId), lst);//lst = Math.min(lst, templst - plan.opIdToBeforeDTDuration_MS.get(succId));


            if (graph.getChildren(opId).isEmpty()) { //if exit node
//                System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId));

                lst = plan.stats.runtime_MS - plan.opIdToProcessingTime_MS.get(opId);
            } else {
                for (Edge outEdge : graph.getChildren(opId)) {
                    succId = outEdge.to;

                    succStartTime = plan.opIdtoStartEndProcessing_MS.get(succId).a;

                    templst = succStartTime - (plan.calculateDelayDistributedStorage(opId, succId));//plan.opIdToBeforeDTDuration_MS.get(succId);

//TODO: add somewhere +1 for data transfer? It starts at the next interval every time...
                    lst = Math.min(templst - plan.calculateDelayDistributedStorage(opId, succId) - plan.opIdToProcessingTime_MS.get(opId), lst);
                }
            }

            opLST.put(opId, lst);
        }

        return opLST;
    }


    public HashMap<Long, Long> computeLST(Plan plan, Long opToAssignId) {

        HashMap<Long, Long> opLST = new HashMap<>();

        for (Long opId : opsSortedReversed()) {


            if (opToAssignId == opId)
                break;
            Long lst = Long.MAX_VALUE;
            if (graph.getChildren(opId).isEmpty()) { //if exit node
//                System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId));

                lst = plan.stats.runtime_MS - plan.opIdToProcessingTime_MS.get(opId);
            } else {
                for (Edge outEdge : graph.getChildren(opId)) {
                    Long succId = outEdge.to;

                    Long succStartTime = plan.opIdtoStartEndProcessing_MS.get(succId).a;

                    long templst = succStartTime - (plan.calculateDelayDistributedStorage(opId, succId));//plan.opIdToBeforeDTDuration_MS.get(succId);

//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + succId + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId) + " dt before succ: " + plan.opIdToBeforeDTDuration_MS.get(succId) + " starting at " + succStartTime);

//TODO: add somewhere +1 for data transfer? It starts at the next interval every time...
                    lst = Math.min(templst - plan.calculateDelayDistributedStorage(opId, succId) - plan.opIdToProcessingTime_MS.get(opId), lst);
                    //lst = Math.min(succStartTime - plan.opIdToAfterDTDuration_MS.get(opId) - plan.opIdToProcessingTime_MS.get(opId), lst);
                }
            }

//            System.out.println(opId + " so lst " + lst);

            opLST.put(opId, lst);


        }

        return opLST;
    }

    public HashMap<Long, Long> computeEST(Plan plan, Long opToAssignId) {//TODO: when predID and opID at same container do not include dataTransfer time from predID to opID opIdToBeforeDTDuration_MS.get(predId)?
        HashMap<Long, Long> opEST = new HashMap<>();


        for (Long opId : opsSorted) {


            Long est = Long.MIN_VALUE;


            //also checks succ at container
            long predEndTime = Long.MIN_VALUE;
            Long predId = null;
            Long contId = plan.assignments.get(opId);
            for (Long nextOpId : plan.contAssignments.get(contId)) {
                if (plan.opIdtoStartEndProcessing_MS.get(nextOpId).b > predEndTime && plan.opIdtoStartEndProcessing_MS.get(nextOpId).b <= plan.opIdtoStartEndProcessing_MS.get(opId).a) {
                    predEndTime = plan.opIdtoStartEndProcessing_MS.get(nextOpId).b + 2 * (plan.calculateDelayDistributedStorage(predId, opId, plan.assignments.get(opId)));
                    ;
                    predId = nextOpId;
                }

            }

            if (predId != null)
                est = Math.max(est, predEndTime);//+ plan.opIdToBeforeDTDuration_MS.get(contId));

            if (graph.getParents(opId).isEmpty()) {
                est = 0L;
            } else {
                for (Edge inEdge : graph.getParents(opId)) {
                    predId = inEdge.from;
//TODO: remove 2* and add after/before once. calculateDelay returns 0 if not parent child
                    predEndTime = plan.opIdtoStartEndProcessing_MS.get(predId).b +
                            //opIdToAfterDTDuration_MS.get(predId) +//This dt is computed only for parents not vm pred even if it was to be included
                            2 * (plan.calculateDelayDistributedStorage(predId, opId, plan.assignments.get(opId)));//plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: do we need any +/-1?


//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + predId + " dt after pred: " + plan.opIdToAfterDTDuration_MS.get(predId) + " finishing at " + predEndTime);

                    est = Math.max(predEndTime, est);
                }
            }
            opEST.put(opId, est);
//            System.out.println(opId + " est " + est);

            if (opToAssignId == opId)
                break;
        }
        return opEST;
    }


    public HashMap<Long, Long> computeEST(Plan plan) {//TODO: when predID and opID at same container do not include dataTransfer time from predID to opID opIdToBeforeDTDuration_MS.get(predId)?
        HashMap<Long, Long> opEST = new HashMap<>();

        for (Long opId : opsSorted) {
            Long est = Long.MIN_VALUE;
            if (graph.getParents(opId).isEmpty()) {
                est = 0L;
            } else {
                for (Edge inEdge : graph.getParents(opId)) {
                    Long predId = inEdge.from;

                    Long predEndTime = plan.opIdtoStartEndProcessing_MS.get(predId).b +
                            //opIdToAfterDTDuration_MS.get(predId) +//This dt is computed only for parents not vm pred even if it was to be included
                            2 * (plan.calculateDelayDistributedStorage(predId, opId, plan.assignments.get(opId)));//plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: do we need any +/-1?


//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + predId + " dt after pred: " + plan.opIdToAfterDTDuration_MS.get(predId) + " finishing at " + predEndTime);

                    est = Math.max(predEndTime, est);
                }
            }
            opEST.put(opId, est);
//            System.out.println(opId + " est " + est);
        }
        return opEST;
    }

    //output opSlack,opSortedBySlack
    public HashMap<Long, Long> computeSlackOps(Plan plan, final HashMap<Long, Long> opSlack, ArrayList<Long> opSortedBySlack) {
        HashMap<Long, Long> opLST = computeLST(plan);
        HashMap<Long, Long> opEST = computeEST(plan);

        for (Long opId : graph.operators.keySet()) {
            Long LST = opLST.get(opId);
            Long EST = opEST.get(opId);
            opSlack.put(opId, LST - EST);
            opSortedBySlack.add(opId);
            //  System.out.println(opId + " slack " + (LST-EST));
        }

        Collections.sort(opSortedBySlack, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return (int) (opSlack.get(o1) - opSlack.get(o2));
            }
        });
        return opEST;
    }


    public Iterable<Long> opsSortedReversed() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return opsSorted.descendingIterator();
            }
        };
    }


    public void sortPlansByDerQuanta(ArrayList<Plan> skylinePlans) {


        final HashMap<Plan, Double> planDerivative = new HashMap<>();
        //sorted skylineplans already
        // Return the fastest for skylines with 2 or less points
        if (skylinePlans.size() <= 2) {
            return;
        }
        Plan p0 = skylinePlans.get(0);


        //keep the plans with min cost/time
        planDerivative.put(p0, Double.MAX_VALUE);
        planDerivative.put(skylinePlans.get(skylinePlans.size() - 1), Double.MAX_VALUE);

        // System.out.println( " ");
        for (int i = 1; i < skylinePlans.size() - 1; ++i) {
            Plan p1 = skylinePlans.get(i);

            Plan p2 = skylinePlans.get(i + 1);

            Statistics p0Stats = p0.stats;
            Statistics p1Stats = p1.stats;
            Statistics p2Stats = p2.stats;
            //System.out.println(p1Stats.quanta+ " "+p1Stats.money);
            double aR = p0Stats.money - p1Stats.money;
            double bR = p1Stats.runtime_MS - p0Stats.runtime_MS;
            double aL = p2Stats.money - p1Stats.money;
            double bL = p2Stats.runtime_MS - p1Stats.runtime_MS;

            double aLR = (p2Stats.runtime_MS - p0Stats.runtime_MS) / 2.0;
            // double aLR=1.0;
            double thetaL = bR / aR;
            double thetaR = bL / aL;
            double theta2P1 = Math.abs(thetaL - thetaR) / (Math.abs(aL - aR) / 2);
            planDerivative.put(p1, theta2P1 / aLR);
            p0 = p1;

        }

        // planDerivative.put(p1, Double.MAX_VALUE);

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                return Double.compare(planDerivative.get(o2),
                        planDerivative.get(o1));
            }
        });

    }

    public void sortPlansByDerCost(ArrayList<Plan> skylinePlans) {


        final HashMap<Plan, Double> planDerivative = new HashMap<>();
        //sorted skylineplans already
        // Return the fastest for skylines with 2 or less points
        if (skylinePlans.size() <= 2) {
            return;
        }
        Plan p0 = skylinePlans.get(0);


        //keep the plans with min cost/time
        planDerivative.put(p0, Double.MAX_VALUE);
        planDerivative.put(skylinePlans.get(skylinePlans.size() - 1), Double.MAX_VALUE);

        // System.out.println( " ");
        for (int i = 1; i < skylinePlans.size() - 1; ++i) {
            Plan p1 = skylinePlans.get(i);

            Plan p2 = skylinePlans.get(i + 1);

            Statistics p0Stats = p0.stats;
            Statistics p1Stats = p1.stats;
            Statistics p2Stats = p2.stats;
            //System.out.println(p1Stats.quanta+ " "+p1Stats.money);
            double aR = p0Stats.money - p1Stats.money;
            double bR = p1Stats.runtime_MS - p0Stats.runtime_MS;
            double aL = p2Stats.money - p1Stats.money;
            double bL = p2Stats.runtime_MS - p1Stats.runtime_MS;

            double aLR = (p2Stats.runtime_MS - p0Stats.runtime_MS) / 2.0;//double aLR = (p2Stats.money - p0Stats.money)/2.0;//
            // double aLR=1.0;
            double thetaL = bR / aR;
            double thetaR = bL / aL;
            double theta2P1 = Math.abs(thetaL - thetaR) / (Math.abs(aL - aR) / 2);
            planDerivative.put(p1, theta2P1 / aLR);
            p0 = p1;

        }

        // planDerivative.put(p1, Double.MAX_VALUE);

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                return Double.compare(planDerivative.get(o2),
                        planDerivative.get(o1));
            }
        });

    }


}