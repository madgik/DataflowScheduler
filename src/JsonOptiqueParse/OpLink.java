
package JsonOptiqueParse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpLink {

    @SerializedName("containerName")
    @Expose
    private String containerName;
    @SerializedName("from")
    @Expose
    private String from;
    @SerializedName("to")
    @Expose
    private String to;
    @SerializedName("paramList")
    @Expose
    private List<ParamList> paramList = null;

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<ParamList> getParamList() {
        return paramList;
    }

    public void setParamList(List<ParamList> paramList) {
        this.paramList = paramList;
    }

}
