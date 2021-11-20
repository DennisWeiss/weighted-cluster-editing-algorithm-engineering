package berlin.tu.algorithmengineering.model;

import berlin.tu.algorithmengineering.Graph;

public class P3 {
    private Vertex u;
    private Vertex v;
    private Vertex w;

    public P3(Vertex u, Vertex v, Vertex w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int getTotalAbsoluteWeight() {
        return Graph.getWeightedNeighbor(u, v).getWeight() + Graph.getWeightedNeighbor(v, w).getWeight()
                - Graph.getWeightedNeighbor(u, w).getWeight();
    }

    public Vertex getU() {
        return u;
    }

    public void setU(Vertex u) {
        this.u = u;
    }

    public Vertex getV() {
        return v;
    }

    public void setV(Vertex v) {
        this.v = v;
    }

    public Vertex getW() {
        return w;
    }

    public void setW(Vertex w) {
        this.w = w;
    }
}
