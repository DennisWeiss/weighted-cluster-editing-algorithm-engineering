package berlin.tu.algorithmengineering.mincut;


import java.util.ArrayList;
import java.util.HashSet;

public class NagamochiIbarakiAlgorithm {

    Vertex last = null;
    Vertex lastMinusOne = null;

    /**
     * This method implements the Namagochi Ibaraki Algorithm for the input Graph to calculate the minCut if available
     * else if the graph is disconnected, it cannot calculate minCut.
     *
     * @param g This is the Graph on which the nagamochi ibaraki algorithm runs to calculate min cut
     **/
    public int getGlobalMinCut(Graph g) {
        int minCut = Integer.MAX_VALUE;

        while (g.vertex.size() > 1) {
            int phaseCut = this.MAOrdering(g);
            if (phaseCut == 0) {
                return -1;
            }
            if (phaseCut < minCut) {
                minCut = phaseCut;
            }
            g = this.mergeGraph(g);
        }

        return minCut;
    }

    /**
     * This method implements the MAOrdering Algorithm for the input Graph to calculate the MA Ordering of the
     * vertices and then return the phase cut value from the last 2 vertices of the order.
     *
     * @param g This is the Graph with vertices whose MA Ordering and phasecut is to be calculated.
     * @return Phase cut value of the graph.
     **/
    public int MAOrdering(Graph g) {
        int maOrderAB = 0;
        Vertex[] MAOrder = new Vertex[g.vertex.size()];
        HashSet<Vertex> set = new HashSet<>();
        int startVertex = (int) (Math.random() * g.vertex.size());
        while (!g.vertex.keySet().contains(startVertex)) {
            startVertex = (int) (Math.random() * g.getN());
        }
        MAOrder[0] = g.vertex.get(startVertex);
        set.add(MAOrder[0]);
        for (int i = 1; i < MAOrder.length; i++) {
            Vertex nodeToBeIncluded = null;
            int sum = 0;
            int testedNode = 0;
            while (!g.vertex.keySet().contains(testedNode) && testedNode < g.getN()) {
                testedNode++;
            }
            Vertex testNode = new Vertex(testedNode);
            while (testedNode < g.getN()) {
                int setSum = 0;
                testNode = new Vertex(testedNode);
                if (!set.contains(testNode)) {
                    for (Vertex u : set) {
                        HashSet<Edge> tmpEdgeSet1 = new HashSet<>(g.adjList.get(u));
                        HashSet<Edge> tmpEdgeSet2 = new HashSet<>(g.adjList.get(testNode));
                        tmpEdgeSet1.retainAll(tmpEdgeSet2);
                        ArrayList<Edge> elist = new ArrayList<>(tmpEdgeSet1);
                        if (tmpEdgeSet1.size() > 0) {
                            setSum += elist.get(0).getCapacity();
                        }
                    }
                    if (setSum > sum) {
                        nodeToBeIncluded = testNode;
                        sum = setSum;
                    }
                }

                testedNode++;
                while (!g.vertex.keySet().contains(testedNode) && testedNode < g.getN()) {
                    testedNode++;
                }
            }
            if (nodeToBeIncluded == null) {
                return 0;
            }
            MAOrder[i] = nodeToBeIncluded;
            set.add(nodeToBeIncluded);
        }
        this.last = MAOrder[g.vertex.size() - 1];
        this.lastMinusOne = MAOrder[g.vertex.size() - 2];
        for (Edge e : g.adjList.get(this.last)) {
            maOrderAB += e.getCapacity();
        }
        return maOrderAB;
    }

    /**
     * This method is used to remove a vertex from the input Graph.
     *
     * @param g This is the input Graph.
     * @param v The vertex to be removed.
     **/
    private void removeVertex(Graph g, Vertex v) {
        HashSet<Edge> edges = new HashSet<>(g.adjList.get(v));
        for (Edge e : edges) {
            this.removeEdge(g, e);
        }
    }

