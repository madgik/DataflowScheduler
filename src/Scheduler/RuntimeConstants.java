package Scheduler;

import java.util.EnumMap;

/**
 * Created by johnchronis on 2/19/17.
 */
public class RuntimeConstants {

    /**
     * Quantum size (in seconds)
     * Default: 1 hour
     */
    public static long quantum_MS = 3600000;

    /**
     * Network speed (in MB/sec)
     * Default: 100 MB/sec (~1GBit)
     */
    public static  double network_speed_B_MS = 104857.6;

    /**
     * Disk throughput in (MB/sec)
     * Default: 200 MB/sec (SSD)
     */
    public static  double disk_throughput__B_MS = 209715.2;

    /**
     * CPU utilization of the data transfer operator
     * Default: 0.1 (10%)
     */
    public static  double data_transfer_CPU = 0.1;

    /**
     * Memory needs of the data transfer operator
     * Default: 1%
     * TODO: Change this to integer in the range [0, 100]
     */
    public static  int data_transfer_memory__MB = 1;




}
