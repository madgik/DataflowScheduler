
package JsonOptiqueParse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Container {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("port")
    @Expose
    private Integer port;
    @SerializedName("dataTransferPort")
    @Expose
    private Integer dataTransferPort;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getDataTransferPort() {
        return dataTransferPort;
    }

    public void setDataTransferPort(Integer dataTransferPort) {
        this.dataTransferPort = dataTransferPort;
    }

}
