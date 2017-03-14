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

        File loadFile = new File(Main.class.getResource("LIGO.n.100.0.dax").getFile());
        //Example.dax//MONTAGE.n.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
             graph = parser.parseDax(loadFile.toURL().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cluster cluster = new Cluster();
//        cluster.addContainer(containerType.LARGE);

        Scheduler sched = new Moheft(graph,cluster);

        SolutionSpace solutions = sched.schedule();

        Cluster cluster2 = new Cluster();

        Scheduler sched2 = new paretoNoHomogen(graph,cluster2);

        SolutionSpace solutions2 = sched2.schedule();

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        mpinfo.add("pareto",solutions2.results);

        mpinfo.add("moheft",solutions.results);


        plotMultiple(mpinfo);

        //TODO: Run the simulation to validate the results for the space of solutions
//        solutions.print();

    }
}
