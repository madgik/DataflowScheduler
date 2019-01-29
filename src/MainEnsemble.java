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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


//java -jar MyScheduler.jar dagMerge ensemblesRankComparison/example/weightByDag/ 10 MONTAGE 100 MONTAGE 100 MONTAGE 100 MONTAGE 100 MONTAGE 100 MONTAGE 100 LIGO 100 LIGO 100 LIGO 100 LIGO 100

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

    //   static Boolean useMoheft = false;


    public static void main(String[] args) {


//        if(jar)
//        jarpath = "/home/ilia/IdeaProjects/MyScheduler/runRemotely/";
        Integer ensembleSize = 20;
        Integer pruning_k = 10;
        String newDir = "ensemblesDec2017/MixedEnsemble4Ligo100Montage50/dagMergeTrue/";
        // arguments for revision
        Integer constraint_mode = 0;
        // constraing modes
        // 0 -> no constraints
        // 1 -> keep one plan after every scheduling step. This plan maximizes fairness and satisfies the money and time constraints
        // 2 -> keep one plan after scheduling is completed. At every intermediate step all the plan that satisfy the constraints are preserved.
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

        String dir = newDir;//"ensemblesRankComparison/MixedEnsemble4Ligo100Montage50/weightByDag/";//"ensemblesRankComparison/MixedEnsemble4Ligo50Montage100/slackByDag/";


        pathPlot = dir;//"./ensembles/LigoEnsemble4MixedSizes/";//sizeBased
        pathOut = dir;// "./ensembles/LigoEnsemble4MixedSizes/";//userPref

        createDir(pathPlot, "");

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

                String appName = "MONTAGE";
                Integer randomSize = random.randomInRange(2, 0);
                //Integer sizesMontage[] = {50, 50, 50};//{100, 100, 100};//{25, 25, 25};//
                //Integer sizesLigo[] = {100, 100, 100};//{50, 50, 50};//{25, 25, 25};//
                Integer size = 100;


                appName = args[i];//"LIGO";
                size = Integer.parseInt(args[i + 1]);//sizesMontage[randomSize];

                if (RuntimeConstants.quantum_MS == RuntimeConstants.OneHour_MS)
                    flowsandParasms.add(new Triple(jarpath + appName + ".n." + size + ".0.dax", 1000, 100));
                else
                    flowsandParasms.add(new Triple(jarpath + appName + ".n." + size + ".0.dax", 1, 1));

            }

        }
