package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.*;
import berlin.tu.algorithmengineering.util.MergeVerticesResult;
import berlin.tu.algorithmengineering.util.MergeVerticesResultBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static int recursiveSteps = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfVertices = scanner.nextInt();

        Graph graph = new Graph(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                Vertex vertex1 = graph.getVertices().get(scanner.nextInt() - 1);
                Vertex vertex2 = graph.getVertices().get(scanner.nextInt() - 1);

                int weight = scanner.nextInt();
                vertex1.addNeighbor(vertex2, weight);
                vertex2.addNeighbor(vertex1, weight);
            }
        }

        List<Edge> edgesToEdit = ce(graph);
        for (Edge edge : edgesToEdit) {
            System.out.printf("%d %d\n", edge.getA() + 1, edge.getB() + 1);
        }
        System.out.printf("#recursive steps: %d\n", recursiveSteps);
    }

    public static List<Edge> ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        recursiveSteps++;

        MergeVerticesResult mergeVerticesResult = mergeVertices(graph, k);

        k = mergeVerticesResult.getK();

        P3 p3 = graph.findP3();
        if (p3 == null) {
            return new ArrayList<>();
        }

        int oldEdgeWeight = graph.editEdge(p3.getU(), p3.getV());
        List<Edge> edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU().getId(), p3.getV().getId()), Math.max(p3.getU().getId(), p3.getV().getId())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getV());

        oldEdgeWeight = graph.editEdge(p3.getV(), p3.getW());
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getV().getId(), p3.getW().getId()), Math.max(p3.getV().getId(), p3.getW().getId())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getV(), p3.getW());

        oldEdgeWeight = graph.editEdge(p3.getU(), p3.getW());
        edgesToEdit = ceBranch(graph, k + oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU().getId(), p3.getW().getId()), Math.max(p3.getU().getId(), p3.getW().getId())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getW());

        reconstructMerge(graph, mergeVerticesResult);

        return null;
    }

    public static void reconstructMerge(Graph graph, MergeVerticesResult mergeVerticesResult) {
        Vertex m = mergeVerticesResult.getMergedVertex();
        Vertex u = m.getMergedFrom1();
        Vertex v = m.getMergedFrom2();

        for (WeightedNeighbor mw : m.getNeighbors()) {
            Vertex w = mw.getVertex();
            WeightedNeighbor wm = Graph.getWeightedNeighbor(w, m);
            w.getNeighbors().remove(wm);
            w.getNeighbors().add(new WeightedNeighbor(u, Graph.getWeightedNeighbor(u, w).getWeight()));
            w.getNeighbors().add(new WeightedNeighbor(v, Graph.getWeightedNeighbor(v, w).getWeight()));
        }

        graph.getVertices().remove(m);
        graph.getVertices().add(u);
        graph.getVertices().add(v);
    }

    public static MergeVerticesResult mergeVertices(Graph graph, int k) {
        for (Vertex u : graph.getVertices()) {
            for (WeightedNeighbor uv : u.getNeighbors()) {
                if (uv.getWeight() > k) {
                    Vertex v = uv.getVertex();
                    Vertex mergedVertex = new Vertex(u, v);
                    for (WeightedNeighbor uw : u.getNeighbors()) {
                        Vertex w = uw.getVertex();
                        WeightedNeighbor wv = Graph.getWeightedNeighbor(w, v);
                        WeightedNeighbor wu = Graph.getWeightedNeighbor(w, u);

                        if (!w.equals(v)) {
                            WeightedNeighbor mergedWeightedNeighbor = new WeightedNeighbor(mergedVertex, wu.getWeight() + wv.getWeight());
                            w.getNeighbors().remove(wu);
                            w.getNeighbors().remove(wv);
                            w.getNeighbors().add(mergedWeightedNeighbor);
                            if (Math.signum(wu.getWeight()) != Math.signum(wv.getWeight())) {
                                k -= Math.min(Math.abs(wu.getWeight()), Math.abs(wv.getWeight()));
                            }
                        }
                    }

                    graph.getVertices().remove(u);
                    graph.getVertices().remove(v);
                    graph.getVertices().add(mergedVertex);

                    ceBranch(graph, k);

                    return new MergeVerticesResultBuilder()
                            .setK(k)
                            .setMergedVertex(mergedVertex)
                            .setMergedEdgeWeight(uv.getWeight())
                            .createMergeVerticesResult();
                }
            }
        }

        return new MergeVerticesResultBuilder()
                .setK(k)
                .createMergeVerticesResult();
    }

    public static List<Edge> ce(Graph graph) {
        for (int k = 0; ; k++) {
            List<Edge> edgesToEdit = ceBranch(graph, k);
            if (edgesToEdit != null) {
                return edgesToEdit;
            }
        }
    }
}
