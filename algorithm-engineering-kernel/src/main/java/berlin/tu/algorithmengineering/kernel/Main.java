package berlin.tu.algorithmengineering.kernel;


import berlin.tu.algorithmengineering.common.DataReduction;
import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;

import java.util.Scanner;
import java.util.Stack;

public class Main {

    public static final boolean DEBUG = false;
    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 250;

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

        //data reductions
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        long startTime = System.currentTimeMillis();

        Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, startTime, MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);

        //output reduced graph
        System.out.printf("%d\n", graph.getNumberOfVertices());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                System.out.printf("%d %d %d\n", i+1, j+1, graph.getEdgeWeights()[i][j]);
            }
        }

        System.out.printf("#weight: %d\n", DataReduction.getTotalCost(mergeVerticesInfoStack));
    }
}