// else {
//            for (int i = 1; i <= ensembleSize; i ++) {
//
//                String appName = "MONTAGE";
//                Integer randomSize = random.randomInRange(2, 0);
//                Integer sizesMontage[] = {50, 50, 50};//{100, 100, 100};//{25, 25, 25};//
//                Integer sizesLigo[] = {100, 100, 100};//{50, 50, 50};//{25, 25, 25};//
//                Integer size = 100;
//
//                if (i % 2 == 1) {
//                    appName = "LIGO";//
//                    size = sizesLigo[randomSize];
//                } else {
//                    appName = "MONTAGE";//"LIGO"; //
//                    size = sizesMontage[randomSize];
//                }
//
//                flowsandParasms.add(new Triple(jarpath + appName + ".n." + size + ".0.dax", 1000, 100));
//
//            }
//        }

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


    //TODO: Run the simulation to validate the results for the space of solutions
    // }


    public static SolutionSpace execute(DAG graph, boolean prune, String method, String rankMethod,
                                        MultiplePlotInfo mpinfo, String toprint, StringBuilder sbOut,
                                        SolutionSpace combined, String type, Boolean multiObjective, Integer pruning_k,
                                        int constraint_mode, double money_constraint, long time_constraint) {
        SolutionSpace space = new SolutionSpace();

        Cluster cluster = new Cluster();

        Scheduler sched;
        //  if(type.equals("multiple"))
        // int pruning_k = 10;
        //method="moheft";
        if (rankMethod.equals("moheft")) {
            sched = new Moheft(graph, cluster, pruning_k);
        } else
            sched = new hhdsEnsemble(graph, cluster, prune, method, rankMethod, multiObjective, pruning_k, constraint_mode,
                    money_constraint, time_constraint);//"dagMerge";//commonEntry:default, perDag, dagMerge
        //  else
        //    sched = new hhds(graph,cluster,prune,method);

        space = sched.schedule();

        sbOut.append(space.toString());

        //  mpinfo.add(toprint+"("+space.size()+") "+space.optimizationTime_MS,space.results);

        // plotUtility plot = new plotUtility();

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

        //      Cluster clusterM = new Cluster();

        // Scheduler schedM = new Moheft(graph, clusterM);

        //     SolutionSpace solutionsM = new SolutionSpace();

//        if(moheft) {
//
//            solutionsM = schedM.schedule();
//
//            sbOut.append(solutionsM.toString());
//
//            mpinfo.add("moheft ("+solutionsM.size()+") " + (solutionsM.optimizationTime_MS), solutionsM.results);
//
//            combined.addAll(solutionsM);
//
//        }

//        //// Execute MOHEFT /////////////////////////
//        int pruning_k = 10;
//        Cluster clusterM = new Cluster();
//        Scheduler MOHEFT = new Moheft(graph, clusterM, pruning_k);
//        SolutionSpace MOHEFTSchedules = new SolutionSpace();
//
//        if (useMoheft) {
//            MOHEFTSchedules = MOHEFT.schedule();
//            sbOut.append(MOHEFTSchedules.toString());
//            //      mpinfo.add("moheft (" + MOHEFTSchedules.size() + ") " + (MOHEFTSchedules.optimizationTime_MS),
//            //        MOHEFTSchedules.results);
//            combined.addAll(MOHEFTSchedules);
//            System.out.println("MOHEFT Done");
//        }
//
//
//        hhdsPlans.clear();
//        hhdsPlans.addAll(MOHEFTSchedules.results);

        sbOut.append("nodes " + graph.getOperators().size() + " edges " + graph.sumEdges()).append("\n");
        sbOut.append(paremetersToPrint + "  sumDataGB " + (graph.sumdata_B / 1073741824)).append("\n");
        sbOut.append("pareto " + type + " time(sec) -> " + paretoToCompare.optimizationTime_MS / 1000).append("\n");
        //  sbOut.append("moheft " + type + " time(sec) -> " + solutionsM.optimizationTime_MS / 1000).append("\n");


//    combined.computeSkyline(false);

        double distMtoC = 0.0, distPtoC = 0.0, distCtoM = 0.0, distCtoP = 0.0;
        double JaccardMtoC = 0.0, JaccardPtoC = 0.0;

        ArrayList<Pair<String, Double>> legendInfo = new ArrayList<>();


//        try {

//        if(moheft) {
//            addImprovementsToLegend(solutionsM, paretoToCompare, legendInfo);
////
//            addDistanceToLegend(solutionsM, paretoToCompare, legendInfo);
//
//            distMtoC = computeDistance(solutionsM, combined).P2Sky;
//            legendInfo.add(new Pair<>("distMtoC", distMtoC));
//
//
//            distPtoC = computeDistance(paretoToCompare, combined).P2Sky;
//            legendInfo.add(new Pair<>("distPtoC", distPtoC));
//
////        distCtoM = computeDistance(combined,solutionsM).P2Sky;
////        legendInfo.add(new Pair<>("distCtoM",distCtoM));
////
////        distCtoP = computeDistance(combined,paretoToCompare).P2Sky;
////        legendInfo.add(new Pair<>("distCtoP",distCtoP));
//
//            JaccardMtoC = computeJaccard(solutionsM, combined);
//            legendInfo.add(new Pair<>("JaccMtoC", JaccardMtoC));
//
//            JaccardPtoC = computeJaccard(paretoToCompare, combined);
//            legendInfo.add(new Pair<>("JaccPtoC", JaccardPtoC));
//
//            sbOut.append("Jaccard from M to C " + JaccardMtoC).append("\n");
//            sbOut.append("Jaccard from P to C " + JaccardPtoC).append("\n");
//
//            sbOut.append("distance from M to C " + distMtoC).append("\n");
//            sbOut.append("distance from P to C " + distPtoC).append("\n");
////        sbOut.append("distance from C to M " + distCtoM).append("\n");
////        sbOut.append("distance from C to P " + distCtoP).append("\n");
//
//
//            legendInfo.add(new Pair<String, Double>("nodes", (double) graph.getOperators().size()));
//            legendInfo.add(new Pair<String, Double>("edges", (double) graph.sumEdges()));
//
//            //            System.out.println(solutions.optimizationTime_MS+" "+solutions.getFastestTime());
//            //            System.out.println((solutions.optimizationTime_MS/solutions.getFastestTime()));
//            double diffF = paretoToCompare.optimizationTime_MS / paretoToCompare.getFastestTime();
//            double diffS = paretoToCompare.optimizationTime_MS / paretoToCompare.getSlowest().stats.runtime_MS;
//            double meanDiff = paretoToCompare.optimizationTime_MS / ((paretoToCompare.getFastestTime() + paretoToCompare.getSlowest().stats.runtime_MS) / 2);
//
//            legendInfo.add(new Pair<String, Double>("OverHeadFastest", (double) (Math.round(diffF * 10000) / 100)));
//            legendInfo.add(new Pair<String, Double>("OverHeadSlowest", (double) (Math.round(diffS * 10000) / 100)));
//            legendInfo.add(new Pair<String, Double>("OverHeadAvg", (double) (Math.round(diffF * 10000) / 100)));
//            legendInfo.add(new Pair<String, Double>("Moheft-pareto (+) OptTime MS", (double) (solutionsM.optimizationTime_MS - paretoToCompare.optimizationTime_MS)));
//        }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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

        //plot.plotMultipleWithLine(combined, legendInfo, mpinfo, filesname, pathPlot, savePlot, showPlot);

//                if(validate){
//                    System.out.println("Running sims");
//                    SimEnginge simeng = new SimEnginge();
//                    for (Plan p:solutions){
//                        simeng.execute(p);
//                    }
//
//                }


    }

    private static void executeHS(DAG graph, boolean prune, String method, MultiplePlotInfo mpinfo,
                                  String toprint, StringBuilder sbOut, SolutionSpace combined) {

        SolutionSpace space = new SolutionSpace();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoHomogenSmall(graph, cluster, prune, method);

        space = sched.schedule();

        sbOut.append(space.toString());

        mpinfo.add(toprint + "(" + space.size() + ") " + space.optimizationTime_MS, space.results);

        combined.addAll(space);

    }

    private static void executeHL(DAG graph, boolean prune, String method, MultiplePlotInfo mpinfo,
                                  String toprint, StringBuilder sbOut, SolutionSpace combined) {

        SolutionSpace space = new SolutionSpace();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoHomogenLarge(graph, cluster, prune, method);

        space = sched.schedule();

        sbOut.append(space.toString());

        mpinfo.add(toprint + "(" + space.size() + ") " + space.optimizationTime_MS, space.results);

        combined.addAll(space);


    }

    private static void addDistanceToLegend(SolutionSpace solutionsM, SolutionSpace paretoToCompare, ArrayList<Pair<String, Double>> legendInfo) {

        double disM = calculateRangeDistance(solutionsM);
        double disP = calculateRangeDistance(paretoToCompare);
        legendInfo.add(new Pair<String, Double>("disRangeMoheft", disM));
        legendInfo.add(new Pair<String, Double>("disRangePareto", disP));
        legendInfo.add(new Pair<String, Double>("disRangeComparison (+)", disM - disP));

    }

    private static double calculateRangeDistance(SolutionSpace space) {
        double range = calculateEuclidean(space.getMaxCostPlan(), space.getMinCostPlan());
        double sum = 0.0;
        Collections.sort(space.results, new Comparator<Plan>() {
            @Override
            public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money, o2.stats.money);
            }
        });
        for (int i = 0; i < space.size() - 1; ++i) {
            sum += calculateEuclidean(space.results.get(i + 1), space.results.get(i));
        }
        double avg = sum / (space.size() - 1);

        sum = 0.0;
        for (int i = 0; i < space.size() - 1; ++i) {
            sum += (Math.abs(calculateEuclidean(space.results.get(i + 1), space.results.get(i)) - avg)) / range;
        }

        return sum;
    }

