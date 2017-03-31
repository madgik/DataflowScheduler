package utils;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParse {

    public static void main(String[] args) throws IOException {

        String filepath = "/Users/johnchronis/Desktop/eulplots/results/Thu_Mar_30_22:49:22_EEST_2017_result_SIPHT.n.100.0.dax_dt10000_dd1.out";

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        FileInputStream fstream = new FileInputStream(filepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

        br.readLine();
        br.readLine();
        String line = br.readLine();
        ArrayList<Pair<Long,Double>> pareto = new ArrayList<>();
        while (line.charAt(0) != '/')   {

            String splits[] = line.split(" ");
            pareto.add(new Pair<>(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
            line = br.readLine();
        }
        mpinfo.addPairs("pareto",pareto);

        line = br.readLine();
        ArrayList<Pair<Long,Double>> moheft = new ArrayList<>();
        while (line.charAt(0) != '/')   {

            String splits[] = line.split(" ");
            moheft.add(new Pair<>(Long.parseLong(splits[0]),Double.parseDouble(splits[1])));
            line = br.readLine();
        }
        mpinfo.addPairs("moheft",moheft);

        line = br.readLine();


        br.close();

        plotUtility plot = new plotUtility();

        plot.plotMultiple(mpinfo,"sipht100","/Users/johnchronis/Desktop/MyScheduler/plots/",true);

    }
}
