package Scheduler;

/**
 * @author ilia
 */



public enum containerType {//ccus as a metric for cpu and price normalized based on 0.085

    /////////artificial containers

//    A	(	1	,	1	,	"	A	"	)	,
//    B	(	1.630434783	,	1.75	,	"	B	"	)	,
//    C	(	2.717391304	,	2.5	,	"	C	"	)	,
//    D	(	3.804347826	,	3.25	,	"	D	"	)	,
//    E	(	4.434782609	,	4	,	"	E	"	)	,
//    F	(	5.217391304	,	4.676470588	,	"	F	"	)	,
//    G	(	5.869565217	,	5.352941176	,	"	G	"	)	,
//    H	(	6.52173913	,	6.029411765	,	"	H	"	)	,
//    I	(	7.663043478	,	6.705882353	,	"	I	"	)	,
//    J	(	9.782608696	,	7.382352941	,	"	J	"	)	,
//    K	(	11.95652174	,	8.058823529	,	"	K	"	)	,
//    L	(	14.13043478	,	8.735294118	,	"	L	"	)	,
//    M	(	17.2826087	,	15.76470588	,	"	M	"	)	,
//    N	(	20.65217391	,	18.39215686	,	"	N	"	)	,
//    O	(	22.82608696	,	21.01960784	,	"	O	"	)	,
//    P	(	23.91304348	,	23.64705882	,	"	P	"	)	,
//    Q	(	26.08695652	,	26.2745098	,	"	Q	"	)	,
//    R	(	28.26086957	,	28.90196078	,	"	R	"	)	,
//    S	(	29.91304348	,	31.52941176	,	"	S	"	)	,
//    T	(	32.60869565	,	34.15686275	,	"	T	"	)	,
//    Y	(	34.7826087	,	36.78431373	,	"	Y	"	)	,
//    W	(	36.95652174	,	39.41176471	,	"	W	"	)	,
//    Z	(	39.13043478	,	42.03921569	,	"	Z	"	)	;
//
//
//    A	(	1.00	,	0.1	,	"	A	"	)	,
//    B	(	1.50	,	0.2	,	"	B	"	)	,
//    C	(	2.00	,	0.32	,	"	C	"	)	,
//    D	(	2.5	,	0.46	,	"	D	"	)	,
//    E	(	3.0	,	0.58	,	"	E	"	)	,
//    F	(	3.5	,	0.73	,	"	F	"	)	,
//    G	(	4.0	,	0.9	,	"	G	"	)	,
//    H	(	4.5	,	1.05	,	"	H	"	)	,
//    I	(	5.0	,	1.21	,	"	I	"	)	,
//    J	(	5.5	,	1.4	,	"	J	"	)	;

//    Cost
//            ($/h) 0.10
//            0.20 0.32 0.46 0.58 0.73 0.90 1.05 1.21 1.40
//    TABLE 3 VM INSTANCE PARAMETERS
//    Computing Capacity (MIPS) 1000 1500 2000 2500 3000 3500 4000 4500 5000 5500

