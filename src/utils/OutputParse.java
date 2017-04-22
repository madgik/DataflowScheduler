package utils;

import Graph.DAG;
import Scheduler.Cluster;
import Scheduler.Plan;
import Scheduler.SolutionSpace;

import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

import static utils.SolutionSpaceUtils.computeDistance;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParse {


    public static void main(String[] args) throws IOException {
        File folder = new File("/Users/johnchronis/Desktop/MyScheduler/plotsEuler/results/");

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            } else {


                String filepath =  fileEntry.getPath();//"/Users/johnchronis/Desktop/MyScheduler/plotsEuler/results/Fri_Mar_31_00:00:32_EEST_2017_result_MONTAGE.n.100.0.dax_dt1000_dd10000.out";
                String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());
                System.out.println("wokrinf on "+ filename);

                MultiplePlotInfo mpinfo = new MultiplePlotInfo();

                FileInputStream fstream = new FileInputStream(filepath);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;

                br.readLine();
                br.readLine();
                String line = br.readLine();
                ArrayList<PairPlot> pareto = new ArrayList<>();
                if(line==null)continue;
                while (line.charAt(0) != '/')   {
                    line = line.trim();
                    String splits[] = line.split(" ");
                    pareto.add(new PairPlot(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
                    line = br.readLine();
                }
                mpinfo.addPairs("pareto",pareto);

                line = br.readLine();
                ArrayList<PairPlot> moheft = new ArrayList<>();
                while (line.charAt(0) != '/')   {
                    line = line.trim();

                    String splits[] = line.split(" ");
                    moheft.add(new PairPlot(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
                    line = br.readLine();
                }
                mpinfo.addPairs("moheft",moheft);

                line = br.readLine();


                plotUtility plot = new plotUtility();

                SolutionSpace comb = new SolutionSpace();

                for(PairPlot p : moheft){
                    Plan pl = new Plan(new DAG(),new Cluster());
                    pl.stats.runtime_MS = p.getTime();
                    pl.stats.money = p.getMoney();
                    comb.add(pl);
                }
                for(PairPlot p : moheft){
                    Plan pl = new Plan(new DAG(),new Cluster());
                    pl.stats.runtime_MS = p.getTime();
                    pl.stats.money = p.getMoney();
                    comb.add(pl);
                }
                comb.computeSkyline(false);


                double distMtoC=0.0,distPtoC=0.0,distCtoM=0.0,distCtoP=0.0;

                ArrayList<Pair<String,Double>> legendInfo = new ArrayList<>();

                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();
                line = br.readLine();

                String toComp[] = line.split(" ");


for (int i=2;i<toComp.length-2;i+=2){
    legendInfo.add(new Pair<>(toComp[i],Double.parseDouble(toComp[i+1])));
                }




                plot.plotMultipleWithLine(comb,legendInfo,mpinfo,filename,"/Users/johnchronis/Desktop/MyScheduler/Outplots/",true,false);



                br.close();


                System.out.println("");









            }
        }


    }
}