//    private static void addImprovementsToLegend(SolutionSpace solutionsM, SolutionSpace paretoToCompare, ArrayList<Pair<String, Double>> legendInfo) {
//        double maxdist = 0.0;
//        double mindist = Double.MAX_VALUE;
//        Plan tplan = null;
//
//        double maxdistMoney =  Double.MAX_VALUE;
//        double maxdistTime = Long.MAX_VALUE;
//
////        double dist = 0.0;
////        double tdist;
////        boolean isparetoSmaller = true;
////        SolutionSpace minSpace = paretoToCompare;
////        SolutionSpace maxSpace = solutionsM;
//
////        if( minSpace.size() > maxSpace.size() ){
////            minSpace = solutionsM;
////            maxSpace = paretoToCompare;
////            isparetoSmaller = false;
////        }
////
////        for(Plan minp:solutionsM){
////            tdist = Double.MAX_VALUE;
////            for(Plan maxp:paretoToCompare){
////                if(tdist>calculateEuclidean(minp,maxp)){
////                    tdist = calculateEuclidean(minp,maxp);
////                    tplan = maxp;
////                }
////                tdist = Math.min(tdist, calculateEuclidean(minp,maxp));
////            }
////            if(isparetoSmaller){
////                maxdistMoney = Math.max(maxdistMoney,  ( (minp.stats.money - tplan.stats.money)/tplan.stats.money)*100 );//pros8esa () kai sta tessera!
////                maxdistTime  = Math.max(maxdistTime,    ( (minp.stats.runtime_MS - tplan.stats.runtime_MS) /tplan.stats.runtime_MS)*100   );
////            }else{
////                maxdistMoney = Math.max(maxdistMoney,  ( (tplan.stats.money - minp.stats.money) /minp.stats.money)*100  );
////                maxdistTime  = Math.max(maxdistTime,    ( (tplan.stats.runtime_MS - minp.stats.runtime_MS) /minp.stats.runtime_MS)*100   );
////            }
////
////        }
//
//
//
//        Collections.sort(paretoToCompare.results, new Comparator<Plan>() {
//            @Override public int compare(Plan o1, Plan o2) {
//                return Double.compare(o1.stats.money,o2.stats.money);
//            }
//        });
//
//
//        double maxPKnee = 0.0;
//        double avgPKnee = 0.0;
//        for( int i=1;i<paretoToCompare.size()-1;++i){
//            Plan p0 = paretoToCompare.results.get(i-1);
//            Plan p1 = paretoToCompare.results.get(i);
//            Plan p2 = paretoToCompare.results.get(i+1);
//            double d = paretoToCompare.getDerMulti(p0,p1,p2, false, paretoToCompare);
//            maxPKnee = Math.max(maxPKnee,d);
//            avgPKnee+=d;
//        }
//        avgPKnee = avgPKnee/paretoToCompare.size()-2;
//
//        Collections.sort(solutionsM.results, new Comparator<Plan>() {
//            @Override public int compare(Plan o1, Plan o2) {
//                return Double.compare(o1.stats.money,o2.stats.money);
//            }
//        });
//
//
//        double maxMKnee = 0.0;
//        double avgMKnee = 0.0;
//        for( int i=1;i<solutionsM.size()-1;++i){
//            Plan p0 = solutionsM.results.get(i-1);
//            Plan p1 = solutionsM.results.get(i);
//            Plan p2 = solutionsM.results.get(i+1);
//            double d = solutionsM.getDerMulti(p0,p1,p2, false, solutionsM);
//            maxMKnee = Math.max(maxMKnee,d);
//            avgMKnee+=d;
//        }
//        avgMKnee = avgMKnee/solutionsM.size()-2;
//
//        legendInfo.add(new Pair<String,Double>("FastestImprovement (>1)", (double) ( solutionsM.getFastestTime()/paretoToCompare.getFastestTime()  )));
//        legendInfo.add(new Pair<String,Double>("CheapestImprovement (>1)", (double) ( solutionsM.getMinCost()/paretoToCompare.getMinCost() ) ));
//
////        legendInfo.add(new Pair<>("maxMoneyImprov (+)",maxdistMoney));
////        legendInfo.add(new Pair<>("maxTimeImprov (+)",maxdistTime));
//
//        legendInfo.add(new Pair<>("avgKnee Comp (>1) ",avgPKnee /avgMKnee));
//        legendInfo.add(new Pair<>("maxKnee Comp (>1) ",maxPKnee /maxMKnee));
//
////        legendInfo.add(new Pair<>("avgPKnee ",avgPKnee));
////        legendInfo.add(new Pair<>("maxPKnee ",maxPKnee));
////
////        legendInfo.add(new Pair<>("avgMKnee ",avgMKnee));
////        legendInfo.add(new Pair<>("maxMKnee ",maxMKnee));
//
//
//
//    }

    public static double calculateEuclidean(Plan a, Plan b) {
        double x = a.stats.runtime_MS - b.stats.runtime_MS;
        double y = a.stats.money - b.stats.money;
        return Math.sqrt((x * x) + (y * y));//or Math.pow(x, 2)+ Math.pow(y, 2)
    }

