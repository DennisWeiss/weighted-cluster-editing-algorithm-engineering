package berlin.tu.algorithmengineering.model;

import java.util.HashSet;
import java.util.Set;

public class Vertex {

    private int id;
    private Set<WeightedNeighbor> neighbors;
    private Set<Vertex> mergedFrom;//always 2


    public Vertex(int id) {
        this.id = id;
        this.neighbors = new HashSet<>();
        this.mergedFrom = null;
    }
    public Vertex(int id, Set<Vertex> mergedFrom) {
        this.id = id;
        this.neighbors = new HashSet<>();
        this.mergedFrom = mergedFrom;
    }
    public Set<WeightedNeighbor> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Set<WeightedNeighbor> neighbors) {
        this.neighbors = neighbors;
    }

    public Set<Vertex> getMergedFrom() {
        return mergedFrom;
    }

    public void setMergedFrom(Set<Vertex> mergedFrom) {
        this.mergedFrom = mergedFrom;
    }

    public void addNeighbor(Vertex vertex,int weight) {
        this.neighbors.add(new WeightedNeighbor(vertex,weight));
    }

    public boolean equals(Vertex vertex){
        return this.id == vertex.id;
    }

    public int getId() {
        return id;
    }
}
