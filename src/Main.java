import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import JsonOptiqueParse.JsonOptiqueParser;
import Lattice.LatticeGenerator;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;
import Tree.TreeGraphGenerator;
import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
import utils.MultiplePlotInfo;
import utils.RandomParameters;
import utils.plotUtility;
import java.io.File;
import java.util.ArrayList;
import java.util.StringJoiner;

import static utils.OptimizationResultVisualizer.showOptimizationResult;



public class Main {

    static Boolean save = false;
    static String path;

    public static void main(String[] args) {

        System.out.println(System.getProperty("user.dir"));
        path = System.getProperty("user.dir ")+"/plots/"+(new java.util.Date());
        new File(path).mkdir();
        path+="/";

        String flow = System.getProperty("flow");
        if( flow != null){
            if(flow.equals("lattice")){
                String d = System.getProperty("d");
                String b = System.getProperty("b");
                System.out.println("Running Lattice d "+d+" b: "+b );
                runLattice(Integer.parseInt(d),Integer.parseInt(b));
            }else{
                String mt = System.getProperty("mt");
                String md = System.getProperty("md");
                String size = System.getProperty("size");
                System.out.println("Running "+flow+" mt "+mt+" md: "+md+ " size "+size );
                runDax(flow+".n."+size+".0.dax",Integer.parseInt(mt),Integer.parseInt(md));
            }
        }else{
            runDax("LIGO.n.50.0.dax",5,1);
//            runJson("TPCH_9_0_4.json",1,100);
//
//            //        runMultipleFlows(50,1000);
//
//            runDax("LIGO.n.500.0.dax",50,1000);
//            runDax("SIPHT.n.100.0.dax",50,1000);
//            runDax("CYBERSHAKE.n.500.0.dax",50,1000);
//            runDax("MONTAGE.n.500.0.dax",50,1000);
            //        //H conf...
            //
            //        runDax("LIGO.n.100.0.dax",100,10);
            //        runDax("SIPHT.n.100.0.dax",100,100);
            //        runDax("CyberShake.n.100.0.dax",500,10);
            //        runDax("MONTAGE.n.100.0.dax",100,1);
            //
            ////        runLattice(5,2);
            ////        runLattice(500,1);//skaei
            //        runLattice(11,3); //6 vs 32 solutions alla kaliteres
            //        runLattice(9,4); // poli kaliteroi + grigoroi
            //        runLattice(7,7); //better
            //        runLattice(5,21); // kalitero emeis
            //        runLattice(3,498); //poli pio grigoroi kalitero gonato alla sta pio grigora mas kerdizei, genika kaliteroi emeis


            //
            //        runJson("2_Q1_6_10.1dat.cleanplan", 1300, 10);

            //        runDax("Example.dax",1000,1);


        }
        //TODO: Run the simulation to validate the results for the space of solutions
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

    private static void runMultipleFlows(int mulTime,int mulData){


        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);

        ArrayList<String> filenames = new ArrayList<>();

        filenames.add("LIGO.n.100.0.dax");
        filenames.add("MONTAGE.n.1000.0.dax");
        filenames.add("CyberShake.n.100.0.dax");
        filenames.add("CyberShake.n.100.0.dax");
        filenames.add("MONTAGE.n.100.0.dax");

        //        filenames.add();

        DAG graph = new DAG();

        try {
            for(String filename: filenames) {
                graph.add( parser.parseDax(Main.class.getResource(filename).getFile()) );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double z = 1.0;
        double randType = 0.0;
        double[] runTime = {0.2,0.4,0.6,0.8,1.0};
        double[] cpuUtil = {1.0};
        double[] memory = {0.3};
        double[] dataout = {0.2,0.4,0.6,0.8,1.0};

        RandomParameters
            params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);


//        graph.add( LatticeGenerator.createLatticeGraph(11,3,params,0) );
//        graph.add( LatticeGenerator.createLatticeGraph(5,21,params,0) );
//        graph.add( LatticeGenerator.createLatticeGraph(d,b,params,0) );


        runDAG(graph," mulT: "+mulTime+" mulD: "+mulData,"ola");




    }



    public static void runDAG(DAG graph, String paremetersToPrint, String type){

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph, cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto "+(solutions.optimizationTime_MS)+" "+solutions.getScoreElastic(), solutions.results);


        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph, clusterM);

        SolutionSpace solutionsM = schedM.schedule();

//        showOptimizationResult("aaaa",solutions.results.get(solutions.size()-1));

        mpinfo.add("moheft "+(solutionsM.optimizationTime_MS)+" "+solutionsM.getScoreElastic(), solutionsM.results);

        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo, type +" ---"+paremetersToPrint
            +" sumDataGB "+ (graph.sumdata_B / 1073741824)+ " n "+graph.getOperators().size()+" e "+graph.sumEdges(),path,save);

        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
        System.out.println(paremetersToPrint + "  sumData GB " + (graph.sumdata_B / 1073741824));
        System.out.println("pareto "+type+" time -> " + solutions.optimizationTime_MS);
        System.out.println("moheft "+type+" time -> " + solutionsM.optimizationTime_MS);
    }



    private static void runLattice(int d, int b) {

        double z = 1.0;
        double randType = 0.0;
        double[] runTime = {0.2,0.4,0.6,0.8,1.0};
        double[] cpuUtil = {1.0};
        double[] memory = {0.3};
        double[] dataout = {0.2,0.4,0.6,0.8,1.0};

        RandomParameters
            params = new RandomParameters(z, randType, runTime, cpuUtil, memory, dataout);

        DAG graph = LatticeGenerator.createLatticeGraph(d,b,params,0);


        runDAG(graph," d: "+d+" b: "+b,"Lattice");


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
            +" sumDataGB "+ (graph.sumdata_B / 1073741824)+ " n "+graph.getOperators().size()+" e "+graph.sumEdges(),path,save);

        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData GB " + (graph.sumdata_B / 1073741824));
        System.out.println("HEFT Example time -> " + solutions.optimizationTime_MS);
    }

    private static void runDax(String filename, int mulTime, int mulData) {

        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);

        DAG graph = null;
        try {
            graph = parser.parseDax(Main.class.getResource(filename).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }


        runDAG(graph," mulT: "+mulTime+" mulD: "+mulData,filename);
   }

    private static void runJson(String filename, int mulTime, int mulData) {

        JsonOptiqueParser parser = new JsonOptiqueParser(mulTime, mulData);

        DAG graph = null;
        try {
            graph = parser.parse(Main.class.getResource(filename).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        runDAG(graph," mulT: "+mulTime+" mulD: "+mulData,filename);

    }




}
