package Graph;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by johnchronis on 2/18/17.
 */
public class DAG {

    public String name;

    public HashMap<Long,Operator> operators;
    public ArrayList<Operator> operatorsList;

    public HashMap<Long,ArrayList<Edge>> edges;
    private HashMap<Long,ArrayList<Edge>> reverseEdges;

    private Long nextId;

//    public  HashMap<String,Data> nameToFile;
//    public  HashMap<String,Long> filenameToFromOpId;


    public DAG(){
        name="";
        operators = new HashMap<>();
        edges = new HashMap<>();
        reverseEdges = new HashMap<>();
//        nameToFile = new HashMap<>();
        nextId = 0L;
        operatorsList = new ArrayList<>();
//        filenameToFromOpId = new HashMap<>();
    }

    public Long addOperator(Operator op){
        op.setId(nextId);
        operators.put(nextId,op);
        operatorsList.add(op);

        edges.put(nextId,new ArrayList<Edge>());
        reverseEdges.put(nextId,new ArrayList<Edge>());

        return nextId++;
    }

    public Operator getOperator(Long id){
        return operators.get(id);
    }

    public void addEdge(Edge e){
//        if(!edges.containsKey(from)){
//            edges.put(from,new ArrayList<Long>());
//        }
        edges.get(e.from).add(e);

//        if(!reverseEdges.containsKey(to)){
//            reverseEdges.put(to,new ArrayList<Long>());
//        }
        reverseEdges.get(e.to).add(e);

    }

    public ArrayList<Edge> getChildren(Long node){
        return edges.get(node);
    }

    public ArrayList<Edge> getParents(Long node){
        return reverseEdges.get(node);
    }

    public ArrayList<Operator> getOperators(){
        return operatorsList;
    }

    public void printEdges(){
        for(Long opid:edges.keySet()){
            System.out.printf(opid+": ");
            for(Edge out: getChildren(opid)){
                System.out.printf(" "+out.to/*+"("+out.data.size_B+")"*/);
            }
            System.out.println();
        }
    }

//    public void addFile(Data data){
//        nameToFile.put(data.name,data);
//    }
//
//    public Data getFile(String name){
//        return nameToFile.get(name);
//    }


}
