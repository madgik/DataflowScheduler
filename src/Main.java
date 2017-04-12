import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import JsonOptiqueParse.JsonOptiqueParser;
import Lattice.LatticeGenerator;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;
import Simulator.SimEnginge;
import Tree.TreeGraphGenerator;
import utils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import static utils.Jaccard.computeJaccard;
import static utils.SolutionSpaceUtils.computeDistance;



public class Main {

    static Boolean savePlot = false;
    static Boolean showPlot = false;
    static String pathPlot;
    static String pathOut;
    static Boolean showOutput = true;
    static Boolean saveOutput = false;
    static Boolean validate = true;

    public static void main(String[] args) {

        pathPlot = "./plots/";
        pathOut = "./results/";

        //        System.out.print("specify with -D: flow d,b,mt,md,showOutput");

        if(savePlot){System.out.println("saving plots to "+ pathPlot);}
        if(saveOutput){System.out.println("saving output to "+ pathOut);}


        String flow = System.getProperty("flow");
        if( flow != null){
            if(flow.equals("lattice")){
                String d = System.getProperty("d");
                String b = System.getProperty("b");
                runLattice(Integer.parseInt(d),Integer.parseInt(b));
            }else{
                String mt = System.getProperty("mt");
                String md = System.getProperty("md");
                runDax(true,flow,Integer.parseInt(mt),Integer.parseInt(md));
            }
        }else{

//            runDax(false,"Example.dax",1,1);
//            File folder = new File("/Users/johnchronis/Desktop/MyScheduler/resources");
//
//            for (final File fileEntry : folder.listFiles()) {
//                if (fileEntry.isDirectory()) {
//                } else {
//                    if(fileEntry.getName().startsWith("TPCH")) {
//                        runJson(false, fileEntry.getName(), 100, 300000);
//                    }
//                }}

//            runDax(false,"MONTAGE.n.100.0.dax",2000,100);
//            runDax(false,"LIGO.n.100.0.dax",100,100);
//
//
//            HashMap<String,Pair<Integer,Integer>> flowsandParasms = new HashMap<>();
//            flowsandParasms.put("MONTAGE.n.100.0.dax",new Pair<>(2000,100));
//            flowsandParasms.put("LIGO.n.100.0.dax",new Pair<>(100,100));
//            runMultipleFlows(flowsandParasms);
            //            flowsandParasms.put("LIGO.n.500.0.dax",new Pair<>(500,500));
//            runDax(true,"MONTAGE.n.100.0.dax",2000,1000);

                        runDax(false,"LIGO.n.100.0.dax",100,100);
//            runDax(false,"MONTAGE.n.100.0.dax",2000,100);

            ArrayList<Triple<String,Integer,Integer>> flowsandParasms = new ArrayList<>();
//            flowsandParasms.add(new Triple("MONTAGE.n.100.0.dax",2000,1000));
//            flowsandParasms.add(new Triple("MONTAGE.n.100.0.dax",2000,1000));
//
//            flowsandParasms.add(new Triple("MONTAGE.n.100.0.dax",2000,1000));
//
////            flowsandParasms.put("LIGO.n.100.0.dax",new Pair<>(100,100));
//            runMultipleFlows(true,flowsandParasms);
//            //


            //
//            ArrayList<Integer> times = new ArrayList<>();
//            ArrayList<Integer> datas = new ArrayList<>();
//
//            times.add(1);times.add(10);times.add(30);
//            times.add(50);times.add(100);times.add(500);
//            times.add(1000);times.add(5000);times.add(10000);
//
//           datas.add(1);datas.add(100);datas.add(500);
//           datas.add(1000);datas.add(5000);datas.add(10000);
//           datas.add(200000);
//
//
//            for(int t:times){
//                for(int d:datas){
//                    runDax(false,"LIGO.n.50.0.dax",t,d);
//                    runDax(false,"LIGO.n.100.0.dax",t,d);
//                    runDax(false,"LIGO.n.500.0.dax",t,d);
//                }
//            }
//            for(int t:times){
//                for(int d:datas){
//                    runDax(false,"CYBERSHAKE.n.50.0.dax",t,d);
//                    runDax(false,"CYBERSHAKE.n.100.0.dax",t,d);
//                    runDax(false,"CYBERSHAKE.n.500.0.dax",t,d);
//                }
//            }
//            for(int t:times){
//                for(int d:datas){
//                    runDax(false,"SIPHT.n.50.0.dax",t,d);
//                    runDax(false,"SIPHT.n.100.0.dax",t,d);
//                    runDax(false,"SIPHT.n.500.0.dax",t,d);
//                }
//            }
//            for(int t:times){
//                for(int d:datas){
//                    runDax(false,"MONTAGE.n.50.0.dax",t,d);
//                    runDax(false,"MONTAGE.n.100.0.dax",t,d);
//                    runDax(false,"MONTAGE.n.500.0.dax",t,d);
//                }
//            }


//            runJson("TPCH_9_0_4.json",1,100);
//
//            runMultipleFlows(50,1000);


//            runLattice(11,3); //6 vs 32 solutions alla kaliteres
//            runLattice(9,4); // poli kaliteroi + grigoroi
//            runLattice(7,7); //better
//            runLattice(5,21); // kalitero emeis
//            runLattice(3,498); //poli pio grigoroi kalitero gonato alla sta pio grigora mas kerdizei, genika kaliteroi emeis


        }
        //TODO: Run the simulation to validate the results for the space of solutions
    }

