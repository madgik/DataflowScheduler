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
