package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Check;

import java.util.*;

public class HEFT implements Scheduler {

    public SolutionSpace space;
    public Cluster cluster;
    public DAG graph;

    ///rankings

    public LinkedList<Long> opsSorted;

    public int maxContainers;
    public boolean backfilling = false;
    public containerType cType;

    private HashMap<Long, Integer> opLevel = new HashMap<>(); //opid->level


    public HEFT(DAG graph, Cluster cl, int maxContainers, containerType cType) {
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
        this.opsSorted = new LinkedList<>();
        this.maxContainers = maxContainers;
        this.cType = cType;

    }

//////create a new object every time you want to call schedule!!!!
    @Override public SolutionSpace schedule() {

        long startCPU = System.currentTimeMillis();

        SolutionSpace skylinePlans = new SolutionSpace();


        computerRankings();

        if (maxContainers == 1) {
            this.onlyOneContainer();
        } else {
            skylinePlans.add(createAssignments());
        }
        // Create solution space


        space.addAll(skylinePlans);

        long endCPU = System.currentTimeMillis();
        space.setOptimizationTime(endCPU - startCPU);
        space.print();


        return space;
    }

    public void computerRankings(){
        final HashMap<Long, Double> rankU = new HashMap<>();
        TopologicalSorting topOrder = new TopologicalSorting(graph);

        for (Long opId : topOrder.iteratorReverse()) {
            double maxRankChild = 0.0;
            Operator op = graph.getOperator(opId);
            for (Edge outEdge : graph.getChildren(opId)) {
                Long ChildId = outEdge.to;
                double comCostChild = Math.ceil(outEdge.data.size_B / RuntimeConstants.distributed_storage_speed_B_MS);
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild + rankU.get(ChildId));
            }

            double wcur = 0.0;
            for (containerType contType : containerType.values())
                wcur += op.resourcesRequirements.runtime_MS / contType.container_CPU;
            int types = containerType.values().length;
            double w = wcur / (double) types;//average execution cost for operator op
            rankU.put(opId, (w + maxRankChild));

        }

        for (Long opId : topOrder.iterator())
            opsSorted.add(opId);

        Comparator<Long> rankComparator = new Comparator<Long>() {
            @Override public int compare(Long op1, Long op2) {
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

    public void sortPlansByDer(ArrayList<Plan> skylinePlans) {


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
            //System.out.println(p1Stats.getTimeInQuanta()+ " "+p1Stats.getMoneyInQuanta());
            double aR = p0Stats.money - p1Stats.money;
            double bR = p1Stats.quanta - p0Stats.quanta;
            double aL = p2Stats.money - p1Stats.money;
            double bL = p2Stats.quanta - p1Stats.quanta;

            //double aLR = (p2Stats.getMoneyInQuanta() - p0Stats.getMoneyInQuanta())/2.0;//double aLR = (p2Stats.getTimeInQuanta() - p0Stats.getTimeInQuanta())/2.0;
            double aLR = 1.0;
            double thetaL = bR / aR;
            double thetaR = bL / aL;
            double theta2P1 = Math.abs(thetaL - thetaR) / (Math.abs(aL - aR) / 2);
            planDerivative.put(p1, theta2P1 / aLR);
            p0 = p1;

        }

        // planDerivative.put(p1, Double.MAX_VALUE);

        Collections.sort(skylinePlans, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(planDerivative.get(o2), planDerivative.get(o1));
            }
        });

    }

    private Plan createAssignments() {
        Plan firstPlan = new Plan(graph, cluster);

        ArrayList<Plan> skylinePlans = new ArrayList<>();
        skylinePlans.add(firstPlan);

        int operAssigned = 0;

        for (Long opId : opsSorted) {
            operAssigned++;

            SolutionSpace allCandidates = new SolutionSpace();

            scheduleToCandidateContainers(opId, firstPlan, allCandidates);

            firstPlan = keepFastest(allCandidates);

        }

        return firstPlan;
    }

    //output planEstimations
    //assume that not empty containers exist
    private void scheduleToCandidateContainers(Long opId, Plan plan, SolutionSpace planEstimations) {

        for (Long contId : plan.cluster.containers.keySet()) {
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId, backfilling);
            planEstimations.add(newPlan);
        }
        if (plan.cluster.contUsed.size() < maxContainers) { //if no empty conts in plan add an empty and assign it
            Plan newPlan = new Plan(plan);
            Long newContId = newPlan.cluster.addContainer(cType);
            newPlan.assignOperator(opId, newContId, backfilling);
            planEstimations.add(newPlan);
        }
    }

    public Plan onlyOneContainer() {
        cluster.addContainer(cType);
        Plan plan = new Plan(graph, cluster);

        for (Operator op : graph.getOperators()) {
            plan.assignOperator(op.getId(), plan.cluster.getContainer(0L).id, backfilling);
        }
        return plan;
    }

    public Plan keepFastest(SolutionSpace plans) {
       Plan p ;

       plans.sort(); // Sort by time breaking equality by sorting by money

       return plans.results.get(0);
    }
}