    public static void runDAG(DAG graph, String paremetersToPrint, String type)
    {

        StringBuilder sbOut = new StringBuilder();

        sbOut.append("Running "+type+" "+paremetersToPrint+ " Pareto, Moheft").append("\n");

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph, cluster);

        SolutionSpace solutions = sched.schedule();

        sbOut.append(solutions.toString());

        mpinfo.add("pareto "+(solutions.optimizationTime_MS)+" "+solutions.getScoreElastic(), solutions.results);


        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph, clusterM);

        SolutionSpace solutionsM = schedM.schedule();

        sbOut.append(solutionsM.toString());

        mpinfo.add("moheft "+(solutionsM.optimizationTime_MS)+" "+solutionsM.getScoreElastic(), solutionsM.results);

        plotUtility plot = new plotUtility();

        //        showOptimizationResult("aaaa",solutions.results.get(solutions.size()-1));


        sbOut.append("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges()).append("\n");
        sbOut.append(paremetersToPrint + "  sumDataGB " + (graph.sumdata_B / 1073741824)).append("\n");
        sbOut.append("pareto "+type+" time -> " + solutions.optimizationTime_MS).append("\n");
        sbOut.append("moheft "+type+" time -> " + solutionsM.optimizationTime_MS).append("\n");


        SolutionSpace combined = new SolutionSpace();
        combined.addAll(solutions);
        combined.addAll(solutionsM);

        combined.computeSkyline(false);

        double distMtoC=0.0,distPtoC=0.0,distCtoM=0.0,distCtoP=0.0;
        double JaccardMtoC=0.0,JaccardPtoC=0.0;

        ArrayList<Pair<String,Double>> legendInfo = new ArrayList<>();


        try {
            distMtoC = computeDistance(solutionsM,combined).P2Sky;
            legendInfo.add(new Pair<>("distMtoC",distMtoC));

            distPtoC = computeDistance(solutions,combined).P2Sky;
            legendInfo.add(new Pair<>("distPtoC",distPtoC));

            distCtoM = computeDistance(combined,solutionsM).P2Sky;
            legendInfo.add(new Pair<>("distCtoM",distCtoM));

            distCtoP = computeDistance(combined,solutions).P2Sky;
            legendInfo.add(new Pair<>("distCtoP",distCtoP));

            JaccardMtoC = computeJaccard(solutionsM,combined);
            legendInfo.add(new Pair<>("JaccMtoC",JaccardMtoC));

            JaccardPtoC = computeJaccard(solutions,combined);
            legendInfo.add(new Pair<>("JaccPtoC",JaccardPtoC));

            sbOut.append("Jaccard from M to C "+JaccardMtoC).append("\n");
            sbOut.append("Jaccard from P to C "+JaccardPtoC).append("\n");


            sbOut.append("distance from M to C "+distMtoC).append("\n");
            sbOut.append("distance from P to C "+distPtoC).append("\n");
            sbOut.append("distance from C to M "+distCtoM).append("\n");
            sbOut.append("distance from C to P "+distCtoP).append("\n");


            legendInfo.add(new Pair<String,Double>("nodes",(double)graph.getOperators().size()));
            legendInfo.add(new Pair<String,Double>("edges",(double)graph.sumEdges()));


        } catch (RemoteException e) {
            e.printStackTrace();
        }

        double ccr =  graph.computeCCR();

        legendInfo.add(new Pair<String,Double>("ccr",ccr));


        sbOut.append("toCompare: "+ type + " sumDataGB: "+(graph.sumdata_B / 1073741824) +" "+
            paremetersToPrint               +
            " paretoOptTime_MS: "+ solutions.optimizationTime_MS + " MoheftOptimizationTime_MS: "+solutionsM.optimizationTime_MS +
            " MtoC "+ distMtoC +" PtoC "+ distPtoC+" CtoM "+ distCtoM +" CtoP "+ distCtoP + " JMtoC "+ JaccardMtoC +" JPtoC "+ JaccardPtoC)
            .append(" ccr ").append(ccr).append("\n");


        String filesname =
                type +
                "___"+paremetersToPrint.replace(" ","_")+
                "_sumDataGB_"+ (graph.sumdata_B / 1073741824)+"_ccr_"+ccr+"__"+
                (new java.util.Date()).toString().replace(" ","_");



        if(showOutput){
            System.out.println(sbOut.toString());
        }
        if(saveOutput){
            PrintWriter out = null;
            try {
                out = new PrintWriter(pathOut+filesname+".txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            out.println(sbOut.toString());
            out.close();

        }

        plot.plotMultipleWithLine(combined, legendInfo ,mpinfo, filesname,
            pathPlot,
            savePlot,
            showPlot);

        if(validate){
            SimEnginge simeng = new SimEnginge();
            for (Plan p:solutions){
                simeng.execute(p);
            }

        }



    }


    private static void runDax(boolean jar, String file, int mulTime, int mulData) {

        System.out.println("Running "+file+" mt "+mulTime+" md: "+mulData + " Pareto, Moheft");

        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);

        DAG graph = null;
        try {
            if(jar){
                graph = parser.parseDax(file);
            }else {
                graph = parser.parseDax(Main.class.getResource(file).getFile());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        String flowname;
        if(file.contains("/")) {
            flowname = file.substring(file.lastIndexOf("/"), file.length());
        }else{
            flowname = file;
        }
        runDAG(graph,"mulT: "+mulTime+" mulD: "+mulData,flowname);
    }

    private static void runLattice(int d, int b) {

        System.out.println("Running Lattice d "+d+" b: "+b + " Pareto, Moheft");

        double z = 1.0;
        double randType = 0.0;
        double[] runTime = {0.2,0.4,0.6,0.8,1.0};
        double[] cpuUtil = {1.0};
        double[] memory = {0.3};
        double[] dataout = {0.2,0.4,0.6,0.8,1.0};

        RandomParameters
            params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);

        DAG graph = LatticeGenerator.createLatticeGraph(d,b,params,0);


        runDAG(graph,"d: "+d+" b: "+b,"Lattice");

    }

    private static void runMultipleFlows(boolean jar,ArrayList<Triple<String,Integer,Integer>> flowsandParasms){




        ArrayList<String> filenames = new ArrayList<>();

        filenames.add("LIGO.n.100.0.dax");
        filenames.add("MONTAGE.n.1000.0.dax");
        filenames.add("CyberShake.n.100.0.dax");
        filenames.add("CyberShake.n.100.0.dax");
        filenames.add("MONTAGE.n.100.0.dax");

        //        filenames.add();

        DAG graph = new DAG();

        try {
            for(Triple<String,Integer,Integer> p: flowsandParasms) {
                PegasusDaxParser parser = new PegasusDaxParser(p.b, p.c);
                if(jar){
                    graph.add(parser.parseDax(p.a));

                }else {
                    graph.add(parser.parseDax(Main.class.getResource(p.a).getFile()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        double z = 1.0;
//        double randType = 0.0;
//        double[] runTime = {0.2,0.4,0.6,0.8,1.0};
//        double[] cpuUtil = {1.0};
//        double[] memory = {0.3};
//        double[] dataout = {0.2,0.4,0.6,0.8,1.0};
//
//        RandomParameters
//            params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);
//
//
//        //        graph.add( LatticeGenerator.createLatticeGraph(11,3,params,0) );
//        //        graph.add( LatticeGenerator.createLatticeGraph(5,21,params,0) );
//        //        graph.add( LatticeGenerator.createLatticeGraph(d,b,params,0) );


        runDAG(graph," multipleFlows +sumdata:"+graph.sumdata_B /1073741824,"multiple");

    }

    private static void runTree(int leafs,int height) {

        double[] avgTimePerLevel = new double[] {0.2, 0.2, 0.2, 0.2, 0.3};
        double initialDataSize = 1000;
        double[] dataReductionPerLevel = new double[] {0.3, 0.3, 0.3, 0.3, 0.3};
        double randomness = 0.0;
        long seed = 0L;

        DAG graph  = TreeGraphGenerator.createTreeGraph(
            leafs, height, 1.0, 1.0,
            avgTimePerLevel, initialDataSize, dataReductionPerLevel, randomness, seed);

        runDAG(graph," leafs: "+leafs+" height: "+height,"tree");

    }

    private static void runHEFT(String filename, int mulTime, int mulData) {
        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);

        DAG graph = null;
        try {
            graph = parser.parseDax(Main.class.getResource(filename).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();


        Cluster cluster;
        Scheduler sched;
        SolutionSpace solutions = new SolutionSpace();
        for(int i=1;i<10;++i){
            cluster = new Cluster();
            sched = new HEFT(graph, cluster,i,containerType.A);
            solutions.addAll(sched.schedule());

        }


        mpinfo.add("HEFT "+(solutions.optimizationTime_MS), solutions.results);



        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo, filename+" --- mulT: "+mulTime+" mulD: "+mulData
            +" sumDataGB "+ (graph.sumdata_B / 1073741824)+ " n "+graph.getOperators().size()+" e "+graph.sumEdges(),
            pathPlot,
            savePlot);

        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData GB " + (graph.sumdata_B / 1073741824));
        System.out.println("HEFT Example time -> " + solutions.optimizationTime_MS);
    }

    private static void runJson(boolean jar,String filename, int mulTime, int mulData) {

        JsonOptiqueParser parser = new JsonOptiqueParser(mulTime, mulData);

        DAG graph = null;
        try {
            if(jar){
                graph = parser.parse(filename);
            }else {
                graph = parser.parse(Main.class.getResource(filename).getFile());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        runDAG(graph," mulT: "+mulTime+" mulD: "+mulData,filename);

    }


}
