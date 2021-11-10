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

    public void flipEdgeExistence(){
        weight *= -1;
        edgeExists = ! edgeExists;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public void setVertex(Vertex vertex) {
        this.vertex = vertex;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
        this.edgeExists = weight > 0;
    }

    public boolean isEdgeExists() {
        return edgeExists;
    }

    public void setEdgeExists(boolean edgeExists) {
        this.edgeExists = edgeExists;
    }
}
