package berlin.tu.algorithmengineering;

import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void mergeVerticesTest1() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();

        int k = 3;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(3, 4, k);

        int[][] expected = new int[][]{
                {0, -1, -1, 0},
                {-1, 0, 2, 4},
                {-1, 2, 0, 0},
                {0, 4, 0, 0}
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertEquals(expected[i][j], graph.getEdgeWeights()[i][j]);
                }
            }
        }

        assertEquals(0, mergeVerticesInfo.getK());
    }

    @Test
    void mergeVerticesTest2() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(deepCopy(edges));
        graph.computeEdgeExists();

        int k = 3;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(4, 3, k);

        int[][] expected = new int[][]{
                {0, -1, -1, 0},
                {-1, 0, 2, 4},
                {-1, 2, 0, 0},
                {0, 4, 0, 0}
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertEquals(expected[i][j], graph.getEdgeWeights()[i][j]);
                }
            }
        }

        assertEquals(0, mergeVerticesInfo.getK());
    }

    @Test
    void testIfGraphIsUnaltered() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(deepCopy(edges));
        graph.computeEdgeExists();

        int k = 3;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(4, 3, k);
        graph.revertMergeVertices(mergeVerticesInfo);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertEquals(edges[i][j], graph.getEdgeWeights()[i][j]);
                }
            }
        }
    }

    @Test
    void testConsecutiveMerges() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(deepCopy(edges));
        graph.computeEdgeExists();

        int k = 3;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(4, 3, k);
        mergeVerticesInfo = graph.mergeVertices(1, 2, mergeVerticesInfo.getK());

        int[][] expected = new int[][]{
                {0, -2, 0},
                {-2, 0, 4},
                {0, 4, 0}
        };

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != j) {
                    assertEquals(expected[i][j], graph.getEdgeWeights()[i][j]);
                }
            }
        }

        assertEquals(0, mergeVerticesInfo.getK());
    }

    @Test
    void testIfGraphIsUnalteredAfterConsecutiveMerges() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(deepCopy(edges));
        graph.computeEdgeExists();

        int k = 3;

        MergeVerticesInfo mergeVerticesInfo1 = graph.mergeVertices(4, 3, k);
        MergeVerticesInfo mergeVerticesInfo2 = graph.mergeVertices(1, 3, mergeVerticesInfo1.getK());
        graph.revertMergeVertices(mergeVerticesInfo2);
        graph.revertMergeVertices(mergeVerticesInfo1);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertEquals(edges[i][j], graph.getEdgeWeights()[i][j]);
                }
            }
        }
    }

    @Test
    void mergeVerticesTestNeighborhoodWeights1() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();
        graph.computeNeighborhoodWeights();

        int k = 3;

        graph.mergeVertices(3, 4, k);

        int[] expected = {0, 6, 2, 4};

        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i], graph.getNeighborhoodWeights()[i]);
        }
    }

    @Test
    void mergeVerticesTestNeighborhoodWeights2() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();
        graph.computeNeighborhoodWeights();

        int k = 3;

        graph.mergeVertices(1, 3, k);

        int[] expected = {1, 15, 4, 12};

        for (int i = 0; i < 4; i++) {
            assertEquals(expected[i], graph.getNeighborhoodWeights()[i]);
        }
    }

    @Test
    void testIfNeighborhoodWeightsIsUnaltered() {
        Graph graph = new Graph(5);
        int[][] edges = {
                {0, -1, -1, -1, 1},
                {-1, 0, 2, 2, 2},
                {-1, 2, 0, 2, -2},
                {-1, 2, 2, 0, 9},
                {1, 2, -2, 9, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();
        graph.computeNeighborhoodWeights();

        int k = 3;

        graph.revertMergeVertices(graph.mergeVertices(1, 3, k));

        int[] expected = {1, 6, 4, 13, 12};

        for (int i = 0; i < 5; i++) {
            assertEquals(expected[i], graph.getNeighborhoodWeights()[i]);
        }
    }

    private int[][] deepCopy(int[][] matrix) {
        if (matrix.length == 0) {
            return new int[0][0];
        }
        int[][] copy = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                copy[i][j] = matrix[i][j];
            }
        }
        return copy;
    }


}