//    private static DAG runDax(boolean jar, String file, int mulTime, int mulData, ArrayList<Plan> plans, String rankMethod, boolean multiObjective) {
//
//        System.out.println("Running "+file+" mt "+mulTime+" md: "+mulData + " Pareto, Moheft");
//
//        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);
//
//        DAG graph = null;
//        try {
//            if(jar){
//                graph = parser.parseDax(file, 0L);
//            }else {
//                graph = parser.parseDax(MainEnsemble.class.getResource(file).getFile(), 0L);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String flowname;
//        if(file.contains("/")) {
//            flowname = file.substring(file.lastIndexOf("/"), file.length());
//        }else{
//            flowname = file;
//        }
//
//        runDAG(graph,"mulT: "+mulTime+" mulD: "+mulData,flowname, plans, rankMethod, multiObjective);
//
//        return graph;
//    }

//    private static void runLattice(int d, int b) {
//
//        System.out.println("Running Lattice d "+d+" b: "+b + " Pareto, Moheft");
//
//        double z = 1.0;
//        double randType = 0.0;
//        double[] runTime = {0.2,0.4,0.6,0.8,1.0};
//        double[] cpuUtil = {1.0};
//        double[] memory = {0.3};
//        double[] dataout = {0.2,0.4,0.6,0.8,1.0};
//
//        RandomParameters
//                params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
//
//        DAG graph = LatticeGenerator.createLatticeGraph(d,b,params,0, RuntimeConstants.quantum_MS);
//
//
//        ArrayList<Plan> plans = new ArrayList<>();
//        runDAG(graph,"d: "+d+" b: "+b,"Lattice", plans, rankMethod);
//
//    }

