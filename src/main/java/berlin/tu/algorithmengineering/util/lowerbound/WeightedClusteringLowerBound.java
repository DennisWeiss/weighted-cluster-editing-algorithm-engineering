package berlin.tu.algorithmengineering.util.lowerbound;

import berlin.tu.algorithmengineering.Graph;

public interface WeightedClusteringLowerBound {

    double getLowerBound(Graph graph);

}
