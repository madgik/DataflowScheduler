import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import JsonOptiqueParse.JsonOptiqueParser;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;
import utils.MultiplePlotInfo;
import utils.plotUtility;
import java.io.File;




public class Main {

    static Boolean save = true;
    static String path;

    public static void main(String[] args) {

        System.out.println(System.getProperty("user.dir"));
        path = System.getProperty("user.dir")+"/plots/"+(new java.util.Date());
        new File(path).mkdir();
        path+="/";

//        runCyberShake(500, 10);
//        runSIPHT(100,  1);
//        runMontage(100, 1);
//        runLIGO(100, 10);

        runDax("LIGO.n.100.0.dax",100,10);
        runDax("SIPHT.n.100.0.dax",100,1);
        runDax("CyberShake.n.100.0.dax",500,10);
        runDax("MONTAGE.n.100.0.dax",100,1);

        runJson("2_Q1_6_10.1dat.cleanplan", 1300, 10);

//         runJson("2_Q1_6_10.1dat.cleanplan", 1300, 10);
//         runJson("2_Q2_6_2dat.cleanplan", 1300, 10);
//         runJson("2_Q3_6_5.4dat.cleanplan", 1300, 10);
//         runJson("2_Q4_6_6.4dat.cleanplan", 1300, 10);
//         runJson("2_Q5_6_10.2dat.cleanplan", 1300, 10);
//         runJson("2_Q6_6_2.3dat.cleanplan", 1300, 10);
//         runJson("2_Q7_6_6dat.cleanplan", 1300, 10);
//         runJson("2_Q8_6_6.3dat.cleanplan", 1300, 10);
//         runJson("2_Q10_6_8dat.cleanplan", 1300, 100);

//        runDax("Example.dax",1000,1);


        //TODO: Run the simulation to validate the results for the space of solutions
    }

    private static void runDax(String filename, int mulTime, int mulData) {

        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);

        DAG graph = null;
        try {
            graph = parser.parseDax(Main.class.getResource(filename).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph, cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto "+(solutions.optimizationTime_MS), solutions.results);


        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph, clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft "+(solutionsM.optimizationTime_MS), solutionsM.results);

        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo, filename+" --- mulT: "+mulTime+" mulD: "+mulData
            +" sumDataGB "+ (graph.sumdata_B / 1073741824)+ " n "+graph.getOperators().size()+" e "+graph.sumEdges(),path,save);

        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData GB " + (graph.sumdata_B / 1073741824));
        System.out.println("pareto Example time -> " + solutions.optimizationTime_MS);
        System.out.println("moheft Example time -> " + solutionsM.optimizationTime_MS);
    }

    private static void runJson(String filename, int mulTime, int mulData) {

        JsonOptiqueParser parser = new JsonOptiqueParser(mulTime, mulData);

        DAG graph = null;
        try {
            graph = parser.parse(Main.class.getResource(filename).getFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        Cluster cluster = new Cluster();

        Scheduler sched = new paretoNoHomogen(graph, cluster);

        SolutionSpace solutions = sched.schedule();

        mpinfo.add("pareto "+(solutions.optimizationTime_MS), solutions.results);


        Cluster clusterM = new Cluster();

        Scheduler schedM = new Moheft(graph, clusterM);

        SolutionSpace solutionsM = schedM.schedule();


        mpinfo.add("moheft "+(solutionsM.optimizationTime_MS), solutionsM.results);

        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo, filename+" --- mulT: "+mulTime+" mulD: "+mulData
            +" sumDataGB "+ (graph.sumdata_B / 1073741824)+ "  n "+graph.getOperators().size()+" e "+graph.sumEdges(),path,save);

        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData GB " + (graph.sumdata_B / 1073741824));
        System.out.println("pareto Example time -> " + solutions.optimizationTime_MS);
        System.out.println("moheft Example time -> " + solutionsM.optimizationTime_MS);
    }

//    static void runExample(int mulTime, int mulData) {
//
//        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);
//        DAG graph = null;
//        try {
//            graph = parser.parseDax(Main.class.getResource("Example.dax").getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, "Example");
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//        System.out.println("pareto Example time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft Example time -> " + solutionsM.optimizationTime_MS);
//    }
//
//    static void runOpti(String s, int mulTime, int mulData) {
//
//        JsonOptiqueParser parser = new JsonOptiqueParser(mulTime, mulData);
//        DAG graph = null;
//        try {
//            graph = parser.parse(Main.class.getResource(s).getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, s);
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//
//        System.out.println("pareto opti time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft opti time -> " + solutionsM.optimizationTime_MS);
//    }
//
//    static void runSIPHT(int mulTime, int mulData) {
//
//        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);
//        DAG graph = null;
//        try {
//            graph = parser.parseDax(Main.class.getResource("SIPHT.n.100.0.dax").getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, "sipht");
//
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//
//        System.out.println("pareto SIPHT time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft SIPHT time -> " + solutionsM.optimizationTime_MS);
//
//
//    }
//
//    static void runMontage(int mulTime, int mulData) {
//
//        PegasusDaxParser parser = new PegasusDaxParser( mulTime,  mulData);
//        DAG graph = null;
//        try {
//
//            graph = parser.parseDax(Main.class.getResource("MONTAGE.n.100.0.dax").getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, "montage");
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//
//        System.out.println("pareto montage time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft montage time -> " + solutionsM.optimizationTime_MS);
//
//
//    }
//
//    static void runCyberShake(int mulTime, int mulData) {
//
//        PegasusDaxParser parser = new PegasusDaxParser(mulTime,mulData);
//        DAG graph = null;
//        try {
//            graph = parser.parseDax(Main.class.getResource("CyberShake.n.100.0.dax").getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, "cybershake");
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//
//        System.out.println("pareto cyber time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft cyber time -> " + solutionsM.optimizationTime_MS);
//
//    }
//
//    static void runLIGO(int mulTime, int mulData) {
//
//        PegasusDaxParser parser = new PegasusDaxParser(mulTime, mulData);
//        DAG graph = null;
//        try {
//            graph = parser.parseDax(Main.class.getResource("LIGO.n.100.0.dax").getFile());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        MultiplePlotInfo mpinfo = new MultiplePlotInfo();
//
//        Cluster cluster = new Cluster();
//
//        Scheduler sched = new paretoNoHomogen(graph, cluster);
//
//        SolutionSpace solutions = sched.schedule();
//
//        mpinfo.add("pareto", solutions.results);
//
//        Cluster clusterM = new Cluster();
//
//        Scheduler schedM = new Moheft(graph, clusterM);
//
//        SolutionSpace solutionsM = schedM.schedule();
//
//
//        mpinfo.add("moheft", solutionsM.results);
//
//        plotUtility plot = new plotUtility();
//
//        plot.plotMultiple(mpinfo, "LIGO");
//        System.out.println("nodes "+graph.getOperators().size()+" edges "+graph.sumEdges());
//        System.out.println("mulTime "+mulTime + " mulData " + mulData + "  sumData MB " + (graph.sumdata_B / 1000000));
//        System.out.println("pareto ligo time -> " + solutions.optimizationTime_MS);
//        System.out.println("moheft ligo time -> " + solutionsM.optimizationTime_MS);
//
//    }


}
