package berlin.tu.algorithmengineering.model;

public class WeightedNeighbor {
    private Vertex vertex;
    private int weight;
    private boolean edgeExists;

    public WeightedNeighbor(Vertex vertex, int weight) {
        this.vertex = vertex;
        this.weight = weight;
        this.edgeExists = weight > 0;
    }

    public Vertex getVertex() {
        return vertex;
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
