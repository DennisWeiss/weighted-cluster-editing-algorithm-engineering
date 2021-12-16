package berlin.tu.algorithmengineering.mincut;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.MergeVerticesInfo;

public class NagamochiIbarakiAlgorithm {

    public static int getGlobalMinCutCost(Graph graph) {
        int[][] q = new int[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                q[i][j] = Integer.MAX_VALUE;
            }
        }
        return getGlobalMinCutCostOfSubGraph(graph, Integer.MAX_VALUE, q);
    }

    private static int getGlobalMinCutCostOfSubGraph(Graph graph, int lambda, int[][] q) {
        if (graph.getNumberOfVertices() < 2) {
            return Integer.MAX_VALUE;
        }
        if (graph.getNumberOfVertices() == 2) {
            return Math.min(lambda, Math.max(graph.getEdgeWeights()[0][1], 0));
        }
        if (lambda == Integer.MAX_VALUE) {
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                lambda = Math.min(lambda, graph.getWeightedDegreeCut(i));
            }
        }
        int[] ordering = capForest(graph, q);
        MergeVerticesInfo mergeVerticesInfo = contractVertices(
                graph, ordering[graph.getNumberOfVertices() - 2], ordering[graph.getNumberOfVertices() - 1]
        );
        int weightedDegreeCut = graph.getWeightedDegreeCut(Math.min(ordering[ordering.length - 2], ordering[ordering.length - 1]));
        lambda = Math.min(
                lambda,
                Math.min(weightedDegreeCut,
                        getGlobalMinCutCostOfSubGraph(graph, lambda, q))
        );
        revertContractVertices(graph, mergeVerticesInfo);
        return lambda;
    }

    private static int[] capForest(Graph graph, int[][] q) {
        boolean[][] scannedEdge = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int[] connectivities = new int[graph.getNumberOfVertices()];
        boolean[] visited = new boolean[graph.getNumberOfVertices()];
        int[] ordering = new int[graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            int vertex = getNextVertexToVisitInCapForest(connectivities, visited);
            visited[vertex] = true;
            ordering[i] = vertex;
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                if (!visited[j] && graph.getEdgeExists()[vertex][j] && !scannedEdge[vertex][j]) {
                    connectivities[j] += graph.getEdgeWeights()[vertex][j];
                    scannedEdge[vertex][j] = true;
                    scannedEdge[vertex][i] = true;
                    q[vertex][j] = connectivities[j];
                    q[j][vertex] = connectivities[i];
                }
            }
        }

        return ordering;
    }

    private static int getNextVertexToVisitInCapForest(int[] connectivities, boolean[] visited) {
        int maxConnectivity = Integer.MIN_VALUE;
        int vertex = -1;
        for (int i = 0; i < connectivities.length; i++) {
            if (!visited[i] && connectivities[i] > maxConnectivity) {
                maxConnectivity = connectivities[i];
                vertex = i;
            }
        }
        return vertex;
    }

    private static MergeVerticesInfo contractVertices(Graph graph, int a, int b) {
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }

        MergeVerticesInfo mergeVerticesInfo = new MergeVerticesInfo(graph.getNumberOfVertices(), a, b);

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i] = graph.getEdgeWeights()[a][i];
            mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] = graph.getEdgeExists()[a][i];
            mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i] = graph.getEdgeWeights()[b][i];
            mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i] = graph.getEdgeExists()[b][i];
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            if (i != a && i != b) {
                int newWeight = Math.max(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i], 0) + Math.max(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i], 0);

                graph.getEdgeWeights()[a][i] = newWeight;
                graph.getEdgeExists()[a][i] = newWeight > 0;
                graph.getEdgeWeights()[i][a] = newWeight;
                graph.getEdgeExists()[i][a] = newWeight > 0;

            }
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            graph.getEdgeWeights()[i][b] = graph.getEdgeWeights()[i][graph.getNumberOfVertices() - 1];
            graph.getEdgeExists()[i][b] = graph.getEdgeExists()[i][graph.getNumberOfVertices() - 1];
            graph.getEdgeWeights()[b][i] = graph.getEdgeWeights()[graph.getNumberOfVertices() - 1][i];
            graph.getEdgeExists()[b][i] = graph.getEdgeExists()[graph.getNumberOfVertices() - 1][i];
        }

        graph.setNumberOfVertices(graph.getNumberOfVertices() - 1);

        return mergeVerticesInfo;
    }

    private static void revertContractVertices(Graph graph, MergeVerticesInfo mergeVerticesInfo) {
        graph.setNumberOfVertices(graph.getNumberOfVertices() + 1);


        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            graph.getEdgeWeights()[graph.getNumberOfVertices() - 1][i] = graph.getEdgeWeights()[mergeVerticesInfo.getSecondVertex()][i];
            graph.getEdgeExists()[graph.getNumberOfVertices() - 1][i] = graph.getEdgeExists()[mergeVerticesInfo.getSecondVertex()][i];
            graph.getEdgeWeights()[i][graph.getNumberOfVertices() - 1] = graph.getEdgeWeights()[i][mergeVerticesInfo.getSecondVertex()];
            graph.getEdgeExists()[i][graph.getNumberOfVertices() - 1] = graph.getEdgeExists()[i][mergeVerticesInfo.getSecondVertex()];
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            graph.getEdgeWeights()[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            graph.getEdgeExists()[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            graph.getEdgeWeights()[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            graph.getEdgeExists()[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            graph.getEdgeWeights()[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            graph.getEdgeExists()[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
            graph.getEdgeWeights()[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            graph.getEdgeExists()[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
        }
    }

}
