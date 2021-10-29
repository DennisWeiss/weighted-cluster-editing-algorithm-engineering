package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.P3;

public class Graph {

    private int numberOfVertices;
    private int[][] edges;

    public Graph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.edges = new int[numberOfVertices][numberOfVertices];
    }

    public Graph copy() {
        Graph copy = new Graph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                copy.getEdges()[i][j] = edges[i][j];
            }
        }
        return copy;
    }

    public int editEdge(int i, int j) {
        edges[Math.min(i, j)][Math.max(i, j)] *= -1;
        return -edges[Math.min(i, j)][Math.max(i, j)];
    }

    public P3 findP3() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                for (int k = j+1; k < numberOfVertices; k++) {
                    if (edges[i][j] > 0 && edges[j][k] > 0 && edges[i][k] <= 0) {
                        return new P3(i, j, k);
                    }
                    if (edges[i][j] > 0 && edges[i][k] > 0 && edges[j][k] <= 0) {
                        return new P3(j, i, k);
                    }
                    if (edges[j][k] > 0 && edges[i][k] > 0 && edges[i][j] <= 0) {
                        return new P3(i, k, j);
                    }
                }
            }
        }
        return null;
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public void setNumberOfVertices(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }

    public int[][] getEdges() {
        return edges;
    }

    public void setEdges(int[][] edges) {
        this.edges = edges;
    }
}
