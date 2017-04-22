package Graph;

import Scheduler.RuntimeConstants;

import java.util.ArrayList;
import java.util.HashMap;

import static utils.random.randomInRange;


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

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

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
        sumdata_B = 0;
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

    public double computeCCR() {
        Long dataSum_B = 0L;
        for(ArrayList<Edge> ed: edges.values()) {
            for (Edge edge :ed) {
                dataSum_B += edge.data.size_B;
            }
        }
        Double com = (2*dataSum_B) / RuntimeConstants.distributed_storage_speed_B_MS;

        long comp=0;

        for(Operator op:operatorsList){
            comp+=op.getRunTime_MS();
        }


        Double ccr = com/comp;
        return ccr;
    }

    public DAG add(DAG g){
        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            Operator newop = new Operator(op.name,op.resourcesRequirements);
            newid = this.addOperator(newop);
            OldIdToNewId.put(oldid,newid);
        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){

                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        this.sumdata_B +=g.sumdata_B;
        return this;
    }

    long midPoint = -1;

    public DAG addHalfPoint(DAG g) {

        long minOldIdNewG;

        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            newid = this.addOperator(op);
            Operator newop = new Operator(op.name,op.resourcesRequirements);
            newid = this.addOperator(newop);        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){
                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        if(midPoint != -1) {
            midPoint = OldIdToNewId.get(g.operatorsList.size() / 2);
            Data d = new Data("", 0);
            this.addEdge(new Edge(midPoint, OldIdToNewId.get(0), d));
        }
        this.sumdata_B +=g.sumdata_B;
        return this;

    }

    public DAG addRandomPoint(DAG g) {
        long randomId = randomInRange(0,Math.toIntExact(g.nextId-1));
        long random = g.operators.get(randomId).getId();

        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            Operator newop = new Operator(op.name,op.resourcesRequirements);
            newid = this.addOperator(newop);
            OldIdToNewId.put(oldid,newid);
        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){
                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        if(midPoint != -1) {
            Data d = new Data("", 0);
            this.addEdge(new Edge(random, OldIdToNewId.get(0), d));
        }
        this.sumdata_B +=g.sumdata_B;
        return this;
    }

    public DAG addPoisson(DAG g) {



        long randomId = randomInRange(0,Math.toIntExact(g.nextId-1));
        long random = g.operators.get(randomId).getId();

        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            Operator newop = new Operator(op.name,op.resourcesRequirements);
            newid = this.addOperator(newop);
            OldIdToNewId.put(oldid,newid);
        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){
                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        if(midPoint != -1) {
            Data d = new Data("", 0);
            this.addEdge(new Edge(random, OldIdToNewId.get(0), d));
        }
        this.sumdata_B +=g.sumdata_B;
        return this;

    }

    public DAG addEnd(DAG g) {
        long endPrev = g.nextId-1;
        HashMap<Long,Long> OldIdToNewId = new HashMap<>();

        Long oldid,newid;
        for(Operator op:g.getOperators()){
            oldid = op.getId();
            Operator newop = new Operator(op.name,op.resourcesRequirements);
            newid = this.addOperator(newop);
            OldIdToNewId.put(oldid,newid);
        }

        for(ArrayList<Edge> ae: g.edges.values()){
            for(Edge e:ae){
                this.addEdge(new Edge(OldIdToNewId.get(e.from),OldIdToNewId.get(e.to),e.data));
            }
        }

        Data d = new Data("", 0);
        this.addEdge(new Edge(endPrev, OldIdToNewId.get(0L), d));

        this.sumdata_B +=g.sumdata_B;
        return this;
    }

    //    public void addFile(Data data){
//        nameToFile.put(data.name,data);
//    }
//
//    public Data getFile(String name){
//        return nameToFile.get(name);
//    }


}
