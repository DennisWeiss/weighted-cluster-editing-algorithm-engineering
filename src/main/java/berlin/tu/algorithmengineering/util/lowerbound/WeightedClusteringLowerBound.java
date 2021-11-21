package berlin.tu.algorithmengineering.util.lowerbound;

import berlin.tu.algorithmengineering.Graph;
import berlin.tu.algorithmengineering.model.P3;

import java.util.List;

public interface WeightedClusteringLowerBound {

    double getLowerBound(Graph graph);
}
