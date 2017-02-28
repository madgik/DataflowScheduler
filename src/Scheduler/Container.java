package Scheduler;

import java.util.ArrayList;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Container {
    public long id;
    public String name;
    public containerType contType;
    public ArrayList<Quantum> opsschedule;
    long UsedUpTo_MS;
    long UsedUpToDT_MS;
    int lastQuantum;


    public Container(long cid,containerType ctype){
        id=cid;
        lastQuantum=-1;
        opsschedule = new ArrayList<>();
        contType = ctype;
        name = "c"+ String.valueOf(cid);
        UsedUpTo_MS = 0;
        UsedUpToDT_MS = 0;
    }

    public void setUseDT(long time){
        UsedUpToDT_MS = Math.max(UsedUpToDT_MS,time);
    }
    public void setUse(long time){
        UsedUpTo_MS = Math.max(UsedUpTo_MS,time);
    }

    public void assignOp(Long opId) {
    }

    public Long getEnd_MS(){
        return UsedUpTo_MS;

//        if(lastQuantum == -1){
//            return 0L;
//        }
//        return opsschedule.get(lastQuantum).usedUpTo;

    }




}
