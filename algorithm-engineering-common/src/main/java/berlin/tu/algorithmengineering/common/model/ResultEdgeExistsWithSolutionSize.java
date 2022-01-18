package berlin.tu.algorithmengineering.common.model;

public class ResultEdgeExistsWithSolutionSize {

    private boolean[][] resultEdgeExists;
    private int solutionSize;

    public ResultEdgeExistsWithSolutionSize(boolean[][] resultEdgeExists, int solutionSize) {
        this.resultEdgeExists = resultEdgeExists;
        this.solutionSize = solutionSize;
    }

    public boolean[][] getResultEdgeExists() {
        return resultEdgeExists;
    }

    public void setResultEdgeExists(boolean[][] resultEdgeExists) {
        this.resultEdgeExists = resultEdgeExists;
    }

    public int getSolutionSize() {
        return solutionSize;
    }

    public void setSolutionSize(int solutionSize) {
        this.solutionSize = solutionSize;
    }
}
