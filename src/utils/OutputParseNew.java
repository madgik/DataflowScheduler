package utils;

import Graph.DAG;
import Scheduler.Cluster;
import Scheduler.Plan;
import Scheduler.SolutionSpace;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParseNew {

    public static void main(String[] args) throws IOException {


        File folder = new File("/Users/johnchronis/code/MyScheduler/mul/");

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {


                String filepath = fileEntry.getPath();
                String filename = filepath.substring(filepath.lastIndexOf("/"), filepath.length());

                MultiplePlotInfo mpinfo = new MultiplePlotInfo();

                FileInputStream fstream = new FileInputStream(filepath);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;

                br.readLine();
                String line = br.readLine();
                ArrayList<Pair<Long, Double>> pareto = new ArrayList<>();
                while (line.charAt(0) != '/') {
                    line = line.trim();
                    String splits[] = line.split(" ");
                    pareto.add(new Pair<>(Long.parseLong(splits[0]), Double.parseDouble(splits[1])));
                    line = br.readLine();
                }
                mpinfo.addPairs("pareto", pareto);

                line = br.readLine();
                ArrayList<Pair<Long, Double>> moheft = new ArrayList<>();
                while (line.charAt(0) != '/') {
                    line = line.trim();

                    String splits[] = line.split(" ");
                    moheft.add(new Pair<>(Long.parseLong(splits[0]), Double.parseDouble(splits[1])));
                    line = br.readLine();
                }
                mpinfo.addPairs("moheft", moheft);

                line = br.readLine();


                plotUtility plot = new plotUtility();

                SolutionSpace comb = new SolutionSpace();

                for (Pair<Long, Double> p : moheft) {
                    Plan pl = new Plan(new DAG(), new Cluster());
                    pl.stats.runtime_MS = p.a;
                    pl.stats.money = p.b;
                    comb.add(pl);
                }
                for (Pair<Long, Double> p : moheft) {
                    Plan pl = new Plan(new DAG(), new Cluster());
                    pl.stats.runtime_MS = p.a;
                    pl.stats.money = p.b;
                    comb.add(pl);
                }
                comb.computeSkyline(false);


                double distMtoC = 0.0, distPtoC = 0.0, distCtoM = 0.0, distCtoP = 0.0;

                ArrayList<Pair<String, Double>> legendInfo = new ArrayList<>();

                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();

                String toComp[] = line.split(" ");


                for (int i = 2; i < toComp.length - 2; i += 2) {
                    legendInfo.add(new Pair<>(toComp[i], Double.parseDouble(toComp[i + 1])));
                }



                plot.plotMultipleWithLine(comb, legendInfo, mpinfo, filename,
                    "/Users/johnchronis/Desktop/MyScheduler/Outplots/", false, true);



                br.close();
            }
        }

    }
}