    //////////





//    A(1.0, 0.92, 1.0,    0.085,"A"),//m1.small
//    B(1.0, 3.43, 1.0,   0.17,"B"),//c1.medium
//    C(1.0, 4.08, 1.0,   0.34,"C"),//m1.large
//    D(1.0, 5.15, 1.0,    0.5,"D"),//c1.medium
//    E(1.0, 7.05, 1.0,    5,"E"),//m2.xlarge
//    F(1.0, 8.78, 1.0,   0.68,"F"),//c1.xlarge
//    G(1.0, 14.89, 1.0,  1.34,"G"),//m2.2xlarge
//    H(1.0, 27.25, 1.0,  2.68,"H");//m2.xlarge

//    A( 0.92,    0.085,  "A"),//m1.small     //2.8
//    //    B( 3.43,    0.17,   "B"),//c1.medium    //0.34
//    C( 4.08,    0.34,   "C"),//m1.large     //1.8
//    //    D( 5.15,    0.57,    "D"),//c1.medium    //1.8
//    E( 7.05,    0.57,      "E"),//m2.xlarge    //2.6
//    //    F( 8.78,    0.68,   "F"),//c1.xlarge  //0.35
//    G( 15.9,   1.34,   "G"),//m2.2xlarge   //2.6
//    H( 27.25,   2.68,   "H");//m2.xlarge    //2.6


//    A( 0.92/0.92,    (0.085/0.085)/3600 ,  "A"),//m1.small     //2.8
////    B( 3.43,    0.17,   "B"),//c1.medium    //0.34
//    C( 4.08/0.92,    (0.34/0.085)/3600,   "C"),//m1.large     //1.8
////    D( 5.15,    0.57,    "D"),//c1.medium    //1.8
//    E( 7.05/0.92,    (0.57/0.085)/3600,      "E"),//m2.xlarge    //2.6
////    F( 8.78,    0.68,   "F"),//c1.xlarge  //0.35
//    G( 15.9/0.92,   (1.34/0.085)/3600,   "G"),//m2.2xlarge   //2.6
//    H( 27.25/0.92,   (2.68/0.085)/3600,   "H");//m2.xlarge    //2.6
////
//
//    A( 0.92/0.92,    (0.085/0.085) ,  "A"),//m1.small     //2.8
//    //    B( 3.43,    0.17,   "B"),//c1.medium    //0.34
//    C( 4.08/0.92,    (0.34/0.085),   "C"),//m1.large     //1.8
//    //    D( 5.15,    0.57,    "D"),//c1.medium    //1.8
//    E( 7.05/0.92,    (0.57/0.085),      "E"),//m2.xlarge    //2.6
//    //    F( 8.78,    0.68,   "F"),//c1.xlarge  //0.35
//    G( 15.9/0.92,   (1.34/0.085),   "G"),//m2.2xlarge   //2.6
//    H( 27.25/0.92,   (2.68/0.085),   "H");//m2.xlarge    //2.6

        A( 0.92/0.92,    (0.085) ,  "A"),//m1.small     //2.8
////////////            B( 3.43,    0.17,   "B"),//c1.medium    //0.34
        C( 4.08/0.92,    (0.34),   "C"),//m1.large     //1.8
        ///////////////    D( 5.15,    0.57,    "D"),//c1.medium    //1.8
        E( 7.05/0.92,    (0.57),      "E"),//m2.xlarge    //2.6
       ///////////// //    F( 8.78,    0.68,   "F"),//c1.xlarge  //0.35
        G( 15.9/0.92,   (1.34),   "G"),//m2.2xlarge   //2.6
        H( 27.25/0.92,   (2.68),   "H");//m2.xlarge    //2.6






//    A( 0.92/0.92,    0.085/0.085,  "A"),//m1.small     //2.8
//        B( 2*0.92/0.92,    2*0.085/0.085,   "B"),//c1.medium    //0.34
//    C( 3*0.92/0.92,    3*0.085/0.085,   "C"),//m1.large     //1.8
//        D( 4*0.92/0.92,    4*0.085/0.085,    "D"),//c1.medium    //1.8
//    E( 5*0.92/0.92,    5*0.085/0.085,      "E"),//m2.xlarge    //2.6
//        F( 6*0.92/0.92,    6*0.085/0.085,   "F"),//c1.xlarge  //0.35
//    G( 7*0.92/0.92,   7*0.085/0.085,   "G"),//m2.2xlarge   //2.6
//    H( 8*0.92/0.92,   8*0.085/0.085,   "H");//m2.xlarge    //2.6








    //commented those out so they have around 2gbs of mem per vcpu

//    ec2
//    A(1.0, 0.92, 1.0, 0.085,"small"),//m1.small
//    B(1.0, 3.43, 1.0, 0.17,"medium"),//c1.medium
//    C(1.0, 4.08, 1.0, 0.34,"large"),//m1.large
//    D(1.0, 5.15, 1.0, 0.68,"xlarge");//c1.medium



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

    containerType(double container_memory_B, double container_CPU, double containerDisk_B, double container_price, String name) {

        this.container_CPU = container_CPU;
        this.container_memory_B = container_memory_B;
        this.containerDisk_B = containerDisk_B;
        this.container_price = container_price;
        this.name = name;

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
        for (containerType nextCT:containerType.values())
        {
            //TODO: i think we just need to add this and we do not need seenct1/2.
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
            //TODO: i think we just need to add this and we do not need seenct1/2.
            if(nextCT==ct1 ){
                return false;
            }
            if(nextCT==ct2)
                return true;


        }

        return false;
    }

}
