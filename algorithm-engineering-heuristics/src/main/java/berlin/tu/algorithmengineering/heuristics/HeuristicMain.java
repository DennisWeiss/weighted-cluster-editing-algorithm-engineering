package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import sun.misc.Signal;
import sun.misc.SignalHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeuristicMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 5;

    public static boolean[][] bestResultEdgeExists;
    public static int bestCost;
    static AtomicBoolean startedPrinting;

    public static void main(String[] args) {
        startedPrinting = new AtomicBoolean(false);
        Graph graph = Utils.readGraphFromConsoleInput();
        //initialize with no edges, that is also a solution
        bestResultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        bestCost = Utils.getCostToChange(graph, bestResultEdgeExists);

        Signal.handle(new Signal("INT"), signal -> printCurrentlyBestSolution(graph));

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

        boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph.copy());

        // Do something like this if you do not do simulated annealing
//        HeuristicMain.bestResultEdgeExists = Utils.copy(resultEdgeExists, graph.getNumberOfVertices());

        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, 5_000);

        Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 20, 10, 1.);
        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, 10_000);

        Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 40, 10, 2.5);
        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, 30_000);


//        boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, resultEdgeExists.length);
//        while (!mergeVerticesInfoStack.empty()) {
//            MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
//            reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
//            graph.revertMergeVertices(mergeVerticesInfo);
//        }

        printCurrentlyBestSolution(graph);
    }

    private static void printCurrentlyBestSolution(Graph graph) {
        if ( ! startedPrinting.getAndSet(true) ) {
            boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), bestResultEdgeExists);

            Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);
            System.exit(0);
        }
    }
}
