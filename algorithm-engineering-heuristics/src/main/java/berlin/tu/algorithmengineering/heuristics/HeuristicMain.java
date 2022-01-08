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

    public static CompletableFuture[] heuristicNeighborhoodFutures = new CompletableFuture[0];
    public static CompletableFuture[] heuristic2Randomized2Futures = new CompletableFuture[0];
    public static CompletableFuture[] heuristic1Futures = new CompletableFuture[0];

    public static Solution[] heuristicNeighborhoodSolutions = new Solution[0];
    public static Solution[] heuristic2Randomized2Solutions = new Solution[0];
    public static Solution[] heuristic1Solutions = new Solution[0];

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
            setBestSolution(graph.getNumberOfVertices());
            System.out.println("#Calling print from interrupt thread");
            printCurrentlyBestSolution(graph);
        });

        Consumer<Solution> updateSolution = solution -> updateSolution(solution, graph.getNumberOfVertices());

        EdgeDeletionsWithCost edgeDeletionsWithCostFromHeuristic1 = null;

        for (int i = 0; i < 20; i++) {
            final int m = i;

            for (int k = 0; k < (i == 0 ? 5 : 1); k++) {
                System.out.printf("#%d: Starting neighborhood heuristic after %dms\n", i, System.currentTimeMillis() - start);

                int nThreadHeuristicNeighborhood = 16;
                heuristicNeighborhoodFutures = new CompletableFuture[nThreadHeuristicNeighborhood];
                heuristicNeighborhoodSolutions = new Solution[nThreadHeuristicNeighborhood];


                for (int j = 0; j < nThreadHeuristicNeighborhood; j++) {
                    final int n = j;
                    heuristicNeighborhoodFutures[j] = CompletableFuture
                            .supplyAsync(() -> {
                                heuristicNeighborhoodSolutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                                boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph.copy());
                                SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, heuristicNeighborhoodSolutions[n], 20_000);
                                return heuristicNeighborhoodSolutions[n];
                            })
                            .thenAccept(updateSolution)
                            .exceptionally(t -> null);
                }

                for (int j = 0; j < nThreadHeuristicNeighborhood; j++) {
                    try {
                        heuristicNeighborhoodFutures[j].join();
                    } catch (Exception e) {

                    }
                }
            }

            System.out.printf("#%d: Starting heuristic1 after %dms\n", i, System.currentTimeMillis() - start);

            if (edgeDeletionsWithCostFromHeuristic1 == null) {
                edgeDeletionsWithCostFromHeuristic1 = Heuristics.getGreedyHeuristic1(graph.copy());
            }

            int nThreadHeuristic1 = 8;
            heuristic1Futures = new CompletableFuture[nThreadHeuristic1];
            heuristic1Solutions = new Solution[nThreadHeuristic1];

            for (int j = 0; j < nThreadHeuristic1; j++) {
                final int n = j;
                final EdgeDeletionsWithCost edgeDeletionsWithCostFinal = edgeDeletionsWithCostFromHeuristic1;
                heuristic1Futures[j] = CompletableFuture
                        .supplyAsync(() -> {
                            heuristic1Solutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                            SimulatedAnnealing.performSimulatedAnnealing(
                                    graph,
                                    Utils.getResultEdgeExistsFromEdgeDeletions(graph, edgeDeletionsWithCostFinal),
                                    heuristic1Solutions[n],
                                    m == 0 ? 10_000 : 30_000
                            );
                            return heuristic1Solutions[n];
                        })
                        .thenAccept(updateSolution)
                        .exceptionally(t -> null);
            }

            for (int j = 0; j < nThreadHeuristic1; j++) {
                try {
                    heuristic1Futures[j].join();
                } catch (Exception e) {

                }
            }

//            if (graph.getNumberOfVertices() <= 200) {
//                System.out.printf("#%d: Starting heuristic2 after %dms\n", i, System.currentTimeMillis() - start);
//
//                int nThreadHeuristic2Randomized2 = 4;
//                heuristic2Randomized2Futures = new CompletableFuture[nThreadHeuristic2Randomized2];
//                heuristic2Randomized2Solutions = new Solution[nThreadHeuristic2Randomized2];
//
//                for (int j = 0; j < nThreadHeuristic2Randomized2; j++) {
//                    final int n = j;
//                    heuristic2Randomized2Futures[j] = CompletableFuture
//                            .supplyAsync(() -> {
//                                heuristic2Randomized2Solutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
//                                boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 20, 10, 1.);
//                                SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, heuristic2Randomized2Solutions[n], m == 0 ? 10_000 : 30_000);
//                                return heuristic2Randomized2Solutions[n];
//                            })
//                            .thenAccept(updateSolution)
//                            .exceptionally(t -> null);
//                }
//
//                for (int j = 0; j < nThreadHeuristic2Randomized2; j++) {
//                    try {
//                        heuristic2Randomized2Futures[j].join();
//                    } catch (Exception e) {
//
//                    }
//                }
//            }
        }

        System.out.println("#Calling print from main thread");
        printCurrentlyBestSolution(graph);
    }

    private static synchronized void setBestSolution(int numberOfVertices) {
        for (Solution[] solutions : new Solution[][]{
                heuristicNeighborhoodSolutions, heuristic2Randomized2Solutions, heuristic1Solutions
        }) {
            for (Solution solution : solutions) {
                updateSolution(solution, numberOfVertices);
            }
        }
    }

    private static synchronized void updateSolution(Solution solution, int numberOfVertices) {
        if (!forceFinish.get() && solution != null && solution.getCost() < bestCost.get()) {
            bestCost.set(solution.getCost());
            bestResultEdgeExists = Utils.copy(solution.getResultEdgeExists(), numberOfVertices);
        }
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
