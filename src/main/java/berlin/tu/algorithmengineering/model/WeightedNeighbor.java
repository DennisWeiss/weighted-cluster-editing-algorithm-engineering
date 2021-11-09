package berlin.tu.algorithmengineering.model;

public class WeightedNeighbor {
    private Vertex neighbor;
    private int weight;
    private boolean edgeExists;

    public WeightedNeighbor(Vertex neighbor, int weight) {
        this.neighbor = neighbor;
        this.weight = weight;
        this.edgeExists = weight > 0;
    }

    public Vertex getVertex() {
        return neighbor;
    }

    public int getWeight() {
        return weight;
    }
    public void flipEdgeExistence(){
        weight *= -1;
        edgeExists = ! edgeExists;
    }
    public boolean isEdgeExists() {
        return edgeExists;
    }
}
