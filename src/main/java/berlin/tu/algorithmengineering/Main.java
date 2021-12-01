package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static final boolean DEBUG = false;

    public static final int FORBIDDEN_VALUE = (int) -Math.pow(2, 16);

    private static int recursiveSteps = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfVertices = scanner.nextInt();

        Graph graph = new Graph(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                int vertex1 = scanner.nextInt() - 1;
                int vertex2 = scanner.nextInt() - 1;
                int edgeWeight = scanner.nextInt();
                graph.setEdge(vertex1, vertex2, edgeWeight);
            }
        }

        graph.computeNeighborhoodWeights();

        boolean[][] edgesToEdit = ceBinarySearchInitial(graph);

        if (DEBUG) {
            int cost = 0;

            for (int i = 0; i < numberOfVertices; i++) {
                for (int j = i+1; j < numberOfVertices; j++) {
                    if (edgesToEdit[i][j]) {
                        cost += Math.abs(graph.getEdgeWeights()[i][j]);
                    }
                }
            }

            System.out.printf("cost = %d\n", cost);
        }

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                if (edgesToEdit[i][j]) {
                    System.out.printf("%d %d\n", i+1, j+1);
                }
            }
        }

        System.out.printf("#recursive steps: %d\n", recursiveSteps);
    }

    public static boolean[][] ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        recursiveSteps++;

        boolean[][] resultEdgeExists;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeWeights()[i][j] > k) {
                    MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(Math.min(i, j), Math.max(i, j), k);
                    resultEdgeExists = ceBranch(graph, mergeVerticesInfo.getK());

                    if (resultEdgeExists != null) {
                        resultEdgeExists = reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
                    }
                    graph.revertMergeVertices(mergeVerticesInfo);

                    return resultEdgeExists;
                }
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeWeights()[i][j] < 0 && graph.getEdgeWeights()[i][j] > FORBIDDEN_VALUE
                        && -graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i]) {
                    int originalWeight = graph.getEdgeWeights()[i][j];
                    graph.getEdgeWeights()[i][j] = FORBIDDEN_VALUE;
                    graph.getEdgeWeights()[j][i] = FORBIDDEN_VALUE;

                    resultEdgeExists = ceBranch(graph, k);

                    graph.getEdgeWeights()[i][j] = originalWeight;
                    graph.getEdgeWeights()[j][i] = originalWeight;

                    return resultEdgeExists;
                }
            }
        }

        List<P3> p3List = graph.findAllP3();
