import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import Lattice.LatticeGenerator;
import Scheduler.*;
import Simulator.SimEnginge;
import utils.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;


//arguments for a small example: dagMerge ensemble20SMontageLigoSecConstraints/ true 2 perSec 10 1 13500 60000 MONTAGE 50 LIGO 100

public class MainEnsemble {

    static Boolean savePlot = true;
    static Boolean showPlot = true;
    static String pathPlot;
    static String pathOut;
    static Boolean showOutput = false;
    static Boolean saveOutput = true;
    static Boolean validate = false;
    static boolean jar = false;
    static boolean runningAtServer = false;
    static String jarpath = "";
    static String resourcePath = "";

    public static void main(String[] args) {

        Integer ensembleSize = 20;
        Integer pruning_k = 10;
        String newDir = "ensemblesDec2017/MixedEnsemble4Ligo100Montage50/dagMergeTrue/";
        // arguments for revision
        Integer constraint_mode = 0;
        // constraing modes
        // 0 -> no constraints
        // 1 -> keep one plan after every scheduling step. This plan maximizes fairness and satisfies the money and time constraints
        // 2 -> keep one plan after scheduling is completed. At every intermediate step all the plan that satisfy the constraints are preserved.
        // 3 -> work as mode zero but in the last step after hetero plans are produced return one plan
        Double money_constraint = 0.0;
        Long time_constraint = 0L;

        String rankMethod = "dagMerge";
        boolean multiObjective = true;

        if (args.length > 1) {
            ensembleSize = Integer.parseInt(args[3]);
            rankMethod = args[0];
            newDir = args[1];
            multiObjective = Boolean.valueOf(args[2]);
            if (args[4].equals("perHour"))
                RuntimeConstants.quantum_MS = RuntimeConstants.OneHour_MS;
            else//perSec
                RuntimeConstants.quantum_MS = RuntimeConstants.OneSec_MS;

            pruning_k = Integer.parseInt(args[5]);
            constraint_mode = Integer.parseInt(args[6]);
            money_constraint = Double.parseDouble(args[7]);
            time_constraint = Long.parseLong(args[8]);
        }

        String dir = newDir;
        pathPlot = dir;//"./ensembles/LigoEnsemble4MixedSizes/";//sizeBased
        pathOut = dir;// "./ensembles/LigoEnsemble4MixedSizes/";//userPref

        createDir(pathPlot, "");
        String OS = System.getProperty("os.name").toLowerCase();
        if (savePlot) {
            System.out.println("saving plots to " + pathPlot);
        }
        if (saveOutput) {
            System.out.println("saving output to " + pathOut);
        }
        if (System.getProperty("user.name").equals("gsmyris")) {
            jar = true;
            jarpath = "/home/gsmyris/jc/";
            resourcePath = "/home/gsmyris/jc2018/";
        }
        if (System.getProperty("user.name").equals("chronis")) {
            jar = true;
            jarpath = "/home/chronis/jc/";
            resourcePath = "/home/chronis/jc2018/";
        }
        if (System.getProperty("user.name").equals("vaggelis")) {
            jar = true;
            jarpath = "/home/vaggelis/jcfiles/jc/";
            resourcePath = "/home/vaggelis/jcfiles/jc2018/";

        }
        if (OS.indexOf("mac") >= 0) {
            jar = true;
            jarpath = "/Users/chronis/code/MyScheduler/resources/";
            resourcePath = "/Users/chronis/code/MyScheduler/resources/";
        }

        ArrayList<Triple<String, Integer, Integer>> flowsandParasms = new ArrayList<>();

        PrintWriter outEnsemble = null;

        PrintWriter outPlanDetails = null;

        try {

            String fileName = "ensemble";

            String filePlanDetails = "planDetails";

            outEnsemble = new PrintWriter(pathOut + fileName + ".dat");
            outPlanDetails = new PrintWriter(pathOut + filePlanDetails + ".txt");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (args.length > 1) {
            for (int i = 9; i < 9 + 2 * ensembleSize; i += 2) {

                String appName = args[i];
                Integer size = Integer.parseInt(args[i + 1]);

                if (RuntimeConstants.quantum_MS == RuntimeConstants.OneHour_MS)
                    flowsandParasms.add(new Triple(jarpath + appName + ".n." + size + ".0.dax", 1000, 100));
                else
                    flowsandParasms.add(new Triple(jarpath + appName + ".n." + size + ".0.dax", 1, 1));

            }

        }

        System.out.println("running multiple dataflows");

        ArrayList<Plan> ensemblePlans = new ArrayList<>();
        DAG graph = runMultipleFlows(jar, flowsandParasms, ensemblePlans, rankMethod, multiObjective, pruning_k,
                constraint_mode, money_constraint, time_constraint);


        outEnsemble.println("money\truntime_MS\tavgSlowdown\t\tavgStretch\tmaxStretch\tunfairness\tunfairnessNorm");

        for (int j = 0; j < ensemblePlans.size(); ++j) {

            Plan p = ensemblePlans.get(j);

            outEnsemble.println(p.stats.money + "\t" + p.stats.runtime_MS + "\t" + p.stats.subdagMeanSlowdown + "\t" + p.stats.subdagMeanResponseTime + "\t" + "\t" + p.stats.subdagMaxResponseTime + " \t" + p.stats.unfairness + "\t" + p.stats.unfairnessNorm);


            System.out.println("\nplan " + j + " runtime money unfairness meanResponse minResponse maxResponse meanMakespan minMakespan maxMakespan: ");
            System.out.println(" " + p.stats.runtime_MS + "\t" + p.stats.money + "\t" + p.stats.unfairness + "\t" + p.stats.subdagMeanResponseTime + "\t" + p.stats.subdagMinResponseTime + "\t" + p.stats.subdagMaxResponseTime + "\t" + p.stats.subdagMeanMakespan + "\t" + p.stats.subdagMinMakespan + "\t" + p.stats.subdagMaxMakespan);

            outPlanDetails.println("\nplan " + j + " runtime money unfairnessNorm ");
            outPlanDetails.println(" " + p.stats.runtime_MS + "\t" + p.stats.money + "\t" + p.stats.unfairnessNorm);
            for (Long dgId : p.stats.subdagFinishTime.keySet()) {
                System.out.println("dag " + dgId + " responseTime " + p.stats.subdagResponseTime.get(dgId) + " makespan " + p.stats.subdagMakespan.get(dgId) + " starts " + p.stats.subdagStartTime.get(dgId) + " ends " + p.stats.subdagFinishTime.get(dgId));
                outPlanDetails.println("dag " + dgId + " responseTime " + p.stats.subdagResponseTime.get(dgId) / p.graph.superDAG.getSubDAG(dgId).computeCrPathLength(new containerType[]{p.cluster.containersList.get(0).contType}) + " makespan " + p.stats.subdagMakespan.get(dgId) + " starts " + p.stats.subdagStartTime.get(dgId) + " ends " + p.stats.subdagFinishTime.get(dgId) + " slowdown " + p.stats.subdagSlowdown.get(dgId));

            }
        }


        outEnsemble.close();
        outPlanDetails.close();


        if (validate) {
            System.out.println("Running sims");
            SimEnginge simeng = new SimEnginge();
            for (Plan p : ensemblePlans) {
                simeng.execute(p);
            }

        }


    }

    public static SolutionSpace execute(DAG graph, boolean prune, String method, String rankMethod,
                                        MultiplePlotInfo mpinfo, String toprint, StringBuilder sbOut,
                                        SolutionSpace combined, String type, Boolean multiObjective, Integer pruning_k,
                                        int constraint_mode, double money_constraint, long time_constraint) {
        SolutionSpace space = new SolutionSpace();

        Cluster cluster = new Cluster();

        Scheduler sched = new hhdsEnsemble(graph, cluster, prune, method, rankMethod, multiObjective, pruning_k, constraint_mode,
                    money_constraint, time_constraint);//"dagMerge";//commonEntry:default, perDag, dagMerge

        space = sched.schedule();

        sbOut.append(space.toString());

        combined.addAll(space);

        return space;
    }


    public static void runDAG(DAG graph, String paremetersToPrint, String type, ArrayList<Plan> hhdsPlans,
                              String rankMethod, boolean multiObjective, Integer pruning_k, int constraint_mode,
                              double money_constraint, long time_constraint) {

        StringBuilder sbOut = new StringBuilder();

        sbOut.append("Running " + type + " " + paremetersToPrint + " Pareto, Moheft").append("\n");

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        SolutionSpace combined = new SolutionSpace();
        SolutionSpace paretoToCompare = execute(graph, true, "Knee", rankMethod, mpinfo, "Hetero",
                sbOut, combined, type, multiObjective, pruning_k, constraint_mode, money_constraint, time_constraint);

        hhdsPlans.addAll(paretoToCompare.results);


        String addToFilename = "_NPRUNE_";

//        boolean moheft = false;


        System.out.println("paretoDone");

        sbOut.append("nodes " + graph.getOperators().size() + " edges " + graph.sumEdges()).append("\n");
        sbOut.append(paremetersToPrint + "  sumDataGB " + (graph.sumdata_B / 1073741824)).append("\n");
        sbOut.append("pareto " + type + " time(sec) -> " + paretoToCompare.optimizationTime_MS / 1000).append("\n");

        ArrayList<Pair<String, Double>> legendInfo = new ArrayList<>();

        double ccr = graph.computeCCR();

        String filesname =
                type + addToFilename +
                        "___" + paremetersToPrint.replace(" ", "_") +
                        "_sumDataGB_" + (graph.sumdata_B / 1073741824) + "_ccr_" + ccr + "__" +
                        (new java.util.Date()).toString().replace(" ", "_");


        legendInfo.add(new Pair<String, Double>("data/comp (ccr)", ccr));

        if (showOutput) {
            System.out.println(sbOut.toString());
        }
        if (saveOutput) {
            PrintWriter out = null;
            try {
                out = new PrintWriter(pathOut + filesname + ".txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            out.println(sbOut.toString());
            out.close();

        }

    }




    private static DAG runMultipleFlows(boolean jar, ArrayList<Triple<String, Integer, Integer>> flowsandParasms, ArrayList<Plan> plans,
                                        String rankMethod, boolean multiObjective, int pruning_k, int constraint_mode,
                                        double money_constraint, long time_constraint) {

        System.out.println("runinng multFLow");
        for (Triple<String, Integer, Integer> tr : flowsandParasms) {
            System.out.println(tr.a + " " + tr.b + " " + tr.c);
        }

        DAG graph = new DAG();
        ArrayList<DAG> graphs = new ArrayList<>();

        try {
            int ensembleSize = flowsandParasms.size();
            Long dagId = 0L;
            int did = 0;
            for (Triple<String, Integer, Integer> p : flowsandParasms) {
                dagId++;
                did++;
                if (p.a.contains("lattice") || p.a.contains("Lattice")) {

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
                    if (jar) {
                        graphs.add(parser.parseDax(p.a, dagId));

                    } else {
                        graphs.add(parser.parseDax(MainEnsemble.class.getResource(p.a).getFile(), dagId));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (DAG g : graphs) {
            HashMap<Long, Long> OldIdToNewId = new HashMap<>();
            graph.add(g, OldIdToNewId);
            graph.superDAG.addSubDAG(g, OldIdToNewId);
            //graphMerged.addSubDAG(g);
        }


        runDAG(graph, " multipleFlows +sumdata:" + graph.sumdata_B / 1073741824, "multiple", plans,
                rankMethod, multiObjective, pruning_k, constraint_mode, money_constraint, time_constraint);

        return graph;
    }


    private static String createDir(String basePath, String dirName) {
        File dir = new File(basePath, dirName);
        if (!dir.mkdirs()) {
            System.out.print("existing DIR " + basePath + " " + dirName);
            //System.exit(1);
        }
        return dir.getAbsolutePath() + "/";
    }

}
