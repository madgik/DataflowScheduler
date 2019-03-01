package main.java.Scheduler;

/**
 * @author ilia
 */



public enum containerType {//ccus as a metric for cpu and price normalized based on 0.085

        A( 0.92/0.92,    (0.085) ,  "A"),//m1.small     //2.8
        C( 4.08/0.92,    (0.34),   "C"),//m1.large     //1.8
        E( 7.05/0.92,    (0.57),      "E"),//m2.xlarge    //2.6
        G( 15.9/0.92,   (1.34),   "G"),//m2.2xlarge   //2.6
        H( 27.25/0.92,   (2.68),   "H");//m2.4xlarge    //2.6

    public static containerType getSmallest(){
        return containerType.values()[0];
    }

    public static containerType getLargest(){
        return containerType.values()[containerType.values().length-1];
    }


    //ContainerResources contResource;
    public double container_memory_B = 1.0;
    public double container_CPU = 1.0;
    public double containerDisk_B = 1.0;
    public double container_price = 1.0;
    public String name;


    containerType(double container_CPU, double container_price, String name){
        this.container_CPU = container_CPU;
        this.container_memory_B = 1.0;
        this.containerDisk_B = 1.0;
        this.container_price = container_price;
        this.name = name;
    }

    public static containerType getNextSmaller(containerType cType){

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

    //is ct1 smaller than ct2
    public static boolean isSmaller(containerType ct1, containerType ct2){
        for (containerType nextCT:containerType.values())
        {
            if(nextCT==ct2 ){
                return false;
            }

            if(nextCT==ct1)
                return true;
        }

        return false;
    }

    //is ct1 larger than ct2
    public static boolean isLarger(containerType ct1, containerType ct2){

        for (containerType nextCT:containerType.values())
        {
            if(nextCT==ct1 ){
                return false;
            }
            if(nextCT==ct2)
                return true;


        }

        return false;
    }

}
