package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.List;
import java.util.Scanner;

public class Main {

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

        boolean[][] edgesToEdit = ce(graph);

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

        P3 p3 = graph.findBiggestWeightP3();

        if (p3 == null) {
            return copy(graph.getEdgeExists(), graph.getNumberOfVertices());
        }

        graph.editEdge(p3.getU(), p3.getV());
        resultEdgeExists = ceBranch(graph, k + graph.getEdgeWeights()[p3.getU()][p3.getV()]);
        graph.editEdge(p3.getU(), p3.getV());

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
        for (int k = 0; ; k++) {
            boolean[][] resultEdgeExists = ceBranch(graph, k);
            if (resultEdgeExists != null) {
                return getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExists);
            }
        }
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
