
package main.java.JsonOptiqueParse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Operator {

    @SerializedName("operatorName")
    @Expose
    private String operatorName;
    @SerializedName("containerName")
    @Expose
    private String containerName;
    @SerializedName("resources")
    @Expose
    private Resources resources;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

}
