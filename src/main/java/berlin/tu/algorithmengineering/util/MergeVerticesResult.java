package berlin.tu.algorithmengineering.util;

import berlin.tu.algorithmengineering.model.Vertex;

public class MergeVerticesResult {
    private int k;
    private Vertex mergedVertex;
    private int mergedEdgeWeight;

    public MergeVerticesResult(int k, Vertex mergedVertex, int mergedEdgeWeight) {
        this.k = k;
        this.mergedVertex = mergedVertex;
        this.mergedEdgeWeight = mergedEdgeWeight;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public Vertex getMergedVertex() {
        return mergedVertex;
    }

    public void setMergedVertex(Vertex mergedVertex) {
        this.mergedVertex = mergedVertex;
    }

    public int getMergedEdgeWeight() {
        return mergedEdgeWeight;
    }

    public void setMergedEdgeWeight(int mergedEdgeWeight) {
        this.mergedEdgeWeight = mergedEdgeWeight;
    }

    public boolean wasMerged() {
        return mergedVertex != null;
    }
}
