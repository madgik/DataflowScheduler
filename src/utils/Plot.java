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
import java.util.List;


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

    public Plot(MultiplePlotInfo info, String name) {

        super(name);

        final XYSeriesCollection data = new XYSeriesCollection();
        for(XYSeries s: info.series){
            data.addSeries(s);
        }
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


}
