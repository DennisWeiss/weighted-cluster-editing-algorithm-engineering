package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.common.model.Edge;
import berlin.tu.algorithmengineering.common.model.P3;
import berlin.tu.algorithmengineering.common.model.heuristics.EdgeDeletionsWithCost;
import berlin.tu.algorithmengineering.common.model.heuristics.EdgeWithScoreDouble;
import berlin.tu.algorithmengineering.common.model.heuristics.EdgeWithScoreInt;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import org.ejml.data.Eigenpair;
import org.ejml.simple.SimpleMatrix;

import java.io.File;
import java.util.*;


public class Heuristics {

    static {
        Loader.loadNativeLibraries();
//        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
//        if (os.equals("mac os x")) { // only for MAC local
//            System.load("/Applications/or-tools_MacOsX-12.0.1_v9.2.9972/ortools-darwin-x86-64/libjniortools.dylib");
//        } else {
//            File file = new File("lib/or-tools_Ubuntu-18.04-64bit_v9.2.9972/extracted-jar/ortools-linux-x86-64/libjniortools.so");
//            String absolutePath = file.getAbsolutePath();
//            System.load(absolutePath);
//        }
    }

    public static final int FORBIDDEN_VALUE = (int) -Math.pow(2, 16);

    // Higher precision means earlier termination
    // and higher error
    static final Double PRECISION = 0.0;

    public static boolean[][] getGreedyHeuristicNeighborhood(Graph graph) {
        boolean[] vertexAdded = new boolean[graph.getNumberOfVertices()];

        int[] vertices = Utils.getIntArrayInRange(graph.getNumberOfVertices());
        Utils.shuffleArray(vertices);

        boolean[][] resultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            int vertex = vertices[i];
            if (!vertexAdded[vertex]) {
                List<Integer> closedNeighborhood = graph.getClosedNeighborhoodOfVertexWithoutVertices(vertex, vertexAdded);
                for (int j = 0; j < closedNeighborhood.size(); j++) {
                    int x = closedNeighborhood.get(j);
                    vertexAdded[x] = true;
                    for (int k = j+1; k < closedNeighborhood.size(); k++) {
                        int y = closedNeighborhood.get(k);
                        resultEdgeExists[x][y] = true;
                        resultEdgeExists[y][x] = true;
                    }
                }
            }
        }

