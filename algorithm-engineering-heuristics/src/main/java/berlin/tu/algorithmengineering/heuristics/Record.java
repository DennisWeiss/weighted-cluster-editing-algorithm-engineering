package berlin.tu.algorithmengineering.heuristics;

import java.util.HashMap;

public class Record{
    HashMap<String, Double> record;
    Integer clusterNo;

    public Record(HashMap<String, Double> record){
        this.record = record;
    }

    public void setClusterNo(Integer clusterNo) {
        this.clusterNo = clusterNo;
    }

    public HashMap<String, Double> getRecord() {
        return record;
    }
}
