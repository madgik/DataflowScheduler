
package JsonOptiqueParse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JsonPlan {

    @SerializedName("containers")
    @Expose
    private List<Container> containers = null;
    @SerializedName("operators")
    @Expose
    private List<Operator> operators = null;
    @SerializedName("op_links")
    @Expose
    private List<OpLink> opLinks = null;

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public List<Operator> getOperators() {
        return operators;
    }

    public void setOperators(List<Operator> operators) {
        this.operators = operators;
    }

    public List<OpLink> getOpLinks() {
        return opLinks;
    }

    public void setOpLinks(List<OpLink> opLinks) {
        this.opLinks = opLinks;
    }

}
