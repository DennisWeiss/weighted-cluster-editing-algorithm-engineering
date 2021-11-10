package berlin.tu.algorithmengineering.util;

import berlin.tu.algorithmengineering.model.Vertex;

public class MergeVerticesResultBuilder {
    private int k;
    private Vertex mergedVertex;
    private int mergedEdgeWeight;

    public MergeVerticesResultBuilder setK(int k) {
        this.k = k;
        return this;
    }

    public MergeVerticesResultBuilder setMergedVertex(Vertex mergedVertex) {
        this.mergedVertex = mergedVertex;
        return this;
    }

    public MergeVerticesResultBuilder setMergedEdgeWeight(int mergedEdgeWeight) {
        this.mergedEdgeWeight = mergedEdgeWeight;
        return this;
    }

    public MergeVerticesResult createMergeVerticesResult() {
        return new MergeVerticesResult(k, mergedVertex, mergedEdgeWeight);
    }
}