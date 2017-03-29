package Graph;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by johnchronis on 2/18/17.
 */
public class DAG {

    public String name;

    public long sumdata_B;

    public HashMap<Long,Operator> operators;
    public ArrayList<Operator> operatorsList;

    public HashMap<Long,ArrayList<Edge>> edges;
    public HashMap<Long, HashMap<Long,Edge>> edgesMap;
    private HashMap<Long,ArrayList<Edge>> reverseEdges;

    private Long nextId;

//    public  HashMap<String,Data> nameToFile;
//    public  HashMap<String,Long> filenameToFromOpId;


    public DAG(){
        name="";
        operators = new HashMap<>();
        edges = new HashMap<>();
        reverseEdges = new HashMap<>();
        edgesMap = new HashMap<>();
//        nameToFile = new HashMap<>();
        nextId = 0L;
        operatorsList = new ArrayList<>();
//        filenameToFromOpId = new HashMap<>();
    }

    public DAG add(DAG g){
        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            newid = this.addOperator(op);
            OldIdToNewId.put(oldid,newid);
        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){

                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        return this;
    }



    public Long addOperator(Operator op){
//        System.out.println("opccreated "+nextId+" "+op.resourcesRequirements.runtime_MS);

        op.setId(nextId);
        operators.put(nextId,op);
        operatorsList.add(op);

        edges.put(nextId,new ArrayList<Edge>());
        reverseEdges.put(nextId,new ArrayList<Edge>());

        edgesMap.put(nextId,new HashMap<Long, Edge>());

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
        edgesMap.get(e.from).put(e.to,e);

//        if(!reverseEdges.containsKey(to)){
//            reverseEdges.put(to,new ArrayList<Long>());
//        }
        reverseEdges.get(e.to).add(e);

    }

    public ArrayList<Edge> getChildren(Long node){
        return edges.get(node);
    }

    public Edge getEdge(Long from, Long to){
        return edgesMap.get(from).get(to);}

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
                System.out.printf(" "+out.to+"("+out.data.size_B+")");
            }
            System.out.println();
        }
    }

    public int sumEdges() {
        int sum=0;
        for(ArrayList<Edge> a:edges.values()){
            sum+=a.size();
        }
        return sum;
    }

    //    public void addFile(Data data){
//        nameToFile.put(data.name,data);
//    }
//
//    public Data getFile(String name){
//        return nameToFile.get(name);
//    }


}
