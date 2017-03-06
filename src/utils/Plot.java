package utils;

import Scheduler.Plan;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;


public class Plot extends ApplicationFrame {

    /**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param title  the frame title.
     */
    public Plot(final String title, ArrayList<Pair<Long,Double>> mydata) {

        super(title);
        final XYSeries series = new XYSeries("Random Data");
        for(Pair p: mydata ){
            series.add((Number)p.a,(Number)p.b);
        }

        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createScatterPlot(
            "Time/Money",
            "Time",
            "Money",
            data,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

    }


    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    *
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************

    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */

    public static void plotPoints(ArrayList<Pair<Long,Double>> a){
        final Plot demo = new Plot("Time/Money",a);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

    public static void plotPlans(String n,ArrayList<Plan> b) {
        ArrayList<Pair<Long,Double>> a = new ArrayList<>();
        for(Plan p: b){
            a.add(new Pair<Long, Double>(p.stats.runtime_MS,p.stats.money));
        }
        final Plot demo = new Plot(n, a);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }




    public static void main(final String[] args) {

        ArrayList<Pair<Long,Double>> a = new ArrayList<>();
        a.add(new Pair(1,1));
        a.add(new Pair(2,2));
        a.add(new Pair(3,3));
        a.add(new Pair(4,9));

        final Plot demo = new Plot("XY Series Demo",a);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}
