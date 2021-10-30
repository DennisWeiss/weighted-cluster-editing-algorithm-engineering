package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.P3;

import java.util.*;

public class Graph {

    private int numberOfVertices;
    private int[][] edges;
    private Map<Integer, Set<Integer>> adjacencyLists = new HashMap<>();

    public Graph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.edges = new int[numberOfVertices][numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++) {
            HashSet<Integer> adjacencyList = new HashSet<>();
            adjacencyLists.put(i, adjacencyList);
        }
    }

    public void fillAdjacencyListsFromEdges() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                if (edges[i][j] > 0) {
                    adjacencyLists.get(i).add(j);
                    adjacencyLists.get(j).add(i);
                }
            }
        }
    }

    public Graph copy() {
        Graph copy = new Graph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                copy.getEdges()[i][j] = edges[i][j];
            }
        }
        copy.fillAdjacencyListsFromEdges();
        return copy;
    }

    public int editEdge(int i, int j) {
        int min = Math.min(i, j);
        int max = Math.max(i, j);
        if (edges[min][max] > 0) {
            this.adjacencyLists.get(i).remove(j);
            this.adjacencyLists.get(j).remove(i);
        } else {
            this.adjacencyLists.get(i).add(j);
            this.adjacencyLists.get(j).add(i);
        }
        edges[min][max] *= -1;
        return -edges[min][max];
    }

    public P3 findP3() {
        List<Integer> verticesOfConnectedComponents = findVerticesOfConnectedComponents();
        for (Integer v : verticesOfConnectedComponents) {
            P3 p3 = findP3(v);
            if (p3 != null) {
                return p3;
            }
        }
        return null;
    }

    private P3 findP3(int u) {
        P3 p3In2Closure = findP3In2Closure(u);

        if (p3In2Closure != null) {
            return p3In2Closure;
        }

        Set<Integer> adjacentVertices = adjacencyLists.get(u);
        for (Integer v : adjacentVertices) {
            if (adjacencyLists.get(v).size() < adjacentVertices.size()) {
                return findP3In2Closure(v);
            }
        }

        return null;
    }

    private P3 findP3In2Closure(int u) {
        Set<Integer> adjacentVertices = adjacencyLists.get(u);
        for (Integer v : adjacentVertices) {
            for (Integer w : adjacencyLists.get(v)) {
                if (w != u && !adjacentVertices.contains(w)) {
                    return new P3(u, v, w);
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of integers where each integer is one vertex of one connected component
     */
    private List<Integer> findVerticesOfConnectedComponents() {
        List<Integer> verticesOfConnectedComponents = new ArrayList<>();

        Set<Integer> remainingVertices = new HashSet<>();
        for (int i = 0; i < numberOfVertices; i++) {
            remainingVertices.add(i);
        }
        while (!remainingVertices.isEmpty()) {
            Integer v = remainingVertices.iterator().next();
            remainingVertices.remove(v);
            verticesOfConnectedComponents.add(v);
            Set<Integer> visitedVertices = new HashSet<>();
            visitedVertices.add(v);
            depthFirstSearch(v, visitedVertices);
            remainingVertices.removeAll(visitedVertices);
        }

        return verticesOfConnectedComponents;
    }

    private void depthFirstSearch(Integer v, Set<Integer> visitedVertices) {
        for (Integer w : adjacencyLists.get(v)) {
            if (!visitedVertices.contains(w)) {
                visitedVertices.add(w);
                depthFirstSearch(w, visitedVertices);
            }
        }
    }


    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public void setNumberOfVertices(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }

    public int[][] getEdges() {
        return edges;
    }

    public void setEdges(int[][] edges) {
        this.edges = edges;
    }

    public Map<Integer, Set<Integer>> getAdjacencyLists() {
        return adjacencyLists;
    }

    public void setAdjacencyLists(Map<Integer, Set<Integer>> adjacencyLists) {
        this.adjacencyLists = adjacencyLists;
    }
}
