package berlin.tu.algorithmengineering;


import berlin.tu.algorithmengineering.model.Edge;
import berlin.tu.algorithmengineering.model.P3;
import berlin.tu.algorithmengineering.model.Vertex;
import berlin.tu.algorithmengineering.model.WeightedNeighbor;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static int recursiveSteps = 0;
    private static int solk = 0;
    private static Set<Edge> forbiddenEdges;

    public static void main(String[] args) {
        forbiddenEdges = new HashSet<>();

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
        System.out.printf("#recursive steps: %d\n", recursiveSteps);//recursiveSteps
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
                    int edgeWeight = adjustToMergedVertices(graph, u, v, mergedVertex);
                    k -= edgeWeight;

                    edgesToEdit = ceBranch(graph, k);

                    reconstructMerge(graph, mergedVertex);

                    if (edgesToEdit != null) {
                        edgesToEdit = reconstructMergeForEdgesToEditList(mergedVertex, edgesToEdit);
                    }
                    return edgesToEdit;
                }
            }
        }

        List<P3> p3List = graph.findAllP3();
        if (p3List.isEmpty()) {
            return new ArrayList<>();
        }

        P3 p3 = getBiggestWeightP3(p3List);

        //delete edge uv
        int oldEdgeWeight = graph.editEdge(p3.getU(), p3.getV());
        //mark as forbidden
        Edge uvEdge = new Edge(p3.getU(), p3.getV());
        forbiddenEdges.add(uvEdge);
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        graph.editEdge(p3.getU(), p3.getV());
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(p3.getU(), p3.getV()));
            return edgesToEdit;
        }
        forbiddenEdges.remove(uvEdge);

        //merge u and v
        Vertex mergedVertex = new Vertex(p3.getU(), p3.getV());
        int edgeWeight = adjustToMergedVertices(graph, p3.getU(), p3.getV(), mergedVertex);
        edgesToEdit = ceBranch(graph, k - edgeWeight);
        reconstructMerge(graph, mergedVertex);
        if (edgesToEdit != null) {
            edgesToEdit = reconstructMergeForEdgesToEditList(mergedVertex, edgesToEdit);
            return edgesToEdit;
        }

        return null;
    }

    private static int adjustToMergedVertices(Graph graph, Vertex u, Vertex v, Vertex mergedVertex) {
        int costs = 0;
        for (WeightedNeighbor uw : u.getNeighbors().values()) {
            Vertex w = uw.getVertex();
            if (!w.equals(v)) {
                WeightedNeighbor vw = Graph.getWeightedNeighbor(v,w);

                int newWeight = uw.getWeight() + vw.getWeight();
                Edge uwEdge = new Edge(u, w);
                Edge vwEdge = new Edge(v, w);
                if (forbiddenEdges.stream().anyMatch(edge -> edge.equals(uwEdge))) {
                    forbiddenEdges.add(new Edge(w, mergedVertex));
                    if (newWeight > 0) {
                        newWeight *= -1;
                    }
                    if (vw.isEdgeExists()) {
                        costs += vw.getWeight();
                    }
                } else if (forbiddenEdges.stream().anyMatch(edge -> edge.equals(vwEdge))) {
                    forbiddenEdges.add(new Edge(w, mergedVertex));
                    if (newWeight > 0) {
                        newWeight *= -1;
                    }
                    if (uw.isEdgeExists()) {
                        costs += uw.getWeight();
                    }
                } else {
                    if (uw.isEdgeExists() != vw.isEdgeExists()) {
                        costs += Math.min(Math.abs(uw.getWeight()), Math.abs(vw.getWeight()));
                    }
                }

                mergedVertex.addNeighbor(w, newWeight);
                w.getNeighbors().remove(u);
                w.getNeighbors().remove(v);
                w.addNeighbor(mergedVertex, newWeight);
            }
        }
        graph.getVertices().remove(u);
        graph.getVertices().remove(v);
        graph.getVertices().add(mergedVertex);
        return costs;
    }

    private static P3 getBiggestWeightP3(List<P3> p3List) {
        int biggestWeight = Integer.MIN_VALUE;
        P3 biggestWeightP3 = null;
        for (P3 p3 : p3List) {
            int totalAbsoluteWeight = p3.getTotalAbsoluteWeight();
            if (totalAbsoluteWeight > biggestWeight) {
                biggestWeightP3 = p3;
                biggestWeight = totalAbsoluteWeight;
            }
        }
        return biggestWeightP3;
    }

    private static List<Edge> reconstructMergeForEdgesToEditList(Vertex m, List<Edge> edgesToEdit) {
        Vertex u = m.getMergedFrom1();
        Vertex v = m.getMergedFrom2();
        for (WeightedNeighbor uw : u.getNeighbors().values()) {
            Vertex w = uw.getVertex();
            if (!w.equals(v)) {
                Edge mwEdge = new Edge(m, w);
                WeightedNeighbor mw = Graph.getWeightedNeighbor(m, w);
                boolean mwToEdit = edgesToEdit.stream().anyMatch(edge -> edge.equals(mwEdge));
                if (uw.isEdgeExists() != (mw.isEdgeExists() != mwToEdit)) {
                    edgesToEdit.add(new Edge(u, w));
                }
            }
        }
        for (WeightedNeighbor vw : v.getNeighbors().values()) {
            Vertex w = vw.getVertex();
            if (!w.equals(u)) {
                Edge mwEdge = new Edge(m, w);
                WeightedNeighbor mw = Graph.getWeightedNeighbor(m, w);
                boolean mwToEdit = edgesToEdit.stream().anyMatch(edge -> edge.equals(mwEdge));
                if (vw.isEdgeExists() != (mw.isEdgeExists() != mwToEdit)) {
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
        forbiddenEdges = forbiddenEdges.stream().filter(edge -> !edge.getA().equals(m) && !edge.getB().equals(m)).collect(Collectors.toSet());
    }

    public static List<Edge> ce(Graph graph) {
        for (int k = 0; ; k++) {
            List<Edge> edgesToEdit = ceBranch(graph, k);

            if (edgesToEdit != null) {
                solk = k;
                return edgesToEdit;
            }
        }
    }
}
