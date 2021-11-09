package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private List<Vertex> vertices;

    public Graph(int numberOfVertices) {
        this.vertices = new ArrayList<>(numberOfVertices);
        for (int i = 0; i< numberOfVertices; i++){
            vertices.add(new Vertex(i));
        }
    }

    public int editEdge(Vertex i, Vertex j) {
        WeightedNeighbor ij = i.getNeighbors().stream()
                .filter(weightedNeighbor->weightedNeighbor.getVertex().equals(j)).findFirst().orElseThrow();
        ij.flipEdgeExistence();
        WeightedNeighbor ji = j.getNeighbors().stream()
                .filter(weightedNeighbor->weightedNeighbor.getVertex().equals(i)).findFirst().orElseThrow();
        ji.flipEdgeExistence();

        return -ij.getWeight();
    }

    public P3 findP3() {
        for (int i = 0; i < getNumberOfVertices(); i++) {
            Vertex u = vertices.get(i);
            for (WeightedNeighbor uv: u.getNeighbors()) {
                for (WeightedNeighbor uw: u.getNeighbors()) {
                    WeightedNeighbor vw = uv.getVertex().getNeighbors().stream()
                            .filter(weightedNeighbor->weightedNeighbor.getVertex().equals(uw.getVertex())).findFirst().orElseThrow();
                    if (! uv.getVertex().equals(uw.getVertex())
                            && uv.isEdgeExists() && vw.isEdgeExists() && !uw.isEdgeExists() ) {
                        return new P3(u, uv.getVertex(), uw.getVertex());
                    }
                }
            }
        }
        return null;
    }

    public int getNumberOfVertices() {
        return vertices.size();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }
}
