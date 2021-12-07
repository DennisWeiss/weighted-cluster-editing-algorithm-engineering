package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.model.OriginalWeightsInfo;
import berlin.tu.algorithmengineering.model.P3;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class DataReduction {

    public static Stack<OriginalWeightsInfo> applyHeavyNonEdgeReduction(Graph graph) {
        Stack<OriginalWeightsInfo> originalWeights = new Stack<>();

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = i+1; j < graph.getNumberOfVertices(); j++) {
                if (graph.getEdgeWeights()[i][j] < 0 && graph.getEdgeWeights()[i][j] > Main.FORBIDDEN_VALUE
                        && -graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i]) {
                    originalWeights.push(new OriginalWeightsInfo(i, j, graph.getEdgeWeights()[i][j]));
                    graph.getEdgeWeights()[i][j] = Main.FORBIDDEN_VALUE;
                    graph.getEdgeWeights()[j][i] = Main.FORBIDDEN_VALUE;
                }
            }
        }
        return originalWeights;
    }

    public static void revertHeavyNonEdgeReduction(Graph graph, Stack<OriginalWeightsInfo> originalWeights) {
        while (!originalWeights.isEmpty()) {
            OriginalWeightsInfo originalWeightsInfo = originalWeights.pop();
            graph.getEdgeWeights()[originalWeightsInfo.getVertex1()][originalWeightsInfo.getVertex2()] = originalWeightsInfo.getOriginalWeight();
            graph.getEdgeWeights()[originalWeightsInfo.getVertex2()][originalWeightsInfo.getVertex1()] = originalWeightsInfo.getOriginalWeight();
        }
    }


    public static MergeVerticesInfo[] applyClosedNeighborhoodReductionRule(Graph graph, int u){
        int sizeOfClosedNeighborhood = 0;
        int costOfMakingClique = 0;
        int costOfCuttingOff = 0;
        int firstInNeighborhood = -1;

        //for Neighbors of u
        for (int v=0; v<graph.getNumberOfVertices(); v++){
            if( v != u && graph.getEdgeWeights()[u][v] == 0 ){
                return null;
            }
            if (v == u || graph.getEdgeExists()[u][v]) { //v in closed neighborhood of u
                if (firstInNeighborhood == -1) {
                    firstInNeighborhood = v;
                }
                sizeOfClosedNeighborhood++;

                for (int w=0; w< graph.getNumberOfVertices(); w++) {
                    if (w == u || graph.getEdgeExists()[u][w]) { //w in closed neighborhood of u
                        if( w != v && graph.getEdgeWeights()[v][w] == 0 ){
                            return null;
                        }
                        if (v != w && graph.getEdgeWeights()[v][w] < 0) {
                            costOfMakingClique += -graph.getEdgeWeights()[v][w];
                        }
                    }else{ // w not in the closed neigborhood of u
                        if (v != w && graph.getEdgeWeights()[v][w] > 0) {
                            costOfCuttingOff += graph.getEdgeWeights()[v][w];
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
        boolean[] edgeExistsOfU = Arrays.copyOf(graph.getEdgeExists()[u], graph.getNumberOfVertices());
        int j = 0;
        for (int i= graph.getNumberOfVertices() - 1; i > firstInNeighborhood; i--) {//numberOfVertices could get decreased, but only by 1
            if (u == i || edgeExistsOfU[i]) {
                //System.out.printf("\tmerge 1st,i: %d %d   %d\n", firstInNeighborhood, i, j);
                mergedVerticesInfos[j] = graph.mergeVertices(firstInNeighborhood, i);
                j++;
            }
        }
        //to undo those you would have to do them in descending order
        return mergedVerticesInfos;
    }

}
