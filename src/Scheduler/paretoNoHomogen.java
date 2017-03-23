 package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.MultiplePlotInfo;
import utils.Pair;

import java.util.*;

/**
 * Created by johnchronis on 2/19/17.
 */
public class paretoNoHomogen implements Scheduler {

    public SolutionSpace space;
    public Cluster cluster;
    public DAG graph;


    public LinkedList<Long> opsSorted ;

    public int maxContainers = 10000000;

    public boolean backfilling = false;
    public boolean backfillingUpgrade = false;

    private HashMap<Long, Integer> opLevel = new HashMap<>(); //opid->level

    public paretoNoHomogen(DAG graph,Cluster cl){
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
        this.opsSorted = new LinkedList<>();
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

//        System.out.println("//////////DEC///////");
//        skylinePlans_DEC.print();
////        skylinePlans_DEC.plot("DEC");
//        mpinfo.add("DEC",skylinePlans_DEC.results);
//
//        System.out.println("//////////INC///////");
//        skylinePlans_INC.print();
////        skylinePlans_INC.plot("INC");
//        mpinfo.add("INC",skylinePlans_INC.results);
//
//        System.out.println("//////////INCDEC///////");
//        skylinePlans_INCDEC.print();
////        skylinePlans_INCDEC.plot("INCDEC");
//        mpinfo.add("INCDEC",skylinePlans_INCDEC.results);



        skylinePlans.addAll(skylinePlans_DEC.results);
        skylinePlans.addAll(skylinePlans_INC.results);
        skylinePlans.addAll(skylinePlans_INCDEC.results);

//        System.out.println("//////////ALL///////");
//        skylinePlans.sort();
//        skylinePlans.print();

        paretoPlans.addAll(computeSkyline(skylinePlans));



//        System.out.println("//////////PARETO///////");
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

        paretoPlans.clear();



        paretoPlans.addAll(homoToHetero(skylinePlans)); //returns only hetero


        paretoPlans.addAll(skylinePlans);

        space.addAll(computeSkyline(paretoPlans));

//        SolutionSpace beforeMigrate = new SolutionSpace();
//        beforeMigrate.addAll(computeSkyline(paretoPlans));


        //space.addAll(beforeMigrate);
//        for(Plan p:beforeMigrate){
//            space.addAll(migrateCriticalOpsToConts(p));
//        }


//
//        MultiplePlotInfo mp = new MultiplePlotInfo();
//        mp.add("befMigrate", beforeMigrate.results);
//        mp.add("afterMigrate", space.results);
//        plotMultiple(mp,"migration");

    //    space.addAll(computeSkyline(paretoPlans));
//moh


        long endCPU_MS = System.currentTimeMillis();
        space.setOptimizationTime(endCPU_MS - startCPU_MS);

//        System.out.println("//////////RESULT///////");

        space.print();
//        space.plot("space");
        mpinfo.add("final space",space.results);



//        plotMultiple(mpinfo);
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

                Double opSpareTime = (double) plan.opIdtoStartEndProcessing_MS
                    .get(opChildId).a - plan.opIdtoStartEndProcessing_MS.get(opId).b;//assumption: output data and communication cost computed in runtime
                opSlackTime = Math.min(childSlack + opSpareTime, opSlackTime);
            }
            long opContID = plan.assignments.get(opId);//consider successor at the container. If last op at container the returned bit (succStart) is -1
            long contEndTime_MS = plan.cluster.getContainer(opContID).UsedUpTo_MS;
            long opEndTime_MS = plan.opIdtoStartEndProcessing_MS.get(opId).b;
            if(contEndTime_MS>opEndTime_MS) {
                long succStart = Long.MAX_VALUE;
                boolean succExists = false;
                for (Long opopId : plan.opIdtoStartEndProcessing_MS.keySet()) {
                    Pair<Long, Long> pair = plan.opIdtoStartEndProcessing_MS.get(opopId);
                    Long contcontId = plan.assignments.get(opopId);
                    long opopStartTime = plan.opIdtoStartEndProcessing_MS.get(opopId).a;

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

            for (final Plan plan : plansInner) {                                                                         //for every plan

                LinkedList<Long> planContainersTobeModified = new LinkedList<>();

                //compute avg slack per container/VM
                computeSlack(plan,contSlack,contOps,opSlack);


                for (Long i: plan.cluster.containers.keySet()) {                                                           // for each cont change it
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

                for (Long k: planContainersTobeModified) {            //for every cont that can be modified, create a new plan

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

                    if (plan.vmUpgrading.equals("increasing"))                                      //modify the container -- TODO check if we could add all smaller and bigger conts
                        newPlan.cluster.update(cont.id,containerType.getNextLarger(cont.contType));
                    else
                        newPlan.cluster.update(cont.id,containerType.getNextSmaller(cont.contType));



                    int opsAssigned=0;                                                              //assign all the ops again to the new plan
                    HashSet<Long> opsAssignedSet = new HashSet<>();
                    HashSet<Long> readyOps = new HashSet<>();

                    findRoots(readyOps);

                    while ( readyOps.size() > 0) {//iterate on the ready to schedule ops

                        opsAssigned++;
                        long nextOpID = nextOperator(readyOps);
                        Operator nextOp = graph.getOperator(nextOpID);
//                        System.out.println("\nHomoToHetero scheduling "+nextOpID + " "+readyOps.toString());

                        newPlan.assignOperator(nextOpID,plan.assignments.get(nextOpID),backfillingUpgrade);

                        findNextReadyOps(readyOps,opsAssignedSet,nextOpID);
                    }

                    //use Double.compare
                    if(newPlan.stats.money >= plan.stats.money && newPlan.stats.runtime_MS >= plan.stats.runtime_MS)//we could use a threshold. e.g. if savings less than 0.1%
                    {
                        break; //no more containers for this plan are going to be modified
                    }
                    skylinePlansNew.add(newPlan);
//
//                     System.out.println("OLDPLAN ");
//                    plan.printInfo();
//                    System.out.println("NEWPLAN");
//                     newPlan.printInfo();
//
                }

            }

            plansInner.clear();
            plansInner.addAll(result.results);


            SolutionSpace modifiedPlans=new SolutionSpace();
            SolutionSpace skylineToModify = computeSkyline(skylinePlansNew);
            for(Plan pToChange: skylinePlansNew)//skylineToModify) //
                modifiedPlans = migrateCriticalOpsToConts(pToChange);
            if(modifiedPlans!=null)
            skylinePlansNew.addAll(modifiedPlans);

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

    private void findRoots(HashSet<Long> readyOps){
        for(Long opid:graph.operators.keySet()){
            if( graph.getParents(opid).size()==0){
                readyOps.add(opid);
            }
        }
    }

    private void findNextReadyOps(HashSet<Long> readyOps,HashSet<Long> opsAssignedSet, Long justScheduledOpId ){
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

    private void findNextReadyOps(HashSet<Long> readyOps,HashSet<Long> readyOpsInner,HashSet<Long> opsAssignedSet, Plan plan ){


        plan.calculateOrderingofOperatorsInContainers();

        if(readyOps.size() == 0){
            findRoots(readyOpsInner);
        }else {
            for(Long opId: readyOps){
                opsAssignedSet.add(opId);
                findNextReadyOps(readyOpsInner, opsAssignedSet, opId);
            }
            readyOps.clear();
        }
        //find the ready operators from plan container ordering



        ArrayList<Long> firstUnscheduledOp = new ArrayList<>();

        for(Stack<Long> s: plan.contIdToSortedOps.values()){ //update the sorted operators
            if(s.size()>0) {
                if (opsAssignedSet.contains(s.peek())) {
                    s.pop();
                }
            }
        }
        //find the ready operators from plan container ordering
        for(Stack<Long> s: plan.contIdToSortedOps.values()){ //update the sorted operators
            if(s.size()>0) {
                firstUnscheduledOp.add(s.peek());
            }
        }

        //intersect firsUnscheduled with readyOps

        for(Long opId: firstUnscheduledOp){
            if(readyOpsInner.contains(opId)){
                readyOps.add(opId);
            }
        }


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

        findRoots(readyOps);

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

//                System.out.println("\nnewly created plans");
                scheduleToCandidateContainers(nextOpID, plan, cType,allCandidates);//allCanditates is an out param
            }
            plans.clear();

//            for(Plan p:allCandidates){
//                p.printInfo();
//            }

//            if(prevPrune+3 == opsAssigned){
//                plans = computeSkyline(allCandidates);
//                prevPrune=opsAssigned+3;
//            }else{
//                plans = new ArrayList<>(allCandidates);
//            }
            plans = computeSkyline(allCandidates);

//            System.out.println("skyline");
//            for(Plan p:plans){
//                p.printInfo();
//            }
            findNextReadyOps(readyOps,opsAssignedSet,nextOpID);

        }

        return plans;
    }

    private void scheduleToCandidateContainers(Long opId , Plan plan,  containerType contType,SolutionSpace planEstimations){
        //assume that not empty containers exist

        for(Long contId: plan.cluster.containers.keySet()){ //add to every existing container
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId,backfilling);
            planEstimations.add(newPlan);

//            newPlan.printAssignments();
        }
        if(plan.cluster.contUsed.size()<maxContainers){  //add a nwe container of contType and assign the op to that
//            for(containerType ctype: contType) {//uncomment to add every ctype
                Plan newPlan = new Plan(plan);
                Long newContId = newPlan.cluster.addContainer(contType);
                newPlan.assignOperator(opId, newContId, backfilling);
                planEstimations.add(newPlan);

//                newPlan.printAssignments();
//            }
        }


        //return planEstimations;
    }

    public Plan onlyOneContainer() {
        containerType contType= containerType.getSmallest();//maybe check for every container later
        Plan plan = new Plan(graph,cluster);

        plan.cluster.addContainer(contType.getSmallest());
        plan.vmUpgrading = "increasing";

        for (Operator op : graph.getOperators()) {
            plan.assignOperator(op.getId(), plan.cluster.getContainer(0L).id,backfilling);
        }
        plan.printAssignments();
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
       final HashMap<Long, Double> b_rank = new HashMap<>(); //opidTobRank
        final HashMap<Long, Double> t_rank = new HashMap<>();
        final HashMap<Long, Double> sum_rank = new HashMap<>();
        final HashMap<Long, Double> slacktime = new HashMap<>();
        final  LinkedList<Long> opsSumRankSorted = new LinkedList<>();
        final LinkedList<Long> opsBySlack = new LinkedList<>();
///   private HashMap<Long, Double> opSlack = new HashMap<>();

        final TopologicalSorting topOrder = new TopologicalSorting(graph);

        HashMap<Integer, Integer> opLevelperLevel = new HashMap <>();
        HashMap<Integer,ArrayList<Long>> opLevelList = new HashMap<>();


        int numLevels=0;
        for(int i=0;i<20;++i){
            opLevelList.put(i,new ArrayList<Long>());
        }

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

            opLevelList.get(level).add(opId);

             System.out.println("op "+ opId + " level " +level);

        }

        for(Integer opop:opLevelList.keySet()){
            System.out.print(opop+ ": ");
            for(Long opopop:opLevelList.get(opop)){
                System.out.print(opopop+", ");
            }
            System.out.println("");
        }

        for(Long opop:graph.operators.keySet()){
            System.out.print(opop+ ": ");
            for(Edge e:graph.getParents(opop)){
                System.out.print(e.from+", ");
            }
            System.out.println("");
        }

        Double crPathLength=0.0;
        for (Long opId : topOrder.iteratorReverse()) {
            double maxRankChild=0.0;
            for (Edge childEdge: graph.getChildren(opId)) {
                double comCostChild = 0.0;
                for(Edge parentofChildEdge: graph.getParents(childEdge.to)) {
                    if(parentofChildEdge.from==opId) {
                        comCostChild = Math.ceil(parentofChildEdge.data.size_B / RuntimeConstants.network_speed_B_MS);
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
//                Operator opParent=graph.getOperator(inLink.from.getopID());
                double comCostParent = Math.ceil(inLink.data.size_B / RuntimeConstants.network_speed_B_MS);
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
//        Collections.sort(opsSorted, rankComparator);




        Comparator<Long> sumrankComparator = new Comparator<Long>() {
            @Override
            public int compare(Long op1, Long op2) {
                double r1 = sum_rank.get(op1);
                double r2 = sum_rank.get(op2);
                if (r1 < r2)//TODO: add precision error
                    return -1;
                else if (r1 > r2)
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

//        System.out.println("sorting");
//        for(Long op: opsSorted)
//            System.out.println(op);
////
//        Comparator<Operator> slackComparator = new Comparator<Operator>() {
//            @Override
//            public int compare(Operator op1, Operator op2) {
//                double r1 = slacktime.get(op1);
//                double r2 = slacktime.get(op2);
//                if (r1 < r2)//TODO: add precision error
//                    return -1;
//                else if (r1 > r2)
//                    return 1;
//                else
//                    return 0;
//            }
//        };
//        // Collections.sort(opsBySlack, slackComparator);
//
//







    }

    public HashMap<Long, Long> computeLST(Plan plan) {

        HashMap <Long, Long> opLST = new HashMap<>();

//        System.out.println("\nfor plan makespan " + plan.stats.runtime_MS );
//        for(Long opId: opsSortedReversed())
//        System.out.println("op " + opId + " starts " + plan.opIdtoStartEndProcessing_MS.get(opId).a + " finishes " + plan.opIdtoStartEndProcessing_MS.get(opId).b + " at vm " + plan.assignments.get(opId));

//        System.out.println("lst");
        plan.printAssignments();
        for(Long opId: opsSortedReversed()) {
            Long lst = Long.MAX_VALUE;


            //also checks succ at container
            long succStart = Long.MAX_VALUE;
            Long succId = null;
            Long contId = plan.assignments.get(opId);
//            plan.printInfo();
            for(Long nextOpId: plan.contAssignments.get(contId)) {
                if(plan.opIdtoStartEndProcessing_MS.get(nextOpId).a<succStart && plan.opIdtoStartEndProcessing_MS.get(nextOpId).a > plan.opIdtoStartEndProcessing_MS.get(opId).a) {
                    succStart = plan.opIdtoStartEndProcessing_MS.get(nextOpId).a;
                    succId = nextOpId;
                }

            }

            if(succId!=null)
                lst = Math.min(lst, succStart - plan.opIdToBeforeDTDuration_MS.get(succId));


            if (graph.getChildren(opId).isEmpty()) { //if exit node
//                System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId));

                lst = plan.stats.runtime_MS - plan.opIdToProcessingTime_MS.get(opId);
            }else {
                for (Edge outEdge : graph.getChildren(opId)) {
                    succId = outEdge.to;

                    Long succStartTime =  plan.opIdtoStartEndProcessing_MS.get(succId).a;

                    long templst = succStartTime - (plan.calculateDelayDistributedStorage(opId,succId));//plan.opIdToBeforeDTDuration_MS.get(succId);

//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + succId + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId) + " dt before succ: " + plan.opIdToBeforeDTDuration_MS.get(succId) + " starting at " + succStartTime);

//TODO: add somewhere +1 for data transfer? It starts at the next interval every time...
                    lst = Math.min(templst - plan.calculateDelayDistributedStorage(opId,succId)  - plan.opIdToProcessingTime_MS.get(opId), lst);
                    //lst = Math.min(succStartTime - plan.opIdToAfterDTDuration_MS.get(opId) - plan.opIdToProcessingTime_MS.get(opId), lst);
                }
            }




//            System.out.println(opId + " lst " + lst);

            opLST.put(opId, lst);
        }

        return opLST;
    }


    public HashMap<Long, Long> computeLST(Plan plan, Long opToAssignId) {

        HashMap <Long, Long> opLST = new HashMap<>();

//        System.out.println("\nfor plan makespan " + plan.stats.runtime_MS );
//        for(Long opId: opsSortedReversed())
//            System.out.println("op " + opId + " starts " + plan.opIdtoStartEndProcessing_MS.get(opId).a + " finishes " + plan.opIdtoStartEndProcessing_MS.get(opId).b + " at vm " + plan.assignments.get(opId));

        for(Long opId: opsSortedReversed()) {


            if(opToAssignId==opId)
                break;
            Long lst = Long.MAX_VALUE;
            if (graph.getChildren(opId).isEmpty()) { //if exit node
//                System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId));

                lst = plan.stats.runtime_MS - plan.opIdToProcessingTime_MS.get(opId);
            }else {
                for (Edge outEdge : graph.getChildren(opId)) {
                    Long succId = outEdge.to;

                    Long succStartTime =  plan.opIdtoStartEndProcessing_MS.get(succId).a;

                    long templst = succStartTime - (plan.calculateDelayDistributedStorage(opId,succId));//plan.opIdToBeforeDTDuration_MS.get(succId);

//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + succId + " dt after op: " + plan.opIdToAfterDTDuration_MS.get(opId) + " dt before succ: " + plan.opIdToBeforeDTDuration_MS.get(succId) + " starting at " + succStartTime);

//TODO: add somewhere +1 for data transfer? It starts at the next interval every time...
                    lst = Math.min(templst - plan.calculateDelayDistributedStorage(opId,succId)  - plan.opIdToProcessingTime_MS.get(opId), lst);
                    //lst = Math.min(succStartTime - plan.opIdToAfterDTDuration_MS.get(opId) - plan.opIdToProcessingTime_MS.get(opId), lst);
                }
            }

//            System.out.println(opId + " so lst " + lst);

            opLST.put(opId, lst);



        }

        return opLST;
    }

    public HashMap<Long,Long> computeEST(Plan plan, Long opToAssignId){//TODO: when predID and opID at same container do not include dataTransfer time from predID to opID opIdToBeforeDTDuration_MS.get(predId)?
        HashMap <Long, Long> opEST = new HashMap<>();


        for(Long opId: opsSorted){



            Long est = Long.MIN_VALUE;


            //also checks succ at container
            long predEndTime = Long.MIN_VALUE;
            Long predId = null;
            Long contId = plan.assignments.get(opId);
            for(Long nextOpId: plan.contAssignments.get(contId)) {
                if(plan.opIdtoStartEndProcessing_MS.get(nextOpId).b>predEndTime && plan.opIdtoStartEndProcessing_MS.get(nextOpId).b <= plan.opIdtoStartEndProcessing_MS.get(opId).a) {
                    predEndTime = plan.opIdtoStartEndProcessing_MS.get(nextOpId).b;
                    predId = nextOpId;
                }

            }

            if(predId!=null)
                est = Math.max(est, predEndTime + plan.opIdToBeforeDTDuration_MS.get(contId));

            if(graph.getParents(opId).isEmpty()){
                est = 0L;
            }else{
                for(Edge inEdge:graph.getParents(opId)){
                    predId = inEdge.from;
//TODO: remove 2* and add after/before once. calculateDelay returns 0 if not parent child
                    predEndTime = plan.opIdtoStartEndProcessing_MS.get(predId).b +
                            //opIdToAfterDTDuration_MS.get(predId) +//This dt is computed only for parents not vm pred even if it was to be included
                            2*(plan.calculateDelayDistributedStorage(predId,opId,plan.assignments.get(opId)));//plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: do we need any +/-1?


//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + predId + " dt after pred: " + plan.opIdToAfterDTDuration_MS.get(predId) + " finishing at " + predEndTime);

                    est = Math.max(predEndTime,est);
                }
            }
            opEST.put(opId,est);
//            System.out.println(opId + " est " + est);

            if(opToAssignId==opId)
                break;
        }
        return opEST;
    }


    public HashMap<Long,Long> computeEST(Plan plan){//TODO: when predID and opID at same container do not include dataTransfer time from predID to opID opIdToBeforeDTDuration_MS.get(predId)?
        HashMap <Long, Long> opEST = new HashMap<>();

        for(Long opId: opsSorted){
            Long est = Long.MIN_VALUE;
            if(graph.getParents(opId).isEmpty()){
                est = 0L;
            }else{
                for(Edge inEdge:graph.getParents(opId)){
                    Long predId = inEdge.from;

                    Long predEndTime = plan.opIdtoStartEndProcessing_MS.get(predId).b +
                        //opIdToAfterDTDuration_MS.get(predId) +//This dt is computed only for parents not vm pred even if it was to be included
                        2*(plan.calculateDelayDistributedStorage(predId,opId,plan.assignments.get(opId)));//plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: do we need any +/-1?


//                    System.out.println(opId + " runtime " + plan.opIdToProcessingTime_MS.get(opId) + " dt from " + opId + " to " + predId + " dt after pred: " + plan.opIdToAfterDTDuration_MS.get(predId) + " finishing at " + predEndTime);

                    est = Math.max(predEndTime,est);
                }
            }
            opEST.put(opId,est);
//            System.out.println(opId + " est " + est);
        }
        return opEST;
    }

    //output opSlack,opSortedBySlack
    public HashMap<Long, Long> computeSlackOps(Plan plan, final HashMap<Long,Long> opSlack,ArrayList<Long> opSortedBySlack){
        HashMap<Long, Long> opLST = computeLST(plan);
        HashMap<Long, Long> opEST = computeEST(plan);

        for(Long opId : graph.operators.keySet()){
            Long LST = opLST.get(opId);
            Long EST = opEST.get(opId);
            opSlack.put(opId,LST-EST);
            opSortedBySlack.add(opId);
          //  System.out.println(opId + " slack " + (LST-EST));
        }

        Collections.sort(opSortedBySlack, new Comparator<Long>() {
            @Override public int compare(Long o1, Long o2) {
                return (int) (opSlack.get(o1) - opSlack.get(o2));
            }
        });
        return opEST;
    }


//    public SolutionSpace migrateOperatorToOtherContainer(Plan p){
//        System.out.println("MIGRATE///////////////////");
//        p.printInfo();
//        p.printAssignments();
//        System.out.println("MIGRATE1///////////////////");
//
//        //for every plan get the most critical ops and move it, then
//        // get the next critical ops and move that and so on
//
//        SolutionSpace plans = new SolutionSpace();
//        plans.add(p);
//        SolutionSpace newPlans = new SolutionSpace();
//
//        SolutionSpace results  = new SolutionSpace();
//
//        Long oldContId;
//        containerType oldContType;
//        Slot newSlot = null;
//
//        HashMap<Long, Long> opEST;
//        HashMap<Long, Long> opLST;
//
//        while(plans.size()>0) {
//
//            for (Plan plan : plans) {
//                //  System.out.println( plan.cluster.containers.size());
//                if(plan.cluster.containers.size()<=1)//TODO: as long as we do not consider using a new container
//                    continue;
//
//
//                HashMap<Long, Long> opSlack = new HashMap<>();
//                ArrayList<Long> opSortedBySlack = new ArrayList<>();
//
//                computeSlackOps(p, opSlack, opSortedBySlack);
//
//                for (Long opId : opSortedBySlack) {
//
//                    newSlot = null;
//                    System.out.println("slack " + opId + " " + opSlack.get(opId));
//
//                    oldContId = plan.assignments.get(opId);
//
//                    oldContType = plan.cluster.getContainer(oldContId).contType;
//                    //for every other cont see if it is eligible
//                    for (Long contId : plan.cluster.containers.keySet()) {//TODO: an idea is to sort containers as well in this step and break sooner
//                        if (contId == oldContId)
//                            continue;//break;//TODO: break or continue?
//                        Container newCont = plan.cluster.getContainer(contId);
//                       /*if (!isSmaller(oldContType, newCont.contType)) {
//                           continue;//break;//TODO: break or continue?
//                       }*/
//                        Collections.sort(newCont.freeSlots);
//                        opEST = computeEST(plan);//if plan is updated, it needs to be computed every time a new op is migrated
//                        opLST = computeLST(plan);
//
//
//                        for (Slot fs : newCont.freeSlots) {
//                            Long newstartTime = Long.MAX_VALUE;
//                            //find first free slot that can host the operator
//                            //TODO: add a condition to prune assignments worst than the current
//                            //                        Long newRuntimeNODT = (long) Math.ceil(graph.getOperator(opId).getRunTime_MS() / newCont.contType.container_CPU);
//                            //                        Long networkDelay = plan.calculateNetworkDelay(opId,contId);
//                            Long opProcessingDuration_MS = plan.opIdToProcessingTime_MS.get(opId);
//                            Long earliestStartTime_MS = plan.opIdToearliestStartTime_MS.get(opId);//TODO: difference between opEST and this?
//                            Long beforeDTDuration_MS = plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: add plan.calculateDelayDistributedStorage(opId,...) instaed?
//
//                            Long LET = Long.MAX_VALUE;
//                            for(Edge childEge :graph.getChildren(opId)){
//                                Long childId = childEge.to;
//                                LET = Math.min(LET,      );
//                            }
//
//
//                            if (fs.end_MS - fs.start_MS >= opProcessingDuration_MS
//                                    && earliestStartTime_MS + beforeDTDuration_MS   <= fs.end_MS - opProcessingDuration_MS
//                                    ){
//                                if(earliestStartTime_MS+beforeDTDuration_MS+opProcessingDuration_MS+afterDT <= min(startTimeChildren))
//
//
//
//                                    newSlot = fs;
//
//
//                            }
//                        }
//                        //create new plan with the new assignment
//                        if (newSlot != null) {
////
////                           Plan newPlan = new Plan(graph, new Cluster());
////                           newPlan.vmUpgrading = plan.vmUpgrading;
////                           for (Container contcont : plan.cluster.containersList) {
////                               newPlan.cluster.addContainer(contcont.contType);
////                           }
////
////                           //assign all the ops again to the new plan
////                           HashSet<Long> opsAssignedSet = new HashSet<>();
////                           HashSet<Long> readyOps = new HashSet<>();
////
////                           findRoots(readyOps);
////
////                           while (readyOps.size() > 0) {//iterate on the ready to schedule ops
////
////                               long nextOpID = nextOperator(readyOps);
////
////                               if (opId == nextOpID) {
////                                   newPlan.assignOperator(nextOpID, contId, false);
////                               } else {
////                                   newPlan.assignOperator(nextOpID, plan.assignments.get(nextOpID),
////                                       false);
////                               }
////
////
////                               findNextReadyOps(readyOps, opsAssignedSet, nextOpID);
////                           }
//                            System.out.println("it is " + plan.cluster.containers.get(contId).contType);
//                            //  plan.cluster.getContainer(contId);
//                            newPlans.add(migrateOperator(plan, opId, contId, newSlot, opEST.get(opId)));//TODO: opEST or plan.opIdToearliestStartTime_MS?
//                            System.out.println("MIGRATE11111111///////////////////");
//                            newPlans.results.get(0).printInfo();
//                            newPlans.results.get(0).printAssignments();
//                            System.out.println("MIGRATE22222222///////////////////");
////                           if(newPlan.stats.money<p.stats.money && newPlan.stats.runtime_MS < p.stats.runtime_MS)
////                           newPlans.add(newPlan);
//                        }
//
//                    }
//                }
//
//            }
//            //   System.out.println(newPlans.size());
//            results.addAll(plans);//TODO check this logic
//            plans.clear();
//            plans.addAll(newPlans);
//            newPlans.clear();
//        }
////        System.out.println("ending");
//        return results;
//    }

    public SolutionSpace migrateCriticalOpsToConts(Plan p) {//TODO: check if plan has different contTypes else break;

p.opsMigrated.clear();//        System.out.println("MIGRATE///////////////////");
//        p.printInfo();
//        p.printAssignments();
//        System.out.println("MIGRATE1///////////////////");

        //for every plan get the most critical ops and move it, then
        // get the next critical ops and move that and so on

        SolutionSpace plans = new SolutionSpace();

        plans.add(p);
        SolutionSpace newPlans = new SolutionSpace();

        SolutionSpace results  = new SolutionSpace();

        Long oldContId;
        containerType oldContType;
        Slot newSlot = null;

        HashMap<Long, Long> opEST;
        HashMap<Long, Long> opLST;

        if(p.stats.containersUsed==1 || p.cluster.countTypes.size() ==1)//TODO: || p.stats. types of vms ==1 do not migrate ops but only to diff types
        return results;

    //    if(plans.size()>0) {
//
//            LinkedList opsMigrated = new LinkedList();
         //   for (Plan plan : plans) {


                //  System.out.println( plan.cluster.containers.size());



                HashMap<Long, Long> opSlackInitial = new HashMap<>();
                ArrayList<Long> opsToMigrateOrdered = new ArrayList<>();


                computeSlackOps(p, opSlackInitial, opsToMigrateOrdered);

//                int opsMigrated=0;
                for (Long opId : opsToMigrateOrdered) {

                    for (Plan plan : plans) {


//                        System.out.println("next plan - migration");
//                        plan.printInfo();

                        HashMap<Long, Long> opSlack = new HashMap<>();
                        ArrayList<Long> opSortedBySlack = new ArrayList<>();

//                        plan.printInfo();
//                        plan.printAssignments();
                        computeSlackOps(plan, opSlack, opSortedBySlack);


                        if(plan.stats.containersUsed==1 || plan.cluster.countTypes.size() ==1)//TODO: || p.stats. types of vms ==1 do not migrate ops but only to diff types
                            continue;
//                        if(p.cluster.containers.size()<=1)//TODO: as long as we do not consider using a new container
//                            return null;

                    if ( opSlack.get(opId) > 0) {//plan.opsMigrated.size()> 0.10*opSortedBySlack.size() &&
                        continue;
                    }

                    if (plan.opsMigrated.contains(opId))
                        continue;
                    else
                        plan.opsMigrated.add(opId);
//                    opsMigrated++;
//                    if(opsMigrated>0.10*opSortedBySlack.size())
//                        break;


                    newSlot = null;
                    //System.out.println("slack " + opId + " " + opSlack.get(opId));

                    oldContId = plan.assignments.get(opId);

                    oldContType = plan.cluster.getContainer(oldContId).contType;
                    //for every other cont see if it is eligible. TODO: sort by util and break loop when no savings
                    for (Long contId : plan.cluster.containers.keySet()) {//TODO: an idea is to sort containers as well in this step and break sooner
                        if (contId == oldContId)
                            continue;
                        Container newCont = plan.cluster.getContainer(contId);

                        if (!containerType.isSmaller(oldContType, newCont.contType)) {
                            continue;
                        }
                        Collections.sort(newCont.freeSlots);

                        //create a tmpPlan with the new assignment of the operator (startTime is not determined yet)

                        Plan tPlan = new Plan(plan);
                        tPlan.assignments.put(opId, contId);
//                       tPlan.cluster.getContainer(contId).opsschedule.add(new Slot(opId,newSlot.start_MS,newSlot.end_MS));
                        Slot torm = null;
                        for (Slot s : tPlan.cluster.getContainer(tPlan.assignments.get(opId)).opsschedule) {
                            if (s.opId == opId) {
                                torm = s;
                            }
                        }
                        tPlan.cluster.getContainer(tPlan.assignments.get(opId)).opsschedule.remove(torm);

                        opEST = computeEST(tPlan, opId);//if plan is updated, it needs to be computed every time a new op is migrated
                        opLST = computeLST(tPlan, opId);

                        for (Slot fs : newCont.freeSlots) {
//                            Long newstartTime = Long.MAX_VALUE;
                            //find first free slot that can host the operator
                            //TODO: add a condition to prune assignments worst than the current
                            //                        Long newRuntimeNODT = (long) Math.ceil(graph.getOperator(opId).getRunTime_MS() / newCont.contType.container_CPU);
                            //                        Long networkDelay = plan.calculateNetworkDelay(opId,contId);
                            Long opProcessingDuration_MS = plan.opIdToProcessingTime_MS.get(opId);
                            Long earliestStartTime_MS = plan.opIdToearliestStartTime_MS.get(opId);//TODO: difference between opEST and this?
                            Long beforeDTDuration_MS = plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: add plan.calculateDelayDistributedStorage(opId,...) instaed?


                            Long lft = Long.MAX_VALUE;
                            for (Edge childEge : graph.getChildren(opId)) {
                                Long childId = childEge.to;
                                lft = Math.min(lft, opLST.get(childId) - 2 * tPlan.calculateDelayDistributedStorage(opId, childId, contId, tPlan.assignments.get(childId)));
                            }

                            if (fs.start_MS + opProcessingDuration_MS <= lft && fs.start_MS >= earliestStartTime_MS) {
                                newSlot = fs;

                            }

                        }
                        //create new plan with the new assignment
                        if (newSlot != null) {
                            Plan newPlan = migrateOperator(plan, opId, contId, newSlot, opEST.get(opId));

                            if (newPlan.stats.money < p.stats.money || newPlan.stats.runtime_MS < p.stats.runtime_MS) {
                                newPlans.add(newPlan);//TODO: opEST or plan.opIdToearliestStartTime_MS?
                                //

                                System.out.println("old plan" + p.stats.money + " " + p.stats.runtime_MS);
                                p.printInfo();
                                p.printAssignments();
                                System.out.println("migrated op " + opId);
                                System.out.println("new plan " + newPlan.stats.money + " " + newPlan.stats.runtime_MS);
                                newPlan.printInfo();
                                newPlan.printAssignments();
                                //
                            }

                        }

                    }

                }
                    if(newPlans.size()>0)
                    results.addAll(newPlans);
//                    plans.clear();
                    plans.addAll(newPlans);
                    newPlans.clear();
                }

//                if(newPlans.size()>0) {
//                                plan.printInfo();
//                                System.out.println("New plans " + newPlans.size());
//                            }
//                            for(Plan pnew: newPlans)
//                                pnew.printInfo();


//            }
//

//            plans.addAll(newPlans);
//            newPlans.clear();
//            results.addAll(plans);//TODO check this logic, add only if better/different from older one
//            plans.clear();

     //   }
//        if(results.size()>0) {
//           // System.out.println("ending migrations");
//            for (Plan pc : results) {
//                pc.opsMigrated.clear();
//                pc.printInfo();
//            }
//            System.out.println("ending migrations");
//        }
        return results;
    }

//    public SolutionSpace migrateToFreeSlots(Plan p ) {
//        System.out.println("MIGRATE///////////////////");
//        p.printInfo();
//        p.printAssignments();
//        System.out.println("MIGRATE1///////////////////");
//
//        //for every plan get the most critical ops and move it, then
//        // get the next critical ops and move that and so on
//
//        SolutionSpace plans = new SolutionSpace();
//        plans.add(p);
//        SolutionSpace newPlans = new SolutionSpace();
//
//        SolutionSpace results  = new SolutionSpace();
//
//        Long oldContId;
//        containerType oldContType;
//        Slot newSlot = null;
//
//        HashMap<Long, Long> opEST;
//        HashMap<Long, Long> opLST;
//
//       if(plans.size()>0) {
//
//           for (Plan plan : plans) {
//
//               System.out.println("migration at plan");
//               plan.printInfo();
//               //  System.out.println( plan.cluster.containers.size());
//               if(plan.cluster.containers.size()<=1)//TODO: as long as we do not consider using a new container
//                   continue;
//
//
//               HashMap<Long, Long> opSlack = new HashMap<>();
//               ArrayList<Long> opSortedBySlack = new ArrayList<>();
//
//               computeSlackOps(p, opSlack, opSortedBySlack);
//
//               int opsMigrated=0;
//               for (Long opId : opSortedBySlack) {
//
//                   opsMigrated++;
//                   if(opsMigrated>0.10*opSortedBySlack.size())
//                       break;
//                   newSlot = null;
////                   System.out.println("slack " + opId + " " + opSlack.get(opId));
//
//                   oldContId = plan.assignments.get(opId);
//
//                   oldContType = plan.cluster.getContainer(oldContId).contType;
//                   //for every other cont see if it is eligible
//                   for (Long contId : plan.cluster.containers.keySet()) {//TODO: an idea is to sort containers as well in this step and break sooner
//                       if (contId == oldContId)
//                           continue;//break;//TODO: break or continue?
//                       Container newCont = plan.cluster.getContainer(contId);
//                       /*if (!isSmaller(oldContType, newCont.contType)) {
//                           continue;//break;//TODO: break or continue?
//                       }*/
//                       Collections.sort(newCont.freeSlots);
//
//
//
//                       //create a tmpPlan with the new assignment of the operator (startTime is not determined yet)
//
//                       Plan tPlan = new Plan(plan);
//                       tPlan.assignments.put(opId,contId);
////                       tPlan.cluster.getContainer(contId).opsschedule.add(new Slot(opId,newSlot.start_MS,newSlot.end_MS));
//                       Slot torm =null;
//                       for(Slot s:tPlan.cluster.getContainer(plan.assignments.get(opId)).opsschedule)
//                       {
//                           if(s.opId == opId){
//                               torm = s;
//                           }
//                       }
//                       tPlan.cluster.getContainer(plan.assignments.get(opId)).opsschedule.remove(torm);
//
//                       opEST = computeEST(tPlan, opId);//if plan is updated, it needs to be computed every time a new op is migrated
//                       opLST = computeLST(tPlan,opId);
//
//                       for (Slot fs : newCont.freeSlots) {
//                           Long newstartTime = Long.MAX_VALUE;
//                           //find first free slot that can host the operator
//                           //TODO: add a condition to prune assignments worst than the current
//                           //                        Long newRuntimeNODT = (long) Math.ceil(graph.getOperator(opId).getRunTime_MS() / newCont.contType.container_CPU);
//                           //                        Long networkDelay = plan.calculateNetworkDelay(opId,contId);
//                           Long opProcessingDuration_MS = plan.opIdToProcessingTime_MS.get(opId);
//                           Long earliestStartTime_MS = plan.opIdToearliestStartTime_MS.get(opId);//TODO: difference between opEST and this?
//                           Long beforeDTDuration_MS = plan.opIdToBeforeDTDuration_MS.get(opId);//TODO: add plan.calculateDelayDistributedStorage(opId,...) instaed?
//
//
//                           Long lft = Long.MAX_VALUE;
//                           for(Edge childEge :graph.getChildren(opId)){
//                                Long childId = childEge.to;
//                                lft = Math.min(lft,  opLST.get(childId) - 2*tPlan.calculateDelayDistributedStorage(opId, childId, contId, tPlan.assignments.get(childId)));
//                           }
//
//if(fs.start_MS+opProcessingDuration_MS<=lft && fs.start_MS>=earliestStartTime_MS)
//    newSlot = fs;
////
////                           Long LET = Long.MAX_VALUE;
////                           for(Edge childEge :graph.getChildren(opId)){
////                                Long childId = childEge.to;
////                                LET = Math.min(LET,      );
////                           }
////
////
////                           if (fs.end_MS - fs.start_MS >= opProcessingDuration_MS
////                               && earliestStartTime_MS + beforeDTDuration_MS   <= fs.end_MS - opProcessingDuration_MS
////                                   ){
////                               if(earliestStartTime_MS+beforeDTDuration_MS+opProcessingDuration_MS+afterDT <= min(startTimeChildren))
////
////
////
////                               newSlot = fs;
////
////
////                           }
//                       }
//                       //create new plan with the new assignment
//                       if (newSlot != null) {
//                           System.out.println("found");
////
////                           Plan newPlan = new Plan(graph, new Cluster());
////                           newPlan.vmUpgrading = plan.vmUpgrading;
////                           for (Container contcont : plan.cluster.containersList) {
////                               newPlan.cluster.addContainer(contcont.contType);
////                           }
////
////                           //assign all the ops again to the new plan
////                           HashSet<Long> opsAssignedSet = new HashSet<>();
////                           HashSet<Long> readyOps = new HashSet<>();
////
////                           findRoots(readyOps);
////
////                           while (readyOps.size() > 0) {//iterate on the ready to schedule ops
////
////                               long nextOpID = nextOperator(readyOps);
////
////                               if (opId == nextOpID) {
////                                   newPlan.assignOperator(nextOpID, contId, false);
////                               } else {
////                                   newPlan.assignOperator(nextOpID, plan.assignments.get(nextOpID),
////                                       false);
////                               }
////
////
////                               findNextReadyOps(readyOps, opsAssignedSet, nextOpID);
////                           }
////                           System.out.println("it is " + plan.cluster.containers.get(contId).contType);
//                         //  plan.cluster.getContainer(contId);
//                             newPlans.add(migrateOperator(plan, opId, contId, newSlot, opEST.get(opId)));//TODO: opEST or plan.opIdToearliestStartTime_MS?
//                           System.out.println("MIGRATE11111111///////////////////");
//                           newPlans.results.get(0).printInfo();
//                           newPlans.results.get(0).printAssignments();
//                           System.out.println("MIGRATE22222222///////////////////");
//                           plan.printInfo();
//                           plan.printAssignments();
////                           if(newPlan.stats.money<p.stats.money && newPlan.stats.runtime_MS < p.stats.runtime_MS)
////                           newPlans.add(newPlan);
//                       }
//
//                   }
//               }
//
//           }
//        //   System.out.println(newPlans.size());
//           results.addAll(plans);//TODO check this logic
//           plans.clear();
//           plans.addAll(newPlans);
//           newPlans.clear();
//       }
////        System.out.println("ending");
//        return results;
//    }

//

    private Plan migrateOperator(Plan plan, Long opId, Long contId, Slot newSlot, Long opEST) {



        Plan newPlan = new Plan(graph,new Cluster());

        Plan tPlan = new Plan(plan);

//        newPlan.assignments.remove(opId);
//        newPlan.assignments.put(opId, contId);
//

        newPlan.vmUpgrading = plan.vmUpgrading;
        for(Container contcont : plan.cluster.containersList){
            newPlan.cluster.addContainer(contcont.contType);
        }
//
//        for(Long i:plan.assignments.keySet()){
//            newPlan.assignments.put(i,plan.assignments.get(i));
//        }


        tPlan.contAssignments.get(tPlan.assignments.get(opId)).remove(opId);

        tPlan.assignments.put(opId,contId);
        //tPlan.contAssignments.get(plan.assignments.get(opId)).remove(opId);
        tPlan.contAssignments.get(contId).add(opId);

        if(newSlot.start_MS == 1644){
            System.out.println("aa");
        }
        tPlan.cluster.getContainer(contId).opsschedule.add(new Slot(opId,newSlot.start_MS,newSlot.end_MS));

        Slot torm = null;
        for(Slot s:tPlan.cluster.getContainer(tPlan.assignments.get(opId)).opsschedule)
        {
            if(s.opId == opId){
                torm = s;
            }
        }
        tPlan.cluster.getContainer(tPlan.assignments.get(opId)).opsschedule.remove(torm);
        tPlan.cluster.getContainer(tPlan.assignments.get(opId)).freeSlots.add(new Slot(torm.start_MS,torm.end_MS));

//        Container cont = newPlan.cluster.containers.get(k);
//
//        if (plan.vmUpgrading.equals("increasing"))                                      //modify the container -- TODO check if we could add all smaller and bigger conts
//            newPlan.cluster.update(cont.id,containerType.getNextLarger(cont.contType));
//        else
//            newPlan.cluster.update(cont.id,containerType.getNextSmaller(cont.contType));




//        Container cont = newPlan.cluster.containers.get(k);

       /* if (plan.vmUpgrading.equals("increasing"))                                      //modify the container -- TODO check if we could add all smaller and bigger conts
            newPlan.cluster.update(cont.id,containerType.getNextLarger(cont.contType));
        else
            newPlan.cluster.update(cont.id,containerType.getNextSmaller(cont.contType));
*/


        int opsAssigned=0;                                                              //assign all the ops again to the new plan
        HashSet<Long> opsAssignedSet = new HashSet<>();
        HashSet<Long> readyOps = new HashSet<>();
        HashSet<Long> readyOpsInner = new HashSet<>();

        findNextReadyOps(readyOps,readyOpsInner,opsAssignedSet, tPlan );

        System.out.println("migrate opID "+opId +"............." + tPlan.assignments.get(opId));

        HashSet<Long> checking = new HashSet<>();

        while(readyOps.size()>0) {

            for (Long nextOpID : readyOps) {//iterate on the ready to schedule ops
                if(checking.contains(nextOpID)){
                    System.out.println("aaaaaaa");
                }
                checking.add(nextOpID);


                    System.out.println(nextOpID+" ");
                if(nextOpID == 6){
                    System.out.println("EUREKA");
                }
                opsAssigned++;
//            long nextOpID = nextOperator(readyOps);
                Operator nextOp = graph.getOperator(nextOpID);
//                        System.out.println("\nHomoToHetero scheduling "+nextOpID + " "+readyOps.toString());

//                if (nextOpID == opId){
//                    newPlan.assignOperator(nextOpID, contId, backfillingUpgrade);
//                }else {
                    newPlan.assignOperator(nextOpID, tPlan.assignments.get(nextOpID), backfillingUpgrade);
//                }

            }

            findNextReadyOps(readyOps, readyOpsInner, opsAssignedSet, tPlan);
        }

        System.out.println("EINAIISA?? "+opsAssigned+" "+graph.operators.size());
        if(opsAssigned != graph.operators.size()){
            System.out.println("NATO");
        }

        System.out.println("/////////migrateOperator()");
        plan.printAssignments();
        System.out.println("////////new plan at migrateOperator()");
        newPlan.printAssignments();
        return newPlan;
    }



    //        System.out.println("newslot for " + opId + " " + newSlot.start_MS + " " + newSlot.end_MS);
//        Plan newPlan  = new Plan(plan);
//
//
//        System.out.println("before removing");
//        newPlan.printInfo();
//        newPlan.printAssignments();
//
//        Long est = Long.MIN_VALUE;
//        if(graph.getParents(opId).isEmpty()){
//            est = 0L;
//        }else{
//            for(Edge inEdge:graph.getParents(opId)){
//                Long preId = inEdge.from;
//
//                Long preEndTime = newPlan.opIdtoStartEndProcessing_MS.get(preId).b;
//
//                est = Math.max(preEndTime,est);
//            }
//        }
//
//        Container oldCont = newPlan.cluster.getContainer(newPlan.assignments.get(opId));
//
//        Long OLDopStart = newPlan.opIdtoStartEndProcessing_MS.get(opId).a;
//        Long OLDopEnd  = newPlan.opIdtoStartEndProcessing_MS.get(opId).b;
//        Long OLDopContStart;
//
//        oldCont.freeSlots.add(new Slot(newPlan.opIdtoStartEndProcessing_MS.get(opId).a,newPlan.opIdtoStartEndProcessing_MS.get(opId).b));
//        Slot opsOldSlot = null;
//        for(Slot ops: oldCont.opsschedule){//finds the old slot of the op an removes it
//            if(ops.opId == opId){
//                opsOldSlot = ops;
//                OLDopContStart = ops.start_MS;
//            }
//        }
//        oldCont.opsschedule.remove(opsOldSlot);
//
//        ///old cont cleaned/////
//
//        newPlan.opIdtoStartEndProcessing_MS.remove(opId);
//        newPlan.opIdToProcessingTime_MS.remove(opId);
//        newPlan.opIdToContainerRuntime_MS.remove(opId);
//        newPlan.opIdToearliestStartTime_MS.remove(opId);
//        newPlan.contAssignments.get(oldCont.id).remove(opId);
//        ////plan cleaned/////
//
//        System.out.println("after removing");
// newPlan.printInfo();
// newPlan.printAssignments();
//
//
//
//
//
//        ///move to change the assignment/////
//
//        newPlan.assignments.put(opId,contId);
//
//        newPlan.contAssignments.get(contId).add(opId);
//
//        Container newCont = newPlan.cluster.getContainer(contId);
//
//        newCont.freeSlots.remove(newSlot);
//
//        Long actualContStartTime = newSlot.start_MS;
//        if(opEST>actualContStartTime){ //add possible free slot before op start
//            actualContStartTime = opEST;
//            newCont.freeSlots.add(new Slot(newSlot.start_MS,actualContStartTime-1));
//        }
//
//
//
//        long startTime_MS = actualContStartTime;
//        long timeNow_MS = actualContStartTime;
//        long startContTime_MS = actualContStartTime;
//
//        System.out.println(contId);
//
//        Container cont = cluster.getContainer(contId);
//
//        System.out.println(cont.contType);
//        ////////////DATA TRANSFER TIMES//////////////////
//
//        long networkDelay_MS = 0;
//        for (Edge link : graph.getParents(opId)) {
//            long fromId = link.from;
//            long fromContId = newPlan.assignments.get(fromId);
//            if (fromContId != contId) {
//
//                long dtTime_MS =
//                    (long) (Math.ceil(link.data.size_B / RuntimeConstants.network_speed_B_MS));
//
//                networkDelay_MS += dtTime_MS;
//            }
//        }
//
//        timeNow_MS += networkDelay_MS;
//        startContTime_MS = timeNow_MS;         //cont runtime starts now
//
//
//        Operator op = graph.getOperator(opId);
//        long runTime = (int) Math.ceil(op.getRunTime_MS() / cont.contType.container_CPU);
//        timeNow_MS += runTime;
//
//        long endTime_MS = timeNow_MS;
//
//        long runspan = endTime_MS - startTime_MS;
//
//        long runspanCont = endTime_MS - startContTime_MS;
//
//
//
//
//        newPlan.opIdToearliestStartTime_MS.put(opId, startTime_MS);
//
//        newPlan.opIdtoStartEndProcessing_MS.put(opId, new Pair<>(startTime_MS, endTime_MS)); //runtime + disk times + network delay
//
//        newPlan.opIdToContainerRuntime_MS.put(opId,endTime_MS - startTime_MS);
//
//        newPlan.opIdToProcessingTime_MS.put(opId,endTime_MS - startContTime_MS);
//
//        cont.setUsedUpTo(endTime_MS);
//        cont.startofUse_MS = Math.min(cont.startofUse_MS,startContTime_MS);    //set the start of use at the container
//
//        cluster.contUsed.add(contId);
//
//        cont.opsschedule.add(new Slot(opId, startContTime_MS, endTime_MS)); //add a new scheduled slot for the operator
//
//        newPlan.stats = new Statistics(newPlan);
//
//
//        //add to newCont.opschedule
//
//        //add possible free slot before op start
//
//
//        ///find all ops with start time after opID and change them

    //        System.out.println("migrateop");
    //   newPlan.printInfo();
//        newPlan.printAssignments();




//    private void computeHomoRankings(containerType cType)
//    {
//        final TopologicalSorting topOrder = new TopologicalSorting(graph);
//
//        final HashMap<Long, Double> rankU = new HashMap<>();
//
//        for (Long opId : topOrder.iteratorReverse()) {
//
//            double maxRankChild=0.0;
//            for (Edge outLink: graph.getChildren(opId)) {
//                double comCostChild = Math.ceil(outLink.data.size_B / RuntimeConstants.network_speed_B_MS);
//                //assumptions for output data and communication cost
//                maxRankChild = Math.max(maxRankChild, comCostChild+rankU.get(outLink.to));
//            }
//
//            double w=graph.getOperator(opId).getRunTime_MS()/cType.container_CPU;
//            rankU.put(opId, (w+maxRankChild));
//
//        }
//        for (Long opId : topOrder.iterator())
//            opsSorted.add(opId);
//
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
//        Collections.sort(opsSorted, rankComparator);
//    }
    public Iterable<Long> opsSortedReversed() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return opsSorted.descendingIterator();
            }
        };
    }

}
