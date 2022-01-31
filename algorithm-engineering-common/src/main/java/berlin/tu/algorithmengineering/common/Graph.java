package berlin.tu.algorithmengineering.common;


import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.common.model.P3;
import berlin.tu.algorithmengineering.common.model.P3WithSharedEdgeP3s;

import java.util.*;

public class Graph {

    private int numberOfVertices;
    private int[][] edgeWeights;
    private boolean[][] edgeExists;
    private int[] neighborhoodWeights;
    private int[] absoluteNeighborhoodWeights;
    private ArrayList<ArrayList<Integer>> connectedComponents;
    private int[] vertexToConnectedComponentIndex;

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
        Graph copy = new Graph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
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

        if (edgeExists[i][j]) {
            int connectedComponentIndexOfI = vertexToConnectedComponentIndex[i];
            int connectedComponentIndexOfJ = vertexToConnectedComponentIndex[j];
            if (connectedComponentIndexOfI != connectedComponentIndexOfJ) {
                for (Integer w : connectedComponents.get(connectedComponentIndexOfJ)) {
                    vertexToConnectedComponentIndex[w] = connectedComponentIndexOfI;
                    connectedComponents.get(connectedComponentIndexOfI).add(w);
                }
                connectedComponents.remove(connectedComponentIndexOfJ);
            }
        }else if (!edgeExists[i][j]) {
            ArrayList<Integer> newConnectedComponent = new ArrayList<>();
            newConnectedComponent.add(j);
            boolean[] visitedVertex = new boolean[numberOfVertices];
            visitedVertex[j] = true;
            if (! pathExists(j,i,newConnectedComponent,visitedVertex)) {
                int newIndex = connectedComponents.size();
                connectedComponents.add(newIndex,newConnectedComponent);
                connectedComponents.get(vertexToConnectedComponentIndex[i]).removeAll(newConnectedComponent);
                for (Integer w : newConnectedComponent) {
                    vertexToConnectedComponentIndex[w] = newIndex;
                }
            }
        }
    }

    private boolean pathExists(int vertex, int otherVertex, ArrayList<Integer> connectedComponent, boolean[] visitedVertex) {

            for (int i = 0; i < numberOfVertices; i++) {
                if (vertex != i && edgeExists[vertex][i] && !visitedVertex[i]) {
                    if (i == otherVertex) return true;
                    connectedComponent.add(i);
                    visitedVertex[i] = true;
                    if (pathExists(i, otherVertex, connectedComponent, visitedVertex)) {
                        return true;
                    }
                }
            }
            return false;
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

        // fix connected components
        if (vertexToConnectedComponentIndex[b] != vertexToConnectedComponentIndex[numberOfVertices-1]) {
            connectedComponents.get(vertexToConnectedComponentIndex[a]).remove(Integer.valueOf(b));
            connectedComponents.get(vertexToConnectedComponentIndex[numberOfVertices-1]).add(b);
            vertexToConnectedComponentIndex[b] = vertexToConnectedComponentIndex[numberOfVertices-1];
        }
        connectedComponents.get(vertexToConnectedComponentIndex[numberOfVertices-1]).remove(Integer.valueOf(numberOfVertices-1));

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

        int a = mergeVerticesInfo.getFirstVertex();
        int b = mergeVerticesInfo.getSecondVertex();

        // fix connected components
        if (vertexToConnectedComponentIndex[a] != vertexToConnectedComponentIndex[numberOfVertices-1]) {
            connectedComponents.get(vertexToConnectedComponentIndex[a]).add(b);
            connectedComponents.get(vertexToConnectedComponentIndex[numberOfVertices-1]).remove(Integer.valueOf(b));
            vertexToConnectedComponentIndex[b] = vertexToConnectedComponentIndex[a];
        }
        connectedComponents.get(vertexToConnectedComponentIndex[numberOfVertices-1]).add(numberOfVertices-1);
    }

    public void revertFromMergeVerticesInfoStack(Stack<MergeVerticesInfo> mergeVerticesInfoStack) {
        MergeVerticesInfo mergeVerticesInfo;
        while ((mergeVerticesInfo = mergeVerticesInfoStack.pop()) != null) {
            revertMergeVertices(mergeVerticesInfo);
        }
    }


    public Graph getSubGraphOfConnectedComponent(int[] subGraphIndices) {
        Graph graph = new Graph(subGraphIndices.length);
        for (int i = 0; i < subGraphIndices.length; i++) {
            for (int j = 0; j < subGraphIndices.length; j++) {
                graph.getEdgeWeights()[i][j] = edgeWeights[subGraphIndices[i]][subGraphIndices[j]];
                graph.getEdgeExists()[i][j] = edgeExists[subGraphIndices[i]][subGraphIndices[j]];
            }
        }
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        ArrayList<Integer> connectedComponent = new ArrayList<>(subGraphIndices.length);
        graph.connectedComponents = new ArrayList<>();
        graph.connectedComponents.add(connectedComponent);
        graph.vertexToConnectedComponentIndex = new int[subGraphIndices.length];
        for (int i = 0; i < subGraphIndices.length; i++) {
            connectedComponent.add(i);
            graph.vertexToConnectedComponentIndex[i] = 0;
        }
        return graph;
    }

    public Graph getSubGraphOfConnectedComponent(Integer[] subGraphIndices) {
        Graph graph = new Graph(subGraphIndices.length);
        for (int i = 0; i < subGraphIndices.length; i++) {
            for (int j = 0; j < subGraphIndices.length; j++) {
                graph.getEdgeWeights()[i][j] = edgeWeights[subGraphIndices[i]][subGraphIndices[j]];
                graph.getEdgeExists()[i][j] = edgeExists[subGraphIndices[i]][subGraphIndices[j]];
            }
        }
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        ArrayList<Integer> connectedComponent = new ArrayList<>(subGraphIndices.length);
        graph.connectedComponents = new ArrayList<>();
        graph.connectedComponents.add(connectedComponent);
        graph.vertexToConnectedComponentIndex = new int[subGraphIndices.length];
        for (int i = 0; i < subGraphIndices.length; i++) {
            connectedComponent.add(i);
            graph.vertexToConnectedComponentIndex[i] = 0;
        }
        return graph;
    }

    public Graph getSubGraphOfConnectedComponent(ArrayList<Integer> subGraphIndices) {
        Graph graph = new Graph(subGraphIndices.size());
        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = 0; j < subGraphIndices.size(); j++) {
                graph.getEdgeWeights()[i][j] = edgeWeights[subGraphIndices.get(i)][subGraphIndices.get(j)];
                graph.getEdgeExists()[i][j] = edgeExists[subGraphIndices.get(i)][subGraphIndices.get(j)];
            }
        }
        graph.computeNeighborhoodWeights();
        graph.computeAbsoluteNeighborhoodWeights();

        ArrayList<Integer> connectedComponent = new ArrayList<>(subGraphIndices.size());
        graph.connectedComponents = new ArrayList<>();
        graph.connectedComponents.add(connectedComponent);
        graph.vertexToConnectedComponentIndex = new int[subGraphIndices.size()];
        for (int i = 0; i < subGraphIndices.size(); i++) {
            connectedComponent.add(i);
            graph.vertexToConnectedComponentIndex[i] = 0;
        }
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

    public int getLowerBound2BasedOnMaximumWeightIndependentSet(List<P3> p3List) {
        Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap = P3WithSharedEdgeP3s.fromP3List(this, p3List);
        Set<P3> freeP3s = new HashSet<>(p3List);
        Set<P3> selectedP3s = new HashSet<>();

        createInitialSelection(p3WithSharedEdgeP3sMap, freeP3s, selectedP3s);

        return performSimulatedAnnealingOnMaximumWeightIndependentSetOfP3s(p3WithSharedEdgeP3sMap, freeP3s, selectedP3s);
    }

    private int getSumOfSmallestAbsoluteWeights(Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap, Set<P3> p3Set) {
        int lowerBound = 0;
        for (P3 p3 : p3Set) {
            lowerBound += p3WithSharedEdgeP3sMap.get(p3).getSmallestAbsoluteEdgeWeight();
        }

        return lowerBound;
    }

    private void createInitialSelection(Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap, Set<P3> freeP3s, Set<P3> selectedP3s) {
        while (!freeP3s.isEmpty()) {
            P3 p3 = freeP3s.iterator().next();
            freeP3s.remove(p3);
            selectedP3s.add(p3);
            for (P3 otherP3 : p3WithSharedEdgeP3sMap.get(p3).getSharedEdgeP3()) {
                if (freeP3s.contains(otherP3)) {
                    freeP3s.remove(otherP3);
                }
            }
        }
    }

    private int performSimulatedAnnealingOnMaximumWeightIndependentSetOfP3s(
            Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap, Set<P3> freeP3s, Set<P3> selectedP3s
    ) {
        final double T_START = 2;
        double t = T_START;
        final int ITERATIONS = 200;

        int currentSolution = getSumOfSmallestAbsoluteWeights(p3WithSharedEdgeP3sMap, selectedP3s);
        int highestSolution = currentSolution;

        for (int i = 0; i < ITERATIONS; i++) {
            Set<P3> perturbedSelection = new HashSet<>(selectedP3s);
            int deltaSolution = perturbSelection(p3WithSharedEdgeP3sMap, perturbedSelection);
            if (deltaSolution >= 0 || Math.random() < Math.exp(deltaSolution / t)) {
                currentSolution += deltaSolution;
                if (currentSolution > highestSolution) {
                    highestSolution = currentSolution;
                }
                selectedP3s = perturbedSelection;
            }
            t -= T_START / ITERATIONS;
        }

        return highestSolution;
    }

    /**
     *
     * @param p3WithSharedEdgeP3sMap
     * @param freeP3s
     * @param selectedP3s
     * @return deltaSolution
     */
    private int perturbSelection(Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap, Set<P3> selectedP3s) {
        if (selectedP3s.isEmpty()) {
            return 0;
        }

        P3 removedP3 = Utils.getRandomElementFromSet(selectedP3s);

        selectedP3s.remove(removedP3);
        int deltaSolution = -p3WithSharedEdgeP3sMap.get(removedP3).getSmallestAbsoluteEdgeWeight();

        Set<P3> freeP3s = getFreeP3sAdjacentToP3(p3WithSharedEdgeP3sMap, removedP3, selectedP3s);

        while (!freeP3s.isEmpty()) {
            P3 p3 = Utils.getRandomElementFromSet(freeP3s);
            freeP3s.remove(p3);
            selectedP3s.add(p3);
            P3WithSharedEdgeP3s p3WithSharedEdgeP3s = p3WithSharedEdgeP3sMap.get(p3);
            deltaSolution += p3WithSharedEdgeP3s.getSmallestAbsoluteEdgeWeight();
            for (P3 otherP3 : p3WithSharedEdgeP3s.getSharedEdgeP3()) {
                if (freeP3s.contains(otherP3)) {
                    freeP3s.remove(otherP3);
                }
            }
        }

        return deltaSolution;
    }

    private Set<P3> getFreeP3sAdjacentToP3(Map<P3, P3WithSharedEdgeP3s> p3WithSharedEdgeP3sMap, P3 p3, Set<P3> selectedP3s) {
        Set<P3> freeP3sAdjacentToP3 = new HashSet<>();

        for (P3 otherP3 : p3WithSharedEdgeP3sMap.get(p3).getSharedEdgeP3()) {
            boolean isFree = true;
            for (P3 otherP3_2 : p3WithSharedEdgeP3sMap.get(otherP3).getSharedEdgeP3()) {
                if (selectedP3s.contains(otherP3_2)) {
                    isFree = false;
                    break;
                }
            }
            if (isFree) {
                freeP3sAdjacentToP3.add(otherP3);
            }
        }

        return freeP3sAdjacentToP3;
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


    public ArrayList<ArrayList<Integer>> getConnectedComponents() {
        return connectedComponents;
    }
    public void computeConnectedComponents() {
        connectedComponents = getConnectedComponents(edgeExists, numberOfVertices);
        vertexToConnectedComponentIndex = getVertexToConnectedComponentIndex(connectedComponents, numberOfVertices);
    }

    public static ArrayList<ArrayList<Integer>> getConnectedComponents(boolean[][] edgeExists, int numberOfVertices) {
        boolean[] visitedVertex = new boolean[numberOfVertices];
        ArrayList<ArrayList<Integer>> connectedComponents = new ArrayList<>();
        for (int i = 0; i < numberOfVertices; i++) {
            if (!visitedVertex[i]) {
                ArrayList<Integer> connectedComponent = new ArrayList<>();
                connectedComponent.add(i);
                visitedVertex[i] = true;
                getConnectedComponentOfVertex(i, connectedComponent, visitedVertex, edgeExists, numberOfVertices);
                connectedComponent.trimToSize();// will never increase again
                connectedComponents.add(connectedComponent);
            }
        }
        return connectedComponents;
    }
    public static ArrayList<Integer> getConnectedComponentOfVertex(
            int vertex, ArrayList<Integer> connectedComponent, boolean[] visitedVertex,
            boolean[][] edgeExists, int numberOfVertices) {
        for (int i = 0; i < numberOfVertices; i++) {
            if (vertex != i && edgeExists[vertex][i] && !visitedVertex[i]) {
                connectedComponent.add(i);
                visitedVertex[i] = true;
                getConnectedComponentOfVertex(i, connectedComponent, visitedVertex, edgeExists, numberOfVertices);
            }
        }
        return connectedComponent;
    }

    public int getTransitiveClosureCost() {
        if (numberOfVertices < 3) {
            return 0;
        }
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

    public void transitiveClosure() {
        if (numberOfVertices < 3) {
            return;
        }
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                if (!edgeExists[i][j] && vertexToConnectedComponentIndex[i] == vertexToConnectedComponentIndex[j]) {
                    flipEdge(i, j);
                }
            }
        }
    }

    public int[] getVertexToConnectedComponentIndex() {
        return vertexToConnectedComponentIndex;
    }

    public static int[] getVertexToConnectedComponentIndex(ArrayList<ArrayList<Integer>> connectedComponents, int numberOfVertices) {
        int index = 0;
        int[] vertexToConnectedComponentIndex = new int[numberOfVertices];
        for (ArrayList<Integer> connectedComponent : connectedComponents) {
            for (Integer vertex : connectedComponent) {
                vertexToConnectedComponentIndex[vertex] = index;
            }
            index++;
        }
        return vertexToConnectedComponentIndex;
    }

    public List<Integer> getClosedNeighborhoodOfVertexWithoutVertices(int vertex, boolean[] excludedVertices) {
        List<Integer> closedNeighborhood = new ArrayList<>();
        for (int i = 0; i < numberOfVertices; i++) {
            if (!excludedVertices[i] && (i == vertex || edgeExists[vertex][i])) {
                closedNeighborhood.add(i);
            }
        }
        return closedNeighborhood;
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
