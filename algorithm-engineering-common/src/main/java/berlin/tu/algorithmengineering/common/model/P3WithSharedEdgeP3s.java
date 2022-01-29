package berlin.tu.algorithmengineering.common.model;

import berlin.tu.algorithmengineering.common.Graph;

import java.util.*;

public class P3WithSharedEdgeP3s {

    private P3 p3;
    private Set<P3> sharedEdgeP3 = new HashSet<>();
    private int smallestAbsoluteEdgeWeight;

    public P3WithSharedEdgeP3s(P3 p3) {
        this.p3 = p3;
    }

    public static Map<P3, P3WithSharedEdgeP3s> fromP3List(Graph graph, List<P3> p3List) {
        List<P3WithSharedEdgeP3s> p3WithSharedEdgeP3sList = new ArrayList<>();
        for (P3 p3 : p3List) {
            P3WithSharedEdgeP3s p3WithSharedEdgeP3s = new P3WithSharedEdgeP3s(p3);
            p3WithSharedEdgeP3s.setSmallestAbsoluteEdgeWeight(graph.getSmallestAbsoluteWeight(p3));

            for (P3 otherP3 : p3List) {
                if (p3 != otherP3 && getNumberOfSharedVertices(p3, otherP3) > 1) {
                    p3WithSharedEdgeP3s.getSharedEdgeP3().add(otherP3);
                }
            }

            p3WithSharedEdgeP3sList.add(p3WithSharedEdgeP3s);
        }
        return listToMap(p3WithSharedEdgeP3sList);
    }

    private static Map<P3, P3WithSharedEdgeP3s> listToMap(List<P3WithSharedEdgeP3s> p3WithSharedEdgeP3sList) {
        Map<P3, P3WithSharedEdgeP3s> p3P3WithSharedEdgeP3sMap = new HashMap<>();
        for (P3WithSharedEdgeP3s p3WithSharedEdgeP3s : p3WithSharedEdgeP3sList) {
            p3P3WithSharedEdgeP3sMap.put(p3WithSharedEdgeP3s.getP3(), p3WithSharedEdgeP3s);
        }
        return p3P3WithSharedEdgeP3sMap;
    }

    private static int getNumberOfSharedVertices(P3 a, P3 b) {
        int sharedVertices = 0;
        if (a.getU() == b.getU() || a.getU() == b.getV() || a.getU() == b.getW()) {
            sharedVertices++;
        }
        if (a.getV() == b.getU() || a.getV() == b.getV() || a.getV() == b.getW()) {
            sharedVertices++;
        }
        if (a.getW() == b.getU() || a.getW() == b.getV() || a.getW() == b.getW()) {
            sharedVertices++;
        }
        return sharedVertices;
    }

    public P3 getP3() {
        return p3;
    }

    public void setP3(P3 p3) {
        this.p3 = p3;
    }

    public Set<P3> getSharedEdgeP3() {
        return sharedEdgeP3;
    }

    public void setSharedEdgeP3(Set<P3> sharedEdgeP3) {
        this.sharedEdgeP3 = sharedEdgeP3;
    }

    public int getSmallestAbsoluteEdgeWeight() {
        return smallestAbsoluteEdgeWeight;
    }

    public void setSmallestAbsoluteEdgeWeight(int smallestAbsoluteEdgeWeight) {
        this.smallestAbsoluteEdgeWeight = smallestAbsoluteEdgeWeight;
    }
}
