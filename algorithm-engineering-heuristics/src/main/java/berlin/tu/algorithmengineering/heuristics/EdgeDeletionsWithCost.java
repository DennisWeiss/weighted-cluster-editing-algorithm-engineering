package berlin.tu.algorithmengineering.heuristics;

import berlin.tu.algorithmengineering.common.model.Edge;

import java.util.HashSet;
import java.util.Set;

public class EdgeDeletionsWithCost {

    Set<Edge> edgeDeletions = new HashSet<>();
    int cost;

    public EdgeDeletionsWithCost(Set<Edge> edgeDeletions, int cost) {
        this.edgeDeletions = edgeDeletions;
        this.cost = cost;
    }

    public Set<Edge> getEdgeDeletions() {
        return edgeDeletions;
    }

    public void setEdgeDeletions(Set<Edge> edgeDeletions) {
        this.edgeDeletions = edgeDeletions;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int addCost(int costDelta) {
        cost += costDelta;
        return cost;
    }
}
