/**
 * Copyright MaDgIK Group 2010 - 2012.
 */
package utils;

import Graph.Operator;
import Scheduler.Plan;
import Scheduler.RuntimeConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import javax.swing.*;

/**
 * @author Herald Kllapi <br>
 *      University of Athens /
 *      Department of Informatics and Telecommunications.
 * @since 1.0
 */
public class OptimizationResultVisualizer extends JPanel {

    private static int frameCNT = 1;

    public static OptimizationResultVisualizer showOptimizationResult(
        String name,
        Plan result) {
        OptimizationResultVisualizer viz = new OptimizationResultVisualizer();
        viz.show(result);

        JFrame frame = new JFrame(frameCNT + " (" + name + ")");
        frame.setContentPane(viz);
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frameCNT++;

        return viz;
    }

    private Plan result = null;

    public OptimizationResultVisualizer() {
    }

    public void show(Plan result) {
        this.result = result;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = GridBagConstHelper.create(
            0, 0,
            1.0, 1.0,
            new Insets(0, 0, 0, 0), GridBagConstraints.BOTH);
    }

    @Override
    public void paintComponent(Graphics g) {
        // Paint background
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;

        paintBack(g2D);
        paintOptionalAssignments(g2D);
        paintContainerAllocation(g2D);
        paintAssigments(g2D);
        paintAssigments2(g2D);
    }

    private void paintBack(Graphics2D g2D) {
        // Get the drawing area
        int dy = getSize().height;
        int dx = getSize().width;

        //    g2D.setPaint(
        //            new GradientPaint(dx / 2, 0,
        //            Color.WHITE,
        //            dx / 2, dy,
        //            Color.LIGHT_GRAY, true));
        g2D.setPaint(Color.WHITE);

        Rectangle rect = new Rectangle(dx, dy);
        g2D.fill(rect);

        g2D.setPaint(Color.BLACK);
        g2D.setStroke(new BasicStroke(1));

        for (int i = 1; i < result.cluster.contUsed.size(); i++) {
            Point2D.Double start = new Point2D.Double(
                0, i * ((double) dy /result.cluster.contUsed.size()));

            Point2D.Double end = new Point2D.Double(
                dx, i * ((double) dy / result.cluster.contUsed.size()));

            Line2D.Double line = new Line2D.Double(start, end);
            g2D.draw(line);
        }
    }

    private void paintAssigments(Graphics2D g2D) {
        int dy = getSize().height;
        int dx = getSize().width;

        int quantumCount = (int) Math.ceil(result.stats.quanta);
        double containerHeight = ((double) dy) / result.cluster.contUsed.size();
        double xScale = ((double) dx) / (quantumCount * RuntimeConstants.quantum_MS);


        for (Long opId : result.assignments.keySet()) {
            Long ass = result.assignments.get(opId);
            Operator oa = result.graph.getOperator(opId);
            //            g2D.setColor(new Color(
            //                    (float) (1 - ((oa.cpuTimeNeeded / (oa.end - oa.start)) / 2)),
            //                    0.6f, 0.6f));


                g2D.setPaint(new Color(0.7f, 0.7f, 1.0f));
                if (oa.name.startsWith("leaf")) {
                    g2D.setPaint(new Color(0.7f, 1.0f, 0.7f));
                }
                else if (oa.name.startsWith("internal")) {
                    g2D.setPaint(new Color(0.7f, 0.7f, 1.0f));
                }
                else if (oa.name.startsWith("root")) {
                    g2D.setPaint(new Color(1.0f, 0.7f, 0.7f));
                }


            Rectangle rect = new Rectangle(
                (int) ((result.opIdtoStartEndProcessing_MS.get(opId).a) * xScale),
                (int) (ass * containerHeight),
                (int) ((result.opIdToProcessingTime_MS.get(opId)) * xScale),
                (int) containerHeight);

            g2D.fill(rect);
        }
    }

