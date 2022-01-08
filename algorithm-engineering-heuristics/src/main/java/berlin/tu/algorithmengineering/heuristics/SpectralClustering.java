package berlin.tu.algorithmengineering.heuristics;

import org.ejml.interfaces.decomposition.EigenDecomposition;
import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.Eigenpair;
import org.ejml.factory.DecompositionFactory;
import org.ejml.simple.SimpleMatrix;

import java.util.*;


public class SpectralClustering {

    SimpleMatrix adjacencyMatrix;

    SpectralClustering(int[][] adjacencyMatrix){
        double [][] temp = new double[adjacencyMatrix.length][adjacencyMatrix.length];
        for(int i = 0; i < adjacencyMatrix.length; i++){
            for(int j = 0; j < adjacencyMatrix.length; j++){
                if(adjacencyMatrix[i][j] > 0){
                    temp[i][j] = adjacencyMatrix[i][j];
                } else {
                    temp[i][j] = 0;
                }
            }
        }
        this.adjacencyMatrix = new SimpleMatrix(temp);
    }

    public List<Eigenpair> getEigenDecomposition() {
        SimpleMatrix graphLaplacian = getGraphLaplacian();

        EigenDecomposition<DenseMatrix64F> eigenValueDecomposition = DecompositionFactory.eig(
                graphLaplacian.getMatrix().getNumRows(), true
        );
        eigenValueDecomposition.decompose(graphLaplacian.getMatrix());

        List<Eigenpair> eigenPairs = new ArrayList<>();

        for (int i = 0; i < eigenValueDecomposition.getNumberOfEigenvalues(); i++) {
            eigenPairs.add(new Eigenpair(
                    eigenValueDecomposition.getEigenvalue(i).getReal(), eigenValueDecomposition.getEigenVector(i)
            ));
        }

        eigenPairs.sort((a, b) -> (int) Math.signum(a.value - b.value));

        return eigenPairs;
    }

    public List<Complex64F> getEigenValues() {
        SimpleMatrix graphLaplacian = getGraphLaplacian();

        EigenDecomposition<DenseMatrix64F> eigenValueDecomposition = DecompositionFactory.eig(
                graphLaplacian.getMatrix().getNumRows(), true
        );
        eigenValueDecomposition.decompose(graphLaplacian.getMatrix());

        List<Complex64F> eigenValues = new ArrayList<>();

        for (int i = 0; i < eigenValueDecomposition.getNumberOfEigenvalues(); i++) {
            eigenValues.add(eigenValueDecomposition.getEigenvalue(i));
        }

        return eigenValues;
    }

    public SimpleMatrix getGraphLaplacian() {
        SimpleMatrix graphLaplacian = degreeMatrix(adjacencyMatrix).minus(adjacencyMatrix);
        return graphLaplacian;
    }

    public SimpleMatrix degreeMatrix(SimpleMatrix adjacencyMatrix) {
        SimpleMatrix degreeMatrix = new SimpleMatrix(
                adjacencyMatrix.getMatrix().getNumRows(), adjacencyMatrix.getMatrix().getNumCols()
        );
        for (int i = 0; i < adjacencyMatrix.getMatrix().getNumRows(); i++) {
            int degree = 0;
            for (int j = 0; j < adjacencyMatrix.getMatrix().getNumCols(); j++) {
                degree += adjacencyMatrix.get(i, j);
            }
            degreeMatrix.set(i, i, degree);
        }
        return degreeMatrix;
    }

    public SimpleMatrix buildSimpleMatrix(List<Eigenpair> listEigenpairs, int k) {
        int n = listEigenpairs.size();
        double[][] matrixH = new double[n][n];
        int m = 0;

        loop:
        for (Eigenpair element : listEigenpairs) {
            matrixH[m] = element.vector.data;
            m++;
        }

        double[][] result = new double[listEigenpairs.size()][k];
        for(int i = 0; i < k; i++){
            for(int j = 0; j < listEigenpairs.size(); j++){
                result[j][i] = matrixH[i][j];
            }
        }


        SimpleMatrix ms = new SimpleMatrix(result);
        return ms;
    }

    public int bestClusterSizeK(List<Eigenpair> listEigenpairs) {
        double maxDelta = 0;
        int k = 1;
        for(int i = 0; i < listEigenpairs.size()-1; i++){
            double delta = Math.abs(listEigenpairs.get(i).value - listEigenpairs.get(i+1).value);
            if(maxDelta < delta){
                k = i+1;
                maxDelta = delta;
            }
        }

        return k;
    }

    public HashMap<Integer, ArrayList<Integer>> getCluster(DataSet data) {
        LinkedList<Record> myRecords = data.getRecords();
        HashMap<Integer, ArrayList<Integer>> result = new HashMap();
        for(Record record: myRecords){
            if(!result.containsKey(record.getClusterNo())){
                result.put(record.getClusterNo(), new ArrayList<>());
            }
            result.get(record.getClusterNo()).add(record.getVertexId());
        }

        return result;
    }
}
