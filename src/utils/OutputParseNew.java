package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParseNew {

    public static void main(String[] args) throws IOException {

        String filepath = "/Users/johnchronis/Desktop/MyScheduler/presentation/result/CYBERSHAKE.n.100.0.dax___mulT:_1_mulD:_1_sumDataGB_97_ccr_0.23357883373736027__Sat_Apr_01_00:25:39_EEST_2017.txt";
        String filename = filepath.substring(filepath.lastIndexOf("/"), filepath.length());

        MultiplePlotInfo mpinfo = new MultiplePlotInfo();

        FileInputStream fstream = new FileInputStream(filepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

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