//    private static void runOneMultipleEND(boolean jar,String file, int mt, int md){
//        DAG graph = new DAG();
//        DAG tmpGraph = null;
//        System.out.println("Running runOneMultipleEND mt "+mt+" md: "+md + " Pareto, Moheft " + file);
//
//
//        try {
//
//            if(file.contains("lattice") || file.contains("Lattice")){
//
//                double z = 1.0;
//                double randType = 0.0;
//                double[] runTime = {0.2,0.4,0.6,0.8,1.0};
//                double[] cpuUtil = {1.0};
//                double[] memory = {0.3};
//                double[] dataout = {0.2,0.4,0.6,0.8,1.0};
//
//                RandomParameters
//                        params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
//
//                tmpGraph = LatticeGenerator.createLatticeGraph(mt,md,params,0, RuntimeConstants.OneHour_MS);
//            }else {
//
//                PegasusDaxParser parser = new PegasusDaxParser(mt, md);
//                if (jar) {
//                    tmpGraph = parser.parseDax(file, 0L);
//
//                } else {
//                    tmpGraph = parser.parseDax(MainEnsemble.class.getResource(file).getFile(), 0L);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        HashMap<Long,Long> OldIdToNewId = new HashMap<>();
//        graph.add(tmpGraph, OldIdToNewId);
//        for (int i = 0; i <10 ; i++) {
//            HashMap<Long,Long> OldIdToNewIdtmp = new HashMap<>();
//            graph.addEnd(tmpGraph, OldIdToNewIdtmp);
//        }
//
////        for (int i = 0; i <2 ; i++) {
////            DAG inGraph = new DAG();
////            inGraph.add(tmpGraph);
////            inGraph.add(tmpGraph);
////            graph.addEnd(inGraph);
////        }
////        for (int i = 0; i <2 ; i++) {
////            graph.addEnd(tmpGraph);
////        }
//
//        ArrayList<Plan> plans = new ArrayList<>();
//        runDAG(graph," oneFlowMultipleTimeEND +sumdata:"+graph.sumdata_B /1073741824,"multiple", plans, rankMethod);
//
//    }

