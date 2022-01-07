package berlin.tu.algorithmengineering.common;

import berlin.tu.algorithmengineering.common.model.Edge;
import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.common.model.heuristics.EdgeDeletionsWithCost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Utils {

    public static boolean[][] reconstructMergeForResultEdgeExists(boolean[][] resultEdgeExists, Graph graph, MergeVerticesInfo mergeVerticesInfo) {
        boolean[][] resultEdgeExistsCopy = copy(resultEdgeExists, graph.getNumberOfVertices() + 1);
        boolean[][] newResultEdgeExists = new boolean[graph.getNumberOfVertices() + 1][graph.getNumberOfVertices() + 1];

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
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    if (edgesToEdit[i][j]) {
                        cost += Math.abs(graph.getEdgeWeights()[i][j]);
                    }
                }
            }
            System.out.printf("cost = %d\n", cost);
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (edgesToEdit[i][j]) {
                    stringBuilder.append(String.format("%d %d\n", i + 1, j + 1));
                }
            }
        }

        System.out.print(stringBuilder);
    }

    public static boolean[][] copy(boolean[][] mat, int size) {
        boolean[][] copy = new boolean[size][size];
        for (int i = 0; i < Math.min(mat.length, size); i++) {
            for (int j = 0; j < Math.min(mat.length, size); j++) {
                copy[i][j] = mat[i][j];
            }
        }
        return copy;
    }


//    public static void copyToThreadSafeList(boolean[][] mat, CopyOnWriteArrayList<Boolean> threadSafeEdgeExistsList, int size) {
//        int n = Math.min(mat.length, size);
//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                threadSafeEdgeExistsList.set(i * n + j, mat[i][j] ? 1 : 0);
//            }
//        }
//    }
//
//    public static boolean[][] copyFromAtomicAtomicIntegerArray(AtomicIntegerArray atomicIntegerArray, int size) {
//        boolean[][] copy = new boolean[size][size];
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                copy[i][j] = atomicIntegerArray.get(i * size + j) == 1;
//            }
//        }
//        return copy;
//    }

    public static <T> void addToStackInOrder(Stack<T> stack, Stack<T> stackToAdd) {
        Stack<T> helperStack = new Stack<>();
        while (!stackToAdd.empty()) {
            helperStack.push(stackToAdd.pop());
        }
        while (!helperStack.empty()) {
            stack.push(helperStack.pop());
        }
    }

    public static Graph readGraphFromConsoleInput() {
        Graph graph = null;
        int numberOfVertices;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            numberOfVertices = Integer.parseInt(reader.readLine());
            graph = new Graph(numberOfVertices);

            String line;
            for (int i = 0; i < numberOfVertices; i++) {
                for (int j = i+1; j < numberOfVertices; j++) {
                    line = reader.readLine();
                    String[] str = line.split("\\s");

                    int vertex1 = Integer.parseInt(str[0]) - 1;
                    int vertex2 = Integer.parseInt(str[1]) - 1;
                    int edgeWeight = Integer.parseInt(str[2]);

                    graph.getEdgeWeights()[vertex1][vertex2] = edgeWeight;
                    graph.getEdgeWeights()[vertex2][vertex1] = edgeWeight;
                }
            }

            reader.close();

            graph.computeEdgeExists();
            graph.computeNeighborhoodWeights();
            graph.computeAbsoluteNeighborhoodWeights();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    public static int getCostToChange(Graph graph, boolean[][] changedEdgeExists) {
        int cost = 0;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j] != changedEdgeExists[i][j])
                    cost += Math.abs(graph.getEdgeWeights()[i][j]);
            }
        }
        return cost;
    }

    public static AtomicInteger getCostToChange(Graph graph, CopyOnWriteArrayList<Boolean> changedEdgeExists) {
        int cost = 0;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j] != changedEdgeExists.get(i * graph.getNumberOfVertices() + j))
                    cost += Math.abs(graph.getEdgeWeights()[i][j]);
            }
        }
        return new AtomicInteger(cost);
    }

    /**
     * Returns random integer over uniform distribution
     *
     * @param from inclusive
     * @param to   exclusive
     * @return random integer
     */
    public static int randInt(int from, int to) {
        return (int) ((to - from) * Math.random() + from);
    }

    public static int[] getIntArrayInRange(int to) {
        return getIntArrayInRange(0, to);
    }

    public static int[] getIntArrayInRange(int from, int to) {
        int[] array = new int[to - from];
        for (int i = 0; i < array.length; i++) {
            array[i] = from + i;
        }
        return array;
    }

    public static void shuffleArray(int[] ar) {
        Random random = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static boolean[][] getResultEdgeExistsFromEdgesDeletions(Graph graph, EdgeDeletionsWithCost edgeDeletionsWithCost) {
        Graph resultGraph = graph.copy();
        for (Edge edge : edgeDeletionsWithCost.getEdgeDeletions()) {
            resultGraph.flipEdge(edge.getA(), edge.getB());
        }
        resultGraph.transitiveClosure();
        return resultGraph.getEdgeExists();
    }


}
