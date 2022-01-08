package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.common.model.heuristics.EdgeDeletionsWithCost;
import berlin.tu.algorithmengineering.heuristics.thread.Solution;
import sun.misc.Signal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HeuristicMain {

    public static final boolean DEBUG = false;

    public static boolean[][] bestResultEdgeExists;
    public static AtomicInteger bestCost;

    static AtomicBoolean startedPrinting;
    public static AtomicBoolean forceFinish;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        startedPrinting = new AtomicBoolean(false);
        forceFinish = new AtomicBoolean(false);
        Graph graph = Utils.readGraphFromConsoleInput();

        //initialize with no edges, that is also a solution
        boolean[][] backUpEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int backUpSolutionCost = Utils.getCostToChange(graph, backUpEdgeExists);

        bestResultEdgeExists = Utils.copy(backUpEdgeExists, graph.getNumberOfVertices());
        bestCost = new AtomicInteger(backUpSolutionCost);

        Signal.handle(new Signal("INT"), signal -> {
            forceFinish.set(true);
            System.out.println("#Calling print from interrupt thread");
            printCurrentlyBestSolution(graph);
        });

        EdgeDeletionsWithCost edgeDeletionsWithCostFromHeuristic1 = null;

        for (int i = 0; i < 5; i++) {
            final int m = i;

            System.out.printf("#%d: Starting neighborhood heuristic after %dms\n", i, System.currentTimeMillis() - start);

            int nThreadHeuristicNeighborhood = 128;

            for (int j = 0; j < nThreadHeuristicNeighborhood; j++) {
                final int n = j;
                boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph.copy());
                SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, 20_000);
            }

            System.out.printf("#%d: Starting heuristic2 after %dms\n", i, System.currentTimeMillis() - start);

            int nThreadHeuristic2Randomized2 = 4;

            for (int j = 0; j < nThreadHeuristic2Randomized2; j++) {
                final int n = j;
                boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 20, 10, 1.);
                SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, m == 0 ? 10_000 : 30_000);
            }

            System.out.printf("#%d: Starting heuristic1 after %dms\n", i, System.currentTimeMillis() - start);

            if (edgeDeletionsWithCostFromHeuristic1 == null) {
                edgeDeletionsWithCostFromHeuristic1 = Heuristics.getGreedyHeuristic1(graph.copy());
            }

            int nThreadHeuristic1 = 16;

            for (int j = 0; j < nThreadHeuristic1; j++) {
                final int n = j;
                SimulatedAnnealing.performSimulatedAnnealing(
                        graph,
                        Utils.getResultEdgeExistsFromEdgeDeletions(graph, edgeDeletionsWithCostFromHeuristic1),
                        m == 0 ? 10_000 : 30_000
                );
            }

            System.out.printf("#%d: Starting heuristic2Thorough after %dms\n", i, System.currentTimeMillis() - start);

            int nThreadHeuristic2ThoroughRandomized2 = 2;

            for (int j = 0; j < nThreadHeuristic2ThoroughRandomized2; j++) {
                final int n = j;
                boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 40, 10, 2.5);
                SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, 30_000);
            }
        }

        System.out.println("#Calling print from main thread");
        printCurrentlyBestSolution(graph);
    }

    private static void printCurrentlyBestSolution(Graph graph) {
        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), bestResultEdgeExists);

        String edgesToEditString = Utils.edgesToEditString(graph, edgesToEdit, DEBUG);

        System.out.println("#About to set startedPrinting variable");
        if (!startedPrinting.getAndSet(true)) {
            System.out.println("#Successfully set startedPrinting variable");
            System.out.print(edgesToEditString);
            System.out.println("#Successfully printed");
            System.exit(0);
        }
    }
}
