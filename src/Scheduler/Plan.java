package Scheduler;

import Graph.DAG;
import Graph.Edge;
import Graph.Operator;
import utils.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

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

    public HashMap<Long, Pair<Long, Long>> opIdtoStartEndProcessing_MS;

    public HashMap<Long, Long> opIdToContainerRuntime_MS; //runtime for the assigned container;
    public HashMap<Long, Long> opIdToProcessingTime_MS;

    public HashMap<Long, Long> opIdToearliestStartTime_MS; //not sure if we should use it

    public HashMap<Long,Long> opIdToBeforeDTDuration_MS;

    public HashMap<Long,Long> opIdToAfterDTDuration_MS;



    public Plan(DAG graph, Cluster cluster) {
        this.graph = graph;
        beforeStats = null;
        assignments = new HashMap<>();
        contIdToOpIds = new HashMap<>();
        this.cluster = new Cluster(cluster);
        opIdtoStartEndProcessing_MS = new HashMap<>();
        contAssignments = new HashMap<>();
        opIdtoStartEndProcessing_MS = new HashMap<>();
        stats = new Statistics(this);
        opIdToearliestStartTime_MS = new HashMap<>();
        opIdToContainerRuntime_MS = new HashMap<>();
        opIdToProcessingTime_MS = new HashMap<>();
        opIdToBeforeDTDuration_MS = new HashMap<>();
        opIdToAfterDTDuration_MS = new HashMap<>();

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
        opIdtoStartEndProcessing_MS = new HashMap<>();
        for (long oid : p.opIdtoStartEndProcessing_MS.keySet()) {
            opIdtoStartEndProcessing_MS.put(oid,
                new Pair<>(p.opIdtoStartEndProcessing_MS.get(oid).a, p.opIdtoStartEndProcessing_MS.get(oid).b));
        }
        stats = new Statistics(p.stats);

        opIdToearliestStartTime_MS = new HashMap<>();
        for (Long opId : p.opIdToearliestStartTime_MS.keySet()) {
            opIdToearliestStartTime_MS.put(opId, p.opIdToearliestStartTime_MS.get(opId));
        }

        opIdToContainerRuntime_MS = new HashMap<>();
        for(Long opId: p.opIdToContainerRuntime_MS.keySet()){
            opIdToContainerRuntime_MS.put(opId,p.opIdToContainerRuntime_MS.get(opId));
        }

        opIdToProcessingTime_MS = new HashMap<>();
        for(Long opId: p.opIdToProcessingTime_MS.keySet()){
            opIdToProcessingTime_MS.put(opId,p.opIdToProcessingTime_MS.get(opId));
        }

        opIdToBeforeDTDuration_MS = new HashMap<>();
        for(Long opId: p.opIdToBeforeDTDuration_MS.keySet()){
            opIdToBeforeDTDuration_MS.put(opId,p.opIdToBeforeDTDuration_MS.get(opId));
        }

        opIdToAfterDTDuration_MS = new HashMap<>();
        for(Long opId: p.opIdToAfterDTDuration_MS.keySet()){
            opIdToAfterDTDuration_MS.put(opId,p.opIdToAfterDTDuration_MS.get(opId));
        }

        this.cluster = new Cluster(p.cluster);

    }

    public void assignOperator(Long opId, Long contId, boolean backfilling) {
        assignments.put(opId, contId);
        if (!contAssignments.containsKey(contId)) {
            contAssignments.put(contId, new ArrayList<Long>());
        }
        contAssignments.get(contId).add(opId);
        beforeStats = new Statistics(stats);

        long startProcessingTime_MS = 0L;
        long endProcessingTime_MS = 0L;

        long beforeDTDuration_MS = 0L;
        long afterDTDuration_MS = 0L;

        long opProcessingDuration_MS = 0L;
        long contOpDuration_MS = 0L;

        long startTimeCont_MS =0L;
        long endTimeCont_MS = 0L; //endTime + max dt duration

        long dependenciesEnd_MS = 0;

        long earliestStartTime_MS = 0L;


        Container cont = cluster.getContainer(contId);
        long contFirstAvailTime_MS = cont.getFirstAvailTime_atEnd_MS();

        ////////////DEPENDENCIES//////////////////


        for (Edge link : graph.getParents(opId)) {              //calculate the max(ParentFS+dtTime)
            long fromId = link.from;

            long fromOpEndTimePLUSDTTime =
                opIdtoStartEndProcessing_MS.get(fromId).b + calculateDelayDistributedStorage(fromId,opId,contId);

            dependenciesEnd_MS = Math.max(dependenciesEnd_MS,fromOpEndTimePLUSDTTime+1);

        }


        //        timeNow_MS += depStart_MS;
//
//        startProcessingTime_MS = timeNow_MS;             ////operator runtime starts now

        ////////////DATA TRANSFER TIMES//////////////////

//        long networkDelay_MS = 0;
//        for (Edge link : graph.getParents(opId)) {
//            long fromId = link.from;
//            long fromContId = assignments.get(fromId);
//            if (fromContId != contId) {
//
//                long dtTime_MS =
//                    (long) (Math.ceil(link.data.size_B / RuntimeConstants.network_speed_B_MS));
//
//                networkDelay_MS += dtTime_MS;
//            }
//        }
//        timeNow_MS += networkDelay_MS;
//        startContTime_MS = timeNow_MS;         //cont runtime starts now

        ///////////////BRING DATA TO OPERATOR MACHINE//////////////////


        for (Edge edge : graph.getParents(opId)) {
            long transferTime = (long) Math.ceil(edge.data.size_B / RuntimeConstants.distributed_storage_speed_B_MS);

            beforeDTDuration_MS = Math.max(beforeDTDuration_MS, transferTime);
        }


        ///////////////OPERATOR PROCESS TIME///////////////


        Operator op = graph.getOperator(opId);
        opProcessingDuration_MS = (int) Math.ceil(op.getRunTime_MS() / cont.contType.container_CPU);

        ////////////////SEND DATA TO DISTRIBUTED STORAGE ////////////////////

        for (Edge edge : graph.getChildren(opId)) {
            int transferTime = (int) Math.ceil(edge.data.size_B / RuntimeConstants.distributed_storage_speed_B_MS);
            afterDTDuration_MS = Math.max(afterDTDuration_MS,transferTime);
        }

        //////////////////INFO///////////////////////


        contOpDuration_MS = beforeDTDuration_MS + opProcessingDuration_MS + afterDTDuration_MS;

        earliestStartTime_MS = dependenciesEnd_MS + 1;

        opIdToearliestStartTime_MS.put(opId, dependenciesEnd_MS+1);


        ////////////////BACKFILLING/////////////////////////////////

        boolean backfilled = false;

        if (backfilling) {

            Collections.sort(cont.freeSlots); //sort the free slots from earliest to latest
            Slot toberemoved = null;

            for (int i = 0; i < cont.freeSlots.size() && !backfilled; ++i) {

                Slot fs = cont.freeSlots.get(i);

                if (  fs.end_MS-fs.start_MS >= opProcessingDuration_MS &&
                    earliestStartTime_MS + beforeDTDuration_MS <= fs.end_MS - opProcessingDuration_MS){
//                    Math.max(fs.start_MS,earliestStartTime_MS) + opProcessingDuration_MS >= fs.end_MS ){ //if operator fits

                    backfilled = true;

                    long pushForward = 0L;
                    if (earliestStartTime_MS + beforeDTDuration_MS < fs.start_MS) {      //free slot is not available when the op is ready
                        pushForward = fs.start_MS - (earliestStartTime_MS + beforeDTDuration_MS);   //so we push the op start,contStart, end times
                    }

                    startTimeCont_MS = earliestStartTime_MS + pushForward;

                    endTimeCont_MS = startTimeCont_MS + contOpDuration_MS;
                    startProcessingTime_MS = startTimeCont_MS + beforeDTDuration_MS;
                    endProcessingTime_MS = startProcessingTime_MS + opProcessingDuration_MS;


                    if (startProcessingTime_MS > fs.start_MS + 1) {                                             // add possible free Slot at start
                        cont.freeSlots.add(new Slot(fs.start_MS, startProcessingTime_MS - 1));
                    }

                    if (endProcessingTime_MS < fs.end_MS - 1) {                                                     //and at end
                        cont.freeSlots.add(new Slot(endProcessingTime_MS + 1, fs.end_MS));
                    }

                    toberemoved = fs;

                }
                if (toberemoved != null) {
                    cont.freeSlots.remove(toberemoved);
                }
            }
        }

        ///////////////////NO BACKFILLING/////////////////////////

        if (!backfilling || (backfilling && !backfilled)) {

            long pushForward = 0L;
            if (earliestStartTime_MS + beforeDTDuration_MS < contFirstAvailTime_MS) {                   //cont is not available when the op is ready
                pushForward = contFirstAvailTime_MS - (earliestStartTime_MS + beforeDTDuration_MS);  //so we push the op start,contStart, end times

            } else if( earliestStartTime_MS > contFirstAvailTime_MS ){           //if starContTime is after the cont was available
                         // add possible free Slot
                cont.freeSlots.add(new Slot(contFirstAvailTime_MS,earliestStartTime_MS+beforeDTDuration_MS));
            }


            startTimeCont_MS = earliestStartTime_MS + pushForward;
            endTimeCont_MS = startTimeCont_MS + contOpDuration_MS;

            startProcessingTime_MS = startTimeCont_MS + beforeDTDuration_MS ;
            endProcessingTime_MS = startProcessingTime_MS + opProcessingDuration_MS ;


        }
        //////////////////////////////////////////////////////////////////


        /////set start and end time for the container
        cont.setStartDT(startTimeCont_MS);
        cont.setUsedUpToDT(endTimeCont_MS);
        cont.setStart(startProcessingTime_MS);
        cont.setUsedUpTo(endProcessingTime_MS);

        ////set start end times for operator////////
        opIdtoStartEndProcessing_MS.put(opId, new Pair<>(startProcessingTime_MS, endProcessingTime_MS)); //processing time ONLY
        opIdToContainerRuntime_MS.put(opId,contOpDuration_MS);
        opIdToProcessingTime_MS.put(opId,opProcessingDuration_MS);
        opIdToBeforeDTDuration_MS.put(opId,beforeDTDuration_MS);
        opIdToAfterDTDuration_MS.put(opId,afterDTDuration_MS);

        ///////set used In cluster
        cluster.contUsed.add(contId);

        //////add scheduled Slot////////
        cont.opsschedule.add(new Slot(opId, startProcessingTime_MS, endProcessingTime_MS)); //add a new scheduled slot for the operator

        //////Update Stats
        stats = new Statistics(this);

    }


    public void updateSlots(Operator op, Plan plan) {


        Long opId = op.getId();
        HashMap <Long, Long> opLST = new HashMap<>();

        LinkedList<Long> opsToUpdate = new LinkedList<>();
        opsToUpdate.addLast(opId);

        while(!opsToUpdate.isEmpty()) {

            Long opIdToUpdate= opsToUpdate.removeFirst();

            //add succ at vm
            Long succId=-1L;
            Long succStartTime=Long.MAX_VALUE;

            Long predId=-1L;
            Long predStartTime=Long.MIN_VALUE;

            //TODO: use any new data structures instead to find succ/predecessor at vm

            //find the successor and predecessor (if any) at the assigned vm
            for(Long opIdNext: plan.contAssignments.keySet())
            {
                if(plan.opIdtoStartEndProcessing_MS.get(opIdNext).a>=plan.opIdtoStartEndProcessing_MS.get(opId).a);
                if(plan.opIdtoStartEndProcessing_MS.get(opIdNext).a<=succStartTime) {
                    succId=opIdNext;
                    succStartTime= plan.opIdtoStartEndProcessing_MS.get(opIdNext).a;
                }


                if(plan.opIdtoStartEndProcessing_MS.get(opIdNext).a<plan.opIdtoStartEndProcessing_MS.get(opId).a);
                if(plan.opIdtoStartEndProcessing_MS.get(opIdNext).a>=predStartTime) {
                    predId=opIdNext;
                    predStartTime= plan.opIdtoStartEndProcessing_MS.get(opIdNext).a;
                }
            }

            //add succesor at vm to the list for updating
            if(succId>=0L)
            opsToUpdate.add(succId);

//add children to the list for updating
            if(!graph.getChildren(opId).isEmpty())
                for (Edge outEdge : graph.getChildren(opId)) {
                    succId = outEdge.to;
                    if(!opsToUpdate.contains(succId))
                        opsToUpdate.addLast(succId);
                }

//if there is a predecessor at vm est is the finish time of the predecessor
            Long est=0L;
            if(predId>=0)
                est= plan.opIdtoStartEndProcessing_MS.get(predId).b;

            //update est if the op has any parents. TODO: data transfer 0 if they are assigned at the new VM after "migration"
            for(Edge inEdge:graph.getParents(opId)){
                predId = inEdge.from;
                Long predEndTime = plan.opIdtoStartEndProcessing_MS.get(predId).b +
                        plan.opIdToAfterDTDuration_MS.get(predId) +
                        plan.opIdToBeforeDTDuration_MS.get(opId);
                est = Math.max(predEndTime,est);
            }

            //new ctype assigned
            containerType cType = plan.cluster.getContainer(plan.assignments.get(opId)).contType;
            long opProcessingDuration_MS = (int) Math.ceil(op.getRunTime_MS() / cType.container_CPU);
            plan.opIdToProcessingTime_MS.put(opId,opProcessingDuration_MS );
            plan.opIdtoStartEndProcessing_MS.put(opId, new Pair<>(est, est+opProcessingDuration_MS));
            //update startendstructures with data transfers
        }

    }

    public Long calculateNetworkDelayBetweenOps(Long parentId, Long childId){
        long netdelay = 0L;
        if(assignments.get(parentId) == assignments.get(childId)){
            return netdelay;
        }
        for(Edge e: graph.edges.get(parentId)){
            if(e.to == childId){
                netdelay =  (long) (Math.ceil(e.data.size_B / RuntimeConstants.network_speed_B_MS));
                break;
            }
        }
        return netdelay;
    }

    public long calculateNetworkDelayBetweenOps(Long parentId, Long contParentId, Long childId, Long contChildId){
        long netdelay = 0L;
        if(contParentId == contChildId){
            return netdelay;
        }
        for(Edge e: graph.edges.get(parentId)){
            if(e.to == childId){
                netdelay =  (long) (Math.ceil(e.data.size_B / RuntimeConstants.network_speed_B_MS));
                break;
            }
        }
        return netdelay;
    }

    public Long calculateDelayDistributedStorage(Long parentId, Long childId, Long childContId){

        if(assignments.get(parentId) == childContId){ //remove to transfer always to distributed storage
            return 0L;
        }
        return (long) Math.ceil(graph.getEdge(parentId,childId).data.size_B / RuntimeConstants.distributed_storage_speed_B_MS);
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
//        }
//
//        for(Long opId: opIdtoStartEnd_MS.keySet()) {
//            System.out.println("op " + opId + " (" + (opIdtoStartEnd_MS.get(opId).b - opIdtoStartEnd_MS.get(opId).a) + ") [ " + opIdtoStartEnd_MS.get(opId).a + " - " + opIdtoStartEnd_MS.get(opId).b + " ]");
//            System.out.println("dataTransfer for op " + opId + " (" + (dataTransfer_MS.get(opId).b - dataTransfer_MS.get(opId).a) + ") [ " + dataTransfer_MS.get(opId).a + " - " + dataTransfer_MS.get(opId).b + " ]");
//
//        }

    }

    public double getScore(Double alphaPar, Double mCost, Double mTime, Double k) {
        return (1.0 - alphaPar) * stats.quanta + alphaPar * (stats.runtime_MS);
    }

}
