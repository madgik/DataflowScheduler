
package main.java.JsonOptiqueParse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Resources {

    @SerializedName("cpu_PER")
    @Expose
    private Double cpuPER;
    @SerializedName("memory_PER")
    @Expose
    private Double memoryPER;
    @SerializedName("memory_EO_PER")
    @Expose
    private Double memoryEOPER;
    @SerializedName("time_MS")
    @Expose
    private Integer timeMS;
    @SerializedName("disk_MB")
    @Expose
    private Double diskMB;

    public Double getCpuPER() {
        return cpuPER;
    }

    public void setCpuPER(Double cpuPER) {
        this.cpuPER = cpuPER;
    }

    public Double getMemoryPER() {
        return memoryPER;
    }

    public void setMemoryPER(Double memoryPER) {
        this.memoryPER = memoryPER;
    }

    public Double getMemoryEOPER() {
        return memoryEOPER;
    }

    public void setMemoryEOPER(Double memoryEOPER) {
        this.memoryEOPER = memoryEOPER;
    }

    public Integer getTimeMS() {
        return timeMS;
    }

    public void setTimeMS(Integer timeMS) {
        this.timeMS = timeMS;
    }

    public Double getDiskMB() {
        return diskMB;
    }

    public void setDiskMB(Double diskMB) {
        this.diskMB = diskMB;
    }

}
