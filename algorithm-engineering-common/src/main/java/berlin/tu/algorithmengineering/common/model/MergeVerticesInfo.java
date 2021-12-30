package berlin.tu.algorithmengineering.common.model;

public class MergeVerticesInfo {

    private int firstVertex;
    private int secondVertex;
    private int[] edgeWeightsOfFirstVertex;
    private int[] edgeWeightsOfSecondVertex;
    private boolean[] edgeExistsOfFirstVertex;
    private boolean[] edgeExistsOfSecondVertex;
    private int cost = 0;

    public MergeVerticesInfo(int numberOfVertices, int firstVertex, int secondVertex) {
        this.edgeWeightsOfFirstVertex = new int[numberOfVertices];
        this.edgeWeightsOfSecondVertex = new int[numberOfVertices];
        this.edgeExistsOfFirstVertex = new boolean[numberOfVertices];
        this.edgeExistsOfSecondVertex = new boolean[numberOfVertices];
        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
    }

    public static int getTotalCost(MergeVerticesInfo[] mergeVerticesInfos) {
        int totalCost = 0;
        for (int i = 0; i < mergeVerticesInfos.length; i++) {
            totalCost += mergeVerticesInfos[i].getCost();
        }
        return totalCost;
    }

    public void increaseCost(int diff) {
        cost += diff;
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

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
