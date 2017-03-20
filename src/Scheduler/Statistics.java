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

    public Statistics(Plan plan){
        runtime_MS = 0;
        quanta = 0;
        money = 0;
        for(Container c:plan.cluster.containersList){
            runtime_MS = Math.max(c.UsedUpTo_MS,runtime_MS);
            int localQuanta = (int) Math.ceil((double)(c.UsedUpTo_MS-c.startofUse_MS)/RuntimeConstants.quantum_MS);
            if(localQuanta == 0 ){
                System.out.println("porep");
            }
//            if(c.UsedUpTo_MS>0){
//                localQuanta+=1;
//            }
            quanta+=localQuanta;
            money+=localQuanta*c.contType.container_price;
        }
        containersUsed = plan.cluster.contUsed.size();
    }


    public void printStats() {
        System.out.println("Time_MS: "+runtime_MS+" Money: "+money+" Quanta: "+quanta );
    }

    public Statistics(Statistics s){
        this.runtime_MS = s.runtime_MS;
        this.quanta = s.quanta;
        this.money = s.money;
        this.containersUsed = s.containersUsed;
    }

}
