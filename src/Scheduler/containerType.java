package Scheduler;

/**
 * @author ilia
 */

public enum containerType {//ccus as a metric for cpu and price normalized based on 0.085

//    A(1.0, 0.92, 1.0,    0.085,"A"),//m1.small
//    B(1.0, 3.43, 1.0,   0.17,"B"),//c1.medium
//    C(1.0, 4.08, 1.0,   0.34,"C"),//m1.large
//    D(1.0, 5.15, 1.0,    0.5,"D"),//c1.medium
//    E(1.0, 7.05, 1.0,    5,"E"),//m2.xlarge
//    F(1.0, 8.78, 1.0,   0.68,"F"),//c1.xlarge
//    G(1.0, 14.89, 1.0,  1.34,"G"),//m2.2xlarge
//    H(1.0, 27.25, 1.0,  2.68,"H");//m2.xlarge

    A( 0.92,    0.085,  "A"),//m1.small     //2.8
//    B( 3.43,    0.17,   "B"),//c1.medium    //0.34
    C( 4.08,    0.34,   "C"),//m1.large     //1.8
//    D( 5.15,    0.57,    "D"),//c1.medium    //1.8
    E( 7.05,    0.57,      "E"),//m2.xlarge    //2.6
//    F( 8.78,    0.68,   "F"),//c1.xlarge  //0.35
    G( 15.9,   1.34,   "G"),//m2.2xlarge   //2.6
    H( 27.25,   2.68,   "H");//m2.xlarge    //2.6

    //commented those out so they have around 2gbs of mem per vcpu

    //ec2




//   0.274/hr	27.24
//   0.322/hr	27.13
//   0.391/hr	26.72
//   0.48/hr	26.51
//   0.206/hr	26.47
//   0.274/hr	27.39
//   0.27/hr	21.41
//   0.14/hr	9.33
//   0.34/hr	4.73


//  27.13,  "SH"),
//    SI( 0.274,    27.24,  "SI");

    //stormcloud


//    0.53/hr	6271	2193.5	27.21
//    0.25/hr	4505	1849.7	6.12
//    0.17/hr	2672	708.5	3.43
//    0.11/hr	1669	491.3	2.47

//    NA( 2.47,  0.11),
//    NB( 3.43,  0.17),
//    NC( 6.12,  0.25),
//    ND( 27.21, 0.53); //realtive cheap for the performance

    //newserver




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

    containerType(double container_memory_B, double container_CPU, double containerDisk_B, double container_price, String name) {

        this.container_CPU = container_CPU;
        this.container_memory_B = container_memory_B;
        this.containerDisk_B = containerDisk_B;
        this.container_price = container_price;
        this.name = name;

    }

    public static containerType getSmallest(){

        return A;
    }

    public static containerType getLargest(){
        return H;
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

    //is ct1 smaller than ct2
    public static boolean isSmaller(containerType ct1, containerType ct2){

        boolean seenct1 = false;
        boolean seenct2 = false;

        containerType nextCType=containerType.getLargest();
        for (containerType nextCT:containerType.values())
        {
            if(nextCT==ct1 && seenct2){
                return false;
            }
            if(nextCT==ct1 && !seenct2){
                return true;
            }

        }

        return false;
    }

}
