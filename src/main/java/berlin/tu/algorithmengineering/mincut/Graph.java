package berlin.tu.algorithmengineering.mincut;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Graph {

    private int n;                               // Total number of vertices
    private int m;                                    // Total number of edges

    public HashMap<Integer, Vertex> vertex;                         // Map names to vertices
    public HashSet<Edge> edges;                                    // Collection of all edges
    public HashMap<Vertex, HashSet<Edge>> adjList;                 // Adjacency List of Graph

    public Graph(int n) {
        this.n = n;
        this.adjList = new HashMap<Vertex, HashSet<Edge>>();
        this.vertex = new HashMap<Integer, Vertex>();
        this.edges = new HashSet<Edge>();
    }

    /**
     * This method is used to generate the vertices randomly with random weights.
     */
    public void generateVertices() {
        for (int i = 0; i < n; i++) {
            Vertex inpV = new Vertex(i);
            this.vertex.put(i, inpV);
            this.adjList.put(inpV, new HashSet<Edge>());
        }
    }

    /**
     * This method is used to accept user input vertices.
     */
    public void getVertices() {
        Scanner sc = new Scanner(System.in);
        System.out.println("enter the number of vertices");
        int n = sc.nextInt();
        this.n = n;
        for (int i = 0; i < n; i++) {
            int num = sc.nextInt();
            Vertex inpV = new Vertex(num);
            this.vertex.put(num, inpV);
            this.adjList.put(inpV, new HashSet<Edge>());
        }
        System.out.println(this.vertex);
    }

    /**
     * This method is used to accept user input edges and edge-weights.
     */
    public void getEdges() {
        Scanner sc = new Scanner(System.in);
        System.out.println("enter the number of vertices");
        int m = sc.nextInt();
        int pedge = 0;
        for (int i = 0; i < m; i++) {
            int f = sc.nextInt();
            int t = sc.nextInt();
            int c = sc.nextInt();
            Edge e = new Edge(this.vertex.get(f), this.vertex.get(t), c);
            this.edges.add(e);
            HashSet<Edge> tmpEdge = this.adjList.get(this.vertex.get(f));
            if (tmpEdge.contains(e)) {
                pedge++;
            }
            tmpEdge.add(e);
            this.adjList.put(this.vertex.get(f), tmpEdge);
            tmpEdge = this.adjList.get(this.vertex.get(t));
            tmpEdge.add(e);
            this.adjList.put(this.vertex.get(t), tmpEdge);
        }
        this.m -= pedge;
    }

    /**
     * This method is used to generate the edges randomly with random weights.
     *
     * @param m indicated the edges to be generated
     */
    public void generateEdges(int m) {
        this.m = m;
        int pedge = 0;
        for (int i = 0; i < m; i++) {
            int f = (int) (Math.random() * n);
            int t = (int) (Math.random() * n);
            int c = (int) (Math.random() * 32);
            while (f == t) {
                f = (int) (Math.random() * n);
            }
            Edge e = new Edge(this.vertex.get(f), this.vertex.get(t), c);
            this.edges.add(e);
            HashSet<Edge> tmpEdge = this.adjList.get(this.vertex.get(f));
            if (tmpEdge.contains(e)) {
                pedge++;
            }
            tmpEdge.add(e);
            this.adjList.put(this.vertex.get(f), tmpEdge);
            tmpEdge = this.adjList.get(this.vertex.get(t));
            tmpEdge.add(e);
            this.adjList.put(this.vertex.get(t), tmpEdge);
        }
        this.m -= pedge;
    }

    public void addEdge(int u, int v, int capacity) {
        Edge e = new Edge(this.vertex.get(u), this.vertex.get(v), capacity);
        this.edges.add(e);
        HashSet<Edge> tmpEdge = this.adjList.get(this.vertex.get(u));
        tmpEdge.add(e);
        this.adjList.put(this.vertex.get(u), tmpEdge);
        tmpEdge = this.adjList.get(this.vertex.get(v));
        tmpEdge.add(e);
        this.adjList.put(this.vertex.get(v), tmpEdge);
    }


    /**
     * This method is used to Print the adjacency list of the graph.
     */
    public void printGraph() {
        for (Vertex v : this.adjList.keySet()) {
            System.out.println(v + " : " + this.adjList.get(v));
        }
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }
}

