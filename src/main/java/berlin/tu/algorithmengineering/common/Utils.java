package berlin.tu.algorithmengineering.common;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.MergeVerticesInfo;

import java.util.Stack;

public class Utils {

    public static boolean[][] reconstructMergeForResultEdgeExists(boolean[][] resultEdgeExists, Graph graph, MergeVerticesInfo mergeVerticesInfo) {
        boolean[][] resultEdgeExistsCopy = copy(resultEdgeExists, resultEdgeExists.length);
        boolean[][] newResultEdgeExists = new boolean[resultEdgeExists.length][resultEdgeExists.length];

        // Just for convenience
        if (graph.getNumberOfVertices() != mergeVerticesInfo.getSecondVertex()) {
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                resultEdgeExistsCopy[graph.getNumberOfVertices()][i] = resultEdgeExists[mergeVerticesInfo.getSecondVertex()][i];
                resultEdgeExistsCopy[i][graph.getNumberOfVertices()] = resultEdgeExists[i][mergeVerticesInfo.getSecondVertex()];
            }
        }

        // Just copy value if neither i or j are merged vertex
        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            for (int j = 0; j < graph.getNumberOfVertices() + 1; j++) {
                if (i != j && i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()
                        && j != mergeVerticesInfo.getFirstVertex() && j != mergeVerticesInfo.getSecondVertex()) {
                    newResultEdgeExists[i][j] = resultEdgeExistsCopy[i][j];
                }
            }
        }

        // Setting resultEdge if a vertex is a vertex which has been merged from
        for (int i = 0; i < graph.getNumberOfVertices() + 1; i++) {
            if (i != mergeVerticesInfo.getFirstVertex() && i != mergeVerticesInfo.getSecondVertex()) {
                boolean edgeExistsWithMergedVertex = resultEdgeExistsCopy[mergeVerticesInfo.getFirstVertex()][i];
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

    public static boolean[][] getEdgesToEditFromResultEdgeExists(boolean[][] edgeExists, boolean[][] resultEdgeExists) {
        boolean[][] edgesToEdit = new boolean[edgeExists.length][edgeExists.length];
        for (int i = 0; i < edgeExists.length; i++) {
            for (int j = 0; j < edgeExists.length; j++) {
                edgesToEdit[i][j] = edgeExists[i][j] != resultEdgeExists[i][j];
            }
        }
        return edgesToEdit;
    }

    public static void printEdgesToEdit(Graph graph, boolean[][] edgesToEdit) {
        printEdgesToEdit(graph, edgesToEdit, false);
    }

    public static void printEdgesToEdit(Graph graph, boolean[][] edgesToEdit, boolean debug) {
        if (debug) {
            int cost = 0;

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                    if (edgesToEdit[i][j]) {
                        cost += Math.abs(graph.getEdgeWeights()[i][j]);
                    }
                }
            }
            System.out.printf("cost = %d\n", cost);
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (edgesToEdit[i][j]) {
                    System.out.printf("%d %d\n", i+1, j+1);
                }
            }
        }
    }

    public static boolean[][] copy(boolean[][] mat, int size) {
        boolean[][] copy = new boolean[mat.length][mat.length];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                copy[i][j] = mat[i][j];
            }
        }
        return copy;
    }

    public static <T> void addToStackInOrder(Stack<T> stack, Stack<T> stackToAdd) {
        Stack<T> helperStack = new Stack<>();
        while (!stackToAdd.empty()) {
            helperStack.push(stackToAdd.pop());
        }
        while (!helperStack.empty()) {
            stack.push(helperStack.pop());
        }
    }

}