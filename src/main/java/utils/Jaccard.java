package main.java.utils;

import main.java.Scheduler.Plan;
import main.java.Scheduler.SolutionSpace;

import java.util.HashSet;

/**
 * Created by johnchronis on 4/10/17.
 */
public class Jaccard {
    public static double computeJaccard(SolutionSpace solutions, SolutionSpace combined) {
        double dist = 0.0;
        HashSet<Plan> inter = new HashSet<>();
        inter.addAll(combined.results);
        inter.retainAll(solutions.results);

        HashSet<Plan> union = new HashSet<>();
        union.addAll(solutions.results);
        union.addAll(combined.results);

        dist = 1 - ((double) inter.size()/ (double) union.size());

        return dist;
    }

}
