package Graph;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ilia on 19/6/2017.
 */
public class DAGmerged {


    public Boolean merged;
    public HashMap <Long,DAG> subDAGList;

    public DAGmerged(){
        merged = false;
      subDAGList = new HashMap<>();

    }

    public void addSubDAG(DAG subDAG){

        if(!merged)
            merged = true;
        subDAGList.put(subDAG.dagId, subDAG);
    }

    public DAG getSubDAG(Long id){
        return subDAGList.get(id);
    }


}
