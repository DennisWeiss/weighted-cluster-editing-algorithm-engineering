package berlin.tu.algorithmengineering.heuristics;

import java.util.HashMap;

public class Record{
    HashMap<String, Double> record;
    Integer clusterNo;
    Integer vertexId;

    public Record(HashMap<String, Double> record, int vertexId){
        this.record = record;
        this.vertexId = vertexId;
    }

    public void setClusterNo(Integer clusterNo) {
        this.clusterNo = clusterNo;
    }

    public Integer getClusterNo(){
        return this.clusterNo;
    }

    public HashMap<String, Double> getRecord() {
        return record;
    }

    public Integer getVertexId() {
        return this.vertexId;
    }

}
