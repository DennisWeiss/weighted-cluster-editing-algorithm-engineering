package berlin.tu.algorithmengineering.main;


import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numberOfVertices = scanner.nextInt();

        Graph graph = new Graph(numberOfVertices);

        for (int i = 0; i < numberOfVertices; i++) {
            for (int j = i+1; j < numberOfVertices; j++) {
                int vertex1 = scanner.nextInt();
                int vertex2 = scanner.nextInt();
                graph.getEdges()[Math.min(vertex1, vertex2) - 1][Math.max(vertex1, vertex2) - 1] = scanner.nextInt();
            }
        }

        List<Edge> edgesToEdit = ce(graph);
        for (Edge edge : edgesToEdit) {
            System.out.printf("%d %d\n", edge.getA() + 1, edge.getB() + 1);
        }
    }

    public static List<Edge> ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }
        P3 p3 = graph.findP3();
        if (p3 == null) {
            return new ArrayList<>();
        }

        int oldEdgeWeight = graph.editEdge(p3.getU(), p3.getV());
        List<Edge> edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU(), p3.getV()), Math.max(p3.getU(), p3.getV())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getV());

        oldEdgeWeight = graph.editEdge(p3.getV(), p3.getW());
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getV(), p3.getW()), Math.max(p3.getV(), p3.getW())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getV(), p3.getW());

        oldEdgeWeight = graph.editEdge(p3.getU(), p3.getW());
        edgesToEdit = ceBranch(graph, k + oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU(), p3.getW()), Math.max(p3.getU(), p3.getW())));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getW());

        return null;
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
