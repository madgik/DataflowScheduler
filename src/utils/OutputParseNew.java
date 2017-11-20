package utils;

import Graph.DAG;
import Scheduler.Cluster;
import Scheduler.Plan;
import Scheduler.RuntimeConstants;
import Scheduler.SolutionSpace;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static utils.Jaccard.computeJaccard;
import static utils.SolutionSpaceUtils.computeDistance;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParseNew {

    public static void main(String[] args) throws IOException {


     //   String spath =
//"/Users/johnchronis/code/MyScheduler/paperExps/test/MONTAGE.n.100.0.dax_OurPrune____mulT:_20_mulD:_1000_sumDataGB_1848_ccr_1.2882492552481952__Sun_Apr_30_23:57:32_PDT_2017.txt";
        String spath = "gnuplotResults/persec/MONTAGE/parsing.txt";
        File file = new File(spath);
//                String filename = filepath.substring(filepath.lastIndexOf("/"), filepath.length());
        System.out.println(file.getAbsolutePath());
                MultiplePlotInfo mpinfo = new MultiplePlotInfo();

                FileInputStream fstream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;
                SolutionSpace  pareto = new SolutionSpace();
                SolutionSpace  moheft = new SolutionSpace();
                SolutionSpace  comb = new SolutionSpace();

                br.readLine();
                String line = br.readLine();
                while (line.charAt(0) != '/') {
                    line = line.trim();
                    String splits[] = line.split(" ");
                    Plan pl = new Plan(new DAG(), new Cluster());
                    pl.stats.runtime_MS = Long.parseLong(splits[0]);
                    pl.stats.money =  Double.parseDouble(splits[1]);
                    pl.stats.quanta = (int) Math.ceil(pl.stats.runtime_MS/ RuntimeConstants.quantum_MS);
                    pareto.add(pl);
                    comb.add(pl);
                    line = br.readLine();
                }
                mpinfo.add("pareto", pareto.results);

                line = br.readLine();
                while (line.charAt(0) != '/') {
                    line = line.trim();
                    String splits[] = line.split(" ");
                    Plan pl = new Plan(new DAG(), new Cluster());
                    pl.stats.runtime_MS = Long.parseLong(splits[0]);
                    pl.stats.money =  Double.parseDouble(splits[1]);
                    moheft.add(pl);
                    comb.add(pl);
                    line = br.readLine();
                }
                mpinfo.add("moheft", moheft.results);

                line = br.readLine();
                line = br.readLine();

                line = br.readLine();
                pareto.optimizationTime_MS = Double.parseDouble((line.split(" ")[4]));
                line = br.readLine();
                moheft.optimizationTime_MS = Double.parseDouble((line.split(" ")[4]));


                plotUtility plot = new plotUtility();

                comb.computeSkyline(false,false);

                ////////print+plot

                double distMtoC = 0.0, distPtoC = 0.0, distCtoM = 0.0, distCtoP = 0.0;
                double JaccardMtoC = 0.0, JaccardPtoC = 0.0;
                ArrayList<Pair<String, Double>> legendInfo = new ArrayList<>();

                System.out.println("hhds, moheft");

                System.out.println("Jaccard:  "+computeJaccard(pareto,comb)+ "    "+computeJaccard(moheft,comb));

                System.out.println("Dist:  "+computeDistance(pareto,comb).P2Sky+"    "+ computeDistance(moheft,comb).P2Sky);

                System.out.println("Distribution:  "+calculateRangeDistance(pareto)+"   "+calculateRangeDistance(moheft));

                System.out.println("FastestImprovement:  "+pareto.getFastestTime() +"   "+moheft.getFastestTime()+ " ___ "+ (double) ( (double)moheft.getFastestTime()/(double)pareto.getFastestTime()  ));
                
                System.out.println("CheapestImprovement:  "+pareto.getMinCost() +" " +moheft.getMinCost() + " ___ "+  (double) (  moheft.getMinCost()/pareto.getMinCost() ));

                double diffF = pareto.optimizationTime_MS/pareto.getFastestTime();
                double diffS = pareto.optimizationTime_MS/pareto.getSlowest().stats.runtime_MS;
                
                System.out.println("OverheadSlowest_P:  "+(double) (Math.round(diffS *10000)/100));

                System.out.println("OverheadFastest_P:  "+(double) (Math.round(diffF *10000)/100));


                printKnee(moheft,pareto);
                /////////////////


                br.close();

                File fileOUT = new File(spath+".combined");
                FileWriter fileWriter = new FileWriter(fileOUT);
                for(Plan p :comb) {
                    fileWriter.write(p.stats.runtime_MS + " "+p.stats.money+"\n");
                }
                fileWriter.flush();
                fileWriter.close();


            }

  



    private static double calculateRangeDistance(SolutionSpace space) {
        double range = calculateEuclidean(space.getMaxCostPlan(),space.getMinCostPlan());
        double sum = 0.0;
        Collections.sort(space.results, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money,o2.stats.money);
            }
        });
        for(int i=0;i<space.size()-1;++i){
            sum += calculateEuclidean(space.results.get(i+1), space.results.get(i));
        }
        double avg = sum/(space.size()-1);

        sum = 0.0;
        for(int i=0;i<space.size()-1;++i){
            sum += ( Math.abs( calculateEuclidean(space.results.get(i+1), space.results.get(i))  - avg))/range;
        }

        return sum;
    }
    public static double calculateEuclidean(Plan a,Plan b){
        double x = a.stats.runtime_MS - b.stats.runtime_MS;
        double y = a.stats.money - b.stats.money;
        return Math.sqrt((x*x)+(y*y));//or Math.pow(x, 2)+ Math.pow(y, 2)
    }


    private static void printKnee(SolutionSpace moheft, SolutionSpace pareto) {
        
        Collections.sort(pareto.results, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money,o2.stats.money);
            }
        });


        double maxPKnee = 0.0;
        double avgPKnee = 0.0;
        for( int i=1;i<pareto.size()-1;++i){
            Plan p0 = pareto.results.get(i-1);
            Plan p1 = pareto.results.get(i);
            Plan p2 = pareto.results.get(i+1);
            double d = pareto.getDer(p0,p1,p2);
            maxPKnee = Math.max(maxPKnee,d);
            avgPKnee+=d;
        }
        avgPKnee = avgPKnee/pareto.size()-2;

        Collections.sort(moheft.results, new Comparator<Plan>() {
            @Override public int compare(Plan o1, Plan o2) {
                return Double.compare(o1.stats.money,o2.stats.money);
            }
        });


        double maxMKnee = 0.0;
        double avgMKnee = 0.0;
        for( int i=1;i<moheft.size()-1;++i){
            Plan p0 = moheft.results.get(i-1);
            Plan p1 = moheft.results.get(i);
            Plan p2 = moheft.results.get(i+1);
            double d = moheft.getDer(p0,p1,p2);
            maxMKnee = Math.max(maxMKnee,d);
            avgMKnee+=d;
        }
        avgMKnee = avgMKnee/moheft.size()-2;



        System.out.println("avgKnee Comp (>1)  "+ (avgPKnee /avgMKnee));

        System.out.println("maxKnee Comp (>1) "+(maxPKnee /maxMKnee));


    }


}
