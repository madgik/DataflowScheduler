
package Scheduler;


import Graph.DAG;
import Graph.Edge;
import Graph.Operator;

import java.util.*;

/**
 * @author ilia
 * Computes the topological order of a DAG using depth-first search.
 * See: https://sites.google.com/site/indy256/algo/topological_sorting
 */

public class TopologicalSorting {
    private List<Long> visited = new ArrayList<>();
    private LinkedList<Long> topOrder = new LinkedList<>();

    public TopologicalSorting(DAG graph) {
        for (Operator op : graph.getOperators()) {
            if (!visited.contains(op.getId()))
                dfs(graph, op.getId());
        }
        Collections.reverse(topOrder);
        visited.clear();
    }

    private void dfs(DAG graph, Long opId) {
        visited.add(opId);
        for (Edge childEdge: graph.getChildren(opId)) {
            if (!visited.contains(childEdge.to))
                dfs(graph, childEdge.to);
        }
        topOrder.add(opId);
    }

    public Iterable<Long> iterator() {
        return topOrder;
    }

    public Iterable<Long> iteratorReverse() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return topOrder.descendingIterator();
            }
        };
    }

}
