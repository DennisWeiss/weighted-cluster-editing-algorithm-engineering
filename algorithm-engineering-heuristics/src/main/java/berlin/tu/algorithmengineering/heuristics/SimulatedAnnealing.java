package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;

import java.util.ArrayList;
import java.util.List;

public class SimulatedAnnealing {

    public static final int DEFAULT_ITERATIONS = 30_000;
    public static final double T = 5;

    public static List<String> simulatedAnnealingCosts = new ArrayList<>();

    public static void performSimulatedAnnealing(Graph graph, boolean[][] resultEdgeExists) {
        performSimulatedAnnealing(graph, resultEdgeExists, DEFAULT_ITERATIONS);
    }

    public static void performSimulatedAnnealing(Graph graph, boolean[][] resultEdgeExists, int iterations) {

        int cost = Utils.getCostToChange(graph, resultEdgeExists);
        if (cost < HeuristicMain.bestCost) {
            HeuristicMain.bestResultEdgeExists = Utils.copy(resultEdgeExists,graph.getNumberOfVertices());
            HeuristicMain.bestCost = cost;
        }

        double t = T;

        for (int i = 0; i < iterations; i++) {
            int vertex = Utils.randInt(0, graph.getNumberOfVertices());
            int moveToVertex = Utils.randInt(0, graph.getNumberOfVertices());

            int deltaCost = getDeltaCost(graph, resultEdgeExists, vertex, moveToVertex);
            double probabilityOfBetterSolution = getProbabilityOfBetterSolution(deltaCost, t);
            t -= 1. / iterations;
            if (probabilityOfBetterSolution == 1 || probabilityOfBetterSolution > Math.random()) {
                applyChange(resultEdgeExists, vertex, moveToVertex);
                cost += deltaCost;
                if (cost < HeuristicMain.bestCost) {
                    HeuristicMain.bestResultEdgeExists = Utils.copy(resultEdgeExists,graph.getNumberOfVertices());
                    HeuristicMain.bestCost = cost;
                }
            }
            simulatedAnnealingCosts.add(String.valueOf(cost));
        }
    }

    private static boolean checkP3(boolean[][] edgeExists) {
        for (int u = 0; u < edgeExists.length; u++) {
            for (int v = 0; v < edgeExists.length; v++) {
                for (int w = u+1; w < edgeExists.length; w++) {
                    if (u != v && v != w && edgeExists[u][v] && edgeExists[v][w] && !edgeExists[u][w]) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void applyChange(boolean[][] edgeExists, int vertex, int moveToVertex) {
        for (int i = 0; i < edgeExists.length; i++) {
            edgeExists[vertex][i] = false;
            edgeExists[i][vertex] = false;
        }
        if (vertex == moveToVertex) {
            return;
        }
        for (int i = 0; i < edgeExists.length; i++) {
            if (edgeExists[moveToVertex][i]) {
                edgeExists[vertex][i] = true;
                edgeExists[i][vertex] = true;
            }
        }
        edgeExists[vertex][moveToVertex] = true;
        edgeExists[moveToVertex][vertex] = true;
    }

    private static int getDeltaCost(Graph graph, boolean[][] resultEdgeExists, int vertex, int moveToVertex) {
        int deltaCost = 0;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            if (vertex != i && resultEdgeExists[vertex][i]) {
                deltaCost += graph.getEdgeWeights()[vertex][i];
            }
        }

        if (vertex != moveToVertex) {
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                if (moveToVertex == i || resultEdgeExists[moveToVertex][i]) {
                    deltaCost -= graph.getEdgeWeights()[vertex][i];
                }
            }
        }

        return deltaCost;
    }

    private static double getProbabilityOfBetterSolution(int deltaCost, double t) {
        return deltaCost > 0 ? Math.exp(-deltaCost / t) : 1;
    }
}
