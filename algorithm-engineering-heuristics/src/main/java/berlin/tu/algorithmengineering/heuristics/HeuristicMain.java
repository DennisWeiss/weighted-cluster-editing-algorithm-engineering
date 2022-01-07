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

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 5;

    public static boolean[][] bestResultEdgeExists;
    public static AtomicInteger bestCost;

    public static CompletableFuture[] heuristicNeighborhoodFutures = new CompletableFuture[0];
    public static CompletableFuture[] heuristic2Randomized2Futures = new CompletableFuture[0];
    public static CompletableFuture[] heuristic1Futures = new CompletableFuture[0];
    public static CompletableFuture[] heuristic2Randomized2ThoroughFutures = new CompletableFuture[0];

    public static Solution[] heuristicNeighborhoodSolutions = new Solution[0];
    public static Solution[] heuristic2Randomized2Solutions = new Solution[0];
    public static Solution[] heuristic1Solutions = new Solution[0];
    public static Solution[] heuristic2Randomized2ThoroughSolutions = new Solution[0];

    static AtomicBoolean startedPrinting;

    public static void main(String[] args) {
        startedPrinting = new AtomicBoolean(false);
        Graph graph = Utils.readGraphFromConsoleInput();

        //initialize with no edges, that is also a solution
        boolean[][] backUpEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int backUpSolutionCost = Utils.getCostToChange(graph, backUpEdgeExists);

        bestResultEdgeExists = Utils.copy(backUpEdgeExists, graph.getNumberOfVertices());
        bestCost = new AtomicInteger(backUpSolutionCost);

        Signal.handle(new Signal("INT"), signal -> {
            setBestSolution(graph.getNumberOfVertices());
            printCurrentlyBestSolution(graph);
        });

//        Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, System.currentTimeMillis(), MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);
//
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

//        HeuristicNeighborhoodThread heuristicNeighborhoodThread = new HeuristicNeighborhoodThread(graph.copy(), bestResultEdgeExists, bestCost);
//        heuristicNeighborhoodThread.start();

        Consumer<Solution> updateSolution = solution -> {
            if (solution != null && solution.getCost() < HeuristicMain.bestCost.get()) {
                HeuristicMain.bestCost.set(solution.getCost());
                HeuristicMain.bestResultEdgeExists = Utils.copy(solution.getResultEdgeExists(), graph.getNumberOfVertices());
            }
        };


        int nThreadHeuristicNeighborhood = 64;
        heuristicNeighborhoodFutures = new CompletableFuture[nThreadHeuristicNeighborhood];
        heuristicNeighborhoodSolutions = new Solution[nThreadHeuristicNeighborhood];

        for (int i = 0; i < nThreadHeuristicNeighborhood; i++) {
            final int n = i;
            heuristicNeighborhoodFutures[i] = CompletableFuture
                    .supplyAsync(() -> {
                        heuristicNeighborhoodSolutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                        boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph.copy());
                        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, heuristicNeighborhoodSolutions[n], 5_000);
                        return heuristicNeighborhoodSolutions[n];
                    })
                    .thenAccept(updateSolution);
        }

        for (int i = 0; i < nThreadHeuristicNeighborhood; i++) {
            heuristicNeighborhoodFutures[i].join();
        }


        int nThreadHeuristic2Randomized2 = 16;
        heuristic2Randomized2Futures = new CompletableFuture[nThreadHeuristic2Randomized2];
        heuristic2Randomized2Solutions = new Solution[nThreadHeuristic2Randomized2];

        for (int i = 0; i < nThreadHeuristic2Randomized2; i++) {
            final int n = i;
            heuristic2Randomized2Futures[i] = CompletableFuture
                    .supplyAsync(() -> {
                        heuristic2Randomized2Solutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                        boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 20, 10, 1.);
                        SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, heuristic2Randomized2Solutions[n], 10_000);
                        return heuristic2Randomized2Solutions[n];
                    })
                    .thenAccept(updateSolution);
        }

        for (int i = 0; i < nThreadHeuristic2Randomized2; i++) {
            heuristic2Randomized2Futures[i].join();
        }

        int nThreadHeuristic1 = 32;
        heuristic1Futures = new CompletableFuture[nThreadHeuristic1];
        heuristic1Solutions = new Solution[nThreadHeuristic1];

        for (int i = 0; i < nThreadHeuristic1; i++) {
            final int n = i;
            heuristic1Futures[i] = CompletableFuture
                    .supplyAsync(() -> {
                        heuristic1Solutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                        EdgeDeletionsWithCost edgeDeletionsWithCost = Heuristics.getGreedyHeuristic1(graph.copy());
                        SimulatedAnnealing.performSimulatedAnnealing(
                                graph,
                                Utils.getResultEdgeExistsFromEdgesDeletions(graph, edgeDeletionsWithCost),
                                heuristic1Solutions[n],
                                10_000
                        );
                        return heuristic1Solutions[n];
                    })
                    .thenAccept(updateSolution);
        }

        for (int i = 0; i < nThreadHeuristic1; i++) {
            heuristic1Futures[i].join();
        }

        for (int i = 0; i < 3; i++) {
            int nThreadHeuristic2Randomized2Thorough = 16;
            heuristic2Randomized2ThoroughFutures = new CompletableFuture[nThreadHeuristic2Randomized2Thorough];
            heuristic2Randomized2ThoroughSolutions = new Solution[nThreadHeuristic2Randomized2Thorough];

            for (int j = 0; j < nThreadHeuristic2Randomized2Thorough; j++) {
                final int n = j;
                heuristic2Randomized2ThoroughFutures[j] = CompletableFuture
                        .supplyAsync(() -> {
                            heuristic2Randomized2ThoroughSolutions[n] = new Solution(backUpEdgeExists, backUpSolutionCost);
                            boolean[][] resultEdgeExists = Heuristics.getGreedyHeuristic2Randomized2(graph.copy(), 40, 10, 2.5);
                            SimulatedAnnealing.performSimulatedAnnealing(graph, resultEdgeExists, heuristic2Randomized2ThoroughSolutions[n], 30_000);
                            return heuristic2Randomized2ThoroughSolutions[n];
                        })
                        .thenAccept(updateSolution);
            }

            for (int j = 0; j < nThreadHeuristic2Randomized2Thorough; j++) {
                heuristic2Randomized2ThoroughFutures[j].join();
            }
        }

//        boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, resultEdgeExists.length);
//        while (!mergeVerticesInfoStack.empty()) {
//            MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
//            reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
//            graph.revertMergeVertices(mergeVerticesInfo);
//        }

        printCurrentlyBestSolution(graph);
    }

    private static synchronized void setBestSolution(int numberOfVertices) {
        for (Solution[] solutions : new Solution[][]{
                heuristicNeighborhoodSolutions, heuristic2Randomized2Solutions, heuristic1Solutions,
                heuristic2Randomized2ThoroughSolutions
        }) {
            for (Solution solution : solutions) {
                if (solution != null && solution.getCost() < HeuristicMain.bestCost.get()) {
                    HeuristicMain.bestCost.set(solution.getCost());
                    HeuristicMain.bestResultEdgeExists = Utils.copy(solution.getResultEdgeExists(), numberOfVertices);
                }
            }
        }
    }

    private static void printCurrentlyBestSolution(Graph graph) {
        if ( ! startedPrinting.getAndSet(true) ) {
            boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), bestResultEdgeExists);

            Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);
            System.exit(0);
        }
    }
}
