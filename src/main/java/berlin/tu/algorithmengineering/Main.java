package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
            System.out.printf("%d %d\n", edge.getA().getId() + 1, edge.getB().getId() + 1);
        }
        System.out.printf("#recursive steps: %d\n", recursiveSteps);
    }

    public static List<Edge> ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        recursiveSteps++;

        List<Edge> edgesToEdit;

        for (Vertex u : graph.getVertices()) {
            for (WeightedNeighbor uv : u.getNeighbors().values()) {
                if (uv.getWeight() > k) {
                    Vertex v = uv.getVertex();
                    Vertex mergedVertex = new Vertex(u, v);
                    for (WeightedNeighbor uw : u.getNeighbors().values()) {
                        Vertex w = uw.getVertex();
                        if (!w.equals(v)) {
                            WeightedNeighbor wv = Graph.getWeightedNeighbor(w, v);
                            WeightedNeighbor wu = Graph.getWeightedNeighbor(w, u);

                            int newWeight = wu.getWeight() + wv.getWeight();
                            WeightedNeighbor mergedWeightedNeighbor = new WeightedNeighbor(mergedVertex, newWeight);
                            mergedVertex.addNeighbor(w, newWeight);
                            w.getNeighbors().remove(u);
                            w.getNeighbors().remove(v);
                            w.getNeighbors().put(mergedWeightedNeighbor.getVertex(), mergedWeightedNeighbor);
                            if (Math.signum(wu.getWeight()) != Math.signum(wv.getWeight())) {
                                k -= Math.min(Math.abs(wu.getWeight()), Math.abs(wv.getWeight()));
                            }
                        }
                    }

                    graph.getVertices().remove(u);
                    graph.getVertices().remove(v);
                    graph.getVertices().add(mergedVertex);

                    edgesToEdit = ceBranch(graph, k);

                    reconstructMerge(graph, mergedVertex);

                    if (edgesToEdit != null) {
                        edgesToEdit = reconstructMergeForEdgesToEditList(mergedVertex, edgesToEdit);
                    }
                    return edgesToEdit;
                }
            }
        }

        P3 p3 = graph.findP3();
        if (p3 == null) {
            return new ArrayList<>();
        }

        int oldEdgeWeight = graph.editEdge(p3.getU(), p3.getV());
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(p3.getU(), p3.getV()));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getV());

        oldEdgeWeight = graph.editEdge(p3.getV(), p3.getW());
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(p3.getV(), p3.getW()));
            return edgesToEdit;
        }
        graph.editEdge(p3.getV(), p3.getW());

        oldEdgeWeight = graph.editEdge(p3.getU(), p3.getW());
        edgesToEdit = ceBranch(graph, k + oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(p3.getU(), p3.getW()));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getW());

        return null;
    }

    private static List<Edge> reconstructMergeForEdgesToEditList(Vertex m, List<Edge> edgesToEdit) {
        Vertex u = m.getMergedFrom1();
        Vertex v = m.getMergedFrom2();
        for (WeightedNeighbor uw : u.getNeighbors().values()) {
            Vertex w = uw.getVertex();
            if (!w.equals(v)) {
                Edge mwEdge = new Edge(m, w);
                WeightedNeighbor weightedNeighbor = Graph.getWeightedNeighbor(m, w);
                if (uw.isEdgeExists() != (weightedNeighbor.isEdgeExists() != edgesToEdit.stream().anyMatch(edge -> edge.equals(mwEdge)))) {
                    edgesToEdit.add(new Edge(u, w));
                }
            }
        }
        for (WeightedNeighbor vw : v.getNeighbors().values()) {
            Vertex w = vw.getVertex();
            if (!w.equals(u)) {
                Edge mwEdge = new Edge(m, w);
                if (vw.isEdgeExists() != (Graph.getWeightedNeighbor(m, w).isEdgeExists() != edgesToEdit.stream().anyMatch(edge -> edge.equals(mwEdge)))) {
                    edgesToEdit.add(new Edge(v, w));
                }
            }
        }
        edgesToEdit = edgesToEdit
                .stream()
                .filter(edge -> !edge.getA().equals(m) && !edge.getB().equals(m))
                .collect(Collectors.toList());
        return edgesToEdit;
    }

    public static void reconstructMerge(Graph graph, Vertex m) {
        Vertex u = m.getMergedFrom1();
        Vertex v = m.getMergedFrom2();

        for (WeightedNeighbor mw : m.getNeighbors().values()) {
            Vertex w = mw.getVertex();
            w.getNeighbors().remove(m);
            w.addNeighbor(u, Graph.getWeightedNeighbor(u, w).getWeight());
            w.addNeighbor(v, Graph.getWeightedNeighbor(v, w).getWeight());
        }

        graph.getVertices().remove(m);
        graph.getVertices().add(u);
        graph.getVertices().add(v);
    }

    public static List<Edge> ce(Graph graph) {
        for (int k = 7; ; k++) {
            List<Edge> edgesToEdit = ceBranch(graph, k);

            if (edgesToEdit != null) {
                return edgesToEdit;
            }
        }
    }
}
