package berlin.tu.algorithmengineering.common;

import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

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

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(3, 4);

        int[][] expected = new int[][]{
                {0, -1, -1, 0},
                {-1, 0, 2, 4},
                {-1, 2, 0, 0},
                {0, 4, 0, 0}
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertThat(graph.getEdgeWeights()[i][j]).isEqualTo(expected[i][j]);
                }
            }
        }

        assertThat(mergeVerticesInfo.getCost()).isEqualTo(3);
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

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(4, 3);

        int[][] expected = new int[][]{
                {0, -1, -1, 0},
                {-1, 0, 2, 4},
                {-1, 2, 0, 0},
                {0, 4, 0, 0}
        };

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertThat(graph.getEdgeWeights()[i][j]).isEqualTo(expected[i][j]);
                }
            }
        }

        assertThat(mergeVerticesInfo.getCost()).isEqualTo(3);
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

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(4, 3);
        graph.revertMergeVertices(mergeVerticesInfo);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertThat(graph.getEdgeWeights()[i][j]).isEqualTo(edges[i][j]);
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

        MergeVerticesInfo mergeVerticesInfo1 = graph.mergeVertices(4, 3);
        MergeVerticesInfo mergeVerticesInfo2 = graph.mergeVertices(1, 2);

        int[][] expected = new int[][]{
                {0, -2, 0},
                {-2, 0, 4},
                {0, 4, 0}
        };

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != j) {
                    assertThat(graph.getEdgeWeights()[i][j]).isEqualTo(expected[i][j]);
                }
            }
        }

        assertThat(mergeVerticesInfo1.getCost() + mergeVerticesInfo2.getCost()).isEqualTo(3);
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

        MergeVerticesInfo mergeVerticesInfo1 = graph.mergeVertices(4, 3);
        MergeVerticesInfo mergeVerticesInfo2 = graph.mergeVertices(1, 3);
        graph.revertMergeVertices(mergeVerticesInfo2);
        graph.revertMergeVertices(mergeVerticesInfo1);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i != j) {
                    assertThat(graph.getEdgeWeights()[i][j]).isEqualTo(edges[i][j]);
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

        graph.mergeVertices(3, 4);

        int[] expected = {0, 6, 2, 4};

        for (int i = 0; i < 4; i++) {
            assertThat(graph.getNeighborhoodWeights()[i]).isEqualTo(expected[i]);
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

        graph.mergeVertices(1, 3);

        int[] expected = {1, 15, 4, 12};

        for (int i = 0; i < 4; i++) {
            assertThat(graph.getNeighborhoodWeights()[i]).isEqualTo(expected[i]);
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

        graph.revertMergeVertices(graph.mergeVertices(1, 3));

        int[] expected = {1, 6, 4, 13, 12};

        for (int i = 0; i < 5; i++) {
            assertThat(graph.getNeighborhoodWeights()[i]).isEqualTo(expected[i]);
        }
    }

    @Test
    void mergeVerticesTestAbsoluteNeighborhoodWeights1() {
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
        graph.computeAbsoluteNeighborhoodWeights();

        graph.mergeVertices(3, 4);

        int[] expected = {2, 7, 3, 4};

        for (int i = 0; i < 4; i++) {
            assertThat(graph.getAbsoluteNeighborhoodWeights()[i]).isEqualTo(expected[i]);
        }
    }

    @Test
    void mergeVerticesTestAbsoluteNeighborhoodWeights2() {
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
        graph.computeAbsoluteNeighborhoodWeights();

        graph.mergeVertices(1, 3);

        int[] expected = {4, 17, 7, 14};

        for (int i = 0; i < 4; i++) {
            assertThat(graph.getAbsoluteNeighborhoodWeights()[i]).isEqualTo(expected[i]);
        }
    }

    @Test
    void testIfAbsoluteNeighborhoodWeightsIsUnaltered() {
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
        graph.computeAbsoluteNeighborhoodWeights();

        graph.revertMergeVertices(graph.mergeVertices(1, 3));

        int[] expected = {4, 7, 7, 14, 14};

        for (int i = 0; i < 5; i++) {
            assertThat(graph.getAbsoluteNeighborhoodWeights()[i]).isEqualTo(expected[i]);
        }
    }

    @Test
    void testGetConnectedComponents() {
        Graph graph = new Graph(7);
        int[][] edges = {
                {0, 1, 1, -1, -1, -1, -1},
                {1, 0, -1, -1, -1, -1, -1},
                {1, -1, 0, -1, -1, -1, -1},
                {-1, -1, -1, 0, 1, -1, 1},
                {-1, -1, -1, 1, 0, -1, -1},
                {-1, -1, -1, -1, -1, 0, 1},
                {-1, -1, -1, 1, -1, 1, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();

        Set<Set<Integer>> connectedComponents = graph.getConnectedComponents();

        assertThat(connectedComponents).hasSize(2);
    }

    @Test
    void getTransitiveClosureCost() {
        Graph graph = new Graph(7);
        int[][] edges = {
                {0, 1, 1, -1, -1, -1, -1},
                {1, 0, -1, -1, -1, -1, -1},
                {1, -1, 0, -1, -1, -1, -1},
                {-1, -1, -1, 0, 1, -3, 1},
                {-1, -1, -1, 1, 0, -1, -1},
                {-1, -1, -1, -3, -1, 0, 1},
                {-1, -1, -1, 1, -1, 1, 0}
        };
        graph.setEdgeWeights(edges.clone());
        graph.computeEdgeExists();

        assertThat(graph.getTransitiveClosureCost()).isEqualTo(6);
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