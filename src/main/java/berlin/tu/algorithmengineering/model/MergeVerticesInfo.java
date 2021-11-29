package berlin.tu.algorithmengineering.model;

public class MergeVerticesInfo {

    private int firstVertex;
    private int secondVertex;
    private int[] edgeWeightsOfFirstVertex;
    private int[] edgeWeightsOfSecondVertex;
    private boolean[] edgeExistsOfFirstVertex;
    private boolean[] edgeExistsOfSecondVertex;
    private boolean[] forbiddenOfFirstVertex;
    private boolean[] forbiddenOfSecondVertex;
    private int k;

    public MergeVerticesInfo(int numberOfVertices, int firstVertex, int secondVertex, int k) {
        this.edgeWeightsOfFirstVertex = new int[numberOfVertices];
        this.edgeWeightsOfSecondVertex = new int[numberOfVertices];
        this.edgeExistsOfFirstVertex = new boolean[numberOfVertices];
        this.edgeExistsOfSecondVertex = new boolean[numberOfVertices];
        this.forbiddenOfFirstVertex = new boolean[numberOfVertices];
        this.forbiddenOfSecondVertex = new boolean[numberOfVertices];
        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.k = k;
    }

    public void reduceK(int diff) {
        k -= diff;
    }

    public int getFirstVertex() {
        return firstVertex;
    }

    public void setFirstVertex(int firstVertex) {
        this.firstVertex = firstVertex;
    }

    public int getSecondVertex() {
        return secondVertex;
    }

    public void setSecondVertex(int secondVertex) {
        this.secondVertex = secondVertex;
    }

    public int[] getEdgeWeightsOfFirstVertex() {
        return edgeWeightsOfFirstVertex;
    }

    public void setEdgeWeightsOfFirstVertex(int[] edgeWeightsOfFirstVertex) {
        this.edgeWeightsOfFirstVertex = edgeWeightsOfFirstVertex;
    }

    public int[] getEdgeWeightsOfSecondVertex() {
        return edgeWeightsOfSecondVertex;
    }

    public void setEdgeWeightsOfSecondVertex(int[] edgeWeightsOfSecondVertex) {
        this.edgeWeightsOfSecondVertex = edgeWeightsOfSecondVertex;
    }

    public boolean[] getEdgeExistsOfFirstVertex() {
        return edgeExistsOfFirstVertex;
    }

    public void setEdgeExistsOfFirstVertex(boolean[] edgeExistsOfFirstVertex) {
        this.edgeExistsOfFirstVertex = edgeExistsOfFirstVertex;
    }

    public boolean[] getEdgeExistsOfSecondVertex() {
        return edgeExistsOfSecondVertex;
    }

    public void setEdgeExistsOfSecondVertex(boolean[] edgeExistsOfSecondVertex) {
        this.edgeExistsOfSecondVertex = edgeExistsOfSecondVertex;
    }

    public boolean[] getForbiddenOfFirstVertex() {
        return forbiddenOfFirstVertex;
    }

    public void setForbiddenOfFirstVertex(boolean[] forbiddenOfFirstVertex) {
        this.forbiddenOfFirstVertex = forbiddenOfFirstVertex;
    }

    public boolean[] getForbiddenOfSecondVertex() {
        return forbiddenOfSecondVertex;
    }

    public void setForbiddenOfSecondVertex(boolean[] forbiddenOfSecondVertex) {
        this.forbiddenOfSecondVertex = forbiddenOfSecondVertex;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
