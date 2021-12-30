package berlin.tu.algorithmengineering.common;


import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.common.model.P3;

import java.util.*;

public class Graph {

    private int numberOfVertices;
    private int[][] edgeWeights;
    private boolean[][] edgeExists;
    private int[] neighborhoodWeights;
    private int[] absoluteNeighborhoodWeights;

    public Graph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.edgeWeights = new int[numberOfVertices][numberOfVertices];
        this.edgeExists = new boolean[numberOfVertices][numberOfVertices];
        this.neighborhoodWeights = new int[numberOfVertices];
        this.absoluteNeighborhoodWeights = new int[numberOfVertices];
    }

    public void setEdge(int vertex1, int vertex2, int weight) {
        edgeWeights[vertex1][vertex2] = weight;
        edgeWeights[vertex2][vertex1] = weight;
        edgeExists[vertex1][vertex2] = weight > 0;
        edgeExists[vertex2][vertex1] = weight > 0;
    }

    public int[] computeNeighborhoodWeights() {
        for (int i = 0; i < numberOfVertices; i++) {
            neighborhoodWeights[i] = getNeighborhoodWeight(i);
        }
        return neighborhoodWeights;
    }

    public int[] computeAbsoluteNeighborhoodWeights() {
        for (int i = 0; i < numberOfVertices; i++) {
            absoluteNeighborhoodWeights[i] = getAbsoluteNeighborhoodWeight(i);
        }
        return absoluteNeighborhoodWeights;
    }

    private int getNeighborhoodWeight(int vertex) {
        int neighborhoodWeight = 0;
        for (int i = 0; i < numberOfVertices; i++) {
            if (i != vertex && edgeExists[vertex][i]) {
                neighborhoodWeight += edgeWeights[vertex][i];
            }
        }
        return neighborhoodWeight;
    }

    private int getAbsoluteNeighborhoodWeight(int vertex) {
        int absoluteNeighborhoodWeight = 0;
        for (int i = 0; i < numberOfVertices; i++) {
            if (i != vertex) {
                absoluteNeighborhoodWeight += Math.abs(edgeWeights[vertex][i]);
            }
        }
        return absoluteNeighborhoodWeight;
    }

    public Graph copy() {
        Graph copy = new Graph(edgeWeights.length);
        for (int i = 0; i < edgeWeights.length; i++) {
            for (int j = 0; j < edgeWeights.length; j++) {
                copy.getEdgeWeights()[i][j] = edgeWeights[i][j];
                copy.getEdgeExists()[i][j] = edgeExists[i][j];
            }
            copy.getNeighborhoodWeights()[i] = neighborhoodWeights[i];
            copy.getAbsoluteNeighborhoodWeights()[i] = absoluteNeighborhoodWeights[i];
        }
        return copy;
    }

    public void flipEdge(int i, int j) {
        edgeWeights[i][j] *= -1;
        edgeWeights[j][i] *= -1;
        edgeExists[i][j] = !edgeExists[i][j];
        edgeExists[j][i] = !edgeExists[j][i];
        neighborhoodWeights[i] += edgeWeights[i][j];
        neighborhoodWeights[j] += edgeWeights[i][j];
    }

    public MergeVerticesInfo mergeVertices(int a, int b) {
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        MergeVerticesInfo mergeVerticesInfo = new MergeVerticesInfo(numberOfVertices, a, b);

        for (int i = 0; i < numberOfVertices; i++) {
            mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i] = edgeWeights[a][i];
            mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] = edgeExists[a][i];
            mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i] = edgeWeights[b][i];
            mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i] = edgeExists[b][i];
        }

        for (int i = 0; i < numberOfVertices; i++) {
            if (i != a && i != b) {
                int newWeight = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i] + mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];

                if (edgeExists[a][i] != edgeExists[b][i]) {
                    mergeVerticesInfo.increaseCost(Math.min(Math.abs(edgeWeights[a][i]), Math.abs(edgeWeights[b][i])));
                }
                edgeWeights[a][i] = newWeight;
                edgeExists[a][i] = newWeight > 0;
                edgeWeights[i][a] = newWeight;
                edgeExists[i][a] = newWeight > 0;

            }
        }

        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[i][b] = edgeWeights[i][numberOfVertices - 1];
            edgeExists[i][b] = edgeExists[i][numberOfVertices - 1];
            edgeWeights[b][i] = edgeWeights[numberOfVertices - 1][i];
            edgeExists[b][i] = edgeExists[numberOfVertices - 1][i];
        }

        for (int i = 0; i < numberOfVertices - 1; i++) {
            if (i == a) {
                int neighborHoodWeight = 0;
                for (int j = 0; j < numberOfVertices - 1; j++) {
                    if (j != a && edgeExists[a][j]) {
                        neighborHoodWeight += edgeWeights[a][j];
                    }
                }
                neighborhoodWeights[i] = neighborHoodWeight;
            } else if (i == b) {
                neighborhoodWeights[i] = neighborhoodWeights[numberOfVertices - 1]
                        - Math.max(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[numberOfVertices - 1], 0)
                        - Math.max(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[numberOfVertices - 1], 0)
                        + Math.max(edgeWeights[a][i], 0);
            } else {
                neighborhoodWeights[i] += -Math.max(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i], 0)
                        - Math.max(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i], 0)
                        + Math.max(edgeWeights[a][i], 0);
            }
        }

        for (int i = 0; i < numberOfVertices - 1; i++) {
            if (i == a) {
                int absoluteNeighborhoodWeight = 0;
                for (int j = 0; j < numberOfVertices - 1; j++) {
                    if (j != a) {
                        absoluteNeighborhoodWeight += Math.abs(edgeWeights[a][j]);
                    }
                }
                absoluteNeighborhoodWeights[i] = absoluteNeighborhoodWeight;
            } else if (i == b) {
                absoluteNeighborhoodWeights[i] = absoluteNeighborhoodWeights[numberOfVertices - 1]
                        - (mergeVerticesInfo.getEdgeExistsOfFirstVertex()[numberOfVertices - 1] != mergeVerticesInfo.getEdgeExistsOfSecondVertex()[numberOfVertices - 1]
                        ? 2 * Math.min(
                        Math.abs(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[numberOfVertices - 1]),
                        Math.abs(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[numberOfVertices - 1])
                )
                        : 0);
            } else {
                if (mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] != mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i]) {
                    absoluteNeighborhoodWeights[i] -= 2 * Math.min(
                            Math.abs(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i]),
                            Math.abs(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i])
                    );
                }
            }
        }

        numberOfVertices--;

        return mergeVerticesInfo;
    }

    public void revertMergeVertices(MergeVerticesInfo mergeVerticesInfo) {
        numberOfVertices++;

        for (int i = 0; i < numberOfVertices - 1; i++) {
            if (i == mergeVerticesInfo.getFirstVertex()) {
                int neighborhoodWeight = 0;
                for (int j = 0; j < numberOfVertices; j++) {
                    if (j != mergeVerticesInfo.getFirstVertex() && mergeVerticesInfo.getEdgeExistsOfFirstVertex()[j]) {
                        neighborhoodWeight += mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[j];
                    }
                }
                neighborhoodWeights[i] = neighborhoodWeight;
            } else if (i == mergeVerticesInfo.getSecondVertex()) {
                int neighborhoodWeight = 0;
                for (int j = 0; j < numberOfVertices; j++) {
                    if (j != mergeVerticesInfo.getSecondVertex() && mergeVerticesInfo.getEdgeExistsOfSecondVertex()[j]) {
                        neighborhoodWeight += mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[j];
                    }
                }
                neighborhoodWeights[i] = neighborhoodWeight;
            } else {
                if (mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] != mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i]) {
                    neighborhoodWeights[i] += -2 * Math.min(edgeWeights[mergeVerticesInfo.getFirstVertex()][i], 0)
                            + Math.max(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i], 0)
                            + Math.max(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i], 0);
                }
            }
        }

        for (int i = 0; i < numberOfVertices - 1; i++) {
            if (i == mergeVerticesInfo.getFirstVertex()) {
                int absoluteNeighborhoodWeight = 0;
                for (int j = 0; j < numberOfVertices; j++) {
                    if (j != mergeVerticesInfo.getFirstVertex()) {
                        absoluteNeighborhoodWeight += Math.abs(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[j]);
                    }
                }
                absoluteNeighborhoodWeights[i] = absoluteNeighborhoodWeight;
            } else if (i == mergeVerticesInfo.getSecondVertex()) {
                int absoluteNeighborhoodWeight = 0;
                for (int j = 0; j < numberOfVertices; j++) {
                    if (j != mergeVerticesInfo.getSecondVertex()) {
                        absoluteNeighborhoodWeight += Math.abs(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[j]);
                    }
                }
                absoluteNeighborhoodWeights[i] = absoluteNeighborhoodWeight;
            } else {
                if (mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] != mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i]) {
                    absoluteNeighborhoodWeights[i] += Math.max(
                            Math.abs(mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i]),
                            Math.abs(mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i])
                    );
                }
            }
        }

        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[numberOfVertices - 1][i] = edgeWeights[mergeVerticesInfo.getSecondVertex()][i];
            edgeExists[numberOfVertices - 1][i] = edgeExists[mergeVerticesInfo.getSecondVertex()][i];
            edgeWeights[i][numberOfVertices - 1] = edgeWeights[i][mergeVerticesInfo.getSecondVertex()];
            edgeExists[i][numberOfVertices - 1] = edgeExists[i][mergeVerticesInfo.getSecondVertex()];
        }

        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            edgeExists[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            edgeWeights[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            edgeExists[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            edgeWeights[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            edgeExists[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
            edgeWeights[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            edgeExists[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
        }
    }

    public void revertFromMergeVerticesInfoStack(Stack<MergeVerticesInfo> mergeVerticesInfoStack) {
        MergeVerticesInfo mergeVerticesInfo;
        while ((mergeVerticesInfo = mergeVerticesInfoStack.pop()) != null) {
            revertMergeVertices(mergeVerticesInfo);
        }
    }


    public Graph getSubGraph(int[] subGraphIndices) {
        Graph graph = new Graph(subGraphIndices.length);
        for (int i = 0; i < subGraphIndices.length; i++) {
            for (int j = 0; j < subGraphIndices.length; j++) {
                graph.getEdgeWeights()[i][j] = edgeWeights[subGraphIndices[i]][subGraphIndices[j]];
                graph.getEdgeExists()[i][j] = edgeExists[subGraphIndices[i]][subGraphIndices[j]];
            }
        }
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        return graph;
    }

    public Graph getSubGraph(List<Integer> subGraphIndices) {
        Graph graph = new Graph(subGraphIndices.size());
        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = 0; j < subGraphIndices.size(); j++) {
                graph.getEdgeWeights()[i][j] = edgeWeights[subGraphIndices.get(i)][subGraphIndices.get(j)];
                graph.getEdgeExists()[i][j] = edgeExists[subGraphIndices.get(i)][subGraphIndices.get(j)];
            }
        }
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        return graph;
    }

    public int getTotalAbsoluteWeight(P3 p3) {
        return edgeWeights[p3.getU()][p3.getV()] + edgeWeights[p3.getV()][p3.getW()] - edgeWeights[p3.getU()][p3.getW()];
    }

    public int getSmallestAbsoluteWeight(P3 p3) {
        return Math.min(Math.min(edgeWeights[p3.getU()][p3.getV()], edgeWeights[p3.getV()][p3.getW()]), -edgeWeights[p3.getU()][p3.getW()]);
    }

    public P3 findP3() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                for (int k = j + 1; k < numberOfVertices; k++) {
                    if (edgeExists[i][j] && edgeExists[j][k] && !edgeExists[i][k]) {
                        return new P3(i, j, k);
                    }
                    if (edgeExists[i][j] && edgeExists[i][k] && !edgeExists[j][k]) {
                        return new P3(j, i, k);
                    }
                    if (edgeExists[j][k] && edgeExists[i][k] && !edgeExists[i][j]) {
                        return new P3(i, k, j);
                    }
                }
            }
        }
        return null;
    }

    public List<P3> findAllP3() {
        List<P3> p3List = new ArrayList<>();
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                for (int k = i + 1; k < numberOfVertices; k++) {
                    if (i != j && j != k && edgeExists[i][j] && edgeExists[j][k] && !edgeExists[i][k]) {
                        p3List.add(new P3(i, j, k));
                    }
                }
            }
        }
        return p3List;
    }

    public List<P3> findAllP3WithEdge(int u, int v) {
        List<P3> p3List = new ArrayList<>();
        for (int w = 0; w < numberOfVertices; w++) {
            if (u != w && v != w) {
                if (edgeExists[u][v]) {
                    if (edgeExists[u][w] && !edgeExists[v][w]) {
                        p3List.add(new P3(v, u, w));
                    } else if (!edgeExists[u][w] && edgeExists[v][w]) {
                        p3List.add(new P3(u, v, w));
                    }
                } else if (edgeExists[u][w] && edgeExists[v][w]) {
                    p3List.add(new P3(u, w, v));
                }
            }
        }
        return p3List;
    }

    public P3 findBiggestWeightP3() {
        int biggestTotalAbsoluteWeight = Integer.MIN_VALUE;
        P3 biggestTotalAbsoluteWeightP3 = null;
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                for (int k = 0; k < numberOfVertices; k++) {
                    if (i != k && j != k && edgeExists[i][j] && edgeExists[j][k] && !edgeExists[i][k]) {
                        P3 p3 = new P3(i, j, k);
                        int totalAbsoluteWeight = getTotalAbsoluteWeight(p3);
                        if (totalAbsoluteWeight > biggestTotalAbsoluteWeight) {
                            biggestTotalAbsoluteWeight = totalAbsoluteWeight;
                            biggestTotalAbsoluteWeightP3 = p3;
                        }
                    }
                }
            }
        }
        return biggestTotalAbsoluteWeightP3;
    }

    public int getLowerBound2(List<P3> p3List) {
        List<P3> sortedP3List = new ArrayList<>(p3List);
        sortedP3List.sort((a, b) -> getSmallestAbsoluteWeight(b) - getSmallestAbsoluteWeight(a));
        boolean[][] isInEdgeDisjointP3List = new boolean[numberOfVertices][numberOfVertices];
        int lowerBound = 0;
        for (P3 p3 : sortedP3List) {
            if (!isInEdgeDisjointP3List[p3.getU()][p3.getV()] && !isInEdgeDisjointP3List[p3.getV()][p3.getW()] && !isInEdgeDisjointP3List[p3.getU()][p3.getW()]) {
                lowerBound += getSmallestAbsoluteWeight(p3);
                isInEdgeDisjointP3List[p3.getU()][p3.getV()] = true;
                isInEdgeDisjointP3List[p3.getV()][p3.getU()] = true;
                isInEdgeDisjointP3List[p3.getV()][p3.getW()] = true;
                isInEdgeDisjointP3List[p3.getW()][p3.getV()] = true;
                isInEdgeDisjointP3List[p3.getU()][p3.getW()] = true;
                isInEdgeDisjointP3List[p3.getW()][p3.getU()] = true;
            }
        }
        return lowerBound;
    }

    public int getWeightedDegreeCut(int u) {
        int weightedDegreeCut = 0;
        for (int i = 0; i < getNumberOfVertices(); i++) {
            if (u != i) {
                weightedDegreeCut += Math.max(edgeWeights[u][i], 0);
            }
        }
        return weightedDegreeCut;
    }

    public boolean[][] computeEdgeExists() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                edgeExists[i][j] = edgeWeights[i][j] > 0;
            }
        }
        return edgeExists;
    }

    public Set<Set<Integer>> getConnectedComponents() {
        Set<Integer> visitedVertices = new HashSet<>();
        Set<Set<Integer>> connectedComponents = new HashSet<>();
        for (int i = 0; i < numberOfVertices; i++) {
            if (!visitedVertices.contains(i)) {
                Set<Integer> connectedComponent = getConnectedComponent(i);
                visitedVertices.addAll(connectedComponent);
                connectedComponents.add(connectedComponent);
            }
        }
        return connectedComponents;
    }

    public Set<Integer> getConnectedComponent(int vertex) {
        return getConnectedComponent(vertex, new HashSet<>());
    }

    private Set<Integer> getConnectedComponent(int vertex, Set<Integer> connectedComponent) {
        connectedComponent.add(vertex);
        for (int i = 0; i < numberOfVertices; i++) {
            if (vertex != i && edgeExists[vertex][i] && !connectedComponent.contains(i)) {
                connectedComponent.addAll(getConnectedComponent(i, connectedComponent));
            }
        }
        return connectedComponent;
    }

    public int getTransitiveClosureCost() {
        if (numberOfVertices < 3) {
            return 0;
        }
        int[] vertexToConnectedComponentIndex = getVertexToConnectedComponentIndex();
        int cost = 0;
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                if (!edgeExists[i][j] && vertexToConnectedComponentIndex[i] == vertexToConnectedComponentIndex[j]) {
                    cost -= edgeWeights[i][j];
                }
            }
        }
        return cost;
    }

    private int[] getVertexToConnectedComponentIndex() {
        return getVertexToConnectedComponentIndex(getConnectedComponents(), numberOfVertices);
    }

    public static int[] getVertexToConnectedComponentIndex(Set<Set<Integer>> connectedComponents, int numberOfVertices) {
        int index = 0;
        int[] vertexToConnectedComponentIndex = new int[numberOfVertices];
        for (Set<Integer> connectedComponent : connectedComponents) {
            for (Integer vertex : connectedComponent) {
                vertexToConnectedComponentIndex[vertex] = index;
            }
            index++;
        }
        return vertexToConnectedComponentIndex;
    }

    public int getNumberOfVertices() {
        return numberOfVertices;
    }

    public void setNumberOfVertices(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
    }

    public int[][] getEdgeWeights() {
        return edgeWeights;
    }

    public void setEdgeWeights(int[][] edgeWeights) {
        this.edgeWeights = edgeWeights;
    }

    public boolean[][] getEdgeExists() {
        return edgeExists;
    }

    public void setEdgeExists(boolean[][] edgeExists) {
        this.edgeExists = edgeExists;
    }

    public int[] getNeighborhoodWeights() {
        return neighborhoodWeights;
    }

    public void setNeighborhoodWeights(int[] neighborhoodWeights) {
        this.neighborhoodWeights = neighborhoodWeights;
    }

    public int[] getAbsoluteNeighborhoodWeights() {
        return absoluteNeighborhoodWeights;
    }

    public void setAbsoluteNeighborhoodWeights(int[] absoluteNeighborhoodWeights) {
        this.absoluteNeighborhoodWeights = absoluteNeighborhoodWeights;
    }
}