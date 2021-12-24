package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.model.Edge;

import java.util.Set;

public class HeuristicMain {

    public static final boolean DEBUG = true;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

//        Heuristics.getGreedyHeuristic2(graph.copy());

        EdgeDeletionsWithCost edgeDeletionsWithCost = Heuristics.getGreedyHeuristic1(graph.copy());

        int cost = 0;

        for (Edge edge : edgeDeletionsWithCost.getEdgeDeletions()) {
            System.out.printf("%d %d\n", edge.getA() + 1, edge.getB() + 1);
            cost += graph.getEdgeWeights()[edge.getA()][edge.getB()];
            graph.flipEdge(edge.getA(), edge.getB());
        }

        if (DEBUG) {
            System.out.println(cost);
        }

        Set<Set<Integer>> connectedComponents = graph.getConnectedComponents();
        for (Set<Integer> connectedComponent : connectedComponents) {
            for (int u : connectedComponent) {
                for (int v : connectedComponent) {
                    if (u < v && !graph.getEdgeExists()[u][v]) {
                        System.out.printf("%d %d\n", u + 1, v + 1);
                        cost -= graph.getEdgeWeights()[u][v];
                    }
                }
            }
        }

        if (DEBUG) {
            System.out.printf("k = %d\n", edgeDeletionsWithCost.getCost());
            System.out.printf("cost = %d\n", cost);
        }

    }
}
