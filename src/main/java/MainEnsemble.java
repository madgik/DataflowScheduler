package main.java;

import main.java.Graph.DAG;
import main.java.Graph.parsers.PegasusDaxParser;
import main.java.Lattice.LatticeGenerator;
import main.java.Simulator.SimEnginge;
import main.java.utils.*;
import main.java.Scheduler.*;

import java.util.ArrayList;
import java.util.HashMap;


public class MainEnsemble {

    static Boolean validate = false;

    public static void main(String[] args) {
        String resources_path = "resources/";

        Integer ensembleSize = 20;
        Integer pruning_k = 10;
        String rankMethod = "dagMerge";
        boolean multiObjective = true;

        // arguments for revision
        Integer constraint_mode = 0;
        // constraint modes
        // 0 -> no constraints
        // 1 -> keep one plan after every scheduling step. This plan maximizes fairness and satisfies the money and time constraints
        // 2 -> keep one plan after scheduling is completed. At every intermediate step all the plan that satisfy the constraints are preserved.
        // 3 -> work as mode zero but in the last step after hetero plans are produced return one plan
        Double money_constraint = 0.0;
        Long time_constraint = 0L;

        if (args.length == 0) {
            System.out.println("No dataflows to schedule.");
            System.exit(0);
        } else if (args.length != 0) {
            rankMethod = args[0];
            multiObjective = Boolean.valueOf(args[1]);
            ensembleSize = Integer.parseInt(args[2]);
            if (args[3].equals("perHour"))
                RuntimeConstants.quantum_MS = RuntimeConstants.OneHour_MS;
            else
                RuntimeConstants.quantum_MS = RuntimeConstants.OneSec_MS;
            pruning_k = Integer.parseInt(args[4]);
            constraint_mode = Integer.parseInt(args[5]);
            money_constraint = Double.parseDouble(args[6]);
            time_constraint = Long.parseLong(args[7]);
        }

        ArrayList<Triple<String, Integer, Integer>> flowsandParasms = new ArrayList<>();

        // Read all dataflow files.
        if (args.length > 1) {
            for (int i = 8; i < 8 + 2 * ensembleSize; i += 2) {
                String appName = args[i];
                Integer size = Integer.parseInt(args[i + 1]);
                if (RuntimeConstants.quantum_MS == RuntimeConstants.OneHour_MS)
                    // 1000 and 1000 scale the running time and output size of the operators of the dataflow
                    flowsandParasms.add(
                            new Triple(resources_path + appName + ".n." + size + ".0.dax", 1000, 100));
                else
                    flowsandParasms.add(
                            new Triple(resources_path + appName + ".n." + size + ".0.dax", 1, 1));
            }
        }

        // Create the schedule
        ArrayList<Plan> ensemblePlans = new ArrayList<>();
        runMultipleFlows(flowsandParasms, ensemblePlans, rankMethod, multiObjective, pruning_k,
                constraint_mode, money_constraint, time_constraint);

        // Print the results
        System.out.format("%-12s\t%-12s\t%-12s\t%-12s\t%-20s\t%-12s\t%-12s\t%12s\n",
                "Plan",
                "Money",
                "Runtime_MS",
                "Unfairness",
                "UnfairnessNormalized",
                "AvgSlowdown",
                "AvgStretch",
                "MaxStretch");

        for (int j = 0; j < ensemblePlans.size(); ++j) {
            Plan p = ensemblePlans.get(j);
            System.out.format("%-12d\t%-12f\t%-12d\t%-12f\t%-20f\t%-12f\t%-12f\t%12f\n",
                    j,
                    p.stats.money,
                    p.stats.runtime_MS,
                    p.stats.unfairness,
                    p.stats.unfairnessNorm,
                    p.stats.subdagMeanSlowdown,
                    p.stats.subdagMeanResponseTime,
                    p.stats.subdagMaxResponseTime);
        }

        if (validate) {
            System.out.println("Running sims");
            SimEnginge simeng = new SimEnginge();
            for (Plan p : ensemblePlans) {
                simeng.execute(p);
            }

        }


    }


    private static DAG runMultipleFlows(ArrayList<Triple<String, Integer, Integer>> flowsandParasms,
                                        ArrayList<Plan> plans, String rankMethod, boolean multiObjective, int pruning_k,
                                        int constraint_mode, double money_constraint, long time_constraint) {
        System.out.println("Scheduling flows:");
        for (Triple<String, Integer, Integer> tr : flowsandParasms) {
            System.out.println(tr.a);
        }

        DAG graph = new DAG();
        ArrayList<DAG> graphs = new ArrayList<>();

        try {
            Long dagId = 0L;
            for (Triple<String, Integer, Integer> p : flowsandParasms) {
                dagId++;
                if (p.a.contains("lattice") || p.a.contains("main/java/Lattice")) {
                    double z = 1.0;
                    double randType = 0.0;
                    double[] runTime = {0.2, 0.4, 0.6, 0.8, 1.0};
                    double[] cpuUtil = {1.0};
                    double[] memory = {0.3};
                    double[] dataout = {0.2, 0.4, 0.6, 0.8, 1.0};

                    RandomParameters
                            params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);

                    graphs.add(LatticeGenerator.createLatticeGraph(p.b, p.c, params, 0, RuntimeConstants.quantum_MS));
                } else {
                    PegasusDaxParser parser = new PegasusDaxParser(p.b, p.c);
                    graphs.add(parser.parseDax(p.a, dagId));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Merge the separate DAG into a single one.
        for (DAG g : graphs) {
            HashMap<Long, Long> OldIdToNewId = new HashMap<>();
            graph.add(g, OldIdToNewId);
            graph.superDAG.addSubDAG(g, OldIdToNewId);
        }

        runDAG(graph, "", "multiple", plans,
                rankMethod, multiObjective, pruning_k, constraint_mode, money_constraint, time_constraint);

        return graph;
    }

    public static void runDAG(DAG graph, String paremetersToPrint, String type, ArrayList<Plan> hhdsPlans,
                              String rankMethod, boolean multiObjective, Integer pruning_k, int constraint_mode,
                              double money_constraint, long time_constraint) {
        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        SolutionSpace combined = new SolutionSpace();
        SolutionSpace paretoToCompare = execute(graph, true, "Knee", rankMethod, mpinfo, "",
                combined, type, multiObjective, pruning_k, constraint_mode, money_constraint, time_constraint);

        hhdsPlans.addAll(paretoToCompare.results);
    }

    public static SolutionSpace execute(DAG graph, boolean prune, String method, String rankMethod,
                                        MultiplePlotInfo mpinfo, String toprint, SolutionSpace combined, String type,
                                        Boolean multiObjective, Integer pruning_k, int constraint_mode,
                                        double money_constraint, long time_constraint) {
        SolutionSpace space;
        Cluster cluster = new Cluster();
        Scheduler sched = new hhdsEnsemble(graph, cluster, prune, method, rankMethod, multiObjective, pruning_k,
                constraint_mode, money_constraint, time_constraint);

        space = sched.schedule();
        combined.addAll(space);
        return space;
    }


}
