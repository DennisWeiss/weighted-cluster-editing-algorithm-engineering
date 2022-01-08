package berlin.tu.algorithmengineering.heuristics.thread;

public class Solution {

    private boolean[][] resultEdgeExists;
    private int cost;

    public Solution(boolean[][] resultEdgeExists, int cost) {
        this.resultEdgeExists = resultEdgeExists;
        this.cost = cost;
    }

    public synchronized boolean[][] getResultEdgeExists() {
        return resultEdgeExists;
    }

    public synchronized void setResultEdgeExists(boolean[][] resultEdgeExists) {
        this.resultEdgeExists = resultEdgeExists;
    }

    public synchronized int getCost() {
        return cost;
    }

    public synchronized void setCost(int cost) {
        this.cost = cost;
    }
}
