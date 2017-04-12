//package Simulator;
//
//
//import Scheduler.Plan;
//import Scheduler.RuntimeConstants;
//import utils.Tuple2;
//import utils.Tuple3;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//
///**
// * Created by johnchronis on 14/6/2016.
// */
//public class ExecutionInfo { //all times are in milliseconds unless stated otherwise
//
//    Plan planGraph;
//    public long quantum_ms = RuntimeConstants.quantum_MS;
//    HashMap<String, Tuple2<String, Integer>> physicalWorkers;
//
//
//    //////////////////////////////////////master_stats////////////////////////
//    public long masterStartTime_ms;
//    public long masterEndTime_ms;
//    public long masterExecutionTime_ms;
//    public double masterExecutionTime_sec;
//
//    public HashMap<Long, Tuple3<Long,Long,Long>> mIdtoTimes; //id->start,end,total in ms
//
//    //////////////////////////////////////worker_stats////////////////////////
//    public long workerStartTime_F_ms;
//    public long workerEndTime_L_ms;
//    public long totalWorkerExecutionTime_ms;
//    public double totalWorkerExecutionTime_sec;
//    public long totalWorkerExecutionTime_StartofFirstEndofLast_ms;
//    public double totalWorkerExecutionTime_StartofFirstEndofLast_sec;
//
//    public HashMap<Long, Tuple3<Long,Long,Long>> wIdtoTimes; //id->start,end,total in ms
//
//
//    //////////////////////////////////////////////////////////////////////////
//
//    public int SchedTime;
//    public int SchedMon;
//    public int SimMoney;
//
//    //helpers
//    public ArrayList<ArrayList<Long>> levelsofExecution;
//
//
//    public ExecutionInfo(HashMap<String, Tuple2<String, Integer>> physicalWorkers, Plan planGraph) {
//        this.planGraph = planGraph;
//        this.physicalWorkers = physicalWorkers;
//
//        mIdtoTimes = new HashMap<>();
//        wIdtoTimes = new HashMap<>();
//
//        levelsofExecution =  new ArrayList<>();
//    }
//
//
//    public void setSchedTM(int time, int mon) {
//        SchedTime = time;
//        SchedMon = mon;
//    }
//
//    public void opStarter_master(long id, long timeS) {
//        mIdtoTimes.put(id,new Tuple3<Long, Long, Long>(timeS,-1L,-1L));
//    }
//    public void opEnd_master(long id, long timeE) {
//        mIdtoTimes.get(id).y = timeE;
//        mIdtoTimes.get(id).z = timeE - mIdtoTimes.get(id).x;
//    }
//    public void opTimes_worker(long id, long timeS, long timeE, long timeT) {
//        wIdtoTimes.put(id,new Tuple3<Long, Long, Long>(timeS,timeE,timeT));
//    }
//
//
//}
