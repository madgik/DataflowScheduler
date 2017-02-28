//package Scheduler;
//
//import java.util.ArrayList;
//import java.util.Collections;
//
///**
// * Created by johnchronis on 2/19/17.
// */
//public class BagOfPlans {
//    public ArrayList<Plan> plans;
//    public ArrayList<Plan> skyline;
//
//    public BagOfPlans(){
//        plans = new ArrayList<>();
//        skyline = new ArrayList<>();
//    }
//
//    public ArrayList<Plan> computerSkyline(){
//        skyline.clear();
//
//        Collections.sort(plans); // Sort by time breaking equality by sorting by money
//
//        Plan previous = null;
//        for (Plan est : plans) {
//            if (previous == null) {
//                skyline.add(est);
//                previous = est;
//                continue;
//            }
//            if (previous.stats.runtime_MS == est.stats.runtime_MS) {
//                // Already sorted by money
//                continue;
//            }
//            if(Math.abs(previous.stats.money - est.stats.money)>0.000000000001) //TODO ji fix or check
//                if (previous.stats.money > est.stats.money) {//use Double.compare. at moheft as well or add precision error
//                    skyline.add(est);
//                    previous = est;
//                }
//        }
//
//
//
//        return skyline;
//    }
//
//
//
//
//}
