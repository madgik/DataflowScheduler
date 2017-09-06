package Graph;

import java.util.HashMap;

/**
 * Created by ilia on 19/6/2017.
 */
public class DAGmerged {


    public Boolean merged;
    public HashMap <Long,DAG> subDAGList;
    public HashMap <Long,HashMap<Long, Long>> subdagToDagOpIds;

    public DAGmerged(){
        merged = false;
      subDAGList = new HashMap<>();
        subdagToDagOpIds = new HashMap<>();

    }

    public void addSubDAG(DAG subDAG, HashMap<Long,Long> OldIdToNewId){


        if(!merged)
            merged = true;
        subDAGList.put(subDAG.dagId, subDAG);
        HashMap<Long,Long> subdagOldIdToNewId = new HashMap<>();

        subdagOldIdToNewId.putAll(OldIdToNewId);
        subdagToDagOpIds.put(subDAG.dagId, subdagOldIdToNewId);


    }

    public DAG getSubDAG(Long id){
        return subDAGList.get(id);
    }


}
