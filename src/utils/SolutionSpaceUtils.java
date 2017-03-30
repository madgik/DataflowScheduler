/**
 * Copyright MaDgIK Group 2010 - 2013.
 */
package utils;

import Scheduler.Plan;
import Scheduler.SolutionSpace;
import Scheduler.Statistics;

import java.awt.geom.Line2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author heraldkllapi
 */
public class SolutionSpaceUtils {
  
  public static SkylineDistance computeDistance(
          SolutionSpace fromSky,
      SolutionSpace toSky) throws RemoteException {
   toSky.sortDist();
    SkylineDistance sd = new SkylineDistance();
    sd.L1 = computeDistanceL1(fromSky, toSky);
    sd.L2 = computeDistanceL2(fromSky, toSky);
    sd.P2L = computeDistanceP2L(fromSky, toSky);
    sd.P2Sky = computeDistanceP2Sky(fromSky, toSky);
    return sd;
  }

  // Sum of L1 distance from point to point
  private static double computeDistanceL1(SolutionSpace fromSky,
      SolutionSpace toSky) {
    if (fromSky.isEmpty()) {
      return Double.MAX_VALUE;
    }
    double distance = 0.0;
    for (Plan from : fromSky) {
      Statistics fromSt = from.stats;
      double minDist = Double.MAX_VALUE;
      for (Plan to : toSky) {
        Statistics toSt = to.stats;
        double dist = 
                Math.abs(fromSt.quanta - toSt.quanta) +
                Math.abs(fromSt.money - toSt.money);
        if (dist < minDist) {
          minDist = dist;
        }
      }
      distance += minDist;
    }
    return distance / fromSky.size();
  }

  // Sum of L2 distance from point to point
  private static double computeDistanceL2(SolutionSpace fromSky,
                                          SolutionSpace toSky) {
    if (fromSky.isEmpty()) {
      return Double.MAX_VALUE;
    }
    double distance = 0.0;
    for (Plan from : fromSky) {
      Statistics fromSt = from.stats;
      double minDist = Double.MAX_VALUE;
      for (Plan to : toSky) {
        Statistics toSt = to.stats;
        double sq =
                Math.pow(fromSt.quanta - toSt.quanta, 2) +
                Math.pow(fromSt.money - toSt.money, 2);
        double dist = Math.sqrt(sq);
        if (dist < minDist) {
          minDist = dist;
        }
      }
      distance += minDist;
    }
    return distance / fromSky.size();
  }

  // Sum of point to line distance of points to lines
  private static double computeDistanceP2L(SolutionSpace from,
                                           SolutionSpace to) {
    if (from.isEmpty()) {
      return Double.MAX_VALUE;
    }
    // Compute to line segments
    ArrayList<Line2D.Double> toLines = new ArrayList<Line2D.Double>();
    // Add the outer lines
    Plan minTimeSched = to.results.get(0);
    Plan maxTimeSched = to.results.get(to.size() - 1);
    double minTime = minTimeSched.stats.quanta;
    double maxTime = maxTimeSched.stats.quanta;
    double minMoney = maxTimeSched.stats.money;
    double maxMoney = minTimeSched.stats.money;
    
    Line2D.Double lineMoneyMax = new Line2D.Double(
            minTime, maxMoney, minTime, 10000000.0);
    Line2D.Double lineTimeMax = new Line2D.Double(
            maxTime, minMoney, 10000000.0, minMoney);
    toLines.add(lineMoneyMax);
    toLines.add(lineTimeMax);
    for (int i = 1; i < to.size(); ++i) {
      Plan srFrom = to.results.get(i - 1);
      Plan srTo = to.results.get(i);
      Line2D.Double line = new Line2D.Double(
              srFrom.stats.quanta,
              srFrom.stats.money,
              srTo.stats.quanta,
              srTo.stats.money);
      toLines.add(line);
    }
    if (toLines.isEmpty()) {
      Plan srFrom = to.results.get(0);
      Plan srTo = to.results.get(0);
      Line2D.Double line = new Line2D.Double(
              srFrom.stats.quanta,
              srFrom.stats.money,
              srTo.stats.quanta,
              srTo.stats.money);
      toLines.add(line);
    }
    double distance = 0.0;
    for (Plan sr : from) {
      Statistics stats = sr.stats;
      double minDist = Double.MAX_VALUE;
      for (Line2D line : toLines) {
        double dist =  line.ptSegDist(stats.quanta,
                                      stats.money);
        if (dist < minDist) {
          minDist = dist;
        }
      }
      distance += minDist;
    }
    return distance / from.size();
  }
  
  // Sum of point to line distance of points to lines
  private static double computeDistanceP2Sky(SolutionSpace from,
                                             SolutionSpace to) throws RemoteException {
    if (from.isEmpty() || to.isEmpty()) {
      return Double.MAX_VALUE;
    }
    // Compute to line segments
    ArrayList<Line2D.Double> toLines = new ArrayList<Line2D.Double>();
    // Add the outer lines
    Plan minTimeSched = to.results.get(0);
    Plan maxTimeSched = to.results.get(to.size() - 1);
    double minTime = minTimeSched.stats.quanta;
    double maxTime = maxTimeSched.stats.quanta;
    double minMoney = maxTimeSched.stats.money;
    double maxMoney = minTimeSched.stats.money;
    
    Line2D.Double lineMoneyMax = new Line2D.Double(
            minTime, maxMoney, minTime, 10000000.0);
    Line2D.Double lineTimeMax = new Line2D.Double(
            maxTime, minMoney, 10000000.0, minMoney);
    toLines.add(lineMoneyMax);
    toLines.add(lineTimeMax);
    // To is sorted on time dimension.
    for (int i = 1; i < to.size(); ++i) {
      Plan srFrom = to.results.get(i - 1);
      double fromT = srFrom.stats.quanta;
      double fromM = srFrom.stats.money;
      
      Plan srTo = to.results.get(i);
      double toT = srTo.stats.quanta;
      double toM = srTo.stats.money;
      
      Line2D.Double lineTime = new Line2D.Double(fromT, fromM, toT, fromM);
      Line2D.Double lineMoney = new Line2D.Double(toT, fromM, toT, toM);
      toLines.add(lineTime);
      toLines.add(lineMoney);
    }
    double distance = 0.0;
    for (Plan f : from) {
      Statistics stats = f.stats;
      double minDist = Double.MAX_VALUE;
      for (Line2D line : toLines) {
        double dist =  line.ptSegDist(stats.quanta,
                                      stats.money);
        if (dist < minDist) {
          minDist = dist;
        }
      }
      distance += minDist;
    }
    if (distance == Double.MAX_VALUE) {
      throw new RemoteException(from.toString() + "\n" + to.toString() + "\n\n" + toLines);
    }
    return distance / from.size();
  }
}
