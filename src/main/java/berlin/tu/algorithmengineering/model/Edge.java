package berlin.tu.algorithmengineering.model;

import java.util.Objects;

public class Edge {
    private Vertex a;
    private Vertex b;

    public Edge(Vertex a, Vertex b) {
        this.a = a;
        this.b = b;
    }

    public Vertex getA() {
        return a;
    }

    public void setA(Vertex a) {
        this.a = a;
    }

    public Vertex getB() {
        return b;
    }

    public void setB(Vertex b) {
        this.b = b;
    }


    public boolean equals(Edge e) {
        return (a.equals(e.getA()) && b.equals(e.getB())) || (a.equals(e.getB()) && b.equals(e.getA()));
    }

    @Override
    public String toString() {
        return "{" + a.toString() + "," + b.toString() + "}";
    }
}
