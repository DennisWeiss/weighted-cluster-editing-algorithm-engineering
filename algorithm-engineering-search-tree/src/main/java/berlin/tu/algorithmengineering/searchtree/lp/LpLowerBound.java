package berlin.tu.algorithmengineering.searchtree.lp;

import berlin.tu.algorithmengineering.common.Graph;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import gurobi.*;


public class LpLowerBound {

    public static double getLowerBound(Graph graph) {
        return getLowerBound(graph, false);
    }

    public static double getLowerBound(Graph graph, boolean debug) {
        int numberOfVertices = graph.getNumberOfVertices();

        try {
            GRBEnv env = new GRBEnv(true);

            if (!debug) {
                env.set(GRB.IntParam.LogToConsole, 0);
            }

            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[][] x = new GRBVar[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    x[i][j] = model.addVar(0, 1, -graph.getEdgeWeights()[i][j], GRB.CONTINUOUS,
                            String.format("%d %d", graph.getNumberOfVertices(), graph.getNumberOfVertices()));
                }
            }

            model.set(GRB.IntAttr.ModelSense, GRB.MINIMIZE);

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                    for (int k = 0; k < graph.getNumberOfVertices(); k++) {
                        if (i != j && j != k && i != k) {
                            GRBLinExpr constraint = new GRBLinExpr();
                            constraint.addTerm(1., x[Math.min(i, j)][Math.max(i, j)]);
                            constraint.addTerm(1., x[Math.min(j, k)][Math.max(j, k)]);
                            constraint.addTerm(-1., x[Math.min(i, k)][Math.max(i, k)]);
                            model.addConstr(1., GRB.GREATER_EQUAL, constraint, String.format("%d %d %d", i, j, k));
                        }
                    }
                }
            }

            model.optimize();

            int constant = 0;

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    constant += Math.max(graph.getEdgeWeights()[i][j], 0);
                }
            }

            return model.get(GRB.DoubleAttr.ObjVal) + constant;
        } catch (GRBException e) {
            e.printStackTrace();
        }
        return Double.MAX_VALUE;
    }

    public static double getLowerBoundOrTools(Graph graph) {
        MPSolver mpSolver = MPSolver.createSolver("GLOP");

        MPVariable[][] x = new MPVariable[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                x[i][j] = mpSolver.makeNumVar(0.0, 1.0, String.format("%d %d", graph.getNumberOfVertices(), graph.getNumberOfVertices()));
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                for (int k = 0; k < graph.getNumberOfVertices(); k++) {
                    if (i != j && j != k && i != k) {
                        MPConstraint constraint = mpSolver.makeConstraint(Double.NEGATIVE_INFINITY, 1.0);
                        constraint.setCoefficient(x[Math.min(i, j)][Math.max(i, j)], 1.0);
                        constraint.setCoefficient(x[Math.min(j, k)][Math.max(j, k)], 1.0);
                        constraint.setCoefficient(x[Math.min(i, k)][Math.max(i, k)], -1.0);
                    }
                }
            }
        }

        MPObjective objective = mpSolver.objective();
        double constant = 0;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                objective.setCoefficient(x[i][j], -graph.getEdgeWeights()[i][j]);
                constant += Math.max(graph.getEdgeWeights()[i][j], 0.0);
            }
        }

        objective.setOffset(constant);

        objective.setMinimization();

        MPSolver.ResultStatus resultStatus = mpSolver.solve();

        return objective.value();
    }
}
