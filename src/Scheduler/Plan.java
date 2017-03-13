package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Pair;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Plan implements Comparable<Plan> {
    public DAG graph;
    public HashMap<Long, Long> assignments;//opIdToContId;
    public HashMap<Long, ArrayList<Long>> contAssignments; //contId->list<opId>
    public HashMap<Long, ArrayList<Long>> contIdToOpIds;
    public Cluster cluster;
    public Statistics stats;
    Statistics beforeStats;
    String vmUpgrading;
    public long comcost = 0;
    public HashMap<Long, Pair<Long, Long>> opIdtoStartEnd_MS;
    public HashMap<Long, Long> opIdToRuntimeAssigned_MS; //runtime for the assigned container;
    public HashMap<Long, Long> opIdToRuntimeAssignedNODT_MS;
    public HashMap<Long, Long> opIdToearliestStartTime_MS; //not sure if we should use it
    public static boolean backfilling = false;


    public Plan(DAG graph, Cluster cluster) {
        this.graph = graph;
        beforeStats = null;
        assignments = new HashMap<>();
        contIdToOpIds = new HashMap<>();
        this.cluster = new Cluster(cluster);
        opIdtoStartEnd_MS = new HashMap<>();
        contAssignments = new HashMap<>();
        opIdtoStartEnd_MS = new HashMap<>();
        stats = new Statistics(this);
        opIdToearliestStartTime_MS = new HashMap<>();
        opIdToRuntimeAssigned_MS = new HashMap<>();
        opIdToRuntimeAssignedNODT_MS = new HashMap<>();

    }

    public Plan(Plan p) {
        this.vmUpgrading = p.vmUpgrading;
        this.graph = p.graph;
        this.beforeStats = p.beforeStats;
        assignments = new HashMap<>();
        for (Long key : p.assignments.keySet()) {
            assignments.put(key, p.assignments.get(key));
        }
        contIdToOpIds = new HashMap<>();
        for (Long key : p.contIdToOpIds.keySet()) {
            ArrayList<Long> list = new ArrayList<>();
            list.addAll(p.contIdToOpIds.get(key));
            contIdToOpIds.put(key, list);
        }
        contAssignments = new HashMap<>();
        for (Long cid : p.contAssignments.keySet()) {
            ArrayList<Long> t = new ArrayList<>();

            t.addAll(p.contAssignments.get(cid));


            contAssignments.put(cid, t);
        }
        opIdtoStartEnd_MS = new HashMap<>();
        for (long oid : p.opIdtoStartEnd_MS.keySet()) {
            opIdtoStartEnd_MS.put(oid,
                new Pair<>(p.opIdtoStartEnd_MS.get(oid).a, p.opIdtoStartEnd_MS.get(oid).b));
        }
        stats = new Statistics(p.stats);

        opIdToearliestStartTime_MS = new HashMap<>();
        for (Long opId : p.opIdToearliestStartTime_MS.keySet()) {
            opIdToearliestStartTime_MS.put(opId, p.opIdToearliestStartTime_MS.get(opId));
        }

        opIdToRuntimeAssigned_MS = new HashMap<>();
        for(Long opId: p.opIdToRuntimeAssigned_MS.keySet()){
            opIdToRuntimeAssigned_MS.put(opId,p.opIdToRuntimeAssigned_MS.get(opId));
        }

        opIdToRuntimeAssignedNODT_MS = new HashMap<>();
        for(Long opId: p.opIdToRuntimeAssignedNODT_MS.keySet()){
            opIdToRuntimeAssignedNODT_MS.put(opId,p.opIdToRuntimeAssignedNODT_MS.get(opId));
        }

        this.cluster = new Cluster(p.cluster);

    }

    public void assignOperator(Long opId, Long contId) {
        assignments.put(opId, contId);
        if (!contAssignments.containsKey(contId)) {
            contAssignments.put(contId, new ArrayList<Long>());
        }
        contAssignments.get(contId).add(opId);
        beforeStats = new Statistics(stats);

        long startTime_MS = 0L;
        long timeNow_MS = 0L;
        long startContTime_MS = 0L;

        Container cont = cluster.getContainer(contId);
        long contFirstAvailTime = cont.getFirstAvailTime_atEnd_MS();

        ////////////DEPENDENCIES//////////////////

        long depStart_MS = 0;
        for (Edge link : graph.getParents(opId)) {
            long fromId = link.from;

            if (depStart_MS < opIdtoStartEnd_MS.get(fromId).b) {
                depStart_MS = opIdtoStartEnd_MS.get(fromId).b;
            }
        }
        //        depStart_MS = Math.max(depStart_MS, contFirstAvailTime);

        timeNow_MS += depStart_MS;
        startTime_MS = timeNow_MS;             ////operator runtime starts now

        ////////////DATA TRANSFER TIMES//////////////////

        long networkDelay_MS = 0;
        for (Edge link : graph.getParents(opId)) {
            long fromId = link.from;
            long fromContId = assignments.get(fromId);
            if (fromContId != contId) {

                long dtTime_MS =
                    (long) (Math.ceil(link.data.size_B / RuntimeConstants.network_speed_B_MS));

                networkDelay_MS += dtTime_MS;
            }
        }
        timeNow_MS += networkDelay_MS;
        startContTime_MS = timeNow_MS;         //cont runtime starts now

        ///////////////DISK INPUT TIME//////////////////


        //        long inputDiskTime_MS = 0;
        //        for (Edge edge : graph.getParents(opId)) {
        //            long diskTime = (long) Math.ceil(edge.data.size_B / RuntimeConstants.disk_throughput_B_MS);
        //            inputDiskTime_MS += diskTime;
        //        }
        //
        //        timeNow_MS += inputDiskTime_MS;


        ///////////////////////////////////////////////////


        Operator op = graph.getOperator(opId);
        long runTime = (int) Math.ceil(op.getRunTime_MS() / cont.contType.container_CPU);
        timeNow_MS += runTime;

        ////////////////DISK OUT TIME/////////////////////////

        //        int outputDiskTime_MS = 0;
        //        long diskdata=0;
        //        for (Edge edge : graph.getChildren(opId)) {
        //            int diskTime = (int) Math.ceil(edge.data.size_B / RuntimeConstants.disk_throughput_B_MS);
        //            diskdata+=edge.data.size_B;
        //            outputDiskTime_MS += diskTime;
        //        }
        //
        //        timeNow_MS += outputDiskTime_MS;


        ///////////RUNTIME////////////////////////////////////


        long endTime_MS = timeNow_MS;

        long runspan = endTime_MS - startTime_MS;

        long runspanCont = endTime_MS - startContTime_MS;

        opIdToearliestStartTime_MS.put(opId, startTime_MS);

        ////////////////BACKFILLING/////////////////////////////////

        boolean backfilled = false;

        if (backfilling) {

            Collections.sort(cont.freeSlots); //sort the free slots from earliest to latest
            Slot toberemoved = null;

            for (int i = 0; i < cont.freeSlots.size() && !backfilled; ++i) {

                Slot fs = cont.freeSlots.get(i);

                if (fs.end_MS - fs.start_MS >= runspanCont && // if the slot is big enough
                    fs.start_MS >= startContTime_MS) { //and the slot starts after the earliest cont start time

                    backfilled = true;

                    if (startContTime_MS < fs.start_MS) {                     //free slot is not available when the op is ready
                        long pushForward = fs.start_MS - startContTime_MS;  //so we push the op start,contStart, end times
                        startContTime_MS += pushForward;
                        startTime_MS += pushForward;
                        endTime_MS += pushForward;

                    }

                    if (startContTime_MS > fs.start_MS) {                                             // add possible free Slot at start
                        cont.freeSlots.add(new Slot(fs.start_MS, startContTime_MS - 1));
                    }

                    if (endTime_MS < fs.end_MS - 1) {                                                     //and at end
                        cont.freeSlots.add(new Slot(endTime_MS + 1, fs.end_MS));
                    }

                    toberemoved = fs;

                }
                if (toberemoved != null) {
                    cont.freeSlots.remove(toberemoved);
                }
            }
        }

        ///////////////////NO BACKFILLING/////////////////////////

        if (!backfilling && !backfilled) {

            if (startContTime_MS < contFirstAvailTime) {                   //cont is not available when the op is ready
                long pushForward = contFirstAvailTime- startContTime_MS;  //so we push the op start,contStart, end times

                startContTime_MS += pushForward;
                startTime_MS += pushForward;
                endTime_MS += pushForward;

            } else {                                                          //if starContTime is after the cont was available
                if (contFirstAvailTime < startContTime_MS - 1) {              // add possible free Slot
                    cont.freeSlots.add(new Slot(contFirstAvailTime, startContTime_MS - 1));
                }
            }

        }
        //////////////////////////////////////////////////////////////////

        opIdtoStartEnd_MS.put(opId, new Pair<>(startTime_MS, endTime_MS)); //runtime + disk times + network delay

        opIdToRuntimeAssigned_MS.put(opId,endTime_MS - startTime_MS);

        opIdToRuntimeAssignedNODT_MS.put(opId,endTime_MS - startContTime_MS);

        cont.setUse(endTime_MS);
        cont.startofUse_MS = Math.min(cont.startofUse_MS,startContTime_MS);    //set the start of use at the container

        cluster.contUsed.add(contId);

        cont.opsschedule.add(new Slot(opId, startContTime_MS, endTime_MS)); //add a new scheduled slot for the operator

        stats = new Statistics(this);
    }

    @Override public int compareTo(Plan other) {  //TODO ji is this right?
        if (stats.runtime_MS == other.stats.runtime_MS) {
            if (Math.abs(stats.money - other.stats.money) < RuntimeConstants.precisionError) {
                return Long.compare(stats.containersUsed, other.stats.containersUsed);
            }
            return Double.compare(stats.money, other.stats.money);
        } else {
            return Long.compare(stats.runtime_MS, other.stats.runtime_MS);
        }
    }

    public void printInfo() {
        //        System.out.println("------Plan Info----");
        StringBuilder i = new StringBuilder();
        i.append(stats.runtime_MS).append(" ").append(stats.money).append(" ").append("conts ")
            .append(cluster.containersList.size()).append("  ");

        for (containerType ct : cluster.countTypes.keySet()) {
            i.append(ct.name).append("(").append(cluster.countTypes.get(ct)).append(")")
                .append(" ");
        }


        System.out.println(i.toString());
        //        System.out.println("------Plan Info END----");

    }

    public void printAssignments() {
//        for(Long contId: this.contAssignments.keySet()) {
//            System.out.println("cont " + contId + ": " + this.contAssignments.get(contId));
//             }
//
//        for(Long opId: opIdtoStartEnd_MS.keySet())
//            System.out.println( "op " + opId + " (" + (opIdtoStartEnd_MS.get(opId).b - opIdtoStartEnd_MS.get(opId).a) + ") [ " + opIdtoStartEnd_MS.get(opId).a + " - " + opIdtoStartEnd_MS.get(opId).b + " ]");

    }

    public double getScore(Double alphaPar, Double mCost, Double mTime, Double k) {
        return (1.0 - alphaPar) * stats.quanta + alphaPar * (stats.runtime_MS);
    }

}
