/**
 * Copyright MaDgIK Group 2010 - 2014.
 */
package Tree;

import Graph.*;
import Scheduler.RuntimeConstants;
import utils.RandomParameters;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author heraldkllapi
 */
public class TreeGraphGenerator {

    public static void main(String[] args) {

        int leafs = 32;
        int height = 4;
        double[] avgTimePerLevel = new double[] {0.2, 0.2, 0.2, 0.2, 0.3};
        double initialDataSize = 1000;
        double[] dataReductionPerLevel = new double[] {0.3, 0.3, 0.3, 0.3, 0.3};
        double randomness = 0.0;
        long seed = 0L;
        DAG graph  = TreeGraphGenerator.createTreeGraph(
            leafs, height, 1.0, 1.0,
            avgTimePerLevel, initialDataSize, dataReductionPerLevel, randomness, seed);


        graph.printEdges();
    }

        public static int getReductionPerLevel(int leafs, int height) {
            // height = log_b(leafs)
            // b^height = leafs
            double b = Math.pow(leafs, 1.0 / (height - 1));
            return (int)Math.ceil(b);
        }



    public static DAG createTreeGraph(
        int leafs, int height,
        double cpu, double mem,
        double[] avgTimePerLevel,
        double initialDataSize, double[] dataReductionPerLevel,
        double randomness, long seed) {

        HashMap<Long,Long> opIdToOutDataSize = new HashMap<Long,Long>();

        int reductionPerLevel = getReductionPerLevel(leafs, height);
        Random rand = new Random(seed);

        DAG graph = new DAG();
        // Add leafs
        LinkedList<Long> leafOps = new LinkedList<Long>();
        for (int o = 0; o < leafs; o++) {
            Operator op = createADDOperator("leaf_0_" + o, avgTimePerLevel[0] + randomness * rand.nextDouble(),
                new double[] { initialDataSize * dataReductionPerLevel[0] +
                    randomness * rand.nextDouble() },graph,opIdToOutDataSize);

            leafOps.add(op.getId());
        }
        // Add internal levels
        LinkedList<Long> fromInternalOps = new LinkedList<Long>(leafOps);
        LinkedList<Long> toInternalOps = new LinkedList<Long>();

        int numOps = (int)Math.ceil((double)leafs / reductionPerLevel);
        double dataSize = initialDataSize * dataReductionPerLevel[0];
        for (int level = 1; level < height - 1; level++) {
            for (int o = 0; o < numOps; o++) {
                Operator op = createADDOperator(
                    "internal_" + level + "_" + o,
                    avgTimePerLevel[level] +  + randomness * rand.nextDouble(),
                    new double[] { dataSize * dataReductionPerLevel[level] +
                        randomness * rand.nextDouble() },graph,opIdToOutDataSize);

                int fromIdx = o * reductionPerLevel;
                int toIdx = o * reductionPerLevel + reductionPerLevel;
                if (level == height - 2 && o == numOps - 1) {
                    toIdx = fromInternalOps.size();
                }
                if (toIdx >= fromInternalOps.size()) {
                    toIdx = fromInternalOps.size();
                }
                for (int i = fromIdx; i < toIdx; ++i) {
                    Long from = fromInternalOps.get(i);

                    Long dataSizeOut = opIdToOutDataSize.get(from);
                    Data df = new Data("noname",dataSizeOut);
                    Edge edge = new Edge(from, op.getId(), df);

                    graph.addEdge(edge);


                }
                toInternalOps.add(op.getId());
            }
            numOps /= reductionPerLevel;
            dataSize *= dataReductionPerLevel[level];

            fromInternalOps = new LinkedList<Long>(toInternalOps);
            toInternalOps.clear();
        }
        // Add root
        Operator root = createADDOperator(
            "root_" + (height - 1) + "_0", avgTimePerLevel[0] + randomness * rand.nextDouble(),
            new double[] { dataSize * dataReductionPerLevel[height - 1] +
                randomness * rand.nextDouble() },graph,opIdToOutDataSize);
        graph.addOperator(root);
        for (int i = 0; i < fromInternalOps.size(); ++i) {
            Long from = fromInternalOps.get(i);

            Long dataSizeOut = opIdToOutDataSize.get(from);
            Data df = new Data("noname",dataSizeOut);
            Edge edge = new Edge(from, root.getId(), df);


            graph.addEdge(edge);


        }
        return graph;
    }

    public static Operator createADDOperator(
        String name,
        double runTimeValue,
        double[] outputData,
        DAG graph,HashMap<Long,Long> opIdToOutDataSize) {

        ResourcesRequirements
            rr = new ResourcesRequirements( (long) runTimeValue,
            100 );

        Operator op = new Operator(name,rr);

        long dataCount=0;
        for(double d : outputData){
            dataCount+=d;
        }

        graph.addOperator(op);
        opIdToOutDataSize.put(op.getId(),dataCount);
        return op;
    }


}
