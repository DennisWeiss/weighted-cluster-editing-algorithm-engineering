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
import gurobi.*;

import java.util.*;

public class Main {

    public static final boolean DEBUG = false;

    public static final int FORBIDDEN_VALUE = (int) -Math.pow(2, 16);

    private static int recursiveSteps = 0;
    private static double PROBABILITY_TO_APPLY_DATA_REDUCTIONS = 0; //TODO ONLY =0 is working!
    private static double PROBABILITY_TO_SPLIT_INTO_CONNECTED_COMPONENTS = 1;
    private static double PROBABILITY_TO_COMPUTE_LP_LOWERBOUND = 1;

    public static void main(String[] args) {
        Graph graph = Utils.readGraphFromConsoleInput();

        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSize = weightedClusterEditingOptim(graph);

        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), resultEdgeExistsWithSolutionSize.getResultEdgeExists());

//        boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(graph.getEdgeExists(), new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()]);

        System.out.print(Utils.edgesToEditString(graph, edgesToEdit, DEBUG));

        System.out.printf("#cost: %d\n", resultEdgeExistsWithSolutionSize.getSolutionSize());
        System.out.printf("#output cost: %d\n", Utils.getCostToChange(graph, resultEdgeExistsWithSolutionSize.getResultEdgeExists()));

        System.out.printf("#recursive steps: %d\n", recursiveSteps);
