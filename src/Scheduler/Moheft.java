package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Check;

import java.util.*;

/**
 * Created by johnchronis on 2/19/17.
 */
public class Moheft implements Scheduler {

    public SolutionSpace space;
    public Cluster cluster;
    public DAG graph;

    ///rankingks

    public  LinkedList<Long> opsSorted ;



    protected int skylinePlansToKeep =20;//10//20
    protected int skylinePruningOption = 2;

    public int maxContainers = 10000000;

    public boolean backfilling = false;

    private HashMap<Long, Integer> opLevel = new HashMap<>(); //opid->level


    public Moheft(DAG graph,Cluster cl){
//        skylinePlans = new ArrayList<>();
//        paretoPlans = new ArrayList<>();
        space = new SolutionSpace();
        this.graph = graph;
        this.cluster = cl;
        this.opsSorted = new LinkedList<>();
    }



    @Override
    public SolutionSpace schedule() {
        long startCPU = System.currentTimeMillis();

        SolutionSpace skylinePlans = new SolutionSpace();

        final HashMap<Long, Double> rankU = new HashMap<>();
        TopologicalSorting topOrder = new TopologicalSorting(graph);

        for (Long opId : topOrder.iteratorReverse()) {
            double maxRankChild=0.0;
            Operator op = graph.getOperator(opId);
            for (Edge outEdge: graph.getChildren(opId)) {
                Long ChildId = outEdge.to;
                double comCostChild = Math.ceil(outEdge.data.size_B / RuntimeConstants.distributed_storage_speed_B_MS);
                //assumptions for output data and communication cost
                maxRankChild = Math.max(maxRankChild, comCostChild+rankU.get(ChildId));
            }

            double wcur=0.0;
            for(containerType contType: containerType.values())
                wcur+=op.resourcesRequirements.runtime_MS/contType.container_CPU;
            int types= containerType.values().length;
            double w=wcur/(double)types;//average execution cost for operator op
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

        if (maxContainers == 1) {
            this.onlyOneContainer();
        } else {

            skylinePlans.addAll(createAssignments());
        }
        // Create solution space

        for (Plan plan : skylinePlans.results) { // TODO ilia check if this is needed

            if (plan == null) {continue;}

            HashMap <containerType, Double> avgOpTime=new HashMap <containerType, Double>();
            HashMap <containerType, Integer> opNumber=new HashMap <containerType, Integer>();

            for (Long opId: plan.assignments.keySet()) {
                Long contId = plan.assignments.get(opId);
                //System.out.println(plan.getAssignments().get(opAss).getOpID() +" processTime "+ plan.getAssignments().get(opAss).processTime+ " runtime "+graph.getOperator(opID).getRunTime_SEC()+ " "+plan.getAssignments().get(opAss).contType);
                containerType cType =  plan.cluster.getContainer(contId).contType;

                int ops = 0;
                double opProcessTime = plan.opIdtoStartEndProcessing_MS
                    .get(opId).b - plan.opIdtoStartEndProcessing_MS.get(opId).a;

                if(opNumber.get(cType)==null) {
                    ops = 1;
                    avgOpTime.put(cType, opProcessTime);
                }
                else {
                    ops = opNumber.get(cType);
                    double processTime = avgOpTime.get(cType)*ops+opProcessTime;
                    ops++;
                    avgOpTime.put(cType, processTime/ops);

                }
                opNumber.put(cType, ops);

                //  System.out.println(opID+" processTime "+ plan.getAssignments().get(opAss).processTime +" contType "+ plan.getAssignments().get(opAss).contType+" contID "+ plan.getAssignments().get(opAss).container+" starts "+plan.activeAssignments.get(opAss).start_SEC +" ends "+ plan.activeAssignments.get(opAss).end_SEC);///plan.getAssignments().get(opAss).contType.container_CPU);
            }


            space.add(plan);
        }
        long endCPU = System.currentTimeMillis();
        space.setOptimizationTime(endCPU - startCPU);
//        space.plot("space");

        return space;
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
        planDerivative.put(skylinePlans.get(skylinePlans.size()-1), Double.MAX_VALUE);

        // System.out.println( " ");
        for (int i = 1; i < skylinePlans.size()-1; ++i) {
            Plan p1 = skylinePlans.get(i);

            Plan p2 = skylinePlans.get(i+1);

            Statistics p0Stats = p0.stats;
            Statistics p1Stats = p1.stats;
            Statistics p2Stats = p2.stats;
            //System.out.println(p1Stats.getTimeInQuanta()+ " "+p1Stats.getMoneyInQuanta());
            double aR = p0Stats.money - p1Stats.money;
            double bR = p1Stats.quanta - p0Stats.quanta;
            double aL = p2Stats.money - p1Stats.money;
            double bL = p2Stats.quanta - p1Stats.quanta;

            //double aLR = (p2Stats.getMoneyInQuanta() - p0Stats.getMoneyInQuanta())/2.0;//double aLR = (p2Stats.getTimeInQuanta() - p0Stats.getTimeInQuanta())/2.0;
            double aLR=1.0;
            double thetaL = bR/aR;
            double thetaR = bL/aL;
            double theta2P1 = Math.abs(thetaL - thetaR)/(Math.abs(aL-aR)/2);
            planDerivative.put(p1, theta2P1/aLR);
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

    private SolutionSpace createAssignments() {
        Plan firstPlan = new Plan(graph, cluster);

        SolutionSpace skylinePlans = new SolutionSpace();
        skylinePlans.add(firstPlan);

        int operAssigned = 0;

        for (Long opId : opsSorted) {
            operAssigned++;

            SolutionSpace allCandidates = new SolutionSpace();

            for (Plan plan : skylinePlans) {
                if (plan == null) {
                    continue;
                }

                getCandidateContainers(opId, plan, allCandidates);
            }
            skylinePlans.clear();


            // Compute new skyline
            skylinePlans = computeSkyline(allCandidates);


            // Prune the skyline ...
//            if (skylinePlans.size() > skylinePlansToKeep && skylinePruningOption == 1) {
//                // Keep only some schedules in the skyline according to their crowding distance
//
//                int schedulesKept = 0;
//                double alpha = 0.50;//not used, it might not make sense
//
//                final HashMap<Plan, Double> planDistance = new HashMap<>();
//
//                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
//                    @Override public int compare(Plan o1, Plan o2) {
//                        return Double.compare(o1.stats.quanta, o2.stats.quanta);
//                    }
//                });
//                for (int p = 0; p < skylinePlans.size(); ++p) {
//                    if (p == 0 || p == skylinePlans.size() - 1) {
//                        planDistance.put(skylinePlans.results.get(p), Double.MAX_VALUE);
//                        // System.out.printf("p %d makespan %f\n", p, skylinePlans.get(p).stats.quanta);
//                    } else {
//                        int makespan_prev = skylinePlans.results.get(p - 1).stats.quanta;
//                        int makespan_next = skylinePlans.results.get(p + 1).stats.quanta;
//                        planDistance.put(skylinePlans.results.get(p), alpha * (makespan_next - makespan_prev));
//                    }
//                }
//
//                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
//                    @Override public int compare(Plan o1, Plan o2) {
//                        return Double.compare(o1.stats.money, o2.stats.money);
//                    }
//                });
//                for (int p = 0; p < skylinePlans.size(); ++p) {
//                    if (p == 0 || p == skylinePlans.size() - 1) {
//                        planDistance.put(skylinePlans.results.get(p), Double.MAX_VALUE);
//                    } else {
//                        Double money_prev = skylinePlans.results.get(p - 1).stats.money;
//                        Double money_next = skylinePlans.results.get(p + 1).stats.money;
//                        planDistance.put(skylinePlans.results.get(p),
//                            planDistance.get(skylinePlans.results.get(p)) + (1 - alpha) * (double) (
//                                money_next - money_prev));
//                    }
//                }
//
//                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
//                    @Override public int compare(Plan o1, Plan o2) {
//                        return Double.compare(planDistance.get(o2), planDistance.get(o1));
//                    }
//                });
//
//                for (int p = 0; p < skylinePlans.size(); ++p) {
//                    if (p < skylinePlansToKeep) {
//                        ++schedulesKept;
//                    } else
//                        skylinePlans.results.set(p, null);
//                }
//
//                Check.True(schedulesKept <= skylinePlansToKeep + 1,
//                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
//            }

            if (skylinePlans.size() > skylinePlansToKeep && skylinePruningOption == 2) {
                // Keep only some schedules in the skyline according to their crowding distance

                int schedulesKept = 0;
                final HashMap<Plan, Double> planDistance = new HashMap<>();

                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
                    @Override public int compare(Plan o1, Plan o2) {
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
                    @Override public int compare(Plan o1, Plan o2) {
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
                    @Override public int compare(Plan o1, Plan o2) {
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

//            if (skylinePlans.size() > skylinePlansToKeep && skylinePruningOption == 3) {
//                // Keep only some schedules in the skyline
//                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
//                    @Override public int compare(Plan o1, Plan o2) {
//                        return Double.compare(o1.stats.runtime_MS, o2.stats.runtime_MS);
//                    }
//                });
//                int schedulesKept = 2;
//                int windowSize =
//                    (int) Math.ceil((skylinePlans.size() - 2.0) / (skylinePlansToKeep - 2.0));
//                for (int p = 1; p < skylinePlans.size() - 1; ++p) {
//                    if (p % windowSize != 0) {
//                        skylinePlans.results.set(p, null);
//                    } else {
//                        ++schedulesKept;
//                    }
//                }
//                Check.True(Math.abs(schedulesKept - skylinePlansToKeep) <= skylinePlansToKeep / 2,
//                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
//            }



//            if (skylinePlans.size() > skylinePlansToKeep && skylinePruningOption == 4) {
//                // Keep only some schedules in the skyline according to their crowding distance
//
//                int schedulesKept = 0;
//
//                sortPlansByDer(skylinePlans.results);
//
//                for (int p = 0; p < skylinePlans.size(); ++p) {
//                    if (p < skylinePlansToKeep) {
//                        ++schedulesKept;
//                    } else {
//                        skylinePlans.results.set(p, null);
//                        //    skylinePlans.remove(p);
//                    }
//
//                }
//
//
//
//                Collections.sort(skylinePlans.results, new Comparator<Plan>() {
//                    @Override public int compare(Plan o1, Plan o2) {
//                        double a = Double.MAX_VALUE;
//                        double b = Double.MAX_VALUE;
//                        if (o1 != null)
//                            a = o1.stats.runtime_MS;
//
//                        if (o2 != null)
//                            b = o2.stats.runtime_MS;
//
//                        return Double.compare(a, b);
//
//                    }
//                });
//
//
//                Check.True(schedulesKept <= skylinePlansToKeep + 1,
//                    "Error. Schedules kept: " + schedulesKept + " / " + skylinePlansToKeep);
//
//            }

        }
        return skylinePlans;
    }

    //output planEstimations
        //assume that not empty containers exist
    private void getCandidateContainers(Long opId, Plan plan, SolutionSpace planEstimations)
     {

        for(Long contId: plan.cluster.containers.keySet()){
            ///////////////
            Plan newPlan = new Plan(plan);
            newPlan.assignOperator(opId, contId, backfilling);
            planEstimations.add(newPlan);
            //////////////

        }
        if(plan.cluster.contUsed.size()<maxContainers){ //if no empty conts in plan add an empty and assign it
            for(containerType ctype: containerType.values()) {
                Plan newPlan = new Plan(plan);
                Long newContId = newPlan.cluster.addContainer(ctype);
                newPlan.assignOperator(opId, newContId, backfilling);
                planEstimations.add(newPlan);
            }
        }
     }

    public Plan onlyOneContainer() {
        containerType contType= containerType.getSmallest();//maybe check for every container later
        Plan plan = new Plan(graph,cluster);


        for (Operator op : graph.getOperators()) {
            plan.assignOperator(op.getId(), plan.cluster.getContainer(0L).id, backfilling);
        }
        return plan;
    }

    public SolutionSpace computeSkyline(SolutionSpace plans){
        SolutionSpace skyline = new SolutionSpace();


        plans.sort(false); // Sort by time breaking equality by sorting by money

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

}















