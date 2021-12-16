package berlin.tu.algorithmengineering;

import berlin.tu.algorithmengineering.mincut.NagamochiIbarakiAlgorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataReductionTest {

    @Test
    void getMinCutCostTest1() {
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

        graph.computeEdgeExists();

        int globalMinCutCost = NagamochiIbarakiAlgorithm.getGlobalMinCutCost(graph);
        assertEquals(24, globalMinCutCost);
    }

    @Test
    void getMinCutCostTest2() {
        int numberOfVertices = 6;
        Graph graph = new Graph(numberOfVertices);
        graph.setEdgeWeights(new int[][]{
                {0, 62, 1, 7, 54, 7},
                {62, 0, 0, 0, 114, 9},
                {1, 0, 0, 98, 0, 41},
                {7, 0, 98, 0, 2, 52},
                {54, 114, 0, 2, 0, 7},
                {7, 9, 41, 52, 7, 0}
        });

        graph.computeEdgeExists();

        int globalMinCutCost = NagamochiIbarakiAlgorithm.getGlobalMinCutCost(graph);
        assertEquals(33, globalMinCutCost);
    }
}