package berlin.tu.algorithmengineering.heuristic;

import berlin.tu.algorithmengineering.model.Edge;

public class EdgeWithScore implements Comparable {

    private Edge edge;
    private int score;

    public EdgeWithScore(Edge edge, int score) {
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
        EdgeWithScore other = (EdgeWithScore) o;
        return score - other.getScore();
    }
}
