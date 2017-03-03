package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Plan implements Comparable<Plan>{
    public DAG graph;
    public HashMap<Long,Long> assignments;//opIdToContId;
    public HashMap<Long,ArrayList<Long>> contAssignments; //contId->list<opId>
    public HashMap<Long, ArrayList<Long>> contIdToOpIds;
    public Cluster cluster;
    Statistics stats;
    Statistics beforeStats;
    String vmUpgrading;
    public HashMap<Long, Pair<Long,Long>> opIdtoStartEnd_MS;


//    public HashSet<Long> readyOps;




    public Plan(DAG graph,Cluster cluster){
        this.graph = graph;
        beforeStats=null;
        assignments = new HashMap<>();
        contIdToOpIds = new HashMap<>();
        this.cluster = new Cluster(cluster);
        opIdtoStartEnd_MS = new HashMap<>();
        contAssignments = new HashMap<>();
        opIdtoStartEnd_MS = new HashMap<>();
        stats = new Statistics(this);

//        readyOps = new HashSet<>();
//        for(Long opid:graph.operators.keySet()){
//            if( graph.getParents(opid).size()==0){
//                readyOps.add(opid);
//            }
//        }


    }

    public Plan(Plan p){
        this.vmUpgrading = p.vmUpgrading;
        this.graph = p.graph;
        this.beforeStats = p.beforeStats;
        assignments = new HashMap<>();
        for(Long key: p.assignments.keySet()){
            assignments.put(key,p.assignments.get(key));
        }
        contIdToOpIds = new HashMap<>();
        for(Long key: p.contIdToOpIds.keySet()){
            ArrayList<Long> list = new ArrayList<>();
            list.addAll(p.contIdToOpIds.get(key));
            contIdToOpIds.put(key,list);
        }
        contAssignments = new HashMap<>();
        for(Long cid:p.contAssignments.keySet()){
            ArrayList<Long> t = new ArrayList<>();

            t.addAll(p.contAssignments.get(cid));


            contAssignments.put(cid,t);
        }
        opIdtoStartEnd_MS = new HashMap<>();
        for(long oid:p.opIdtoStartEnd_MS.keySet()){
            opIdtoStartEnd_MS.put(
                oid,
                new Pair<>(
                    p.opIdtoStartEnd_MS.get(oid).a,
                    p.opIdtoStartEnd_MS.get(oid).b
                )
            );
        }
        stats = new Statistics(p.stats);

        this.cluster = new Cluster(p.cluster);


        //        readyOps = new HashSet<>();
        //        for(Long opid:graph.operators.keySet()){
        //            if( graph.getParents(opid).size()==0){
        //                readyOps.add(opid);
        //            }
        //        }

    }

    public void assignOperator(Long opId,Long contId){
        assignments.put(opId,contId);
        if(!contAssignments.containsKey(contId)){
            contAssignments.put(contId,new ArrayList<Long>());
        }
        contAssignments.get(contId).add(opId);
        beforeStats = new Statistics(stats);
        //        cluster.getContainer(contId).assignOp(opId);


        long timeNow_MS = 0L;
        //TODO ji change for backfilling

        Container cont = cluster.getContainer(contId);
        long contFirstAvailTime = cont.getEnd_MS();

        long depStart_MS = 0;
        for (Edge link : graph.getParents(opId)) {
            long fromId = link.from;

            if (depStart_MS < opIdtoStartEnd_MS.get(fromId).b) {
                depStart_MS = opIdtoStartEnd_MS.get(fromId).b;
            }
        }
        depStart_MS = Math.max(depStart_MS, contFirstAvailTime);

        timeNow_MS+= depStart_MS;
        long startTime_MS = timeNow_MS;

        long networkDelay_MS=0;
        for (Edge link : graph.getParents(opId)) {
            long fromId = link.from;
            long fromContId  = assignments.get(fromId);
            Container fromCont = cluster.getContainer(fromContId);
            if (fromContId != contId) {
                //TODO ji better division
                long dtTime_MS = (long) (1000*( Math.ceil(link.data.size_B / RuntimeConstants.network_speed__B_SEC)));

                // Set network usage to containers
                long dtStart = timeNow_MS;
                long dtEnd = dtStart + dtTime_MS;
                fromCont.setUseDT(dtEnd);

                networkDelay_MS += dtTime_MS;
            }
        }
        timeNow_MS += networkDelay_MS;


        //TODO ji add disk delay?

        Operator op = graph.getOperator(opId);
        int runTime = (int) Math.ceil(op.getRunTime_MS() / cont.contType.container_CPU);
        timeNow_MS += runTime;

        long endTime_MS = timeNow_MS;



        opIdtoStartEnd_MS.put(opId,new Pair<>(startTime_MS,endTime_MS));
        cont.setUse(endTime_MS);
        cont.startofUse_MS = Math.min(cont.startofUse_MS,startTime_MS);

        cluster.contUsed.add(contId);

        cont.setUse(endTime_MS); //TODO ji do this better for backfilling

        stats = new Statistics(this);




        //        Boolean allAssigned;
//        readyOps.remove(opId);
//        for(Edge childEdge:graph.getChildren(opId)){
//            Long childopId = childEdge.to;
//            allAssigned = true;
//            for(Edge ParentChildEdge: graph.getParents(childopId)){
//                Long ParentChildOpId = ParentChildEdge.from;
//                if(!assignments.containsKey(ParentChildOpId)){
//                    allAssigned = false;
//                }
//            }
//            if(allAssigned) {
//                readyOps.add(childopId);
//            }
//        }

    }



    public Statistics calculateStatistics(){
        //TODO j implement
        return stats;
    }


    @Override
    public int compareTo(Plan other){  //TODO ji is this right?
        if(stats.runtime_MS == other.stats.runtime_MS){
            if(Double.compare(stats.money,other.stats.money)==0){
                return Long.compare(stats.containersUsed, other.stats.containersUsed);
            }
            return Double.compare(stats.money,other.stats.money);
        }else{
            return Long.compare(stats.runtime_MS,other.stats.runtime_MS);
        }
    }

    public void printInfo() {
//        System.out.println("------Plan Info----");
        StringBuilder i = new StringBuilder();
        i.append(stats.runtime_MS).append(" ")
            .append(stats.money).append(" ")
            .append("conts ").append(cluster.containersList.size()).append("  ");

        for(containerType ct: cluster.countTypes.keySet()){
            i.append(ct.name).append("(")
                .append(cluster.countTypes.get(ct))
                .append(")").append(" ");
        }


        System.out.println(i.toString());
//        System.out.println("------Plan Info END----");

    }

    public double getScore(Double alphaPar, Double mCost, Double mTime, Double k){
                    return (1.0-alphaPar)*stats.quanta+alphaPar*(stats.runtime_MS);
    }




}
