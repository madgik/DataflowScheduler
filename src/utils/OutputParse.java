package utils;

import java.io.*;

/**
 * Created by johnchronis on 3/31/17.
 */
public class OutputParse {

    public static void main(String[] args) throws IOException {

        String filepath = "/Users/johnchronis/Desktop/eulplots/results/Thu_Mar_30_22:49:22_EEST_2017_result_SIPHT.n.100.0.dax_dt10000_dd1.out";

        FileInputStream fstream = new FileInputStream(filepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;

        br.readLine();
        br.readLine();
        String line = br.readLine();
        while (line.charAt(0) != '/')   {



            line = br.readLine();
        }

        br.close();



    }
}
