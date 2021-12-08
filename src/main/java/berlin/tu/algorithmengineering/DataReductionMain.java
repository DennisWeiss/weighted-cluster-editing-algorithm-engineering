package berlin.tu.algorithmengineering;

import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.OriginalWeightsInfo;

import java.util.Scanner;
import java.util.Stack;

public class DataReductionMain {

    public static final boolean DEBUG = true;

    private static int weight = 0;

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

        applyDataReductions(graph);

        //output reduced graph
        System.out.printf("%d\n", graph.getNumberOfVertices());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                System.out.printf("%d %d %d\n", i+1, j+1, graph.getEdgeWeights()[i][j]);
            }
        }
        System.out.printf("#weight: %d\n", weight);
    }

    private static void applyDataReductions(Graph graph) {
        boolean changed = false;

        //heavy non-edge
        Stack<OriginalWeightsInfo> originalWeightsBeforeHeavyNonEdgeReduction = DataReduction.applyHeavyNonEdgeReduction(graph);
        if (!originalWeightsBeforeHeavyNonEdgeReduction.isEmpty()) {
            changed = true;
        }
        while (!originalWeightsBeforeHeavyNonEdgeReduction.isEmpty()) {
            originalWeightsBeforeHeavyNonEdgeReduction.pop();
            if (DEBUG) {
                System.out.print("heavy non-edge data reduction applied\n");
            }
        }

        //heavy edge single end & heavy edge both ends
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                if (i != j && (graph.getEdgeWeights()[i][j] + Math.abs(graph.getEdgeWeights()[i][j]) >= graph.getAbsoluteNeighborhoodWeights()[i]
                        || 3 * graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i] + graph.getNeighborhoodWeights()[j]
                )) {
                    MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(Math.min(i, j), Math.max(i, j));
                    changed = true;
                    weight += mergeVerticesInfo.getCost();
                    if (DEBUG) {
                        System.out.printf("heavy edge data reduction applied for %d and %d (new weight: %d)\n", i + 1, j + 1, weight);
                    }
                }
            }
        }

        //large neighborhood rule
        MergeVerticesInfo[][] mergeVerticesInfos = new MergeVerticesInfo[graph.getNumberOfVertices()][];
        for (int u = 0; u < graph.getNumberOfVertices(); u++) {//currently we do not try to apply the rule to the N[merged vertex] again
            mergeVerticesInfos[u] = DataReduction.applyClosedNeighborhoodReductionRule(graph, u);
            if (mergeVerticesInfos[u] != null) {
                changed = true;
                weight += MergeVerticesInfo.getTotalCost(mergeVerticesInfos[u]);
                if (DEBUG) {
                    System.out.printf("large neighborhood data reduction applied for %d (new weight: %d)\n", u + 1, weight);
                }
            }
        }

        if (changed) {
            applyDataReductions(graph);
        }
    }
}
