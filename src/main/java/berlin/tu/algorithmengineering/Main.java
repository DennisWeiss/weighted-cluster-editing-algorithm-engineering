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
    private static Set forbiddenEdges;

    public static void main(String[] args) {
        forbiddenEdges = new HashSet();

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
        System.out.printf("#recursive steps: %d\n", solk);//TODO recursiveSteps
    }

    public static List<Edge> ceBranch(Graph graph, int k) {
        if (k < 0) {
            return null;
        }

        recursiveSteps++;

        List<Edge> edgesToEdit;

        for (Vertex u : graph.getVertices()) {
            for (WeightedNeighbor uv : u.getNeighbors()) {
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

        P3 p3 = graph.findP3();
        if (p3 == null) {
            return new ArrayList<>();
        }

        //delete edge uv
        int oldEdgeWeight = graph.editEdge(p3.getU(), p3.getV());
        //todo mark as forbidden
        Edge uvEdge = new Edge(p3.getU(), p3.getV());
        forbiddenEdges.add(uvEdge);
        edgesToEdit = ceBranch(graph, k - oldEdgeWeight);
        if (edgesToEdit != null) {
            edgesToEdit.add(new Edge(p3.getU(), p3.getV()));
            return edgesToEdit;
        }
        graph.editEdge(p3.getU(), p3.getV());
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
        for (WeightedNeighbor uw : u.getNeighbors()) {
            Vertex w = uw.getVertex();
            if (!w.equals(v)) {
                WeightedNeighbor wv = Graph.getWeightedNeighbor(w, v);
                WeightedNeighbor wu = Graph.getWeightedNeighbor(w, u);

                int newWeight = wu.getWeight() + wv.getWeight();
                Edge uwEdge = new Edge(u,w);
                Edge vwEdge = new Edge(v,w);
                if (forbiddenEdges.stream().anyMatch(edge -> edge.equals(uwEdge))){
                    forbiddenEdges.add(new Edge(w,mergedVertex));
                    if(newWeight > 0){
                        newWeight *= -1;
                    }
                    if (wv.isEdgeExists()) {
                        //TODO maybe editEdge?
                        costs += wv.getWeight();
                    }
                }else if(forbiddenEdges.stream().anyMatch(edge -> edge.equals(vwEdge))){
                    forbiddenEdges.add(new Edge(w,mergedVertex));
                    if(newWeight > 0){
                        newWeight *= -1;
                    }
                    if (wv.isEdgeExists()) {
                        //TODO maybe editEdge?
                        costs += wu.getWeight();
                    }
                }else{
                    if (wu.isEdgeExists() != wv.isEdgeExists()) {
                        //TODO maybe editEdge?
                        costs += Math.min(Math.abs(wu.getWeight()), Math.abs(wv.getWeight()));
                    }
                }

                WeightedNeighbor mergedWeightedNeighbor = new WeightedNeighbor(mergedVertex, newWeight);
                mergedVertex.getNeighbors().add(new WeightedNeighbor(w, newWeight));
                w.getNeighbors().remove(wu);
                w.getNeighbors().remove(wv);
                w.getNeighbors().add(mergedWeightedNeighbor);
            }
        }
        graph.getVertices().remove(u);
        graph.getVertices().remove(v);
        graph.getVertices().add(mergedVertex);
        return costs;
    }

    private static List<Edge> reconstructMergeForEdgesToEditList(Vertex m, List<Edge> edgesToEdit) {
        Vertex u = m.getMergedFrom1();
        Vertex v = m.getMergedFrom2();
        for (WeightedNeighbor uw : u.getNeighbors()) {
            Vertex w = uw.getVertex();
            if (!w.equals(v)) {
                Edge mwEdge = new Edge(m, w);
                WeightedNeighbor weightedNeighbor = Graph.getWeightedNeighbor(m, w);
                if (uw.isEdgeExists() != (weightedNeighbor.isEdgeExists() != edgesToEdit.stream().anyMatch(edge -> edge.equals(mwEdge)))) {
                    edgesToEdit.add(new Edge(u, w));
                }
            }
        }
        for (WeightedNeighbor vw : v.getNeighbors()) {
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
