package berlin.tu.algorithmengineering.ilp;

import berlin.tu.algorithmengineering.common.Graph;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearIntExpr;
import ilog.cplex.IloCplex;

public class LinearProgramming {

    public static boolean[][] getResultEdgeExists(Graph graph) {
        return getResultEdgeExists(graph, false);
    }

    public static boolean[][] getResultEdgeExists(Graph graph, boolean debug) {
        int numberOfVertices = graph.getNumberOfVertices();

        try {
            IloCplex cplex = new IloCplex();

            if (!debug) {
                // To not output logs to console
                cplex.setOut(null);
            }


            IloIntVar[][] x = new IloIntVar[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
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



            return getResultEdgeExists(graph, numberOfVertices, cplex, x);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean[][] getResultEdgeExists(Graph graph, int numberOfVertices, IloCplex cplex, IloIntVar[][] x) throws IloException {
        boolean[][] resultEdgeExists = new boolean[numberOfVertices][numberOfVertices];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (Math.round(cplex.getValue(x[i][j])) == 1.0) {
                    resultEdgeExists[i][j] = true;
                    resultEdgeExists[j][i] = true;
                }
            }
        }
        return resultEdgeExists;
    }
}
