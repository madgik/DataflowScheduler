package utils;

import Scheduler.Plan;
import org.jfree.data.xy.XYSeries;

import java.util.ArrayList;

/**
 * Created by johnchronis on 3/9/17.
 */
public class MultiplePlotInfo {
    ArrayList<XYSeries> series ;

    public MultiplePlotInfo(){
        series = new ArrayList<>();
    }

    public void add(String name, ArrayList<Plan> ps){
        XYSeries xy = new XYSeries(name);
        for(Plan p:ps){
            xy.add((p.stats.runtime_MS / 60000),(Double)p.stats.money);
        }
        series.add(xy);
    }

    public void addPairs(String name, ArrayList<Pair<Long,Double>> ps){
        XYSeries xy = new XYSeries(name);
        for(Pair<Long,Double> p:ps){
            xy.add((double)(p.a / 60000),(Double)p.b);
        }
        series.add(xy);
    }


}
