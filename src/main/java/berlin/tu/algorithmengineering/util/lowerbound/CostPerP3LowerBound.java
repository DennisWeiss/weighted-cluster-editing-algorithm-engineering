package berlin.tu.algorithmengineering.util.lowerbound;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.List;

public class CostPerP3LowerBound implements WeightedClusteringLowerBound {

    @Override
    public double getLowerBound(Graph graph) {
        List<P3> p3List = graph.findAllP3();
        return p3List.size() * getMinimumCostPerP3(graph, p3List);
    }

    private double getMinimumCostPerP3(Graph graph, List<P3> p3List) {
        double minCostPerP3 = Double.MAX_VALUE;
        for (Vertex v : graph.getVertices()) {
            for (WeightedNeighbor vw : v.getNeighbors().values()) {
                if (vw.isEdgeExists()) {
                    int numberOfP3sOfEdge = getNumberOfP3sOfEdge(p3List, v, vw.getVertex());
                    if (numberOfP3sOfEdge > 0) {
                        double costPerP3 =  (double) vw.getWeight() / numberOfP3sOfEdge;
                        minCostPerP3 = Math.min(minCostPerP3, costPerP3);
                    }
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