    /**
     * This method is used to redirect all edges from the last vertex in the MA order
     * to the last but one vertex in the MA order.
     *
     * @param g    This is the input Graph.
     * @param eset Set of edges to be redirected.
     **/
    private void redirectEdge(Graph g, ArrayList<Edge> eset) {
        for (Edge e : eset) {
            this.removeEdge(g, e);
            Vertex one = e.otherEnd(this.last);
            Vertex two = this.lastMinusOne;
            Edge newEdge = new Edge(one, two, e.getCapacity());
            this.addEdge(g, newEdge);
        }
    }

    /**
     * This method is used to remove a edge from the input Graph.
     *
     * @param g This is the input Graph.
     * @param e The edge to be removed.
     **/
    public void removeEdge(Graph g, Edge e) {
        Vertex from = e.getFrom();
        HashSet<Edge> eset = g.adjList.get(from);
        eset.remove(e);
        g.adjList.put(from, eset);
        Vertex to = e.getTo();
        eset = g.adjList.get(to);
        eset.remove(e);
        g.adjList.put(to, eset);

    }

    /**
     * This method is used to add a edge to the input Graph.
     *
     * @param g This is the input Graph.
     * @param e The edge to be added.
     **/
    public void addEdge(Graph g, Edge e) {
        Vertex from = e.getFrom();
        Vertex to = e.getTo();
        HashSet<Edge> eset = g.adjList.get(from);
        eset.add(e);
        g.adjList.put(from, eset);
        eset = g.adjList.get(to);
        eset.add(e);
        g.adjList.put(to, eset);
    }

    /**
     * This method is used to merge the 2 vertices in the MA ordering and return a new Graph.
     *
     * @param g This is the input Graph.
     * @return New Graph with 2 vertices from the MA order of vertices merged.
     **/
    public Graph mergeGraph(Graph g) {
        int newWeight = 0;
        for (int num : g.vertex.keySet()) {
            Vertex tmp = g.vertex.get(num);
            if (!tmp.equals(this.last) && !tmp.equals(this.lastMinusOne)) {
                HashSet<Edge> lastES = new HashSet<>(g.adjList.get(this.last));
                HashSet<Edge> lastMinusOneES = new HashSet<>(g.adjList.get(this.lastMinusOne));
                HashSet<Edge> curES = new HashSet<>(g.adjList.get(tmp));
                lastES.retainAll(curES);
                ArrayList<Edge> a1 = new ArrayList<>(lastES);
                lastMinusOneES.retainAll(curES);
                ArrayList<Edge> a2 = new ArrayList<>(lastMinusOneES);
                lastES = new HashSet<>(g.adjList.get(this.last));
                lastMinusOneES = new HashSet<>(g.adjList.get(this.lastMinusOne));
                lastMinusOneES.retainAll(lastES);
                ArrayList<Edge> a3 = new ArrayList<>(lastMinusOneES);
                if (a1.size() == 1 && a2.size() == 1) {
                    Edge celast = a1.get(0);
                    Edge celastMinusOne = a2.get(0);
                    this.removeEdge(g, celast);
                    this.removeEdge(g, celastMinusOne);
                    newWeight += a1.get(0).getCapacity() + a2.get(0).getCapacity();
                    Edge newEdge = new Edge(tmp, lastMinusOne, newWeight);
                    this.addEdge(g, newEdge);
                }
                if (a1.size() == 1 && a2.size() == 0) {
                    //these edges shd be redirected to lastMinusOne
                    this.redirectEdge(g, a1);
                }
                if (a3.size() == 1) {
                    HashSet<Edge> edg = g.adjList.get(this.lastMinusOne);
                    edg.remove(a3.get(0));
                    g.adjList.put(this.lastMinusOne, edg);
                    g.edges.remove(a3.get(0));
                    g.edges.remove(a3.get(0));
                }
            }
        }
        this.removeVertex(g, this.last);
        g.adjList.remove(this.last);
        g.vertex.remove(this.last.getName());
        return g;
    }
}