//        P3 p3 = graph.findBiggestWeightP3();

        if (p3List.isEmpty()) {
            return copy(graph.getEdgeExists(), graph.getNumberOfVertices());
        }

        P3 p3 = getBiggestWeightP3(graph, p3List);

        if (graph.getLowerBound2(p3List) > k) {
            return null;
        }

        graph.flipEdge(p3.getU(), p3.getV());
        int cost = graph.getEdgeWeights()[p3.getU()][p3.getV()];
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = FORBIDDEN_VALUE;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = FORBIDDEN_VALUE;
        resultEdgeExists = ceBranch(graph, k + cost);
        graph.flipEdge(p3.getU(), p3.getV());
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = -cost;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = -cost;

        if (resultEdgeExists != null) {
            return resultEdgeExists;
        }

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV(), k);
        resultEdgeExists = ceBranch(graph, mergeVerticesInfo.getK());
        if (resultEdgeExists != null) {
            resultEdgeExists = reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
        }
        graph.revertMergeVertices(mergeVerticesInfo);

        return resultEdgeExists;
    }

    private static P3 getBiggestWeightP3(Graph graph, List<P3> p3List) {
        int biggestWeight = Integer.MIN_VALUE;
        P3 biggestWeightP3 = null;
        for (P3 p3 : p3List) {
            int totalAbsoluteWeight = graph.getTotalAbsoluteWeight(p3);
            if (totalAbsoluteWeight > biggestWeight) {
                biggestWeightP3 = p3;
                biggestWeight = totalAbsoluteWeight;
            }
        }
        return biggestWeightP3;
    }

    private static boolean[][] reconstructMergeForResultEdgeExists(boolean[][] resultEdgeExists, Graph graph, MergeVerticesInfo mergeVerticesInfo) {
        boolean[][] newResultEdgeExists = copy(resultEdgeExists, resultEdgeExists.length);
        boolean[][] newNewResultEdgeExists = new boolean[resultEdgeExists.length][resultEdgeExists.length];

        if (graph.getNumberOfVertices() != mergeVerticesInfo.getSecondVertex()) {
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                newResultEdgeExists[graph.getNumberOfVertices()][i] = resultEdgeExists[mergeVerticesInfo.getSecondVertex()][i];
                newResultEdgeExists[i][graph.getNumberOfVertices()] = resultEdgeExists[i][mergeVerticesInfo.getSecondVertex()];
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            for (int j = 0; j < graph.getNumberOfVertices() + 1; j++) {
                if (i != j && i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()
                        && j != mergeVerticesInfo.getFirstVertex() && j != mergeVerticesInfo.getSecondVertex()) {
                    newNewResultEdgeExists[i][j] = newResultEdgeExists[i][j];
                }
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            if (i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()) {
                boolean edgeExistsWithMergedVertex = newResultEdgeExists[mergeVerticesInfo.getFirstVertex()][i];
                newNewResultEdgeExists[mergeVerticesInfo.getFirstVertex()][i] = edgeExistsWithMergedVertex;
                newNewResultEdgeExists[i][mergeVerticesInfo.getFirstVertex()] = edgeExistsWithMergedVertex;
                newNewResultEdgeExists[mergeVerticesInfo.getSecondVertex()][i] = edgeExistsWithMergedVertex;
                newNewResultEdgeExists[i][mergeVerticesInfo.getSecondVertex()] = edgeExistsWithMergedVertex;
            }
        }



        newNewResultEdgeExists[mergeVerticesInfo.getFirstVertex()][mergeVerticesInfo.getSecondVertex()] = true;
        newNewResultEdgeExists[mergeVerticesInfo.getSecondVertex()][mergeVerticesInfo.getFirstVertex()] = true;

        return newNewResultEdgeExists;
    }

    public static boolean[][] ce(Graph graph) {
        Graph graphCopy = null;
        if (DEBUG) {
            graphCopy = graph.copy();
        }
        for (int k = 0; ; k++) {
            boolean[][] resultEdgeExists = ceBranch(graph, k);
            if (DEBUG) {
                for (int i = 0; i < graphCopy.getNumberOfVertices(); i++) {
                    for (int j = 0; j < graphCopy.getNumberOfVertices(); j++) {
                        if (i != j && graph.getEdgeWeights()[i][j] != graphCopy.getEdgeWeights()[i][j]) {
                            System.out.printf("weights not equal at [%d, %d] with k = %d\n", i, j, k);
                        }
                        if (i != j && graph.getEdgeExists()[i][j] != graphCopy.getEdgeExists()[i][j]) {
                            System.out.printf("edgeExists not equal at [%d, %d] with k = %d\n", i, j, k);
                        }
                    }
                }
            }

            if (resultEdgeExists != null) {
                if (DEBUG) System.out.printf("last k = %d\n", k);
                return getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExists);
            }
        }
    }

    public static boolean[][] ceBinarySearchInitial(Graph graph) {
        final double FACTOR = 1.2;

        boolean[][] resultEdgeExists = ceBranch(graph, 0);
        if (resultEdgeExists == null) {
            int lo = 1;
            int hi = 1;
            for (int k = 1; ; k = (int) Math.ceil(FACTOR * k)) {
                hi = k;
                resultEdgeExists = ceBranch(graph, k);
                if (resultEdgeExists != null) {
                    resultEdgeExists = ceBinarySearch(graph, lo, hi);
                    break;
                }
                lo = k;
            }
        }
        return getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExists);
    }

    private static boolean[][] ceBinarySearch(Graph graph, int lo, int hi) {
        if (lo == hi) {
            return ceBranch(graph, lo);
        }
        int k = (lo + hi) / 2;
        boolean[][] resultEdgeExists = ceBranch(graph, k);
        if (resultEdgeExists != null) {
            if (lo == k) {
                return resultEdgeExists;
            }
            return ceBinarySearch(graph, lo, k);
        }
        return ceBinarySearch(graph, k + 1, hi);
    }

    private static boolean[][] getEdgesToEditFromResultEdgeExists(boolean[][] edgeExists, boolean[][] resultEdgeExists) {
        boolean[][] edgesToEdit = new boolean[edgeExists.length][edgeExists.length];
        for (int i = 0; i < edgeExists.length; i++) {
            for (int j = 0; j < edgeExists.length; j++) {
                edgesToEdit[i][j] = edgeExists[i][j] != resultEdgeExists[i][j];
            }
        }
        return edgesToEdit;
    }

    private static boolean[][] copy(boolean[][] mat, int size) {
        if (mat.length == 0) {
            return new boolean[0][0];
        }
        boolean[][] copy = new boolean[mat.length][mat[0].length];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                copy[i][j] = mat[i][j];
            }
        }
        return copy;
    }
}
