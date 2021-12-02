package berlin.tu.algorithmengineering.model;

public class OriginalWeightsInfo {

    private int vertex1;
    private int vertex2;
    private int originalWeight;

    public OriginalWeightsInfo(int vertex1, int vertex2, int originalWeight) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.originalWeight = originalWeight;
    }

    public int getVertex1() {
        return vertex1;
    }

    public void setVertex1(int vertex1) {
        this.vertex1 = vertex1;
    }

    public int getVertex2() {
        return vertex2;
    }

    public void setVertex2(int vertex2) {
        this.vertex2 = vertex2;
    }

    public int getOriginalWeight() {
        return originalWeight;
    }

    public void setOriginalWeight(int originalWeight) {
        this.originalWeight = originalWeight;
    }
}