//    private static void  runEnseble(boolean jar,String file, int mt, int md,int times){
//        DAG graph = new DAG();
//        DAG tmpGraph = null;
//        System.out.println("Running runEnseble "+times+" mt "+mt+" md: "+md + " Pareto, Moheft " + file);
//
//
//        try {
//
//            if(file.contains("lattice") || file.contains("Lattice")){
//
//                double z = 1.0;
//                double randType = 0.0;
//                double[] runTime = {0.2,0.4,0.6,0.8,1.0};
//                double[] cpuUtil = {1.0};
//                double[] memory = {0.3};
//                double[] dataout = {0.2,0.4,0.6,0.8,1.0};
//
//                RandomParameters
//                    params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
//
//                tmpGraph = LatticeGenerator.createLatticeGraph(mt,md,params,0,RuntimeConstants.OneHour_MS);
//            }else {
//
//                PegasusDaxParser parser = new PegasusDaxParser(mt, md);
//                if (jar) {
//                    tmpGraph = parser.parseDax(file,0L);
//
//                } else {
//                    tmpGraph = parser.parseDax(Main.class.getResource(file).getFile(), 0L);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//       graph = tmpGraph;
//
//
//        runDAGS(graph," Ensevlw +sumdata:"+graph.sumdata_B /1073741824,"multiple",times);
//
//    }

