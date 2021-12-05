package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.ArrayList;
import java.util.List;

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

    public MergeVerticesInfo[] applyClosedNeighborhoodReductionRule(int u){
        int sizeOfClosedNeighborhood = 0;
        int costOfMakingClique = 0;
        int costOfCuttingOff = 0;
        int firstInNeighborhood = -1;

        //for Neighbors of u
        for (int v=0; v<numberOfVertices; v++){
            if( v != u && edgeWeights[u][v] == 0 ){
                return null;
            }
            if (v == u || edgeExists[u][v]) { //v in closed neighborhood of u
                if (firstInNeighborhood == -1) {
                    firstInNeighborhood = v;
                }
                sizeOfClosedNeighborhood++;

                for (int w=0; w<numberOfVertices; w++) {
                    if( w != v && edgeWeights[v][w] == 0 ){
                        return null;
                    }
                    if (w == u || edgeExists[u][w]) { //w in closed neighborhood of u
                        if (v != w && edgeWeights[v][w] < 0) {
                            costOfMakingClique += -edgeWeights[v][w];
                        }
                    }else{ // w not in the closed neigborhood of u
                        if (v != w && edgeWeights[v][w] > 0) {
                            costOfCuttingOff += edgeWeights[v][w];
                        }
                    }
                }
            }
        }
        // factor 2 is already included in costOfMakingClique because of double counting
        if (sizeOfClosedNeighborhood == 1 || costOfMakingClique + costOfCuttingOff >= sizeOfClosedNeighborhood){
            return null;
        }

        // merge all in closed neighborhood
        MergeVerticesInfo[] mergedVerticesInfos = new MergeVerticesInfo[sizeOfClosedNeighborhood-1];
        int j = 0;
        for (int i=numberOfVertices - 1; i > firstInNeighborhood && j >= 0; i--) {//numberOfVertices could get decreased, but only by 1
            if (u == i || edgeExists[u][i]) {
                //System.out.printf("\tmerge 1st,i: %d %d   %d\n", firstInNeighborhood, i, j);
                mergedVerticesInfos[j] = mergeVertices(firstInNeighborhood, i);
                j++;
            }
        }
        //to undo those you would have to do them in descending order
        return mergedVerticesInfos;
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

    public boolean[][] computeEdgeExists() {
        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = 0; j < numberOfVertices; j++) {
                edgeExists[i][j] = edgeWeights[i][j] > 0;
            }
        }
        return edgeExists;
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