        return resultEdgeExists;
    }

    public static EdgeDeletionsWithCost getGreedyHeuristic1(Graph graph) {
        EdgeDeletionsWithCost edgeDeletionsWithCost = new EdgeDeletionsWithCost(new HashSet<>(), 0);
        int transitiveClosureCost = graph.getTransitiveClosureCost();
        if (transitiveClosureCost == 0 || HeuristicMain.forceFinish.get()) {
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

    public static boolean[][] getSpectralClusteringHeuristic(Graph graph) {
        Graph resultGraph = graph.copy();
        Graph bestGraph = resultGraph.copy();

        SpectralClustering spectralClustering = new SpectralClustering(resultGraph.copy().getEdgeWeights());
        List<Eigenpair> listEigenpairs = spectralClustering.getEigenDecomposition();

        int backUpSolutionCost = Integer.MAX_VALUE;

        //int k = spectralClustering.bestClusterSizeK(listEigenpairs);

        for(int k = 1; k < graph.getNumberOfVertices()/2; k++) {
            Graph oldGraph = resultGraph.copy();

            SimpleMatrix myH = spectralClustering.buildSimpleMatrix(listEigenpairs, k);
            DataSet data = new DataSet(myH);

            for(int l = 0; l < 10; l++){
                // Call k means
                kmeans(data, k);

                // build clique cased on clustering
                HashMap<Integer, ArrayList<Integer>> myCluster = spectralClustering.getCluster(data);
                for (Map.Entry<Integer, ArrayList<Integer>> entry : myCluster.entrySet()) {
                    ArrayList<Integer> value = entry.getValue();
                    for (int i = 0; i < value.size(); i++) {
                        applyChange(resultGraph.getEdgeExists(), value.get(i), value.get(0));
                    }
                }

                int currentCost = Utils.getCostToChange(oldGraph, resultGraph.getEdgeExists());
                if(currentCost < backUpSolutionCost){
                    bestGraph = resultGraph.copy();
                    backUpSolutionCost = currentCost;
                }
                resultGraph = oldGraph.copy();
            }
        }
        return bestGraph.getEdgeExists();
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

    public static boolean[][] getGreedyHeuristic2Randomized2(Graph graph, int k, int l, double alpha) {
        boolean[][] resultEdgeExistsWithMinCost = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int minCost = getCost(graph, resultEdgeExistsWithMinCost);

        double[][] edgeScores = connectivityHeuristicOfEdges(graph, (int) Math.max(alpha * Math.pow(graph.getNumberOfVertices(), 0.5), 5), k);
        int maxScore = getMaxScore(edgeScores);

        double pFrom = 0.6;
        double pTo = 1.0;

        loopScores:
        for (int score = maxScore; score > 0; score--) {
            for (double p = pFrom; p <= pTo; p += (pTo - pFrom) / l) {
                if (HeuristicMain.forceFinish.get()) {
                    break loopScores;
                }
                boolean[][] resultEdgeExists = getTransitiveClosureOfResultEdgeExists(getResultEdgeExistsWithMinScoreRandomized(edgeScores, score, p));
                int cost = getCost(graph, resultEdgeExists);
                if (cost < minCost) {
                    minCost = cost;
                    resultEdgeExistsWithMinCost = resultEdgeExists;
                }
                if (getConnectedComponentOfResultEdgeExists(0, resultEdgeExists).size() == graph.getNumberOfVertices()) {
                    break loopScores;
                }
            }
        }

        return resultEdgeExistsWithMinCost;
    }
    public static boolean[][] getGreedyHeuristic4random(Graph graph) {

        //start with no edges
        boolean[][] newEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

        // sort edges, according to signed weight
        PriorityQueue<EdgeWithScoreInt> edgesWithScore = new PriorityQueue<>(Collections.reverseOrder());
        for (int i=0; i < graph.getNumberOfVertices(); i++) {
            for (int j=i+1; j < graph.getNumberOfVertices(); j++) {
                edgesWithScore.add(new EdgeWithScoreInt(new Edge(i, j), graph.getEdgeWeights()[i][j]));
            }
        }

        while (edgesWithScore.size() > 0) {
            Edge edgeToAdd = edgesWithScore.poll().getEdge();
            int u = edgeToAdd.getA();
            int v = edgeToAdd.getB();
//            System.out.println("#edge weight="+graph.getEdgeWeights()[u][v]);

            // calc how good or bad it would be to merge the cliques of u and v
            int costDifference = 0;
            for (int w_u=0; w_u < graph.getNumberOfVertices(); w_u++) {
                if (w_u == u || newEdgeExists[u][w_u]) {
                    for (int w_v=0; w_v < graph.getNumberOfVertices(); w_v++) {
                        if (w_v == v || newEdgeExists[v][w_v]) {
                            costDifference -= graph.getEdgeWeights()[w_u][w_v];
                        }
                    }
                }
            }

            if ( (costDifference == 0 && Math.random() < .5)
                    || (costDifference > 0 && Math.random()*(costDifference+2) < 1)
                    || (costDifference < 0 && Math.random()*(-costDifference+2) > 1) ) {
                //merge cliques of u and v
                for (int w_u=0; w_u < graph.getNumberOfVertices(); w_u++) {
                    if (w_u == u || newEdgeExists[u][w_u]) {
                        for (int w_v=0; w_v < graph.getNumberOfVertices(); w_v++) {
                            if (w_v == v || newEdgeExists[v][w_v]) {
                                newEdgeExists[w_v][w_u] = true;
                                newEdgeExists[w_u][w_v] = true;
                            }
                        }
                    }
                }
            }
        }

        return newEdgeExists;
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
                    boolean edgeExists = Math.random() < p * edgeScores[i][j] / maxScore;
                    resultEdgeExists[i][j] = edgeExists;
                    resultEdgeExists[j][i] = edgeExists;
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
                    boolean edgeExists = edgeScores[i][j] >= minScore;
                    resultEdgeExists[i][j] = edgeExists;
                    resultEdgeExists[j][i] = edgeExists;
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
                boolean edgeExists = vertexToConnectedComponentIndex[i] == vertexToConnectedComponentIndex[j];
                resultEdgeExists[i][j] = edgeExists;
                resultEdgeExists[j][i] = edgeExists;
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

                    int edgeFlipCost = -graph.getEdgeWeights()[i][j];// is negative
                    graph.getEdgeWeights()[i][j] = -FORBIDDEN_VALUE;
                    graph.getEdgeWeights()[j][i] = -FORBIDDEN_VALUE;
                    graph.getAbsoluteNeighborhoodWeights()[i] += -FORBIDDEN_VALUE - edgeFlipCost;
                    graph.getAbsoluteNeighborhoodWeights()[j] += -FORBIDDEN_VALUE - edgeFlipCost;

                    int d = getD(graph, i, j);

                    graph.flipEdge(i, j);

                    int score = d - getD(graph, i, j) + edgeFlipCost;
                    edgeScores[i][j] = score;
                    edgeScores[j][i] = score;

                    graph.getEdgeWeights()[i][j] = edgeFlipCost;
                    graph.getEdgeWeights()[j][i] = edgeFlipCost;
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
            //TODO better:
            /*int w = p3.getW();
            if (w == u || w == v) w = p3.getV();
            if (w == u || w == v) w = p3.getU();
            lowerBound += Math.min(Math.abs(graph.getEdgeWeights()[u][w]),Math.abs(graph.getEdgeWeights()[v][w]));
             */
        }
        return lowerBound;
    }

    private static double[][] scoreEdgesLp(Graph graph) {
        int iter = 20;

        double[][] connectivityHeuristics = connectivityHeuristicOfEdges(graph, (int) Math.max(0.5 * Math.pow(graph.getNumberOfVertices(), 0.5), 5), iter);
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
                if (HeuristicMain.forceFinish.get()) {
                    return connectivityHeuristics;
                }
                addScoresLp(graph, indices.subList(j, Math.min(j + n, graph.getNumberOfVertices())), connectivityHeuristics);
            }
        }

        return connectivityHeuristics;
    }

    private static void addScoresLp(Graph graph, List<Integer> subGraphIndices, double[][] edgeScores) {
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

    /* K-Means++ implementation, initializes K centroids from data */
    static LinkedList<HashMap<String, Double>> kmeanspp(DataSet data, int K) {
        LinkedList<HashMap<String,Double>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet()); // Abschnittener H Vektor

        for(int i=1; i<K; i++){
            centroids.add(data.calculateWeighedCentroid());
        }

        return centroids;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
     * to records in the dataset */
    static void kmeans(DataSet data, int K){
        // Select K initial centroids
        LinkedList<HashMap<String,Double>> centroids = kmeanspp(data, K);

        // Initialize Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;

        while (true) {

            // Assign observations to centroids
            var records = data.getRecords();

            // For each record
            for(var record : records){
                Double minDist = Double.MAX_VALUE;
                // Find the centroid at a minimum distance from it and add the record to its cluster
                for(int i = 0; i < centroids.size(); i++){
                    Double dist = DataSet.euclideanDistance(centroids.get(i), record.getRecord());
                    if(dist < minDist){
                        minDist = dist;
                        record.setClusterNo(i);
                    }
                }
            }

            // Recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // Exit condition, SSE changed less than PRECISION parameter
            Double newSSE = data.calculateTotalSSE(centroids);
            if(SSE-newSSE <= PRECISION){
                break;
            }
            SSE = newSSE;
        }
    }

    private static void applyChange(boolean[][] edgeExists, int vertex, int moveToVertex) {
        for (int i = 0; i < edgeExists.length; i++) {
            edgeExists[vertex][i] = false;
            edgeExists[i][vertex] = false;
        }
        if (vertex == moveToVertex) {
            return;
        }
        for (int i = 0; i < edgeExists.length; i++) {
            if (edgeExists[moveToVertex][i]) {
                edgeExists[vertex][i] = true;
                edgeExists[i][vertex] = true;
            }
        }
        edgeExists[vertex][moveToVertex] = true;
        edgeExists[moveToVertex][vertex] = true;
    }

}
