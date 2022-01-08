package berlin.tu.algorithmengineering.csp;


import berlin.tu.algorithmengineering.common.DataReduction;
import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import com.google.ortools.sat.*;

import java.io.File;
import java.util.Locale;
import java.util.Stack;

public class ConstraintSatisfactionMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 100;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

        int numberOfVertices = graph.getNumberOfVertices();

        Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, System.currentTimeMillis(), MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);

        //Loader.loadNativeLibraries();
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.equals("mac os x")) { // only for MAC local
            System.load("/Applications/or-tools_MacOsX-12.0.1_v9.2.9972/ortools-darwin-x86-64/libjniortools.dylib");
        } else {
            File file = new File("lib/or-tools_Ubuntu-18.04-64bit_v9.2.9972/extracted-jar/ortools-linux-x86-64/libjniortools.so");
            String absolutePath = file.getAbsolutePath();
            System.load(absolutePath);
        }

        boolean[][] resultEdgeExists = weightedClusterEditingBinarySearchInitial(graph);

        boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, numberOfVertices);
        while (!mergeVerticesInfoStack.empty()) {
            MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
            reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
            graph.revertMergeVertices(mergeVerticesInfo);
        }

        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), reconstructedResultsEdgeExists);

        System.out.print(Utils.edgesToEditString(graph, edgesToEdit, DEBUG));
    }

    private static boolean[][] weightedClusterEditing(Graph graph, int k) {
        CpModel cpModel = new CpModel();

        IntVar[][] x = new IntVar[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                x[i][j] = cpModel.newIntVar(0, 1, i + " " + j);
            }
        }

        for (int u = 0; u < graph.getNumberOfVertices(); u++) {
            for (int v = u+1; v < graph.getNumberOfVertices(); v++) {
                for (int w = v+1; w < graph.getNumberOfVertices(); w++) {
                    LinearExpr numberOfEdgesInTriple = LinearExpr.sum(new IntVar[]{x[u][v], x[v][w], x[u][w]});
                    cpModel.addDifferent(numberOfEdgesInTriple, 2);
                }
            }
        }

        int numberOfEdges = graph.getNumberOfVertices() * (graph.getNumberOfVertices() - 1) / 2;
        IntVar[] edges = new IntVar[numberOfEdges];
        int[] weights = new int[numberOfEdges];
        int constant = 0;

        int l = 0;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++, l++) {
                edges[l] = x[i][j];
                weights[l] = -graph.getEdgeWeights()[i][j];
                constant += Math.max(graph.getEdgeWeights()[i][j], 0);
            }
        }

        LinearExpr cost = LinearExpr.scalProd(edges, weights);

        cpModel.addLessOrEqual(cost, k - constant);

        CpSolver cpSolver = new CpSolver();

        CpSolverStatus solveStatus = cpSolver.solve(cpModel);

        if (solveStatus == CpSolverStatus.OPTIMAL) {
            return getResultEdgeExists(graph, cpSolver, x);
        }
        return null;
    }

    private static boolean[][] getResultEdgeExists(Graph graph, CpSolver cpSolver, IntVar[][] x) {
        boolean[][] resultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (cpSolver.value(x[i][j]) == 1) {
                    resultEdgeExists[i][j] = true;
                    resultEdgeExists[j][i] = true;
                }
            }
        }

        return resultEdgeExists;
    }

    public static boolean[][] weightedClusterEditingBinarySearchInitial(Graph graph) {
        final double FACTOR = 1.2;

        boolean[][] resultEdgeExists = weightedClusterEditing(graph, 0);
        if (resultEdgeExists == null) {
            int lo = 1;
            int hi = 1;
            for (int k = 1; ; k = (int) Math.ceil(FACTOR * k)) {
                hi = k;
                resultEdgeExists = weightedClusterEditing(graph, k);
                if (resultEdgeExists != null) {
                    resultEdgeExists = weightedClusterEditingBinarySearch(graph, lo, hi);
                    break;
                }
                lo = k;
            }
        }
        return resultEdgeExists;
    }

    private static boolean[][] weightedClusterEditingBinarySearch(Graph graph, int lo, int hi) {
        if (lo == hi) {
            if (DEBUG) {
                System.out.printf("last k = %d\n", lo);
            }
            return weightedClusterEditing(graph, lo);
        }
        int k = (lo + hi) / 2;
        boolean[][] resultEdgeExists = weightedClusterEditing(graph, k);
        if (resultEdgeExists != null) {
            if (lo == k) {
                if (DEBUG) {
                    System.out.printf("last k = %d\n", k);
                }
                return resultEdgeExists;
            }
            return weightedClusterEditingBinarySearch(graph, lo, k);
        }
        return weightedClusterEditingBinarySearch(graph, k + 1, hi);
    }

}
