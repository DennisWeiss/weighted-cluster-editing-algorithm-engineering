package berlin.tu.algorithmengineering.searchtree;


import berlin.tu.algorithmengineering.common.DataReduction;
import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import berlin.tu.algorithmengineering.common.model.MergeVerticesInfo;
import berlin.tu.algorithmengineering.common.model.OriginalWeightsInfo;
import berlin.tu.algorithmengineering.common.model.P3;
import berlin.tu.algorithmengineering.common.model.ResultEdgeExistsWithSolutionSize;
import berlin.tu.algorithmengineering.heuristics.Heuristics;
import berlin.tu.algorithmengineering.heuristics.SimulatedAnnealing;
import berlin.tu.algorithmengineering.searchtree.lp.LpLowerBound;

import java.util.List;
import java.util.Stack;

public class Main {

    public static final boolean DEBUG = false;

    public static final int FORBIDDEN_VALUE = (int) -Math.pow(2, 16);

    private static int recursiveSteps = 0;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSize = weightedClusterEditingOptim(graph);

        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExistsWithSolutionSize.getResultEdgeExists());

        System.out.print(Utils.edgesToEditString(graph, edgesToEdit, DEBUG));

        System.out.printf("#cost: %d\n", resultEdgeExistsWithSolutionSize.getSolutionSize());
        System.out.printf("#output cost: %d\n", Utils.getCostToChange(graph, resultEdgeExistsWithSolutionSize.getResultEdgeExists()));
        System.out.printf("#recursive steps: %d\n", recursiveSteps);
    }

    private static boolean[][] weightedClusterEditingBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        List<P3> p3List = graph.findAllP3();

        if (p3List.isEmpty()) {
            return Utils.copy(graph.getEdgeExists(), graph.getNumberOfVertices());
        }

        if (graph.getLowerBound2(p3List) > k) {
            return null;
        }

        Stack<OriginalWeightsInfo> originalWeightsBeforeHeavyNonEdgeReduction = DataReduction.applyHeavyNonEdgeReduction(graph);

        P3 p3 = getBiggestWeightP3(graph, p3List);

        boolean[][] resultEdgeExists;

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                if (i != j && (graph.getEdgeWeights()[i][j] > k
                        || graph.getEdgeWeights()[i][j] + Math.abs(graph.getEdgeWeights()[i][j]) >= graph.getAbsoluteNeighborhoodWeights()[i]
                        || 3 * graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i] + graph.getNeighborhoodWeights()[j]
                )) {
                    MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(Math.min(i, j), Math.max(i, j));
                    resultEdgeExists = weightedClusterEditingBranch(graph, k - mergeVerticesInfo.getCost());

                    if (resultEdgeExists != null) {
                        resultEdgeExists = Utils.reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
                    }
                    graph.revertMergeVertices(mergeVerticesInfo);
                    DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                    return resultEdgeExists;
                }
            }
        }

        for (int i = 0; i < graph.getNumberOfVertices(); i++) {
            MergeVerticesInfo[] mergeVerticesInfos = DataReduction.applyClosedNeighborhoodReductionRule(graph, i);
            if (mergeVerticesInfos != null) {
                resultEdgeExists = weightedClusterEditingBranch(graph, k - MergeVerticesInfo.getTotalCost(mergeVerticesInfos));

                for (int j = mergeVerticesInfos.length - 1; j >= 0; j--) {
                    if (resultEdgeExists != null) {
                        resultEdgeExists = Utils.reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfos[j]);
                    }
                    graph.revertMergeVertices(mergeVerticesInfos[j]);
                }

                DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                return resultEdgeExists;
            }
        }

        recursiveSteps++;

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
        resultEdgeExists = weightedClusterEditingBranch(graph, k - mergeVerticesInfo.getCost());
        if (resultEdgeExists != null) {
            resultEdgeExists = Utils.reconstructMergeForResultEdgeExists(resultEdgeExists, graph, mergeVerticesInfo);
        }
        graph.revertMergeVertices(mergeVerticesInfo);

        if (resultEdgeExists != null) {
            DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);
            return resultEdgeExists;
        }

        graph.flipEdge(p3.getU(), p3.getV());
        int cost = graph.getEdgeWeights()[p3.getU()][p3.getV()];
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = FORBIDDEN_VALUE;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = FORBIDDEN_VALUE;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] += -FORBIDDEN_VALUE + cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] += -FORBIDDEN_VALUE + cost;
        resultEdgeExists = weightedClusterEditingBranch(graph, k + cost);
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = cost;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] -= -FORBIDDEN_VALUE + cost;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] -= -FORBIDDEN_VALUE + cost;
        graph.flipEdge(p3.getU(), p3.getV());

        DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

        return resultEdgeExists;
    }

    private static P3 getBiggestWeightP3(Graph graph, List<P3> p3List) {
        int biggestWeight = Integer.MIN_VALUE;
        P3 biggestWeightP3 = null;
        for (P3 p3 : p3List) {
            int totalAbsoluteWeight = graph.getTotalAbsoluteWeight(p3);
            if (totalAbsoluteWeight > biggestWeight) {
                biggestWeightP3 = p3;
                biggestWeight = totalAbsoluteWeight;
            }
        }
        return biggestWeightP3;
    }

    public static boolean[][] weightedClusterEditing(Graph graph) {
        Graph graphCopy = null;
        if (DEBUG) {
            graphCopy = graph.copy();
        }
        for (int k = 793; ; k++) {
            recursiveSteps++;
            boolean[][] resultEdgeExists = weightedClusterEditingBranch(graph, k);
            if (DEBUG) {
                boolean correct = true;
                for (int i = 0; i < graphCopy.getNumberOfVertices(); i++) {
                    for (int j = 0; j < graphCopy.getNumberOfVertices(); j++) {
                        if (i != j && graph.getEdgeWeights()[i][j] != graphCopy.getEdgeWeights()[i][j]) {
                            System.out.printf("weights %d and %d not equal at [%d, %d] with k = %d\n", graph.getEdgeWeights()[i][j], graphCopy.getEdgeWeights()[i][j], i, j, k);
                            correct = false;
                        }
                        if (i != j && graph.getEdgeExists()[i][j] != graphCopy.getEdgeExists()[i][j]) {
                            System.out.printf("edgeExists not equal at [%d, %d] with k = %d\n", i, j, k);
                            correct = false;
                        }
                    }
                }
                if (correct) {
                    System.out.printf("k = %d is correct\n", k);
                }
            }

            if (resultEdgeExists != null) {
                if (DEBUG) System.out.printf("last k = %d\n", k);
                return resultEdgeExists;
            }
        }
    }

    public static boolean[][] weightedClusterEditingBinarySearchInitial(Graph graph) {
        final double FACTOR = 1.2;

        recursiveSteps++;
        boolean[][] resultEdgeExists = weightedClusterEditingBranch(graph, 0);
        if (resultEdgeExists == null) {
            int lo = 1;
            int hi = 1;
            for (int k = 1; ; k = (int) Math.ceil(FACTOR * k)) {
                hi = k;
                recursiveSteps++;
                resultEdgeExists = weightedClusterEditingBranch(graph, k);
                if (resultEdgeExists != null) {
                    resultEdgeExists = weightedClusterEditingBinarySearch(graph, lo, hi);
                    break;
                }
                lo = k;
            }
        }
        return resultEdgeExists;
    }

    private static boolean[][] weightedClusterEditingBinarySearch(Graph graph, int lo, int hi) {
        if (lo == hi) {
            if (DEBUG) {
                System.out.printf("last k = %d\n", lo);
            }
            recursiveSteps++;
            return weightedClusterEditingBranch(graph, lo);
        }
        int k = (lo + hi) / 2;
        recursiveSteps++;
        boolean[][] resultEdgeExists = weightedClusterEditingBranch(graph, k);
        if (resultEdgeExists != null) {
            if (lo == k) {
                if (DEBUG) {
                    System.out.printf("last k = %d\n", k);
                }
                return resultEdgeExists;
            }
            return weightedClusterEditingBinarySearch(graph, lo, k);
        }
        return weightedClusterEditingBinarySearch(graph, k + 1, hi);
    }

    private static ResultEdgeExistsWithSolutionSize weightedClusterEditingOptim(Graph graph) {
        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSize = new ResultEdgeExistsWithSolutionSize(null, Integer.MAX_VALUE);

        for (int i = 0; i < 64; i++) {
            boolean[][] heuristicResultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph.copy());
            int heuristicCost = Utils.getCostToChange(graph, heuristicResultEdgeExists);
            if (heuristicCost < resultEdgeExistsWithSolutionSize.getSolutionSize()) {
                resultEdgeExistsWithSolutionSize.setResultEdgeExists(heuristicResultEdgeExists);
                resultEdgeExistsWithSolutionSize.setSolutionSize(heuristicCost);
            }
        }

        SimulatedAnnealing.performSimulatedAnnealing(
                graph, Utils.copy(resultEdgeExistsWithSolutionSize.getResultEdgeExists(), graph.getNumberOfVertices()), 20_000, resultEdgeExistsWithSolutionSize
        );

        return weightedClusterEditingOptim(
                graph, 0, resultEdgeExistsWithSolutionSize.getResultEdgeExists(), resultEdgeExistsWithSolutionSize.getSolutionSize()
        );
    }

    private static ResultEdgeExistsWithSolutionSize weightedClusterEditingOptim(Graph graph, int costToEdit,
                                                                                boolean[][] upperBoundSolutionEdgeExists,
                                                                                int upperBound) {
        recursiveSteps++;

        List<P3> p3List = graph.findAllP3();

        if (costToEdit >= upperBound || costToEdit + graph.getLowerBound2(p3List) >= upperBound) {
            return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
        }

//        P3 p3 = graph.findP3();

        if (p3List.isEmpty()) {
            return new ResultEdgeExistsWithSolutionSize(Utils.copy(graph.getEdgeExists(), graph.getNumberOfVertices()), costToEdit);
        }

        P3 p3 = getBiggestWeightP3(graph, p3List);

        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
        ResultEdgeExistsWithSolutionSize solutionAfterMerge = weightedClusterEditingOptim(
                graph, costToEdit + mergeVerticesInfo.getCost(), upperBoundSolutionEdgeExists, upperBound
        );
        if (solutionAfterMerge.getSolutionSize() < upperBound) {
            upperBoundSolutionEdgeExists = Utils.reconstructMergeForResultEdgeExists(
                    solutionAfterMerge.getResultEdgeExists(), graph, mergeVerticesInfo
            );
            upperBound = solutionAfterMerge.getSolutionSize();
        }
        graph.revertMergeVertices(mergeVerticesInfo);

        graph.flipEdge(p3.getU(), p3.getV());
        int costToFlip = graph.getEdgeWeights()[p3.getU()][p3.getV()];
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = FORBIDDEN_VALUE;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = FORBIDDEN_VALUE;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] += -FORBIDDEN_VALUE + costToFlip;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] += -FORBIDDEN_VALUE + costToFlip;
        ResultEdgeExistsWithSolutionSize solutionAfterDeletion = weightedClusterEditingOptim(
                graph, costToEdit - costToFlip, upperBoundSolutionEdgeExists, upperBound
        );
        graph.getEdgeWeights()[p3.getU()][p3.getV()] = costToFlip;
        graph.getEdgeWeights()[p3.getV()][p3.getU()] = costToFlip;
        graph.getAbsoluteNeighborhoodWeights()[p3.getU()] -= -FORBIDDEN_VALUE + costToFlip;
        graph.getAbsoluteNeighborhoodWeights()[p3.getV()] -= -FORBIDDEN_VALUE + costToFlip;
        graph.flipEdge(p3.getU(), p3.getV());

        if (solutionAfterDeletion.getSolutionSize() < upperBound) {
            upperBoundSolutionEdgeExists = solutionAfterDeletion.getResultEdgeExists();
            upperBound = solutionAfterDeletion.getSolutionSize();
        }

        return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
    }
}
