package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Graph {

    private List<Vertex> vertices;

    public Graph(int numberOfVertices) {
        this.vertices = new ArrayList<>(numberOfVertices);
        for (int i = 0; i< numberOfVertices; i++){
            vertices.add(new Vertex(i));
        }
    }

    public int editEdge(Vertex i, Vertex j) {
        WeightedNeighbor ij = getWeightedNeighbor(i, j);
        ij.flipEdgeExistence();
        WeightedNeighbor ji = getWeightedNeighbor(j, i);
        ji.flipEdgeExistence();

        return -ij.getWeight();
    }

    public P3 findP3() {
        for (int i = 0; i < getNumberOfVertices(); i++) {
            Vertex u = vertices.get(i);
            for (WeightedNeighbor uv: u.getNeighbors().values()) {
                for (WeightedNeighbor uw: u.getNeighbors().values()) {
                    if (!uv.getVertex().equals(uw.getVertex())) {
                        WeightedNeighbor vw = getWeightedNeighbor(uv.getVertex(), uw.getVertex());
                        if (uv.isEdgeExists() && vw.isEdgeExists() && !uw.isEdgeExists() ) {
                            return new P3(u, uv.getVertex(), uw.getVertex());
                        }
                    }
                }
            }
        }
        return null;
    }

    public List<P3> findAllP3() {
        List<P3> p3List = new ArrayList<>();
        for (int i = 0; i < getNumberOfVertices(); i++) {
            Vertex u = vertices.get(i);
            for (WeightedNeighbor uv: u.getNeighbors().values()) {
                for (WeightedNeighbor uw: u.getNeighbors().values()) {
                    if (!uv.getVertex().equals(uw.getVertex())) {
                        WeightedNeighbor vw = getWeightedNeighbor(uv.getVertex(), uw.getVertex());
                        if (uv.isEdgeExists() && vw.isEdgeExists() && !uw.isEdgeExists() ) {
                            p3List.add(new P3(u, uv.getVertex(), uw.getVertex()));
                        }
                    }
                }
            }
        }
        return p3List;
    }

    public static WeightedNeighbor getWeightedNeighbor(Vertex w, Vertex v) {
        return w.getNeighbors().get(v);
    }

    public int getNumberOfVertices() {
        return vertices.size();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }
}
