package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.Main;
import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

public class Heuristics {

    public static EdgeDeletionsWithCost getGreedyHeuristic1(Graph graph) {
        EdgeDeletionsWithCost edgeDeletionsWithCost = new EdgeDeletionsWithCost(new HashSet<>(), 0);
        int transitiveClosureCost = graph.getTransitiveClosureCost();
        if (transitiveClosureCost == 0) {
            return edgeDeletionsWithCost;
        }
        int[][] edgeScores = scoreEdges(graph);
        PriorityQueue<EdgeWithScore> edgesWithScore = new PriorityQueue<>(Collections.reverseOrder());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    edgesWithScore.add(new EdgeWithScore(new Edge(i, j), edgeScores[i][j]));
                }
            }
        }

        Set<Set<Integer>> connectedComponents;

        while ((connectedComponents = graph.getConnectedComponents()).size() < 2) {
            EdgeWithScore edgeToRemove = edgesWithScore.poll();
            edgeDeletionsWithCost.getEdgeDeletions().add(edgeToRemove.getEdge());
            edgeDeletionsWithCost.addCost(graph.getEdgeWeights()[edgeToRemove.getEdge().getA()][edgeToRemove.getEdge().getB()]);
            graph.flipEdge(edgeToRemove.getEdge().getA(), edgeToRemove.getEdge().getB());
        }

        int[] vertexToConnectedComponentIndex = Graph.getVertexToConnectedComponentIndex(connectedComponents, graph.getNumberOfVertices());
        for (Edge edge : new ArrayList<>(edgeDeletionsWithCost.getEdgeDeletions())) {
            if (vertexToConnectedComponentIndex[edge.getA()] == vertexToConnectedComponentIndex[edge.getB()]) {
                edgeDeletionsWithCost.getEdgeDeletions().remove(edge);
                graph.flipEdge(edge.getA(), edge.getB());
                edgeDeletionsWithCost.addCost(-graph.getEdgeWeights()[edge.getA()][edge.getB()]);
            }
        }

        if (edgeDeletionsWithCost.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        Iterator<Set<Integer>> connectedComponentsIterator = connectedComponents.iterator();
        ArrayList<Integer> subGraphIndices1 = new ArrayList<>(connectedComponentsIterator.next());
        Graph subGraph = graph.getSubGraph(subGraphIndices1);

        EdgeDeletionsWithCost edgeDeletionsWithCostOfSubGraph1 = getGreedyHeuristic1(subGraph);
        if (edgeDeletionsWithCost.getCost() + edgeDeletionsWithCostOfSubGraph1.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        ArrayList<Integer> subGraphIndices2 = new ArrayList<>(connectedComponentsIterator.next());
        subGraph = graph.getSubGraph(subGraphIndices2);

        EdgeDeletionsWithCost edgeDeletionsWithCostOfSubGraph2 = getGreedyHeuristic1(subGraph);
        if (edgeDeletionsWithCost.getCost() + edgeDeletionsWithCostOfSubGraph1.getCost()
                + edgeDeletionsWithCostOfSubGraph2.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        for (Edge edge : edgeDeletionsWithCostOfSubGraph1.getEdgeDeletions()) {
            edgeDeletionsWithCost.getEdgeDeletions().add(new Edge(subGraphIndices1.get(edge.getA()), subGraphIndices1.get(edge.getB())));
        }
        for (Edge edge : edgeDeletionsWithCostOfSubGraph2.getEdgeDeletions()) {
            edgeDeletionsWithCost.getEdgeDeletions().add(new Edge(subGraphIndices2.get(edge.getA()), subGraphIndices2.get(edge.getB())));
        }
        edgeDeletionsWithCost.addCost(edgeDeletionsWithCostOfSubGraph1.getCost());
        edgeDeletionsWithCost.addCost(edgeDeletionsWithCostOfSubGraph2.getCost());
        return edgeDeletionsWithCost;
    }

    private static int[][] scoreEdges(Graph graph) {
        int[][] edgeScores = new int[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    int d = getD(graph, i, j);

                    graph.flipEdge(i, j);
                    int edgeFlipCost = graph.getEdgeWeights()[i][j];
                    graph.getEdgeWeights()[i][j] = Main.FORBIDDEN_VALUE;
                    graph.getEdgeWeights()[j][j] = Main.FORBIDDEN_VALUE;
                    graph.getAbsoluteNeighborhoodWeights()[i] += -Main.FORBIDDEN_VALUE + edgeFlipCost;
                    graph.getAbsoluteNeighborhoodWeights()[j] += -Main.FORBIDDEN_VALUE + edgeFlipCost;

                    int score = d - getD(graph, i, j) + edgeFlipCost;
                    edgeScores[i][j] = score;
                    edgeScores[j][i] = score;

                    graph.getEdgeWeights()[i][j] = edgeFlipCost;
                    graph.getEdgeWeights()[j][i] = edgeFlipCost;
                    graph.getAbsoluteNeighborhoodWeights()[i] -= -Main.FORBIDDEN_VALUE + edgeFlipCost;
                    graph.getAbsoluteNeighborhoodWeights()[j] -= -Main.FORBIDDEN_VALUE + edgeFlipCost;
                    graph.flipEdge(i, j);
                }
            }
        }
        return edgeScores;
    }

    private static int getD(Graph graph, int u, int v) {
        List<P3> p3List = graph.findAllP3WithEdge(u, v);
        int lowerBound = 0;
        for (P3 p3 : p3List) {
            lowerBound += graph.getSmallestAbsoluteWeight(p3);

        }
        return lowerBound;
    }

    private static int[][] scoreEdgesLp(Graph graph) {
        try {
            IloCplex cplex = new IloCplex();

            // To not output logs to console
            cplex.setOut(null);

            IloNumVar[][] x = new IloNumVar[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    x[i][j] = cplex.intVar(0, 1);
                }
            }

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                    for (int k = 0; k < graph.getNumberOfVertices(); k++) {
                        if (i != j && j != k && i != k) {
                            IloLinearNumExpr constraint = cplex.linearNumExpr();
                            constraint.addTerm(1, x[Math.min(i, j)][Math.max(i, j)]);
                            constraint.addTerm(1, x[Math.min(j, k)][Math.max(j, k)]);
                            constraint.addTerm(-1, x[Math.min(i, k)][Math.max(i, k)]);
                            cplex.addGe(1, constraint);
                        }
                    }
                }
            }

            IloLinearNumExpr objective = cplex.linearNumExpr();
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

            System.out.println(cplex.getObjValue());
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    System.out.printf("%d %d: %f\n", i, j, cplex.getValue(x[i][j]));
                }
            }
            System.out.printf("");
        } catch (IloException e) {
            e.printStackTrace();
        }

        return new int[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
    }


}
