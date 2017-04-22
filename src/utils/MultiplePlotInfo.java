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
            xy.add(p.stats.money,p.stats.runtime_MS / 60000);
        }
        series.add(xy);
    }

    public void addPairs(String name, ArrayList<PairPlot> ps){
        XYSeries xy = new XYSeries(name);
        for(PairPlot<Long,Double> p:ps){
            xy.add(p.getMoney(),(p.getTime()/ 60000));
        }
        series.add(xy);
    }


}
