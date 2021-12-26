package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.Main;
import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.*;

public class Heuristics {

    public static EdgeDeletionsWithCost getGreedyHeuristic1(Graph graph) {
        EdgeDeletionsWithCost edgeDeletionsWithCost = new EdgeDeletionsWithCost(new HashSet<>(), 0);
        int transitiveClosureCost = graph.getTransitiveClosureCost();
        if (transitiveClosureCost == 0) {
            return edgeDeletionsWithCost;
        }
        int[][] edgeScores = scoreEdges(graph);
        PriorityQueue<EdgeWithScoreInt> edgesWithScore = new PriorityQueue<>(Collections.reverseOrder());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    edgesWithScore.add(new EdgeWithScoreInt(new Edge(i, j), edgeScores[i][j]));
                }
            }
        }

        Set<Set<Integer>> connectedComponents;

        while ((connectedComponents = graph.getConnectedComponents()).size() < 2) {
            EdgeWithScoreInt edgeToRemove = edgesWithScore.poll();
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


    public static EdgeDeletionsWithCost getGreedyHeuristicLp(Graph graph) {
        EdgeDeletionsWithCost edgeDeletionsWithCost = new EdgeDeletionsWithCost(new HashSet<>(), 0);
        int transitiveClosureCost = graph.getTransitiveClosureCost();
        if (transitiveClosureCost == 0) {
            return edgeDeletionsWithCost;
        }

        double[][] edgeScores = scoreEdgesLp(graph);

        PriorityQueue<EdgeWithScoreDouble> edgesWithScore = new PriorityQueue<>(Collections.reverseOrder());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    edgesWithScore.add(new EdgeWithScoreDouble(new Edge(i, j), edgeScores[i][j]));
                }
            }
        }

        Set<Set<Integer>> connectedComponents;

        while ((connectedComponents = graph.getConnectedComponents()).size() < 2) {
            EdgeWithScoreDouble edgeToRemove = edgesWithScore.poll();
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

        EdgeDeletionsWithCost edgeDeletionsWithCostOfSubGraph1 = getGreedyHeuristicLp(subGraph);
        if (edgeDeletionsWithCost.getCost() + edgeDeletionsWithCostOfSubGraph1.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        ArrayList<Integer> subGraphIndices2 = new ArrayList<>(connectedComponentsIterator.next());
        subGraph = graph.getSubGraph(subGraphIndices2);

        EdgeDeletionsWithCost edgeDeletionsWithCostOfSubGraph2 = getGreedyHeuristicLp(subGraph);
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

    private static double[][] getEdgesScoresOfSubGraph(double[][] edgeScores, List<Integer> subGraphIndices) {
        double[][] edgesScoresOfSubGraph = new double[subGraphIndices.size()][subGraphIndices.size()];
        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = i+1; j < subGraphIndices.size(); j++) {
                edgesScoresOfSubGraph[i][j] = edgeScores[subGraphIndices.get(i)][subGraphIndices.get(j)];
            }
        }
        return edgesScoresOfSubGraph;
    }

    public static boolean[][] getGreedyHeuristic2(Graph graph) {
        int iter = 40;

        boolean[][] resultEdgeExistsWithMinCost = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int minCost = getCost(graph, resultEdgeExistsWithMinCost);

        double[][] edgeScores = connectivityHeuristicOfEdges(graph, (int) Math.max(2 * Math.pow(graph.getNumberOfVertices(), 0.5), 5), iter);
        int maxScore = getMaxScore(edgeScores);

        for (int score = maxScore; score > 0; score--) {
            boolean[][] resultEdgeExists = getTransitiveClosureOfResultEdgeExists(getResultEdgeExistsWithMinScore(edgeScores, score));
            int cost = getCost(graph, resultEdgeExists);
            if (cost < minCost) {
                minCost = cost;
                resultEdgeExistsWithMinCost = resultEdgeExists;
            }
        }

        return resultEdgeExistsWithMinCost;
    }

    public static boolean[][] getGreedyHeuristic2Randomized2(Graph graph) {
        int iter = 40;

        boolean[][] resultEdgeExistsWithMinCost = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int minCost = getCost(graph, resultEdgeExistsWithMinCost);

        double[][] edgeScores = connectivityHeuristicOfEdges(graph, (int) Math.max(2 * Math.pow(graph.getNumberOfVertices(), 0.5), 5), iter);
        int maxScore = getMaxScore(edgeScores);

        for (int score = maxScore; score > 0; score--) {
            for (double p = 0.2; p <= 1; p += 0.2) {
                boolean[][] resultEdgeExists = getTransitiveClosureOfResultEdgeExists(getResultEdgeExistsWithMinScoreRandomized(edgeScores, score, p));
                int cost = getCost(graph, resultEdgeExists);
                if (cost < minCost) {
                    minCost = cost;
                    resultEdgeExistsWithMinCost = resultEdgeExists;
                }
            }
        }

        return resultEdgeExistsWithMinCost;
    }

    public static boolean[][] getGreedyHeuristic2Randomized(Graph graph) {
        int iter = 50;

        boolean[][] resultEdgeExistsWithMinCost = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int minCost = getCost(graph, resultEdgeExistsWithMinCost);

        double[][] edgeScores = connectivityHeuristicOfEdges(graph, 20, iter);
        int maxScore = getMaxScore(edgeScores);

        for (double p = 0.0; p < 1; p += 1.0 / 1000.0) {
            boolean[][] resultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = i + 1; j < graph.getNumberOfVertices(); j++) {
                    resultEdgeExists[i][j] = Math.random() < p * edgeScores[i][j] / maxScore;
                }
            }
            resultEdgeExists = getTransitiveClosureOfResultEdgeExists(resultEdgeExists);
            int cost = getCost(graph, resultEdgeExists);
            if (cost < minCost) {
                minCost = cost;
                resultEdgeExistsWithMinCost = resultEdgeExists;
            }
        }

        return resultEdgeExistsWithMinCost;
    }

    private static boolean[][] getResultEdgeExistsWithMinScore(double[][] edgeScores, int minScore) {
        boolean[][] resultEdgeExists = new boolean[edgeScores.length][edgeScores.length];
        for (int i = 0; i < edgeScores.length; i++) {
            for (int j = i+1; j < edgeScores.length; j++) {
                resultEdgeExists[i][j] = edgeScores[i][j] >= minScore;
            }
        }
        return resultEdgeExists;
    }

    private static boolean[][] getResultEdgeExistsWithMinScoreRandomized(double[][] edgeScores, int minScore, double p) {
        boolean[][] resultEdgeExists = new boolean[edgeScores.length][edgeScores.length];
        for (int i = 0; i < edgeScores.length; i++) {
            for (int j = i+1; j < edgeScores.length; j++) {
                if (Math.random() < p) {
                    resultEdgeExists[i][j] = edgeScores[i][j] >= minScore;
                }
            }
        }
        return resultEdgeExists;
    }

    private static int getMaxScore(double[][] scores) {
        int maxScore = Integer.MIN_VALUE;
        for (int i = 0; i < scores.length; i++) {
            for (int j = i+1; j < scores[i].length; j++) {
                maxScore = (int) Math.max(maxScore, scores[i][j]);
            }
        }
        return maxScore;
    }

    private static boolean[][] getTransitiveClosureOfResultEdgeExists(boolean[][] resultEdgeExists) {
        if (resultEdgeExists.length < 3) {
            return resultEdgeExists;
        }
        int[] vertexToConnectedComponentIndex = getVertexToConnectedComponentIndexOfResultEdgeExists(resultEdgeExists);
        for (int i = 0; i < resultEdgeExists.length; i++) {
            for (int j = i+1; j < resultEdgeExists.length; j++) {
                resultEdgeExists[i][j] =  vertexToConnectedComponentIndex[i] == vertexToConnectedComponentIndex[j];
            }
        }
        return resultEdgeExists;
    }

    private static Set<Set<Integer>> getConnectedComponentsOfResultEdgeExists(boolean[][] resultEdgeExists) {
        Set<Integer> visitedVertices = new HashSet<>();
        Set<Set<Integer>> connectedComponents = new HashSet<>();
        for (int i = 0; i < resultEdgeExists.length; i++) {
            if (!visitedVertices.contains(i)) {
                Set<Integer> connectedComponent = getConnectedComponentOfResultEdgeExists(i, resultEdgeExists);
                visitedVertices.addAll(connectedComponent);
                connectedComponents.add(connectedComponent);
            }
        }
        return connectedComponents;
    }

    public static Set<Integer> getConnectedComponentOfResultEdgeExists(int vertex, boolean[][] resultEdgeExists) {
        return getConnectedComponentOfResultEdgeExists(vertex, new HashSet<>(), resultEdgeExists);
    }

    private static Set<Integer> getConnectedComponentOfResultEdgeExists(int vertex, Set<Integer> connectedComponent, boolean[][] resultEdgeExists) {
        connectedComponent.add(vertex);
        for (int i = 0; i < resultEdgeExists.length; i++) {
            if (vertex != i && resultEdgeExists[vertex][i] && !connectedComponent.contains(i)) {
                connectedComponent.addAll(getConnectedComponentOfResultEdgeExists(i, connectedComponent, resultEdgeExists));
            }
        }
        return connectedComponent;
    }

    private static int[] getVertexToConnectedComponentIndexOfResultEdgeExists(boolean[][] resultEdgeExists) {
        return Graph.getVertexToConnectedComponentIndex(getConnectedComponentsOfResultEdgeExists(resultEdgeExists), resultEdgeExists.length);
    }

    private static int getCost(Graph graph, boolean[][] resultEdgeExists) {
        int cost = 0;
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j] != resultEdgeExists[i][j]) {
                    cost += Math.abs(graph.getEdgeWeights()[i][j]);
                }
            }
        }
        return cost;
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

    private static double[][] scoreEdgesLp(Graph graph) {
        int iter = 20;

        double[][] connectivityHeuristics = connectivityHeuristicOfEdges(graph, (int) Math.max(0.2 * Math.pow(graph.getNumberOfVertices(), 2.0/3.0), 5), iter);
        double[][] edgeScores = new double[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                edgeScores[i][j] = graph.getEdgeExists()[i][j] ? iter - connectivityHeuristics[i][j] : 0;
            }
        }

        return edgeScores;
    }

    private static double[][] connectivityHeuristicOfEdges(Graph graph, int n, int iter) {
        double[][] connectivityHeuristics = new double[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        List<Integer> indices = getIndexList(graph.getNumberOfVertices());

        for (int i = 0; i < iter; i++) {
            Collections.shuffle(indices);
            for (int j = 0; j < graph.getNumberOfVertices(); j += n) {
                addScoresLp(graph, indices.subList(j, Math.min(j + n, graph.getNumberOfVertices())), connectivityHeuristics);
            }
        }

        return connectivityHeuristics;
    }

    private static void addScoresLp(Graph graph, List<Integer> subGraphIndices, double[][] edgeScores) {
        Loader.loadNativeLibraries();
        MPSolver mpSolver = MPSolver.createSolver("GLOP");

        MPVariable[][] x = new MPVariable[subGraphIndices.size()][subGraphIndices.size()];

        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = i+1; j < subGraphIndices.size(); j++) {
                x[i][j] = mpSolver.makeNumVar(0.0, 1.0, String.format("%d %d", subGraphIndices.get(i), subGraphIndices.get(j)));
            }
        }

        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = 0; j < subGraphIndices.size(); j++) {
                for (int k = 0; k < subGraphIndices.size(); k++) {
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

        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = i + 1; j < subGraphIndices.size(); j++) {
                objective.setCoefficient(x[i][j], -graph.getEdgeWeights()[subGraphIndices.get(i)][subGraphIndices.get(j)]);
                constant += Math.max(graph.getEdgeWeights()[subGraphIndices.get(i)][subGraphIndices.get(j)], 0.0);
            }
        }

        objective.setOffset(constant);

        objective.setMinimization();

        MPSolver.ResultStatus resultStatus = mpSolver.solve();

        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = i+1; j < subGraphIndices.size(); j++) {
                Integer u = subGraphIndices.get(i);
                Integer v = subGraphIndices.get(j);
                double value = x[i][j].solutionValue();
                edgeScores[u][v] += value;
                edgeScores[v][u] += value;
            }
        }
    }

    private static List<Integer> getIndexList(int n) {
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            indexList.add(i);
        }
        return indexList;
    }


}
