package utils;

import javax.imageio.*;

import Scheduler.Plan;

import Scheduler.SolutionSpace;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

import static java.lang.System.out;


public class Plot extends ApplicationFrame {

    /**
     * A demonstration application showing an XY series containing a null value.
     *
     * @param title the frame title.
     */


    public Plot(final String title, ArrayList<Pair<Long, Double>> mydata) {

        super(title);
        final XYSeries series = new XYSeries("Random Data");
        for (Pair p : mydata) {
            series.add((Number) p.a, (Number) p.b);
        }

        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory
            .createScatterPlot("Time/Money", "Time", "Money", data, PlotOrientation.VERTICAL, true,
                true, false);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

    }

    public Plot(MultiplePlotInfo info, String name, String path, Boolean save) {

        super(name);



        final XYSeriesCollection data = new XYSeriesCollection();
        for (XYSeries s : info.series) {
            data.addSeries(s);
        }
        final JFreeChart chart = ChartFactory
            .createScatterPlot("Time/Money", "Time (Minutes)", "Money", data,
                PlotOrientation.VERTICAL, true, true, false);



            ChartPanel chartPanel;
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
            setContentPane(chartPanel);

        if (save) {
            File f = new File(path + name+".png");
            try {
                ChartUtilities.saveChartAsPNG(f, chart, 800, 600);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public Plot(SolutionSpace combined,ArrayList<Pair<String,Double>> legendInfo , MultiplePlotInfo info, String name, String path,
        Boolean save,Boolean show) {
        super(name);



        final XYSeriesCollection data = new XYSeriesCollection();
        for (XYSeries s : info.series) {
            data.addSeries(s);
        }

        XYSeries comb = new XYSeries("combined");
        for(Plan p:combined){
            comb.add((double)(p.stats.runtime_MS / 60000),(Number)p.stats.money);
        }

        data.addSeries(comb);


        JFreeChart chart = ChartFactory.createXYLineChart("Time/Money", "Time (Minutes)", "Money", data, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = (XYPlot)chart.getPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();


        for(int i=0;i<info.series.size();++i) {
            // "0" is the line plot
            renderer.setSeriesLinesVisible(i, false);
            renderer.setSeriesShapesVisible(i, true);
        }
        // "1" is the scatter plot
        renderer.setSeriesLinesVisible(info.series.size(), true);
        renderer.setSeriesShapesVisible(info.series.size(), false);

        plot.setRenderer(renderer);

        chart.addLegend(new LegendTitle(new LegendItemSource() {
            @Override public LegendItemCollection getLegendItems() {

                LegendItemCollection col = new LegendItemCollection();
                for(Pair<String,Double> li: legendInfo ){

                    col.add(new LegendItem(li.a+": "+li.b));
                }
                return col;
            }
        }));

        if (save) {
            File f = new File(path + name+ ".png");
            try {
                ChartUtilities.saveChartAsPNG(f, chart, 800, 600);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(show) {
            ChartPanel chartPanel;
            chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
            setContentPane(chartPanel);
        }


    }
}
