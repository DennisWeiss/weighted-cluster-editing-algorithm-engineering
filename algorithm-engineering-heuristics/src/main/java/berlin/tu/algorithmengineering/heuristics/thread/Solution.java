package berlin.tu.algorithmengineering.heuristics.thread;

public class Solution {

    private boolean[][] resultEdgeExists;
    private int cost;

    public Solution(boolean[][] resultEdgeExists, int cost) {
        this.resultEdgeExists = resultEdgeExists;
        this.cost = cost;
    }

    public boolean[][] getResultEdgeExists() {
        return resultEdgeExists;
    }

    public void setResultEdgeExists(boolean[][] resultEdgeExists) {
        this.resultEdgeExists = resultEdgeExists;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