//    private static void runDAGS(DAG graph, String paremetersToPrint, String type, int times) {
//
//        StringBuilder sbOut = new StringBuilder();
//
//        sbOut.append("Running "+type+" "+paremetersToPrint+ " Pareto, Moheft").append("\n");
//
//      //  MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//
//
//        //        Cluster clusterValkanas = new Cluster();
//        //
//        //        Scheduler schedValkanas = new paretoNoHomogen(graph, clusterValkanas,true,"valkanas");
//        //
//        //        SolutionSpace solutionsValkanas = schedValkanas.schedule();
//        //
//        //        sbOut.append(solutionsValkanas.toString());
//        //
//        //        mpinfo.add("paretoPValkanas("+solutionsValkanas.size()+")"+(solutionsValkanas.optimizationTime_MS)+" "+solutionsValkanas.getScoreElastic(), solutionsValkanas.results);
//        //
//        //
//        //        Cluster clusterCrow = new Cluster();
//        //
//        //        Scheduler schedCrow = new paretoNoHomogen(graph, clusterCrow,true,"crowding");
//        //
//        //        SolutionSpace solutionsCrow = schedCrow.schedule();
//        //
//        //        sbOut.append(solutionsCrow.toString());
//        //
//        //        mpinfo.add("paretoPCrow("+solutionsCrow.size()+")"+(solutionsCrow.optimizationTime_MS)+" "+solutionsCrow.getScoreElastic(), solutionsCrow.results);
//
//
//        Cluster clusterPNP = new Cluster();
//
//        Scheduler schedPNP = new hhds(graph, clusterPNP,false,"");
//
//        SolutionSpace solutionsPNP = schedPNP.schedule();
//
//        long nextID = graph.getNextId();
//
//        for(int i=0;i<times-1;++i){
//
//            DAG tg = new DAG();
//            tg.setNextId(nextID);
//            tg.add(graph);
//            nextID = tg.getNextId();
//            Plan knee = solutionsPNP.getKnee();
//
//            schedPNP = new hhds(tg, knee.cluster,false,"");
//
//            solutionsPNP = schedPNP.schedule();
//        }
//
//        sbOut.append(solutionsPNP.toString());
//
//
//
////        mpinfo.add("paretoNP("+solutionsPNP.size()+")"+(solutionsPNP.optimizationTime_MS)+" "+solutionsPNP.getScoreElastic(), solutionsPNP.results);
//
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new hhds(graph, cluster,true,"all");
//
//        SolutionSpace solutions = sched.schedule();
//
//        for(int i=0;i<times-1;++i) {
//
//            DAG tg = new DAG();
//            tg.setNextId(nextID);
//            tg.add(graph);
//            nextID = tg.getNextId();
//            Plan knee = solutionsPNP.getKnee();
//
//
//            sched = new hhds(tg, knee.cluster,true,"all");
//
//            solutions = sched.schedule();
//
//        }
//
//        sbOut.append(solutions.toString());
//
////        mpinfo.add("paretoPALL("+solutions.size()+")"+(solutions.optimizationTime_MS)+" "+solutions.getScoreElastic(), solutions.results);
//
//
//        System.out.println("paretoDone");
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//        for(int i=0;i<times-1;++i) {
//
//            schedM = new Moheft(graph, solutionsM.getKnee().cluster);
//
//            solutionsM = schedM.schedule();
//
//        }
//
//        sbOut.append(solutionsM.toString());
//
////        mpinfo.add("moheft "+(solutionsM.optimizationTime_MS)+" "+solutionsM.getScoreElastic(), solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//
//        sbOut.append("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges()).append("\n");
//        sbOut.append(paremetersToPrint + "  sumDataGB " + (graph.sumdata_B / 1073741824)).append("\n");
//        sbOut.append("pareto "+type+" time -> " + solutions.optimizationTime_MS/1000).append("\n");
//        sbOut.append("paretoNP "+type+" time -> " + solutionsPNP.optimizationTime_MS/1000).append("\n");
//        sbOut.append("moheft "+type+" time -> " + solutionsM.optimizationTime_MS/1000).append("\n");
//
//
//        SolutionSpace combined = new SolutionSpace();
//        combined.addAll(solutions);
//        combined.addAll(solutionsM);
//
//        combined.computeSkyline(false);
//
//        double distMtoC=0.0,distPtoC=0.0,distCtoM=0.0,distCtoP=0.0;
//        double JaccardMtoC=0.0,JaccardPtoC=0.0;
//
//        ArrayList<Pair<String,Double>> legendInfo = new ArrayList<>();
//
//
//        try {
//            distMtoC = computeDistance(solutionsM,combined).P2Sky;
//            legendInfo.add(new Pair<>("distMtoC",distMtoC));
//
//            distPtoC = computeDistance(solutions,combined).P2Sky;
//            legendInfo.add(new Pair<>("distPtoC",distPtoC));
//
//            distCtoM = computeDistance(combined,solutionsM).P2Sky;
//            legendInfo.add(new Pair<>("distCtoM",distCtoM));
//
//            distCtoP = computeDistance(combined,solutions).P2Sky;
//            legendInfo.add(new Pair<>("distCtoP",distCtoP));
//
//            JaccardMtoC = computeJaccard(solutionsM,combined);
//            legendInfo.add(new Pair<>("JaccMtoC",JaccardMtoC));
//
//            JaccardPtoC = computeJaccard(solutions,combined);
//            legendInfo.add(new Pair<>("JaccPtoC",JaccardPtoC));
//
//            sbOut.append("Jaccard from M to C "+JaccardMtoC).append("\n");
//            sbOut.append("Jaccard from P to C "+JaccardPtoC).append("\n");
//
//
//            sbOut.append("distance from M to C "+distMtoC).append("\n");
//            sbOut.append("distance from P to C "+distPtoC).append("\n");
//            sbOut.append("distance from C to M "+distCtoM).append("\n");
//            sbOut.append("distance from C to P "+distCtoP).append("\n");
//
//
//            legendInfo.add(new Pair<String,Double>("nodes",(double)graph.getOperators().size()));
//            legendInfo.add(new Pair<String,Double>("edges",(double)graph.sumEdges()));
//
//            System.out.println(solutions.optimizationTime_MS+" "+solutions.getFastestTime());
//            System.out.println((solutions.optimizationTime_MS/solutions.getFastestTime()));
//            double diffF = solutions.optimizationTime_MS/solutions.getFastestTime();
//            double diffS = solutions.optimizationTime_MS/solutions.getSlowest().stats.runtime_MS;
//            double meanDiff = solutions.optimizationTime_MS / ((solutions.getFastestTime()+solutions.getSlowest().stats.runtime_MS)/2);
//
//            legendInfo.add(new Pair<String,Double>("OverHeadFastest", (double) (Math.round(diffF *10000)/100)));
//            legendInfo.add(new Pair<String,Double>("OverHeadSlowest", (double) (Math.round(diffS *10000)/100)));
//            legendInfo.add(new Pair<String,Double>("OverHeadAvg", (double) (Math.round(meanDiff *10000)/100)));
//            legendInfo.add(new Pair<String,Double>("Moheft-paretoNP (+) OptTime MS",  (double)(solutionsM.optimizationTime_MS - solutions.optimizationTime_MS)));
//            legendInfo.add(new Pair<String,Double>("Moheft-paretoP (+) OptTime MS",  (double)(solutionsM.optimizationTime_MS - solutionsPNP.optimizationTime_MS)));
//
//
//
//
//
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//        double ccr =  graph.computeCCR();
//
//        legendInfo.add(new Pair<String,Double>("ccr",ccr));
//
//
//        sbOut.append("toCompare: "+ type + " sumDataGB: "+(graph.sumdata_B / 1073741824) +" "+
//            paremetersToPrint               +
//            " paretoOptTime_MS: "+ solutions.optimizationTime_MS + " MoheftOptimizationTime_MS: "+solutionsM.optimizationTime_MS +
//            " MtoC "+ distMtoC +" PtoC "+ distPtoC+" CtoM "+ distCtoM +" CtoP "+ distCtoP + " JMtoC "+ JaccardMtoC +" JPtoC "+ JaccardPtoC)
//            .append(" ccr ").append(ccr).append("\n");
//
//
//        String filesname =
//            type +
//                "___"+paremetersToPrint.replace(" ","_")+
//                "_sumDataGB_"+ (graph.sumdata_B / 1073741824)+"_ccr_"+ccr+"__"+
//                (new java.util.Date()).toString().replace(" ","_");
//
//
//
//        if(showOutput){
//            System.out.println(sbOut.toString());
//        }
//        if(saveOutput){
//            PrintWriter out = null;
//            try {
//                out = new PrintWriter(pathOut+filesname+".txt");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            out.println(sbOut.toString());
//            out.close();
//
//        }
//
////        plot.plotMultipleWithLine(combined, legendInfo ,mpinfo, filesname,
////            pathPlot,
////            savePlot,
////            showPlot);
//
//        if(validate){
//            System.out.println("Running sims");
//            SimEnginge simeng = new SimEnginge();
//            for (Plan p:solutions){
//                simeng.execute(p);
//            }
//
//        }
//
//
//
//    }


