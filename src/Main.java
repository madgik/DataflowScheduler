import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import JsonOptiqueParse.JsonOptiqueParser;
import JsonOptiqueParse.JsonPlan;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;
import com.google.gson.Gson;
import utils.MultiplePlotInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static utils.Plot.plotMultiple;

public class Main {

    public static void main(String[] args)  {


//        runExample();
//        runCyberShake();
//        runSIPHT();
        runMontage();
//        runLIGO();

//        File loadFile = new File(Main.class.getResource("Example.dax").getPath());
//
//        JsonOptiqueParser parser = new JsonOptiqueParser();
//        DAG graph=null;
//        try {
//            graph = parser.parse(loadFile.getAbsolutePath());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph,cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto",solutions.results);
//
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph,clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft",solutionsM.results);
//
//        plotMultiple(mpinfo,"opti");
//
//        System.out.println("pareto Example time -> "+solutions.optimizationTime_MS);
//        System.out.println("moheft Example time -> "+solutionsM.optimizationTime_MS);
//

        //TODO: Run the simulation to validate the results for the space of solutions
    }

    static void runExample(){

        //        File loadFile = new File(Main.class.getResource("MONTAGE.n.100.0.dax").getFile());
        //        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser(1,1);
        DAG graph=null;
        try {
            graph = parser.parseDax(Main.class.getResource("Example.dax").getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto",solutions.results);

        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph,clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft",solutionsM.results);

        plotMultiple(mpinfo,"Example");

        System.out.println("pareto Example time -> "+solutions.optimizationTime_MS);
        System.out.println("moheft Example time -> "+solutionsM.optimizationTime_MS);


    }

    static void runOpti(){
        File loadFile = new File(Main.class.getResource("2_Q5_6_10.2dat.cleanplan").getPath());

                JsonOptiqueParser parser = new JsonOptiqueParser();
                DAG graph=null;
                try {
                    graph = parser.parse(loadFile.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                MultiplePlotInfo mpinfo = new MultiplePlotInfo();

                Cluster cluster = new Cluster();

                Scheduler sched = new paretoNoHomogen(graph,cluster);

                SolutionSpace solutions = sched.schedule();

                mpinfo.add("pareto",solutions.results);


                Cluster clusterM = new Cluster();

                Scheduler schedM = new Moheft(graph,clusterM);

                SolutionSpace solutionsM = schedM.schedule();


                mpinfo.add("moheft",solutionsM.results);

                plotMultiple(mpinfo,"opti");

                System.out.println("pareto opti time -> "+solutions.optimizationTime_MS);
                System.out.println("moheft opti time -> "+solutionsM.optimizationTime_MS);
    }


    static void runSIPHT(){

        //        File loadFile = new File(Main.class.getResource("MONTAGE.n.100.0.dax").getFile());
        //        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser(100,1);
        DAG graph=null;
        try {
            graph = parser.parseDax(Main.class.getResource("SIPHT.n.100.0.dax").getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto",solutions.results);

        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph,clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft",solutionsM.results);

        plotMultiple(mpinfo,"SIPHT");

        System.out.println("pareto SIPHT time -> "+solutions.optimizationTime_MS);
        System.out.println("moheft SIPHT time -> "+solutionsM.optimizationTime_MS);


    }


    static void runMontage(){

//        File loadFile = new File(Main.class.getResource("MONTAGE.n.100.0.dax").getFile());
//        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
            graph = parser.parseDax(Main.class.getResource("MONTAGE.n.25.0.dax").getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto",solutions.results);

        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph,clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft",solutionsM.results);

        plotMultiple(mpinfo,"MONTAGE");

        System.out.println("pareto montage time -> "+solutions.optimizationTime_MS);
        System.out.println("moheft montage time -> "+solutionsM.optimizationTime_MS);


    }

    static void runCyberShake(){
//        File loadFile = new File(Main.class.getResource("CyberShake.n.100.0.dax").getFile());
        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
            graph = parser.parseDax(Main.class.getResource("CyberShake.n.100.0.dax").getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto",solutions.results);

        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph,clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft",solutionsM.results);

        plotMultiple(mpinfo,"cybershake");

        System.out.println("pareto cyber time -> "+solutions.optimizationTime_MS);
        System.out.println("moheft cyber time -> "+solutionsM.optimizationTime_MS);

    }

    static void runLIGO(){
//        File loadFile = new File(Main.class.getResource("LIGO.n.100.0.dax").getFile());
        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser(100,1);
        DAG graph=null;
        try {
            graph = parser.parseDax(Main.class.getResource("LIGO.n.100.0.dax").getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto",solutions.results);

        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph,clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft",solutionsM.results);

        plotMultiple(mpinfo,"LIGO");

        System.out.println("pareto ligo time -> "+solutions.optimizationTime_MS);
        System.out.println("moheft ligo time -> "+solutionsM.optimizationTime_MS);

    }


}
