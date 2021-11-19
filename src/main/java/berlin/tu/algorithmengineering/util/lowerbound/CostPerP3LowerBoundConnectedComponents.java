package berlin.tu.algorithmengineering.util.lowerbound;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CostPerP3LowerBoundConnectedComponents implements WeightedClusteringLowerBound {

    @Override
    public double getLowerBound(Graph graph) {
        int lowerBound = 0;
        for (Graph connectedComponent : getConnectedComponents(graph)) {
            List<P3> p3List = connectedComponent.findAllP3();
            lowerBound += getMinimumCostPerP3(graph, p3List) * p3List.size();
        }
        return lowerBound;
    }

    private List<Graph> getConnectedComponents(Graph graph) {
        Set<Vertex> unvisited = new HashSet<>(graph.getVertices());
        List<Graph> connectedComponents = new ArrayList<>();
        while (!unvisited.isEmpty()) {
            connectedComponents.add(new Graph(new ArrayList(getGraphOfConnectedComponent(graph, unvisited.iterator().next(), unvisited))));
        }
        return connectedComponents;
    }

    private Set<Vertex> getGraphOfConnectedComponent(Graph graph, Vertex vertex, Set<Vertex> unvisited) {
        unvisited.remove(vertex);
        Set<Vertex> vertices = new HashSet<>();
        for (WeightedNeighbor vw : vertex.getNeighbors().values()) {
            if (vw.isEdgeExists()) {
                Vertex w = vw.getVertex();
                if (vw.isEdgeExists() && unvisited.contains(w)) {
                    vertices.addAll(getGraphOfConnectedComponent(graph, w, unvisited));
                }
            }
        }
        return vertices;
    }

    private double getMinimumCostPerP3(Graph graph, List<P3> p3List) {
        double minCostPerP3 = Double.MAX_VALUE;
        for (Vertex v : graph.getVertices()) {
            for (WeightedNeighbor vw : v.getNeighbors().values()) {
                int numberOfP3sOfEdge = getNumberOfP3sOfEdge(p3List, v, vw.getVertex());
                if (numberOfP3sOfEdge > 0) {
                    double costPerP3 =  (double) vw.getWeight() / numberOfP3sOfEdge;
                    minCostPerP3 = Math.min(minCostPerP3, costPerP3);
                }
            }
        }
        return minCostPerP3;
    }

    private int getNumberOfP3sOfEdge(List<P3> p3List, Vertex v, Vertex w) {
        int n = 0;
        for (P3 p3 : p3List) {
            if (
                    (p3.getU().equals(v) && p3.getV().equals(w))
                            || (p3.getU().equals(w) && p3.getV().equals(v))
                            || (p3.getU().equals(v) && p3.getW().equals(w))
                            || (p3.getU().equals(w) && p3.getW().equals(v))
                            || (p3.getV().equals(v) && p3.getW().equals(w))
                            || (p3.getV().equals(w) && p3.getW().equals(v))
            ) {
                n++;
            }
        }
        return n;
    }

}
