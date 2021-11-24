package berlin.tu.algorithmengineering.util.lowerbound;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinCostEdgeDisjointP3LowerBound {

    public double getLowerBound(List<P3> list) {
        int lowerBound = 0;
        for (P3 p3 : getEdgeDisjointP3List(list)) {
            Vertex u = p3.getU();
            Vertex v = p3.getV();
            Vertex w = p3.getW();
            lowerBound += Math.min(
                    Math.min(Graph.getWeightedNeighbor(u, v).getWeight(), Graph.getWeightedNeighbor(v, w).getWeight()),
                    -Graph.getWeightedNeighbor(u, w).getWeight()
            );
        }
        return lowerBound;
    }

    private List<P3> getEdgeDisjointP3List(List<P3> list) {
        list.sort((a, b) -> b.getMinAbsoluteWeight() - a.getMinAbsoluteWeight());
        List<P3> edgeDisjointP3List = new ArrayList<>();
        Set<Vertex> verticesInEdgeDisjointP3List = new HashSet<>();
        for (P3 p3 : list) {
            if (getNumberOfVerticesInSet(p3, verticesInEdgeDisjointP3List) < 2) {
                edgeDisjointP3List.add(p3);
                verticesInEdgeDisjointP3List.add(p3.getU());
                verticesInEdgeDisjointP3List.add(p3.getV());
                verticesInEdgeDisjointP3List.add(p3.getW());
            }
        }
        return edgeDisjointP3List;
    }

    private int getNumberOfVerticesInSet(P3 p3, Set<Vertex> set) {
        int n = 0;
        if (set.contains(p3.getU())) {
            n++;
        }
        if (set.contains(p3.getV())) {
            n++;
        }
        if (set.contains(p3.getW())) {
            n++;
        }
        return n;
    }
}