//        System.out.printf("#recursive steps: %d\n", getLowerBound2BasedOnMaximumWeightIndependentSetIlp(graph, graph.findAllP3()));
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

        int cost = graph.flipEdgeAndSetForbidden(p3.getU(), p3.getV());
        resultEdgeExists = weightedClusterEditingBranch(graph, k + cost);
        graph.flipBackForbiddenEdge(p3.getU(), p3.getV(), cost);

        DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

        return resultEdgeExists;
    }

    public static int getLowerBound2BasedOnMaximumWeightIndependentSetIlp(Graph graph, List<P3> p3List) {
        int[] vertexWeights = new int[p3List.size()];
        boolean[][] edges = new boolean[vertexWeights.length][vertexWeights.length];

        for (int i = 0; i < vertexWeights.length; i++) {
            vertexWeights[i] = graph.getSmallestAbsoluteWeight(p3List.get(i));
        }

        for (int i = 0; i < vertexWeights.length; i++) {
            for (int j = i+1; j < vertexWeights.length; j++) {
                boolean edgeExists = graph.getNumberOfSharedVertices(p3List.get(i), p3List.get(j)) > 1;
                edges[i][j] = edgeExists;
                edges[j][i] = edgeExists;
            }
        }

        try {
            GRBEnv env = new GRBEnv(true);
            env.set(GRB.IntParam.LogToConsole, 0);
            env.start();

            GRBModel model = new GRBModel(env);

            GRBVar[] x = new GRBVar[vertexWeights.length];

            for (int i = 0; i < vertexWeights.length; i++) {
                x[i] = model.addVar(0, 1, vertexWeights[i], GRB.INTEGER, String.valueOf(i));
            }

            model.set(GRB.IntAttr.ModelSense, GRB.MAXIMIZE);

            for (int i = 0; i < vertexWeights.length; i++) {
                for (int j = 0; j < vertexWeights.length; j++) {
                    if (i != j && edges[i][j]) {
                        GRBLinExpr constraint = new GRBLinExpr();
                        constraint.addTerm(1., x[i]);
                        constraint.addTerm(1., x[j]);
                        model.addConstr(1., GRB.GREATER_EQUAL, constraint, String.format("%d %d", i, j));
                    }
                }
            }

            model.optimize();

            return (int) Math.round(model.get(GRB.DoubleAttr.ObjVal));
        } catch (GRBException e) {
            e.printStackTrace();
        }

        return 0;
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

    private static ResultEdgeExistsWithSolutionSize getUpperBound(Graph graph, int greedyIterations, int simulatedAnnealingIterations) {
        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSize = new ResultEdgeExistsWithSolutionSize(null, Integer.MAX_VALUE);

        for (int i = 0; i < greedyIterations; i++) {
            boolean[][] heuristicResultEdgeExists = Heuristics.getGreedyHeuristicNeighborhood(graph);
            int heuristicCost = Utils.getCostToChange(graph, heuristicResultEdgeExists);
            if (heuristicCost < resultEdgeExistsWithSolutionSize.getSolutionSize()) {
                resultEdgeExistsWithSolutionSize.setResultEdgeExists(heuristicResultEdgeExists);
                resultEdgeExistsWithSolutionSize.setSolutionSize(heuristicCost);
            }
        }

        if (simulatedAnnealingIterations > 0) {
            SimulatedAnnealing.performSimulatedAnnealing(
                    graph, Utils.copy(resultEdgeExistsWithSolutionSize.getResultEdgeExists(), graph.getNumberOfVertices()), simulatedAnnealingIterations, resultEdgeExistsWithSolutionSize
            );
        }

        return resultEdgeExistsWithSolutionSize;
    }

    private static ResultEdgeExistsWithSolutionSize weightedClusterEditingOptim(Graph graph) {
        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSizeOfUpperBound = getUpperBound(graph, 32, 20_000);

        return weightedClusterEditingOptim(
                graph, 0, resultEdgeExistsWithSolutionSizeOfUpperBound.getResultEdgeExists(),
                resultEdgeExistsWithSolutionSizeOfUpperBound.getSolutionSize()
        );
    }

    private static ResultEdgeExistsWithSolutionSize weightedClusterEditingOptim(Graph graph, int costToEdit,
                                                                                boolean[][] upperBoundSolutionEdgeExists,
                                                                                int upperBound) {

        if (costToEdit >= upperBound) {
            return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
        }

        //data reductions
        Stack<OriginalWeightsInfo> originalWeightsBeforeHeavyNonEdgeReduction = null;
        if (Utils.RANDOM.nextDouble() < PROBABILITY_TO_APPLY_DATA_REDUCTIONS) {
            originalWeightsBeforeHeavyNonEdgeReduction = DataReduction.applyHeavyNonEdgeReduction(graph);

            int remainingCost = upperBound - costToEdit;

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                for (int j = 0; j < graph.getNumberOfVertices(); j++) {
                    if (i != j && (graph.getEdgeWeights()[i][j] > remainingCost
                            || graph.getEdgeWeights()[i][j] + Math.abs(graph.getEdgeWeights()[i][j]) >= graph.getAbsoluteNeighborhoodWeights()[i]
                            || 3 * graph.getEdgeWeights()[i][j] >= graph.getNeighborhoodWeights()[i] + graph.getNeighborhoodWeights()[j]
                    )) {
                        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(Math.min(i, j), Math.max(i, j));
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
                        DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                        return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
                    }
                }
            }

            for (int i = 0; i < graph.getNumberOfVertices(); i++) {
                MergeVerticesInfo[] mergeVerticesInfos = DataReduction.applyClosedNeighborhoodReductionRule(graph, i);
                if (mergeVerticesInfos != null) {
                    ResultEdgeExistsWithSolutionSize solutionAfterMerge = weightedClusterEditingOptim(
                            graph, costToEdit + MergeVerticesInfo.getTotalCost(mergeVerticesInfos), upperBoundSolutionEdgeExists, upperBound
                    );

                    for (int j = mergeVerticesInfos.length - 1; j >= 0; j--) {
                        if (solutionAfterMerge.getSolutionSize() < upperBound) {
                            upperBoundSolutionEdgeExists = Utils.reconstructMergeForResultEdgeExists(
                                    solutionAfterMerge.getResultEdgeExists(), graph, mergeVerticesInfos[j]
                            );
                            upperBound = solutionAfterMerge.getSolutionSize();
                        }
                        graph.revertMergeVertices(mergeVerticesInfos[j]);
                    }

                    DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);

                    return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
                }
            }
        }

        List<P3> p3List = graph.findAllP3();

        if (p3List.isEmpty()) {
            return new ResultEdgeExistsWithSolutionSize(Utils.copy(graph.getEdgeExists(), graph.getNumberOfVertices()), costToEdit);
        }

        // call recursively for all connected components
        if (Utils.RANDOM.nextDouble() < PROBABILITY_TO_SPLIT_INTO_CONNECTED_COMPONENTS) {
            ArrayList<ArrayList<Integer>> connectedComponents = graph.getConnectedComponents();
            if (connectedComponents.size() > 1) {
                boolean[][] resultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];

                for (ArrayList<Integer> subGraphIndices : connectedComponents) {
                    // TODO might not be needed if data reductions are applied
                    if (subGraphIndices.size() == 1) {
                        continue;
                    } else if (subGraphIndices.size() == 2) {
                        resultEdgeExists[subGraphIndices.get(0)][subGraphIndices.get(1)] = true;
                        resultEdgeExists[subGraphIndices.get(1)][subGraphIndices.get(0)] = true;
                        continue;
                    }
                    Graph subGraph = graph.getSubGraphOfConnectedComponent(subGraphIndices);

                    boolean[][] parentUpperBoundEdgeExists = getEdgeExistsOfSubGraph(upperBoundSolutionEdgeExists, subGraphIndices);
                    //to test: quick:
//                int parentUpperBoundCost = upperBound - costToEdit; //reduce because the costToEdit would be added for each component otherwise
                    //to test: middle slow/quick:
                    int parentUpperBoundCost = Math.min(upperBound - costToEdit, Utils.getCostToChange(subGraph, parentUpperBoundEdgeExists));
                    //to test: slow:
                    //TODO use parameter or other heuristic: don't recalculate upper bound ALWAYS again
//                ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSizeOfUpperBound = getUpperBound(subGraph, 4, false);
//                if (resultEdgeExistsWithSolutionSizeOfUpperBound.getSolutionSize() < parentUpperBoundCost) {
//                    parentUpperBoundEdgeExists = resultEdgeExistsWithSolutionSizeOfUpperBound.getResultEdgeExists();
//                    parentUpperBoundCost = resultEdgeExistsWithSolutionSizeOfUpperBound.getSolutionSize();
//                }
                    //end tests
                    ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSizeOfSubGraph =
                            weightedClusterEditingOptim(
                                    subGraph, 0, parentUpperBoundEdgeExists, parentUpperBoundCost
                            );

                    costToEdit += resultEdgeExistsWithSolutionSizeOfSubGraph.getSolutionSize();
                    if (costToEdit >= upperBound) {
                        return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
                    }

                    for (int j = 0; j < subGraph.getNumberOfVertices(); j++) {
                        for (int k = j + 1; k < subGraph.getNumberOfVertices(); k++) {
                            if (resultEdgeExistsWithSolutionSizeOfSubGraph.getResultEdgeExists()[j][k]) {
                                resultEdgeExists[subGraphIndices.get(j)][subGraphIndices.get(k)] = true;
                                resultEdgeExists[subGraphIndices.get(k)][subGraphIndices.get(j)] = true;
                            }
                        }
                    }
                }

                return new ResultEdgeExistsWithSolutionSize(resultEdgeExists, costToEdit);
            }
        }
        recursiveSteps++;

        //LP lower bound
        if (recursiveSteps == 1 || Utils.RANDOM.nextDouble() < PROBABILITY_TO_COMPUTE_LP_LOWERBOUND) {
            double lowerBound = LpLowerBound.getLowerBoundOrTools(graph);
            if (costToEdit + lowerBound >= upperBound) {
                return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
            }
        }
        //Lower Bound 2
