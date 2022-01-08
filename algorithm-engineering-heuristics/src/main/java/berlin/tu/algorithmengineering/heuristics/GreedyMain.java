package berlin.tu.algorithmengineering.heuristics;


import berlin.tu.algorithmengineering.common.Graph;
import berlin.tu.algorithmengineering.common.Utils;
import org.ejml.data.Complex64F;
import org.ejml.data.Eigenpair;
import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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


    public static void main(String[] args) {
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
        SimpleMatrix simpleMatrix = spectralClustering.getGraphLaplacian();
        //List<Complex64F> list = spectralClustering.getEigenValues();
        List<Eigenpair> listEigenpairs = spectralClustering.getEigenDecomposition();

        SimpleMatrix myH = spectralClustering.buildSimpleMatrix((listEigenpairs));

        int k = spectralClustering.bestClusterSizeK(listEigenpairs);

        DataSet data = new DataSet(myH);

        // Cluster
        kmeans(data, k);

        // Output into a csv
        data.createCsvOutput("files/sampleClustered.csv");
    }
}
