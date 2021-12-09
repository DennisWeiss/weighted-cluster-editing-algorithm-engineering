package berlin.tu.algorithmengineering;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataReductionTest {

    @Test
    void getMinCutCost() {
        int numberOfVertices = 6;
        Graph graph = new Graph(numberOfVertices);
        graph.setEdgeWeights(new int[][]{
                {0, 16, 13, -1, -1, -1},
                {16, 0, 10, 12, -1, -1},
                {13, 10, 0, 9, 14, -1},
                {-1, 12, 9, 0, 7, 20},
                {-1, -1, 14, 7, 0, 4},
                {-1, -1, -1, 20, 4, 0}
        });

        Set<Integer> indices = new HashSet<>();

        for (int i = 0; i < numberOfVertices; i++) {
            indices.add(i);
        }

        assertEquals(24, DataReduction.getNaiveMinCutCost(graph, indices));
    }
}