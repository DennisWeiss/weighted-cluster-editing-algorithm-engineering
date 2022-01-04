package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;

public class HeuristicMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 5;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

//        Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, System.currentTimeMillis(), MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);

//        EdgeDeletionsWithCost edgeDeletionsWithCost = Heuristics.getGreedyHeuristic1(graph.copy());
//
//        int cost = 0;
//
//        for (Edge edge : edgeDeletionsWithCost.getEdgeDeletions()) {
//            System.out.printf("%d %d\n", edge.getA() + 1, edge.getB() + 1);
//            cost += graph.getEdgeWeights()[edge.getA()][edge.getB()];
//            graph.flipEdge(edge.getA(), edge.getB());
//        }
//
//        Set<Set<Integer>> connectedComponents = graph.getConnectedComponents();
//        for (Set<Integer> connectedComponent : connectedComponents) {
//            for (int u : connectedComponent) {
//                for (int v : connectedComponent) {
//                    if (u < v && !graph.getEdgeExists()[u][v]) {
//                        System.out.printf("%d %d\n", u + 1, v + 1);
//                        cost -= graph.getEdgeWeights()[u][v];
//                    }
//                }
//            }
//        }
//
//        if (DEBUG) {
//            System.out.printf("k = %d\n", edgeDeletionsWithCost.getCost());
//            System.out.printf("cost = %d\n", cost);
//        }

        boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 20, 10, 1);

        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists);

//        boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, resultEdgeExists.length);
//        while (!mergeVerticesInfoStack.empty()) {
//            MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
//            reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
//            graph.revertMergeVertices(mergeVerticesInfo);
//        }

        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExists);

        Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);

//        System.out.printf("#recursive steps: %d, %f\n", Heuristics.optimumScore, Heuristics.optimumP);
    }
}
