package berlin.tu.algorithmengineering.common.model.heuristics;


import berlin.tu.algorithmengineering.common.model.Edge;

public class EdgeWithScoreDouble implements Comparable {

    private Edge edge;
    private double score;

    public EdgeWithScoreDouble(Edge edge, double score) {
        this.edge = edge;
        this.score = score;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Object o) {
        EdgeWithScoreDouble other = (EdgeWithScoreDouble) o;
        return (int) Math.signum(score - other.getScore());
    }
}
