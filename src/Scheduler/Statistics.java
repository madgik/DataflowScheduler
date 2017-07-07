package Scheduler;

import java.util.HashMap;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Statistics {


    public long runtime_MS;
    public int quanta;
    public double money;
    public long containersUsed;
    public double contUtilization;

    public HashMap  <Long, Long> subdagStartTime = new HashMap<>();//dagId, time
    public HashMap  <Long, Long> subdagFinishTime = new HashMap<>();//dagId, time
    public HashMap  <Long, Long> subdagMakespan = new HashMap<>();//dagId, time
    public Double subdagMeanMakespan;//dagId, time

    public Statistics(Plan plan){
        runtime_MS = 0;
        quanta = 0;
        money = 0;
        contUtilization=0.0;
        for(Container c:plan.cluster.containersList){
            if(!plan.cluster.contUsed.contains(c.id)){
                if(c.startofUse_MS>-1){
                    try {
                        throw new Exception("BUGSSS");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                continue;}
            runtime_MS = Math.max(c.UsedUpTo_MS,runtime_MS);
            int localQuanta = (int) Math.ceil((double)(c.UsedUpTo_MS-c.startofUse_MS)/RuntimeConstants.quantum_MS);
            if(localQuanta == 0 ){
                System.out.println("NO QUANTA IS USED IN CONTAINER");
            }
//            if(c.UsedUpTo_MS>0){
//                localQuanta+=1;
//            }
            quanta+=localQuanta;
            money+=localQuanta*c.contType.container_price;
        }
        containersUsed = plan.cluster.contUsed.size();







        long usedTimeSum =0;
        long freeTimeSum = 0;
        double minUtil = 10.0;
        double maxUtil = -5.0;
        double AvgUtil= 0;
        double UtilQuantum = 0;
        int countfs =0;
        double AvgQUtil = 0;
        double MinQUtil = 10.0;
        double MaxQUtil = -5.0;



        for(Container c:plan.cluster.containersList){
            long dtTime = 0;
            long proTime = 0;
            long contTime = 0;
            long ftime = 0;
            double Util = 0.0;
            double Util2 = 0.0;
            double QUtil = 0.0;
            double Util3;



            for(Long opId: plan.assignments.keySet()){
                if(plan.assignments.get(opId) == c.id){
                  //  System.out.println(plan.opIdtoStartEndProcessing_MS.get(opId).a);
                    dtTime += plan.opIdToBeforeDTDuration_MS.get(opId) + plan.opIdToAfterDTDuration_MS.get(opId);
                    proTime += plan.opIdToProcessingTime_MS.get(opId);
                }
            }
            contTime = c.UsedUpTo_MS - c.startofUse_MS;
            Util = (double)(dtTime+proTime) / (double)contTime;

            int quantaUsed = (int) Math.ceil((double)(c.UsedUpTo_MS-c.startofUse_MS)/RuntimeConstants.quantum_MS);
            QUtil = (double)(dtTime+proTime) / (double)(quantaUsed*RuntimeConstants.quantum_MS);




            for(Slot s: c.freeSlots){
                countfs++;
                freeTimeSum+=s.end_MS - s.start_MS;
                ftime += s.end_MS - s.start_MS;

            }

            Util2 = (double)(contTime - ftime) / (double)contTime;

            long o=0;

            for(Slot s: c.opsschedule){
                usedTimeSum+= s.end_MS - s.start_MS;
                o += s.end_MS - s.start_MS;

            }

            Util3 = (double)(o) / (double)contTime;


            if(Math.abs(Util-Util2)>0.2 || Math.abs(Util-Util3)>0.2 || Math.abs(Util3-Util2)>0.2 ){
                try {
                    throw new Exception("Problem!!!");
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }



            minUtil = Math.min(minUtil,Util);
            maxUtil = Math.max(maxUtil,Util);

            MinQUtil = Math.min(MinQUtil,QUtil);
            MaxQUtil = Math.max(MaxQUtil,QUtil);


            AvgQUtil+=QUtil;
            AvgUtil+=Util;

        }


        int a = plan.cluster.containersList.size();
        int b = plan.cluster.contUsed.size();

        AvgUtil = (double) AvgUtil / plan.cluster.contUsed.size();
        AvgQUtil = (double) AvgQUtil / plan.cluster.contUsed.size();



        contUtilization = AvgUtil;



      //  for the case of ensemble

        if(plan.graph.superDAG.merged) {//add a constraint to compute them only when all dags have been scheduled. not for partial plans



//////////////////

       //     System.out.println("\n\nStatistics");

            for(Long opId: plan.assignments.keySet()){//   for(Long opId : plan.graph.operators.keySet()){//for( Long contId: p0.contAssignments.keySet()) {

                Long dId = plan.graph.operators.get(opId).dagID-1;


                Long minStartTime = Long.MAX_VALUE;
                Long maxEndTime = Long.MIN_VALUE;

                if(subdagStartTime.isEmpty() || !subdagStartTime.containsKey(dId)) {
                //    System.out.println(plan.opIdtoStartEndProcessing_MS.get(opId).a);
                    minStartTime = plan.opIdtoStartEndProcessing_MS.get(opId).a;//startTime
                    maxEndTime = plan.opIdtoStartEndProcessing_MS.get(opId).b;//startTime

                    subdagStartTime.put(dId, minStartTime);
                    subdagFinishTime.put(dId, maxEndTime);
                 //   minCostEnsemble.put(dId, minMoney);

                }
                else {

                    Long ts = subdagStartTime.get(dId);
                    Long te = subdagFinishTime.get(dId);
                  //  Double c = minCostEnsemble.get(dId);

                    //p0.opIdtoStartEndProcessing_MS.get(opId).b //finish time
                    minStartTime = Math.min(ts, plan.opIdtoStartEndProcessing_MS.get(opId).a);//startTime
                    maxEndTime = Math.max(te, plan.opIdtoStartEndProcessing_MS.get(opId).b);//startTime

                    subdagStartTime.put(dId, minStartTime);
                    subdagFinishTime.put(dId, maxEndTime);
               //     minCostEnsemble.put(dId, minMoney);


                }


                //  System.out.println(" added " + dId + " " + maxEndTimeEnsemble.get(dId) + " " + maxEndTime);

            }


            Long meanMakespan = 0L;
            for(Long dgId: subdagFinishTime.keySet()) {
                subdagMakespan.put(dgId, subdagFinishTime.get(dgId) - subdagStartTime.get(dgId));
                meanMakespan += subdagFinishTime.get(dgId) - subdagStartTime.get(dgId);
            }

            if(subdagFinishTime.size()>0)
            subdagMeanMakespan = meanMakespan/(double)subdagFinishTime.size();
//            {

               // System.out.println("dag " + dgId + " makespan "  + makespanDag + " starts " +  subdagStartTime.get(dgId) + " ends " + subdagFinishTime.get(dgId));

//            }


        }
////////////////////

    }


    public void printStats() {
        System.out.println("Time_MS: "+runtime_MS+" Money: "+money+" Quanta: "+quanta );
    }

    public Statistics(Statistics s){
        this.runtime_MS = s.runtime_MS;
        this.quanta = s.quanta;
        this.money = s.money;
        this.containersUsed = s.containersUsed;
        this.contUtilization = s.contUtilization;
    }

}
