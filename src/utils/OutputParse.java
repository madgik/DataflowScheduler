package utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParse {

    public static void main(String[] args) throws IOException {

        String filepath = "/Users/johnchronis/Desktop/MyScheduler/plotsEuler/results/Fri_Mar_31_00:00:32_EEST_2017_result_MONTAGE.n.100.0.dax_dt1000_dd10000.out";
        String filename = filepath.substring(filepath.lastIndexOf("/"), filepath.length());

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        FileInputStream fstream = new FileInputStream(filepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

        br.readLine();
        br.readLine();
        String line = br.readLine();
        ArrayList<Pair<Long,Double>> pareto = new ArrayList<>();
        while (line.charAt(0) != '/')   {
            line = line.trim();
            String splits[] = line.split(" ");
            pareto.add(new Pair<>(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
            line = br.readLine();
        }
        mpinfo.addPairs("pareto",pareto);

        line = br.readLine();
        ArrayList<Pair<Long,Double>> moheft = new ArrayList<>();
        while (line.charAt(0) != '/')   {
            line = line.trim();

            String splits[] = line.split(" ");
            moheft.add(new Pair<>(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
            line = br.readLine();
        }
        mpinfo.addPairs("moheft",moheft);

        line = br.readLine();


        br.close();

        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo,filename,"/Users/johnchronis/Desktop/MyScheduler/Outplots/",true);

    }
}
