package berlin.tu.algorithmengineering.linearprogramming;

import berlin.tu.algorithmengineering.DataReduction;
import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

import java.util.Scanner;
import java.util.Stack;

public class LinearProgrammingMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 100;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfVertices = scanner.nextInt();

        Graph graph = new Graph(numberOfVertices);

        try {
            IloCplex cplex = new IloCplex();

            if (!DEBUG) {
                // To not output logs to console
                cplex.setOut(null);
            }

            for (int i = 0; i < numberOfVertices; i++) {
                for (int j = i + 1; j < numberOfVertices; j++) {
                    int vertex1 = scanner.nextInt() - 1;
                    int vertex2 = scanner.nextInt() - 1;
                    int edgeWeight = scanner.nextInt();

                    graph.getEdgeWeights()[vertex1][vertex2] = edgeWeight;
                    graph.getEdgeWeights()[vertex2][vertex1] = edgeWeight;
                }
            }

            graph.computeEdgeExists();
            graph.computeNeighborhoodWeights();
            graph.computeAbsoluteNeighborhoodWeights();

            Stack<MergeVerticesInfo> mergeVerticesInfoStack = DataReduction.applyDataReductions(graph, System.currentTimeMillis(), MIN_CUT_COMPUTATION_TIMEOUT, DEBUG);

            IloIntVar[][] x = new IloIntVar[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                    x[i][j] = cplex.intVar(0, 1);
                }
            }

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                    for (int k = 0; k < graph.getNumberOfVertices(); k++) {
                        if (i != j && j != k && i != k) {
                            IloLinearIntExpr constraint = cplex.linearIntExpr();
                            constraint.addTerm(1, x[Math.min(i, j)][Math.max(i, j)]);
                            constraint.addTerm(1, x[Math.min(j, k)][Math.max(j, k)]);
                            constraint.addTerm(-1, x[Math.min(i, k)][Math.max(i, k)]);
                            cplex.addGe(1, constraint);
                        }
                    }
                }
            }

            IloLinearIntExpr objective = cplex.linearIntExpr();
            int constant = 0;

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    objective.addTerm(-graph.getEdgeWeights()[i][j], x[i][j]);
                    constant += Math.max(graph.getEdgeWeights()[i][j], 0);
                }
            }

            objective.setConstant(constant);

            cplex.addMinimize(objective);

            cplex.solve();

            if (DEBUG) {
                System.out.printf("k = %d + %d\n", (int) cplex.getObjValue(), DataReduction.getTotalCost(mergeVerticesInfoStack));
            }

            boolean[][] resultEdgeExists = new boolean[numberOfVertices][numberOfVertices];

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    if (Math.round(cplex.getValue(x[i][j])) == 1.0) {
                        resultEdgeExists[i][j] = true;
                        resultEdgeExists[j][i] = true;
                    }
                }
            }

            boolean[][] reconstructedResultsEdgeExists = Utils.copy(resultEdgeExists, resultEdgeExists.length);
            while (!mergeVerticesInfoStack.empty()) {
                MergeVerticesInfo mergeVerticesInfo = mergeVerticesInfoStack.pop();
                reconstructedResultsEdgeExists = Utils.reconstructMergeForResultEdgeExists(reconstructedResultsEdgeExists, graph, mergeVerticesInfo);
                graph.revertMergeVertices(mergeVerticesInfo);
            }

            boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), reconstructedResultsEdgeExists);

            Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

}
