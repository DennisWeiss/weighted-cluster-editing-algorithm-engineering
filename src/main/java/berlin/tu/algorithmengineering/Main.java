package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;

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
        System.out.printf("#recursive steps: %d\n", recursiveSteps);
    }

    public static List<Edge> ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        recursiveSteps++;

        P3 p3 = graph.findP3();
        if (p3 == null) {
            return new ArrayList<>();
        }

        int uv = graph.getEdges()[Math.min(p3.getU(), p3.getV())][Math.max(p3.getU(), p3.getV())];
        int vw = graph.getEdges()[Math.min(p3.getV(), p3.getW())][Math.max(p3.getV(), p3.getW())];
        int uw = -graph.getEdges()[Math.min(p3.getU(), p3.getW())][Math.max(p3.getU(), p3.getW())];

        List<Edge> edgesToEdit;

        if (uv > vw && uv > uw) {
            edgesToEdit = branchOnUV(graph, k, p3);
            if (edgesToEdit != null) {
                return edgesToEdit;
            }
            if (vw > uw) {
                edgesToEdit = branchOnVW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnUW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            } else {
                edgesToEdit = branchOnUW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnVW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            }
        } else if (vw > uv && vw > uw) {
            edgesToEdit = branchOnVW(graph, k, p3);
            if (edgesToEdit != null) {
                return edgesToEdit;
            }
            if (uv > uw) {
                edgesToEdit = branchOnUV(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnUW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            } else {
                edgesToEdit = branchOnUW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnUV(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            }
        } else {
            edgesToEdit = branchOnUW(graph, k, p3);
            if (edgesToEdit != null) {
                return edgesToEdit;
            }
            if (vw > uv) {
                edgesToEdit = branchOnVW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnUV(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            } else {
                edgesToEdit = branchOnUV(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
                edgesToEdit = branchOnVW(graph, k, p3);
                if (edgesToEdit != null) {
                    return edgesToEdit;
                }
            }
        }

        return null;
    }

    private static List<Edge> branchOnUW(Graph graph, int k, P3 p3) {
        Graph editedGraph = graph.copy();
        int oldEdgeWeight = editedGraph.editEdge(p3.getU(), p3.getW());
        List<Edge> edgesToEdit = ceBranch(editedGraph, k + oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU(), p3.getW()), Math.max(p3.getU(), p3.getW())));
            return edgesToEdit;
        }
        return null;
    }

    private static List<Edge> branchOnVW(Graph graph, int k, P3 p3) {
        Graph editedGraph = graph.copy();
        int oldEdgeWeight = editedGraph.editEdge(p3.getV(), p3.getW());
        List<Edge> edgesToEdit = ceBranch(editedGraph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getV(), p3.getW()), Math.max(p3.getV(), p3.getW())));
            return edgesToEdit;
        }
        return null;
    }

    private static List<Edge> branchOnUV(Graph graph, int k, P3 p3) {
        Graph editedGraph = graph.copy();
        int oldEdgeWeight = editedGraph.editEdge(p3.getU(), p3.getV());
        List<Edge> edgesToEdit = ceBranch(editedGraph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(Math.min(p3.getU(), p3.getV()), Math.max(p3.getU(), p3.getV())));
            return edgesToEdit;
        }
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
