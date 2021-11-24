package berlin.tu.algorithmengineering.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vertex {

    private Integer id;
    private Map<Vertex, WeightedNeighbor> neighbors;
    private Vertex mergedFrom1;
    private Vertex mergedFrom2;


    public Vertex(int id) {
        this.id = id;
        this.neighbors = new HashMap<>();
    }

    public Vertex(Vertex mergedFrom1, Vertex mergedFrom2) {
        this.neighbors = new HashMap<>();
        this.mergedFrom1 = mergedFrom1;
        this.mergedFrom2 = mergedFrom2;
    }

    public Map<Vertex, WeightedNeighbor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Map<Vertex, WeightedNeighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vertex getMergedFrom1() {
        return mergedFrom1;
    }

    public void setMergedFrom1(Vertex mergedFrom1) {
        this.mergedFrom1 = mergedFrom1;
    }

    public Vertex getMergedFrom2() {
        return mergedFrom2;
    }

    public void setMergedFrom2(Vertex mergedFrom2) {
        this.mergedFrom2 = mergedFrom2;
    }

    public void addNeighbor(Vertex vertex, int weight) {
        this.neighbors.put(vertex, new WeightedNeighbor(vertex, weight));
    }

    public boolean equals(Vertex vertex){
        if (this.id != null && vertex.getId() != null) {
            return this.id.equals(vertex.getId());
        }
        if ((this.id == null && vertex.getId() != null) || (this.id != null && vertex.getId() == null)) {
            return false;
        }
        return mergedFrom1.equals(vertex.getMergedFrom1()) && mergedFrom2.equals(vertex.getMergedFrom2());
    }

    @Override
    public String toString() {
        return "(" + (id != null ? id + 1 : (mergedFrom1.toString() + '-' + mergedFrom2.toString())) + ')';
    }
}
