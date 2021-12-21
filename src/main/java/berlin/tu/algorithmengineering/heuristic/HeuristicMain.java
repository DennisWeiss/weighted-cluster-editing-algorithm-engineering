package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.model.Edge;

import java.util.Set;

public class HeuristicMain {

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

        EdgeDeletionsWithCost edgeDeletionsWithCost = Heuristics.getGreedyHeuristic1(graph.copy());

        for (Edge edge : edgeDeletionsWithCost.getEdgeDeletions()) {
            System.out.printf("%d %d\n", edge.getA() + 1, edge.getB() + 1);
            graph.flipEdge(edge.getA(), edge.getB());
        }

        Set<Set<Integer>> connectedComponents = graph.getConnectedComponents();
        for (Set<Integer> connectedComponent : connectedComponents) {
            for (int u : connectedComponent) {
                for (int v : connectedComponent) {
                    if (u < v && !graph.getEdgeExists()[u][v]) {
                        System.out.printf("%d %d\n", u + 1, v + 1);
                    }
                }
            }
        }


    }
}
