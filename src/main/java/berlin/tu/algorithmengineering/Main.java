package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.OriginalWeightsInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Main {

    public static final boolean DEBUG = true;

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
        graph.computeAbsoluteNeighborhoodWeights();

        boolean[][] resultEdgeExists = ce(graph);

        boolean[][] edgesToEdit = getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExists);

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

    private static boolean[][] ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        List<P3> p3List = graph.findAllP3();

        if (p3List.isEmpty()) {
            return copy(graph.getEdgeExists(), graph.getNumberOfVertices());
        }

        if (graph.getLowerBound2(p3List) > k) {
            return null;
        }

        Stack<OriginalWeightsInfo> originalWeightsBeforeHeavyNonEdgeReduction = DataReduction.applyHeavyNonEdgeReduction(graph);

        P3 p3 = getBiggestWeightP3(graph, p3List);

        boolean[][] resultEdgeExists;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                if (i != j && (graph.getEdgeWeights()[i][j] > k
                        || graph.getEdgeWeights()[i][j] + Math.abs(graph.getEdgeWeights()[i][j]) >= graph.getAbsoluteNeighborhoodWeights()[i]
                        || 3 * graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i] + graph.getNeighborhoodWeights()[j]
                )) {
                    MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(Math.min(i, j), Math.max(i, j));
                    resultEdgeExists = ceBranch(graph, k - mergeVerticesInfo.getCost());

                    if (resultEdgeExists != null) {
                        resultEdgeExists = reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
                    }
                    graph.revertMergeVertices(mergeVerticesInfo);
                    DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                    return resultEdgeExists;
                }
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            MergeVerticesInfo[] mergeVerticesInfos = DataReduction.applyClosedNeighborhoodReductionRule(graph,i);
            if (mergeVerticesInfos != null) {
                resultEdgeExists = ceBranch(graph, k - MergeVerticesInfo.getTotalCost(mergeVerticesInfos));

                for (int j = mergeVerticesInfos.length - 1; j >= 0; j--) {
                    if (resultEdgeExists != null) {
                        resultEdgeExists = reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfos[j]);
                    }
                    graph.revertMergeVertices(mergeVerticesInfos[j]);
                }

                DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                return resultEdgeExists;
            }
        }

        recursiveSteps++;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
        resultEdgeExists = ceBranch(graph, k - mergeVerticesInfo.getCost());
        if (resultEdgeExists != null) {
            resultEdgeExists = reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
        }
        graph.revertMergeVertices(mergeVerticesInfo);

        if (resultEdgeExists != null) {
            DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);
            return resultEdgeExists;
        }

        graph.flipEdge(p3.getU(), p3.getV());
        int cost = graph.getEdgeWeights()[p3.getU()][p3.getV()];
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = FORBIDDEN_VALUE;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = FORBIDDEN_VALUE;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] += -FORBIDDEN_VALUE + cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] += -FORBIDDEN_VALUE + cost;
        resultEdgeExists = ceBranch(graph, k + cost);
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = cost;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] -= -FORBIDDEN_VALUE + cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] -= -FORBIDDEN_VALUE + cost;
        graph.flipEdge(p3.getU(), p3.getV());

        DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

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
        boolean[][] ResultEdgeExistsCopy = copy(resultEdgeExists, resultEdgeExists.length);
        boolean[][] newResultEdgeExists = new boolean[resultEdgeExists.length][resultEdgeExists.length];

        // Just for convenience
        if (graph.getNumberOfVertices() != mergeVerticesInfo.getSecondVertex()) {
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                ResultEdgeExistsCopy[graph.getNumberOfVertices()][i] = resultEdgeExists[mergeVerticesInfo.getSecondVertex()][i];
                ResultEdgeExistsCopy[i][graph.getNumberOfVertices()] = resultEdgeExists[i][mergeVerticesInfo.getSecondVertex()];
            }
        }

        // Just copy value if neither i or j are merged vertex
        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            for (int j = 0; j < graph.getNumberOfVertices() + 1; j++) {
                if (i != j && i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()
                        && j != mergeVerticesInfo.getFirstVertex() && j != mergeVerticesInfo.getSecondVertex()) {
                    newResultEdgeExists[i][j] = ResultEdgeExistsCopy[i][j];
                }
            }
        }

        // Setting resultEdge if a vertex is a vertex which has been merged from
        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            if (i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()) {
                boolean edgeExistsWithMergedVertex = ResultEdgeExistsCopy[mergeVerticesInfo.getFirstVertex()][i];
                newResultEdgeExists[mergeVerticesInfo.getFirstVertex()][i] = edgeExistsWithMergedVertex;
                newResultEdgeExists[i][mergeVerticesInfo.getFirstVertex()] = edgeExistsWithMergedVertex;
                newResultEdgeExists[mergeVerticesInfo.getSecondVertex()][i] = edgeExistsWithMergedVertex;
                newResultEdgeExists[i][mergeVerticesInfo.getSecondVertex()] = edgeExistsWithMergedVertex;
            }
        }

        newResultEdgeExists[mergeVerticesInfo.getFirstVertex()][mergeVerticesInfo.getSecondVertex()] = true;
        newResultEdgeExists[mergeVerticesInfo.getSecondVertex()][mergeVerticesInfo.getFirstVertex()] = true;

        return newResultEdgeExists;
    }

    public static boolean[][] ce(Graph graph) {
        Graph graphCopy = null;
        if (DEBUG) {
            graphCopy = graph.copy();
        }
        for (int k = 793; ; k++) {
            recursiveSteps++;
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
                return resultEdgeExists;
            }
        }
    }

    public static boolean[][] ceBinarySearchInitial(Graph graph) {
        final double FACTOR = 1.2;

        recursiveSteps++;
        boolean[][] resultEdgeExists = ceBranch(graph, 0);
        if (resultEdgeExists == null) {
            int lo = 1;
            int hi = 1;
            for (int k = 1; ; k = (int) Math.ceil(FACTOR * k)) {
                hi = k;
                recursiveSteps++;
                resultEdgeExists = ceBranch(graph, k);
                if (resultEdgeExists != null) {
                    resultEdgeExists = ceBinarySearch(graph, lo, hi);
                    break;
                }
                lo = k;
            }
        }
        return resultEdgeExists;
    }

    private static boolean[][] ceBinarySearch(Graph graph, int lo, int hi) {
        if (lo == hi) {
            if (DEBUG) {
                System.out.printf("last k = %d\n", lo);
            }
            recursiveSteps++;
            return ceBranch(graph, lo);
        }
        int k = (lo + hi) / 2;
        recursiveSteps++;
        boolean[][] resultEdgeExists = ceBranch(graph, k);
        if (resultEdgeExists != null) {
            if (lo == k) {
                if (DEBUG) {
                    System.out.printf("last k = %d\n", k);
                }
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
        boolean[][] copy = new boolean[mat.length][mat.length];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                copy[i][j] = mat[i][j];
            }
        }
        return copy;
    }
}
