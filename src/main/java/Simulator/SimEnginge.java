package main.java.Simulator;

import main.java.Graph.Edge;
import main.java.Scheduler.Plan;
import main.java.Scheduler.RuntimeConstants;
import main.java.utils.Pair;

import java.util.*;

/**
 * Created by johnchronis on 6/18/16.
 */
public class SimEnginge {

    ExecutionState state ;

    public SimEnginge() {}

    private void findNextReadyOps(HashSet<Long> readyOps,HashSet<Long> opsAssignedSet, Long justScheduledOpId ){
        Boolean allAssigned;               //find new readyops
        readyOps.remove(justScheduledOpId);
        opsAssignedSet.add(justScheduledOpId);
        for (Edge childEdge : state.plan.graph.getChildren(justScheduledOpId)) {
            Long childopId = childEdge.to;
            allAssigned = true;
            for (Edge ParentChildEdge : state.plan.graph.getParents(childopId)) {
                Long ParentChildOpId = ParentChildEdge.from;
                if (!opsAssignedSet.contains(ParentChildOpId)) {
                    allAssigned = false;
                }
            }
            if (allAssigned) {
                readyOps.add(childopId);
            }
        }
    }

    public void execute(Plan p){

        Long clock = 0L;

        this.state = new ExecutionState(p);

        state.findRoots();

        ArrayList<Long> toScheduleOps = new ArrayList<>();

        while ( !state.hasplanterminated(clock) ) {

            toScheduleOps.addAll(state.getNowOps(clock));

            toScheduleOps.sort(new Comparator<Long>() {
                @Override public int compare(Long o1, Long o2) {
                    return (int)(state.plan.opIdtoStartEndProcessing_MS.get(o1).a - state.plan.opIdtoStartEndProcessing_MS.get(o2).a);
                }
            });

            ArrayList notScheduled = new ArrayList();
            for (Long nextOpid : toScheduleOps) {

                Long contID = state.plan.assignments.get(nextOpid);

                if ( state.canSchedule(nextOpid,contID,clock)){

//                    System.out.println("Master schedules operator " + nextOpid );

                    state.schedule(nextOpid,contID,clock);

                }else{
                    notScheduled.add(nextOpid);
                }
            }
            toScheduleOps = notScheduled;
            notScheduled.clear();

            clock = state.calculateNextClock(clock);

            state.solveDependencies(clock);

        }

        double money = calculateMoney();

        System.out.println("SimEngine plan simulated (sched,sim) Time: ("+state.plan.stats.runtime_MS+","+state.masterEndTime_ms+") Money: ("
        +state.plan.stats.money +","+money +")");


//        System.out.println("//////////plan");
//        for(Container c : p.cluster.containersList){
//            System.out.println(c.id + " "+ c.contType + " " + c.contType.container_price + " "+
//                c.UsedUpTo_MS+ " " +c.startofUse_MS+ " " + (c.UsedUpTo_MS-c.startofUse_MS) +
//                (int) Math.ceil((double)(c.UsedUpTo_MS-c.startofUse_MS)/RuntimeConstants.quantum_MS) + " "+
//                (int) Math.ceil((double)(c.UsedUpTo_MS-c.startofUse_MS)/RuntimeConstants.quantum_MS) * c.contType.container_price);
//        }
//        System.out.println("/////////sim");
//        for(Long cid : state.workerStartEndTime_MS.keySet()){
//            Container pcont = state.plan.cluster.getContainer(cid);
//            Pair<Long,Long> pair = state.workerStartEndTime_MS.get(cid);
//            System.out.println(cid + " "+ pcont.contType + " " + pcont.contType.container_price + " "+
//                (pair.b)+ " " +(pair.a)+ " " + (pair.b - pair.a) +
//                (int) Math.ceil((double)(pair.b - pair.a)/RuntimeConstants.quantum_MS) + " "+
//                (int) Math.ceil((double)(pair.b - pair.a)/RuntimeConstants.quantum_MS) * pcont.contType.container_price);
//        }

    }

    private double calculateMoney() {
        int quanta = 0;
        double money = 0.0;
        for(Long cid :state.workerStartEndTime_MS.keySet()){
            Pair<Long,Long> se = state.workerStartEndTime_MS.get(cid);


            int localQuanta = (int) Math.ceil((double)(se.b-se.a)/ RuntimeConstants.quantum_MS);

            quanta+=localQuanta;
            money+=localQuanta* state.plan.cluster.getContainer(cid).contType.container_price;
        }
        return money;
    }



}

