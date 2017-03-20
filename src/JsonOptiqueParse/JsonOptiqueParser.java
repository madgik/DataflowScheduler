package JsonOptiqueParse;

import Graph.DAG;
import Graph.Data;
import Graph.Edge;
import Graph.ResourcesRequirements;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Created by johnchronis on 3/16/17.
 */
public class JsonOptiqueParser {


    long sumdata = 0;
    private Long mult_data = 50L;
    private Long mult_time = 10L;

    public JsonOptiqueParser(long mulDATA, long multTime){
        mult_data = mulDATA;
        mult_time = multTime;
    }
    public JsonOptiqueParser(){

    }


    public DAG parse(String filepath){


        Gson gson = new Gson();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JsonPlan jp = gson.fromJson(br, JsonPlan.class);


        DAG graph = new DAG();

        HashMap<String,Long> opnametoId = new HashMap<>();
        HashMap<Long,Long> opIdtoDisk = new HashMap<>();



        for(Operator opjp : jp.getOperators()){

            ResourcesRequirements
                re = new ResourcesRequirements(opjp.getResources().getTimeMS() * mult_time, -1);

           Graph.Operator op = new Graph.Operator(opjp.getOperatorName(),re);

            graph.addOperator(op);

            opnametoId.put(opjp.getOperatorName(),op.getId());

            opIdtoDisk.put(op.getId(),Double.valueOf(opjp.getResources().getDiskMB()*1000).longValue());


        }



        for(OpLink opl : jp.getOpLinks()){

            Long from = opnametoId.get(opl.getFrom());
            Long to = opnametoId.get(opl.getTo());

            Data d = new Data("emptyName",opIdtoDisk.get(from)*mult_data);
            sumdata+=opIdtoDisk.get(from)*mult_data;
            Edge e = new Edge(from,to,d);

            graph.addEdge(e);

        }

        System.out.println("data GB sum "+sumdata/1000000000);
        System.out.println("ops#  "+graph.getOperators().size());
        System.out.println("links#  "+jp.getOpLinks().size());
        return graph;
    }


}