//        if (recursiveSteps % 2 == 0){ //TODO use level or probablity instead?
//            if (costToEdit + graph.getLowerBound2(p3List) >= upperBound) {
//                return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
//            }
//        }

        P3 p3 = getBiggestWeightP3(graph, p3List);

        //merge or delete, TODO add parameter to change/use heuristic?
        MergeVerticesInfo mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSizeUpperBoundAfterMerge = getUpperBound(graph, 8, 200);
        graph.revertMergeVertices(mergeVerticesInfo);

        int costToFlip = graph.flipEdgeAndSetForbidden(p3.getU(), p3.getV());
        ResultEdgeExistsWithSolutionSize resultEdgeExistsWithSolutionSizeUpperBoundAfterDeletion = getUpperBound(graph, 8, 200);
        graph.flipBackForbiddenEdge(p3.getU(), p3.getV(), costToFlip);

        if (resultEdgeExistsWithSolutionSizeUpperBoundAfterDeletion.getSolutionSize() + mergeVerticesInfo.getCost() < resultEdgeExistsWithSolutionSizeUpperBoundAfterMerge.getSolutionSize() - costToFlip) {
            costToFlip = graph.flipEdgeAndSetForbidden(p3.getU(), p3.getV());
            ResultEdgeExistsWithSolutionSize solutionAfterDeletion = weightedClusterEditingOptim(
                    graph, costToEdit - costToFlip, upperBoundSolutionEdgeExists, upperBound
            );
            if (solutionAfterDeletion.getSolutionSize() < upperBound) {
                upperBoundSolutionEdgeExists = solutionAfterDeletion.getResultEdgeExists();
                upperBound = solutionAfterDeletion.getSolutionSize();
            }
            graph.flipBackForbiddenEdge(p3.getU(), p3.getV(), costToFlip);

            mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
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
        } else {
            mergeVerticesInfo = graph.mergeVertices(p3.getU(), p3.getV());
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

            costToFlip = graph.flipEdgeAndSetForbidden(p3.getU(), p3.getV());
            ResultEdgeExistsWithSolutionSize solutionAfterDeletion = weightedClusterEditingOptim(
                    graph, costToEdit - costToFlip, upperBoundSolutionEdgeExists, upperBound
            );
            graph.flipBackForbiddenEdge(p3.getU(), p3.getV(), costToFlip);

            if (solutionAfterDeletion.getSolutionSize() < upperBound) {
                upperBoundSolutionEdgeExists = solutionAfterDeletion.getResultEdgeExists();
                upperBound = solutionAfterDeletion.getSolutionSize();
            }
        }

        if (originalWeightsBeforeHeavyNonEdgeReduction != null) {
            DataReduction.revertHeavyNonEdgeReduction(graph, originalWeightsBeforeHeavyNonEdgeReduction);
        }

        return new ResultEdgeExistsWithSolutionSize(upperBoundSolutionEdgeExists, upperBound);
    }

    private static boolean[][] getEdgeExistsOfSubGraph(boolean[][] parentEdgeExists, ArrayList<Integer> subGraphIndices) {
        boolean[][] edgeExists = new boolean[subGraphIndices.size()][subGraphIndices.size()];
        for (int i = 0; i < subGraphIndices.size(); i++) {
            for (int j = 0; j < subGraphIndices.size(); j++) {
                edgeExists[i][j] = parentEdgeExists[subGraphIndices.get(i)][subGraphIndices.get(j)];
            }
        }
        return edgeExists;
    }
}
