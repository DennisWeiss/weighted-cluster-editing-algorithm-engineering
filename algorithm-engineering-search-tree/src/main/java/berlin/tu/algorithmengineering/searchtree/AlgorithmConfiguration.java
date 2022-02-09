package berlin.tu.algorithmengineering.searchtree;

public class AlgorithmConfiguration {

    public static final double DEFAULT_PROBABILITY_TO_SPLIT_INTO_CONNECTED_COMPONENTS = 1;
    public static final double DEFAULT_PROBABILITY_TO_COMPUTE_LP_LOWER_BOUND = 1;
    public static final double DEFAULT_PROBABILITY_TO_COMPUTE_LOWER_BOUND_2 = 1;
    public static final int DEFAULT_GREEDY_HEURISTIC_ITERATIONS = 32;
    public static final int DEFAULT_SIMULATED_ANNEALING_ITERATIONS = 20_000;
    public static final double DEFAULT_T_START = 5;

    private double probabilityToSplitIntoConnectedComponents = DEFAULT_PROBABILITY_TO_SPLIT_INTO_CONNECTED_COMPONENTS;
    private double probabilityToComputeLpLowerBound = DEFAULT_PROBABILITY_TO_COMPUTE_LP_LOWER_BOUND;
    private double probabilityToComputeLowerBound2 = DEFAULT_PROBABILITY_TO_COMPUTE_LOWER_BOUND_2;
    private int greedyHeuristicIterations = DEFAULT_GREEDY_HEURISTIC_ITERATIONS;
    private int simulatedAnnealingIterations = DEFAULT_SIMULATED_ANNEALING_ITERATIONS;
    private double tStart = DEFAULT_T_START;

    public double getProbabilityToSplitIntoConnectedComponents() {
        return probabilityToSplitIntoConnectedComponents;
    }

    public void setProbabilityToSplitIntoConnectedComponents(double probabilityToSplitIntoConnectedComponents) {
        this.probabilityToSplitIntoConnectedComponents = probabilityToSplitIntoConnectedComponents;
    }

    public double getProbabilityToComputeLpLowerBound() {
        return probabilityToComputeLpLowerBound;
    }

    public void setProbabilityToComputeLpLowerBound(double probabilityToComputeLpLowerBound) {
        this.probabilityToComputeLpLowerBound = probabilityToComputeLpLowerBound;
    }

    public double getProbabilityToComputeLowerBound2() {
        return probabilityToComputeLowerBound2;
    }

    public void setProbabilityToComputeLowerBound2(double probabilityToComputeLowerBound2) {
        this.probabilityToComputeLowerBound2 = probabilityToComputeLowerBound2;
    }

    public int getGreedyHeuristicIterations() {
        return greedyHeuristicIterations;
    }

    public void setGreedyHeuristicIterations(int greedyHeuristicIterations) {
        this.greedyHeuristicIterations = greedyHeuristicIterations;
    }

    public int getSimulatedAnnealingIterations() {
        return simulatedAnnealingIterations;
    }

    public void setSimulatedAnnealingIterations(int simulatedAnnealingIterations) {
        this.simulatedAnnealingIterations = simulatedAnnealingIterations;
    }

    public double gettStart() {
        return tStart;
    }

    public void settStart(double tStart) {
        this.tStart = tStart;
    }
}
