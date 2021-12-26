package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.common.Utils;

public class SimulatedAnnealing {

    public static final int ITERATIONS = 5000;
    public static final double T = 1;

    public static void performSimulatedAnnealing(Graph graph, boolean[][] resultEdgeExists) {
        for (int i = 0; i < ITERATIONS; i++) {
            int vertex = Utils.randInt(0, graph.getNumberOfVertices());
            int moveToVertex = Utils.randInt(0, graph.getNumberOfVertices());
            double probabilityOfBetterSolution = getProbabilityOfBetterSolution(getDeltaCost(graph, resultEdgeExists, vertex, moveToVertex), 1.0 * i / ITERATIONS);
            if (probabilityOfBetterSolution == 1 || probabilityOfBetterSolution > Math.random()) {
                applyChange(resultEdgeExists, vertex, moveToVertex);
            }
//            if (checkP3(resultEdgeExists)) {
//                System.out.printf("P3 %d %d\n", vertex, moveToVertex);
//            }
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

    private static double getProbabilityOfBetterSolution(int deltaCost, double alpha) {
        return deltaCost > 0 ? Math.exp(-deltaCost / (alpha * T)) : 1;
    }
}
