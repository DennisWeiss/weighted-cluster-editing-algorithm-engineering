package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.model.Edge;

public class EdgeWithScoreInt implements Comparable {

    private Edge edge;
    private int score;

    public EdgeWithScoreInt(Edge edge, int score) {
        this.edge = edge;
        this.score = score;
    }

    public Edge getEdge() {
        return edge;
    }

    public void setEdge(Edge edge) {
        this.edge = edge;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }


    @Override
    public int compareTo(Object o) {
        EdgeWithScoreInt other = (EdgeWithScoreInt) o;
        return score - other.getScore();
    }
}
