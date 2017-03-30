package utils;

import Scheduler.Plan;
import Scheduler.SolutionSpace;
import org.jfree.chart.ChartUtilities;
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


    public  void plotMultiple(MultiplePlotInfo info,String name,String path, Boolean save){
        final Plot demo = new Plot(info,name, path,  save);

        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

    public  void plotMultipleWithLine(SolutionSpace combined,ArrayList<Pair<String,Double>> legendInfo ,MultiplePlotInfo info,String name,String path, Boolean save,Boolean show){
        final Plot demo = new Plot(combined,legendInfo,info,name, path,save,show);
        if(show) {
            demo.pack();
            RefineryUtilities.centerFrameOnScreen(demo);
            demo.setVisible(true);
        }


//

    }
}