//    private static void runOneMultipleHALF(boolean jar,String file, int mt, int md){
//        DAG graph = new DAG();
//        DAG tmpGraph = null;
//        System.out.println("Running runOneMultipleHALF mt "+mt+" md: "+md + " Pareto, Moheft " + file);
//
//        try {
//
//            if(file.contains("lattice") || file.contains("Lattice")){
//
//                double z = 1.0;
//                double randType = 0.0;
//                double[] runTime = {0.2,0.4,0.6,0.8,1.0};
//                double[] cpuUtil = {1.0};
//                double[] memory = {0.3};
//                double[] dataout = {0.2,0.4,0.6,0.8,1.0};
//
//                RandomParameters
//                    params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
//
//                tmpGraph = LatticeGenerator.createLatticeGraph(mt,md,params,0);
//            }else {
//
//                PegasusDaxParser parser = new PegasusDaxParser(mt, md);
//                if (jar) {
//                    tmpGraph = parser.parseDax(file, 0L);
//
//                } else {
//                    tmpGraph = parser.parseDax(Main.class.getResource(file).getFile(),0L);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        graph.add(tmpGraph);
//        for (int i = 0; i <10 ; i++) {
//            graph.addHalfPoint(tmpGraph);
//        }
//
//        //        for (int i = 0; i <2 ; i++) {
//        //            DAG inGraph = new DAG();
//        //            inGraph.add(tmpGraph);
//        //            inGraph.add(tmpGraph);
//        //            graph.addEnd(inGraph);
//        //        }
//        //        for (int i = 0; i <2 ; i++) {
//        //            graph.addEnd(tmpGraph);
//        //        }
//
//        ArrayList<Plan> plans = new ArrayList<>();
//        runDAG(graph," oneFlowMultipleTimeEND +sumdata:"+graph.sumdata_B /1073741824,"multiple", plans);
//
//    }


    private static DAG runMultipleFlows(boolean jar, ArrayList<Triple<String, Integer, Integer>> flowsandParasms, ArrayList<Plan> plans,
                                        String rankMethod, boolean multiObjective, int pruning_k, int constraint_mode,
                                        double money_constraint, long time_constraint) {

        System.out.println("runinng multFLow");
        for (Triple<String, Integer, Integer> tr : flowsandParasms) {
            System.out.println(tr.a + " " + tr.b + " " + tr.c);
        }

        DAG graph = new DAG();
        // DAGmerged graphMerged = new DAGmerged();
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

//            ArrayList <Long> minTime = new ArrayList<>();
//        ArrayList <Double> minCost = new ArrayList<>();
//
//            Long time=Long.MAX_VALUE;
//            Double money=Double.MAX_VALUE;
//
//        for(int i=0;i<plans.size()-1;++i) {
//            Plan p0;
//            p0 = plans.get(i);
//            time =Math.min(time, p0.stats.runtime_MS);
//            money = Math.min(money, p0.stats.money);
//        }
//            minTime.add(time);
//        minCost.add(money);

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
