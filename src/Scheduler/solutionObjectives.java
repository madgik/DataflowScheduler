package Scheduler;

/**
 * Created by ilia on 7/11/2017.
 */

public class solutionObjectives {


    /**
     * The objectives of this solution.
     */
    private Plan p;
    private final double[] objectives;


    public solutionObjectives(Plan p, int numberOfObjectives) {
        objectives = new double[numberOfObjectives];
    }

    public solutionObjectives(Plan p, double[] objectives) {
        this(p, objectives.length);

        for (int i = 0; i < objectives.length; i++) {
            setObjective(i, objectives[i]);
        }
    }


    public void setObjectives(double[] objectives) {
        if (objectives.length != this.objectives.length) {
            throw new IllegalArgumentException("invalid number of objectives");
        }

        for (int i = 0; i < objectives.length; i++) {
            this.objectives[i] = objectives[i];
        }
    }


    public void setObjective(int index, double objective) {
        objectives[index] = objective;
    }

    public double[] getObjectives() {
        return objectives.clone();
    }

    public double getObjective(int index) {
        return objectives[index];
    }


    public Plan getPlan() {
        return p;
    }


}
