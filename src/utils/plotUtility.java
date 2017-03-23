package utils;

import Scheduler.Plan;
import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnchronis on 3/20/17.
 */
public class plotUtility {

    public plotUtility(){};

    public  void plotPoints(String n,ArrayList<Pair<Long,Double>> a){
        final Plot demo = new Plot(n,a);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

    public  void plotPlans(String n,List<Plan> b) {
        ArrayList<Pair<Long,Double>> a = new ArrayList<>();
        for(Plan p: b){
            a.add(new Pair<Long, Double>(p.stats.runtime_MS,p.stats.money));
        }
        final Plot demo = new Plot(n, a);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }


    public  void plotMultiple(MultiplePlotInfo info,String name){
        final Plot demo = new Plot(info,name);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }
}