    private void paintOptionalAssignments(Graphics2D g2D) {
        int dy = getSize().height;
        int dx = getSize().width;

        int quantumCount = (int) Math.ceil(result.stats.quanta);
        double containerHeight = ((double) dy) / result.cluster.contUsed.size();
        double xScale = ((double) dx) / (quantumCount * RuntimeConstants.quantum_MS);

        for (Long opId : result.assignments.keySet()) {
            Long ass = result.assignments.get(opId);
            Operator op = result.graph.getOperator(opId);
            //            g2D.setColor(new Color(
            //                    (float) (1 - ((oa.cpuTimeNeeded / (oa.end - oa.start)) / 2)),
            //                    0.6f, 0.6f));



                g2D.setPaint(new Color(0.7f, 0.7f, 1.0f));


            Rectangle rect = new Rectangle(
                (int) ((result.opIdtoStartEndProcessing_MS.get(opId).a) * xScale),
                (int) (ass * containerHeight),
                (int) ((result.opIdToProcessingTime_MS.get(opId)) * xScale),
                (int) containerHeight);

            g2D.fill(rect);
        }
    }

    private void paintContainerAllocation(Graphics2D g2D) {
        int dy = getSize().height;
        int dx = getSize().width;

        double containerHeight = ((double) dy) / result.cluster.contUsed.size();
        int quantumCount = (int) Math.ceil(result.stats.quanta);
        double quantumSize = (double) dx / quantumCount;

        g2D.setColor(new Color(1.0f, 0.4f, 0.4f));
        g2D.setStroke(new BasicStroke(1.5f));

        int totalQNum = 0;

        for (int qNum = 0; qNum < quantumCount; qNum++) {
            for (int cNum = 0; cNum < result.cluster.contUsed.size(); cNum++) {
                if (existOperatorInQuantum(result,(long)cNum,(long)qNum)) {

                    Rectangle rect = new Rectangle(
                        (int) ((double) qNum * quantumSize),
                        (int) ((double) cNum * containerHeight - 3 + containerHeight / 2.0),
                        (int) quantumSize,
                        (int) 6);

                    g2D.fill(rect);

                    totalQNum++;
                }
            }
        }
    }
    Boolean existOperatorInQuantum(Plan result, Long cid, Long q){
            long qS = RuntimeConstants.quantum_MS*q + result.cluster.getContainer(cid).startofUse_MS;
            long qE = qS+RuntimeConstants.quantum_MS;


            for(Long opId:result.contAssignments.get(cid)){
                if(result.opIdtoStartEndProcessing_MS.get(opId).a >= qS && result.opIdtoStartEndProcessing_MS.get(opId).a <= qE){
                    return true;
                }
                if(result.opIdtoStartEndProcessing_MS.get(opId).a < qS && result.opIdtoStartEndProcessing_MS.get(opId).b > qE){
                    return true;
                }
                if(result.opIdtoStartEndProcessing_MS.get(opId).b >= qS && result.opIdtoStartEndProcessing_MS.get(opId).b < qE){
                    return true;
                }
            }

        return false;
    }

    private void paintAssigments2(Graphics2D g2D) {
//        int dy = getSize().height;
//        int dx = getSize().width;
//
//        int quantumCount = (int) Math.ceil(result.stats.quanta);
//        double containerHeight = ((double) dy) / result.cluster.contUsed.size();
//        double xScale = ((double) dx) / (quantumCount * RuntimeConstants.quantum_MS;
//
//        g2D.setColor(new Color(0.3f, 0.3f, 0.3f));
//
//        LinkedList<Long> assignments = new LinkedList<Long>();
//        assignments.addAll(result.operatorAssigments);
//        assignments.addAll(result.optionalAssignments);
//
//        for (int cNum = 0; cNum < result.cluster.contUsed.size(); cNum++) {
//            PriorityQueue<Double> thresholds = SchedulerUtils.getThresholds(cNum, assignments);
//
//            if (thresholds.size() > 0) {
//                double start = thresholds.poll();
//                while (thresholds.size() > 0) {
//                    double end = thresholds.poll();
//
//                    ArrayList<OperatorAssignment> overlaps = SchedulerUtils.getOverlaps(
//                        assignments, cNum, start, end);
//
//                    if (overlaps.size() > 0) {
//
//                        Rectangle rect = new Rectangle(
//                            (int) (start * xScale),
//                            (int) ((double) cNum * containerHeight),
//                            (int) ((end - start) * xScale),
//                            (int) containerHeight);
//
//                        g2D.draw(rect);
//
//                        //            g2D.drawString(overlaps.size() + "",
//                        //                    (int) (start * xScale) + 5,
//                        //                    (int) ((double) cNum * containerHeight) + 15);
//                    }
//
//                    start = end;
//                }
//            }
//        }
    }


}
