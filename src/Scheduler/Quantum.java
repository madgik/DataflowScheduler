package Scheduler;

import java.util.ArrayList;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Quantum {

    public Quantum(Quantum q) {
        usedUpTo = q.usedUpTo;
        slots = new ArrayList<>();
        for(Slot s:q.slots){
            slots.add(new Slot(s));
        }
        freeSlots = new ArrayList<>();
        for(Slot s:q.freeSlots){
            freeSlots.add(new Slot(s));
        }
    }

    class Slot{
        long opId;
        long start_MS;
        long end_MS;

        public Slot(long start_MS,long end_MS){
            this.start_MS = start_MS;
            this.end_MS = end_MS;
            this.opId = -1L;
        }

        public Slot(long opId,long start_MS,long end_MS){
            this.start_MS = start_MS;
            this.end_MS = end_MS;
            this.opId = opId;
        }

        public Slot(Slot s) {
            opId = s.opId;
            start_MS = s.start_MS;
            end_MS = s.end_MS;
        }
    }
    ArrayList<Slot> slots;
    ArrayList<Slot> freeSlots;
    long usedUpTo;

    public Quantum(){
        usedUpTo = 0;
        slots=new ArrayList<>();
        freeSlots = new ArrayList<>();
        freeSlots.add(new Slot(0,RuntimeConstants.quantum_MS));
    }



}
