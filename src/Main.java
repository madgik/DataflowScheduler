import Graph.DAG;
import Graph.parsers.PegasusDaxParser;
import Scheduler.Cluster;
import Scheduler.Scheduler;
import Scheduler.*;

import java.io.File;

public class Main {

    public static void main(String[] args)  {

        File loadFile = new File(Main.class.getResource("MONTAGE.n.1000.0.dax").getFile());
        //Example.dax//MONTAGE.25.0.dax//"MONTAGE.n.100.0.dax"//"Example.dax"
        PegasusDaxParser parser = new PegasusDaxParser();
        DAG graph=null;
        try {
             graph = parser.parseDax(loadFile.toURL().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Cluster cluster = new Cluster();
//        cluster.addContainer(containerType.LARGE);

        Scheduler sched = new paretoNoHomogen(graph,cluster);

        SolutionSpace solutions = sched.schedule();

//        solutions.print();

    }
}
