package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.Main;
import berlin.tu.algorithmengineering.model.Edge;

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
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    edgesWithScore.add(new EdgeWithScore(new Edge(i, j), edgeScores[i][j]));
                }
            }
        }

        Set<Set<Integer>> connectedComponents;

        while ((connectedComponents = graph.getConnectedComponents()).size() < 2) {
            EdgeWithScore edgeToRemove = edgesWithScore.poll();
            edgeDeletionsWithCost.getEdgeDeletions().add(edgeToRemove.getEdge());
            graph.flipEdge(edgeToRemove.getEdge().getA(), edgeToRemove.getEdge().getB());
            edgeDeletionsWithCost.addCost(-graph.getEdgeWeights()[edgeToRemove.getEdge().getA()][edgeToRemove.getEdge().getB()]);
        }

        int[] vertexToConnectedComponentIndex = Graph.getVertexToConnectedComponentIndex(connectedComponents, graph.getNumberOfVertices());
        for (Edge edge : new ArrayList<>(edgeDeletionsWithCost.getEdgeDeletions())) {
            if (vertexToConnectedComponentIndex[edge.getA()] == vertexToConnectedComponentIndex[edge.getB()]) {
                edgeDeletionsWithCost.getEdgeDeletions().remove(edge);
                graph.flipEdge(edge.getA(), edge.getB());
                edgeDeletionsWithCost.addCost(graph.getEdgeWeights()[edge.getA()][edge.getB()]);
            }
        }

        if (edgeDeletionsWithCost.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        Iterator<Set<Integer>> connectedComponentsIterator = connectedComponents.iterator();
        ArrayList<Integer> subGraphIndices1 = new ArrayList<>(connectedComponentsIterator.next());
//        int[] indexInSubGraph1 = getIndexInSubGraph(subGraphIndices, graph.getNumberOfVertices());
        Graph subGraph = graph.getSubGraph(subGraphIndices1);

        EdgeDeletionsWithCost edgeDeletionsWithCostOfSubGraph1 = getGreedyHeuristic1(subGraph);
        if (edgeDeletionsWithCost.getCost() + edgeDeletionsWithCostOfSubGraph1.getCost() >= transitiveClosureCost) {
            return new EdgeDeletionsWithCost(new HashSet<>(), transitiveClosureCost);
        }

        ArrayList<Integer> subGraphIndices2 = new ArrayList<>(connectedComponentsIterator.next());
//        int[] indexInSubGraph2 = getIndexInSubGraph(subGraphIndices, graph.getNumberOfVertices());
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

//    private static int[] getIndexInSubGraph(List<Integer> subGraphIndices, int numberOfVerticesInParentGraph) {
//        int[] indexInSubGraph = new int[numberOfVerticesInParentGraph];
//        for (int i = 0; i < subGraphIndices.size(); i++) {
//            indexInSubGraph[subGraphIndices.get(i)] = i;
//        }
//        return indexInSubGraph;
//    }

    private static int[][] scoreEdges(Graph graph) {
        int[][] edgeScores = new int[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        int lowerBound = graph.getLowerBound2(graph.findAllP3());
        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeExists()[i][j]) {
                    graph.flipEdge(i, j);
                    int edgeFlipCost = graph.getEdgeWeights()[i][j];
                    graph.getEdgeWeights()[i][j] = Main.FORBIDDEN_VALUE;
                    graph.getEdgeWeights()[j][j] = Main.FORBIDDEN_VALUE;
                    graph.getAbsoluteNeighborhoodWeights()[i] += -Main.FORBIDDEN_VALUE + edgeFlipCost;
                    graph.getAbsoluteNeighborhoodWeights()[j] += -Main.FORBIDDEN_VALUE + edgeFlipCost;

                    int score = lowerBound - graph.getLowerBound2(graph.findAllP3()) + edgeFlipCost;
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



}
