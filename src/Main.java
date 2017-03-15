import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;
import utils.MultiplePlotInfo;

import java.io.File;

import static utils.Plot.plotMultiple;

public class Main {

    public static void main(String[] args)  {

//        File loadFile = new File(Main.class.getResource("MONTAGE.n.100.0.dax").getFile());
//        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
//        PegasusDaxParser parser = new PegasusDaxParser();
//        DAG graph=null;
//        try {
//            graph = parser.parseDax(loadFile.toURL().toString());
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
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph,clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft",solutionsM.results);
//
//        plotMultiple(mpinfo,"MONTAGE");
//
//        System.out.println("pareto montage time -> "+solutions.optimizationTime_MS);
//        System.out.println("moheft montage time -> "+solutionsM.optimizationTime_MS);



        runLIGO();
        runCyberShake();

        //TODO: Run the simulation to validate the results for the space of solutions
        //        solutions.print();

    }


    static void runCyberShake(){
        File loadFile = new File(Main.class.getResource("CyberShake.n.100.0.dax").getFile());
        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
            graph = parser.parseDax(loadFile.toURL().toString());
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
        File loadFile = new File(Main.class.getResource("LIGO.n.100.0.dax").getFile());
        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
            graph = parser.parseDax(loadFile.toURL().toString());
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
