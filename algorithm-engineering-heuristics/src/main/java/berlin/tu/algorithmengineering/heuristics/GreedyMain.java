package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import org.ejml.data.Complex64F;
import org.ejml.data.Eigenpair;
import org.ejml.simple.SimpleMatrix;

import java.io.IOException;
import java.util.*;

public class GreedyMain {

    public static final boolean DEBUG = false;

    public static final int MIN_CUT_COMPUTATION_TIMEOUT = 5;

    private static boolean[][] originalEdgeExists;
    public static boolean[][] bestResultEdgeExists;
    public static int bestCost;

    // Higher precision means earlier termination
    // and higher error
    static final Double PRECISION = 0.0;

    /* K-Means++ implementation, initializes K centroids from data */
    static LinkedList<HashMap<String, Double>> kmeanspp(DataSet data, int K) {
        LinkedList<HashMap<String,Double>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet()); // Abschnittener H Vektor

        for(int i=1; i<K; i++){
            centroids.add(data.calculateWeighedCentroid());
        }

        return centroids;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
     * to records in the dataset */
    static void kmeans(DataSet data, int K){
        // Select K initial centroids
        LinkedList<HashMap<String,Double>> centroids = kmeanspp(data, K);

        // Initialize Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;

        while (true) {

            // Assign observations to centroids
            var records = data.getRecords();

            // For each record
            for(var record : records){
                Double minDist = Double.MAX_VALUE;
                // Find the centroid at a minimum distance from it and add the record to its cluster
                for(int i = 0; i < centroids.size(); i++){
                    Double dist = DataSet.euclideanDistance(centroids.get(i), record.getRecord());
                    if(dist < minDist){
                        minDist = dist;
                        record.setClusterNo(i);
                    }
                }
            }

            // Recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // Exit condition, SSE changed less than PRECISION parameter
            Double newSSE = data.calculateTotalSSE(centroids);
            if(SSE-newSSE <= PRECISION){
                break;
            }
            SSE = newSSE;
        }
    }

    private static void applyChange(boolean[][] edgeExists, int vertex, int moveToVertex) {
        for (int i = 0; i < edgeExists.length; i++) {
            edgeExists[vertex][i] = false;
            edgeExists[i][vertex] = false;
        }
        if (vertex == moveToVertex) {
            return;
        }
        for (int i = 0; i < edgeExists.length; i++) {
            if (edgeExists[moveToVertex][i]) {
                edgeExists[vertex][i] = true;
                edgeExists[i][vertex] = true;
            }
        }
        edgeExists[vertex][moveToVertex] = true;
        edgeExists[moveToVertex][vertex] = true;
    }


    public static void main(String[] args) throws IOException {
        Graph graph = Utils.readGraphFromConsoleInput();
        originalEdgeExists = graph.getEdgeExists();
        //initialize with no edges, that is also a solution
        bestResultEdgeExists = new boolean[graph.getNumberOfVertices()][graph.getNumberOfVertices()];
        bestCost = Utils.getCostToChange(graph, bestResultEdgeExists);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            //called on SIGINT or normal finishes
            @Override
            public void run() {
                boolean[][] edgesToEdit = Utils.getEdgesToEditFromResultEdgeExists(originalEdgeExists, bestResultEdgeExists);

                Utils.printEdgesToEdit(graph, edgesToEdit, DEBUG);
                System.out.printf("#recursive steps: %d, %f\n", Heuristics.optimumScore, Heuristics.optimumP);
            }
        });


        SpectralClustering spectralClustering = new SpectralClustering(graph.copy().getEdgeWeights());
        List<Eigenpair> listEigenpairs = spectralClustering.getEigenDecomposition();


        int k = spectralClustering.bestClusterSizeK(listEigenpairs);
        SimpleMatrix myH = spectralClustering.buildSimpleMatrix(listEigenpairs, k);

        DataSet data = new DataSet(myH);

        // Call k means
        kmeans(data, k);

        // build clique cased on clustering
        HashMap<Integer, ArrayList<Integer>> myCluster = spectralClustering.getCluster(data);
        for (Map.Entry<Integer, ArrayList<Integer>> entry : myCluster.entrySet()) {
            ArrayList<Integer> value = entry.getValue();
            for(int i = 1; i < value.size(); i++){
                applyChange(graph.getEdgeExists(), value.get(i), value.get(0));
            }
        }
    }
}
