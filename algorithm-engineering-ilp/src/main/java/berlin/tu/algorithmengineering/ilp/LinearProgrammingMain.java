package berlin.tu.algorithmengineering.ilp;


import berlin.tu.algorithmengineering.common.DataReduction;
import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;

import java.util.Stack;

public class LinearProgrammingMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 100;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();
        Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, System.currentTimeMillis(), MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);

        boolean[][] resultEdgeExists = LinearProgramming.getResultEdgeExists(graph, DEBUG);

//        if (DEBUG) {
//            System.out.printf("k = %d + %d\n", (int) cplex.getObjValue(), DataReduction.getTotalCost(mergeVerticesInfoStack));
//        }

        boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, resultEdgeExists.length);
        while (!mergeVerticesInfoStack.empty()) {
            MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
            reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
            graph.revertMergeVertices(mergeVerticesInfo);
        }

        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), reconstructedResultsEdgeExists);

        Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);
    }

}
