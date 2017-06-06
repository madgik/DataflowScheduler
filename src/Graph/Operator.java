package Graph;


import java.util.HashSet;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Operator {

    private Long id;
    public String name;
    public ResourcesRequirements resourcesRequirements;
//    public HashSet<String> inFiles;
//    public HashSet<String> outFiles;
    public String className;
    public double cpuBoundness;
    public Long dagID;
//    public String behavior;


    public Operator(String name, ResourcesRequirements resourcesRequirements){
        this.name = name;
        this.resourcesRequirements = resourcesRequirements;
        this.id = -1L;
        this.dagID = -1L;
//        inFiles = new HashSet<>();
//        outFiles = new HashSet<>();
    }

//    public long getRunTime_SEC(){
//        return resourcesRequirements.runtime_MS/1000;
//    }


    public long getRunTime_MS(){
        return resourcesRequirements.runtime_MS;
    }

    public void setId(Long id){
        this.id = id;
    }

    public void setDAGId(Long dagID){
        this.dagID = dagID;
    }

    public long getId(){
        return id;
    }

//    public void addInFile(String name){
//        inFiles.add(name);
//    }
//
//    public void addOutFile(String name){
//        outFiles.add(name);
//    }
}
