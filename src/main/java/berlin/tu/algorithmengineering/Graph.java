package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private int numberOfVertices;
    private int[][] edgeWeights;
    private boolean[][] edgeExists;
    private boolean[][] forbidden;

    public Graph(int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.edgeWeights = new int[numberOfVertices][numberOfVertices];
        this.edgeExists = new boolean[numberOfVertices][numberOfVertices];
        this.forbidden = new boolean[numberOfVertices][numberOfVertices];
    }

    public void setEdge(int vertex1, int vertex2, int weight) {
        edgeWeights[vertex1][vertex2] = weight;
        edgeWeights[vertex2][vertex1] = weight;
        edgeExists[vertex1][vertex2] = weight > 0;
        edgeExists[vertex2][vertex1] = weight > 0;
    }

    public Graph copy() {
        Graph copy = new Graph(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                copy.getEdgeWeights()[i][j] = edgeWeights[i][j];
                copy.getEdgeExists()[i][j] = edgeExists[i][j];
                copy.getForbidden()[i][j] = forbidden[i][j];
            }
        }
        return copy;
    }

    public int editEdge(int i, int j) {
        edgeWeights[i][j] *= -1;
        edgeWeights[j][i] *= -1;
        edgeExists[i][j] = !edgeExists[i][j];
        edgeExists[j][i] = !edgeExists[j][i];
        return -edgeWeights[i][j];
    }

    public void setToForbidden(int i, int j) {
        forbidden[i][j] = true;
        forbidden[j][i] = true;
    }

    public void setToNotForbidden(int i, int j) {
        forbidden[i][j] = false;
        forbidden[j][i] = false;
    }

    public MergeVerticesInfo mergeVertices(int a, int b, int k) {
        MergeVerticesInfo mergeVerticesInfo = new MergeVerticesInfo(numberOfVertices, a, b, k);

        for (int i = 0; i < numberOfVertices; i++) {
            mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i] = edgeWeights[a][i];
            mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i] = edgeExists[a][i];
            mergeVerticesInfo.getForbiddenOfFirstVertex()[i] = forbidden[a][i];
            mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i] = edgeWeights[b][i];
            mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i] = edgeExists[b][i];
            mergeVerticesInfo.getForbiddenOfSecondVertex()[i] = forbidden[b][i];
        }

        for (int i = 0; i < numberOfVertices; i++) {
            if (i != a && i != b) {
                int newWeight = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i] + mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];

                if (forbidden[a][i]) {
                    mergeVerticesInfo.reduceK(Math.max(edgeWeights[b][i], 0));
                    if (newWeight > 0) {
                        newWeight *= -1;
                    }
                    edgeWeights[a][i] = newWeight;
                    edgeExists[a][i] = false;
                    edgeWeights[i][a] = newWeight;
                    edgeExists[i][a] = false;
                } else if (forbidden[b][i]) {
                    mergeVerticesInfo.reduceK(Math.max(edgeWeights[a][i], 0));
                    if (newWeight > 0) {
                        newWeight *= -1;
                    }
                    edgeWeights[a][i] = newWeight;
                    edgeExists[a][i] = false;
                    edgeWeights[i][a] = newWeight;
                    edgeExists[i][a] = false;
                    forbidden[a][i] = true;
                    forbidden[i][a] = true;
                } else {
                    if (edgeExists[a][i] != edgeExists[b][i]) {
                        mergeVerticesInfo.reduceK(Math.min(Math.abs(edgeWeights[a][i]), Math.abs(edgeWeights[b][i])));
                    }
                    edgeWeights[a][i] = newWeight;
                    edgeExists[a][i] = newWeight > 0;
                    edgeWeights[i][a] = newWeight;
                    edgeExists[i][a] = newWeight > 0;
                    forbidden[a][i] = false;
                    forbidden[i][a] = false;

                }
            }
        }

        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[i][b] = edgeWeights[i][numberOfVertices-1];
            edgeExists[i][b] = edgeExists[i][numberOfVertices-1];
            edgeWeights[b][i] = edgeWeights[numberOfVertices-1][i];
            edgeExists[b][i] = edgeExists[numberOfVertices-1][i];
        }

        numberOfVertices--;

        return mergeVerticesInfo;
    }

    public void revertMergeVertices(MergeVerticesInfo mergeVerticesInfo) {
        numberOfVertices++;
        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[numberOfVertices-1][i] = edgeWeights[mergeVerticesInfo.getSecondVertex()][i];
            edgeExists[numberOfVertices-1][i] = edgeExists[mergeVerticesInfo.getSecondVertex()][i];
            forbidden[numberOfVertices-1][i] = forbidden[mergeVerticesInfo.getSecondVertex()][i];
            edgeWeights[i][numberOfVertices-1] = edgeWeights[i][mergeVerticesInfo.getSecondVertex()];
            edgeExists[i][numberOfVertices-1] = edgeExists[i][mergeVerticesInfo.getSecondVertex()];
            forbidden[i][numberOfVertices-1] = forbidden[i][mergeVerticesInfo.getSecondVertex()];
        }

        for (int i = 0; i < numberOfVertices; i++) {
            edgeWeights[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            edgeExists[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            forbidden[mergeVerticesInfo.getFirstVertex()][i] = mergeVerticesInfo.getForbiddenOfFirstVertex()[i];
            edgeWeights[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeWeightsOfFirstVertex()[i];
            edgeExists[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getEdgeExistsOfFirstVertex()[i];
            forbidden[i][mergeVerticesInfo.getFirstVertex()] = mergeVerticesInfo.getForbiddenOfFirstVertex()[i];
            edgeWeights[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            edgeExists[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
            forbidden[mergeVerticesInfo.getSecondVertex()][i] = mergeVerticesInfo.getForbiddenOfSecondVertex()[i];
            edgeWeights[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeWeightsOfSecondVertex()[i];
            edgeExists[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getEdgeExistsOfSecondVertex()[i];
            forbidden[i][mergeVerticesInfo.getSecondVertex()] = mergeVerticesInfo.getForbiddenOfSecondVertex()[i];
        }
    }

    public int getTotalAbsoluteWeight(P3 p3) {
        return edgeWeights[p3.getU()][p3.getV()] + edgeWeights[p3.getV()][p3.getW()] - edgeWeights[p3.getU()][p3.getW()];
    }

    public P3 findP3() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                for (int k = j+1; k < numberOfVertices; k++) {
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
            for (int j = i + 1; j < numberOfVertices; j++) {
                for (int k = 0; k < numberOfVertices; k++) {
                    if (i != k && j != k && edgeExists[i][j] && edgeExists[j][k] && !edgeExists[i][k]) {
                        p3List.add(new P3(i, j, k));
                    }
                }
            }
        }
        return p3List;
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

    public boolean[][] getForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean[][] forbidden) {
        this.forbidden = forbidden;
    }
}
