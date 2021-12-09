package berlin.tu.algorithmengineering.mincut;

public class Vertex {
    private int name;

    public Vertex(int name) {
        this.name = name;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    /**
     * This method is used to override the hashcode method for the class Vertex.
     */
    public int hashCode() {
        // use hash codes of the underlying objects
        return this.name;
    }

    /**
     * This method is used to override the equals method for the class Vertex.
     */
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Vertex v = (Vertex) o;

        if (v.name == this.name) {
            return true;
        }
        return false;
    }

    /**
     * This method is used to print the vertex object.
     */
    public String toString() {
        return String.valueOf(this.name);
    }
}
