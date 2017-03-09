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
            xy.add((Number)p.stats.runtime_MS,(Number)p.stats.money);
        }
        series.add(xy);
    }

}
