package Scheduler;

/**
 * @author ilia
 */

public enum containerType {//ccus as a metric for cpu and price normalized based on 0.085

    SMALL(1.0, 0.92, 1.0,  0.085,"small"),//m1.small
    MEDIUM(1.0, 3.43, 1.0, 0.17,"medium"),//c1.medium
    LARGE(1.0, 4.08, 1.0, 0.34,"large"),//m1.large
    XLARGE(1.0, 5.15, 1.0, 0.68,"xlarge");//c1.medium

    //    SMALL(1.0, 0.92 / 0.92, 1.0, 0.085 / 0.085,"small"),//m1.small
    //    MEDIUM(1.0, 3.43 / 0.92, 1.0, 0.17 / 0.085,"medium"),//c1.medium
    //    LARGE(1.0, 4.08 / 0.92, 1.0, 0.34 / 0.085,"large"),//m1.large
    //    XLARGE(1.0, 5.15 / 0.92, 1.0, 0.68 / 0.085,"xlarge");//c1.medium

    //ContainerResources contResource;
    public double container_memory_MB = 1.0;
    public double container_CPU = 1.0;
    public double containerDisk_MB = 1.0;
    public double container_price = 1.0;
    public String name;



    containerType(double container_memory_MB, double container_CPU, double containerDisk_MB, double container_price, String name) {
        //    this.contResource.container_CPU=container_CPU;
        //  this.contResource.container_memory_MB=container_memory_MB;
        // this.contResource.containerDisk_MB=containerDisk_MB;
        this.container_CPU = container_CPU;
        this.container_memory_MB = container_memory_MB;
        this.containerDisk_MB = containerDisk_MB;
        this.container_price = container_price;
        this.name = name;

    }

    public static containerType getSmallest(){
        return SMALL;
    }

    public static containerType getLargest(){
        return XLARGE;
    }

    public static containerType getNextSmaller(containerType cType){

        // Iterator cTypes=Arrays.asList(containerType.values()).iterator();

        containerType prevCType=containerType.getSmallest();
        for (containerType nextCT:containerType.values())
        {
            if(nextCT.equals(cType))
                return prevCType;

            prevCType=nextCT;

        }
        return prevCType;
    }


    public static containerType getNextLarger(containerType cType){

        int next=0;
        containerType nextCType=containerType.getLargest();
        for (containerType nextCT:containerType.values())
        {
            if(next==1) {
                nextCType = nextCT;
                return nextCType;
            }

            if(nextCT.equals(cType))
                next=1;

        }
        return nextCType;
    }


}
