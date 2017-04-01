package utils;

import Lattice.LatticeGenerator;

import java.util.ArrayList;

/**
 * Created by johnchronis on 3/31/17.
 */
public class TestScriptGenerator {

    public static void main(String[] args) {
        String path="/home/gsmyris/jc/";

        ///////////GENERAL//////////////////////////////////////////////
        ArrayList<String> sci = new ArrayList<>();
        sci.add(path+"CYBERSHAKE.n.100.0.dax");
        sci.add(path+"LIGO.n.100.0.dax");
        sci.add(path+"MONTAGE.n.100.0.dax");
        sci.add(path+"SIPHT.n.100.0.dax");

        for(String fl: sci){
            System.out.println("java  -Dflow="+fl+" -Dmt=1 -Dmd=1 -jar MyScheduler.jar -Xms5g -Xmx240g &" );
            System.out.println("java  -Dflow="+fl+" -Dmt=1 -Dmd=1000 -jar MyScheduler.jar -Xms5g -Xmx240g &" );
            System.out.println("java  -Dflow="+fl+" -Dmt=50 -Dmd=1000 -jar MyScheduler.jar -Xms5g -Xmx240g &" );

        }
        ///////////////////////////////////////////////////////////////

        ///////////LIGO100//////////////////////////////////////////////
        String fl = path+"LIGO.n.100.0.dax";
        ArrayList<Pair<Integer,Integer>> ligo100confs = new ArrayList<>();
        ligo100confs.add(new Pair(50,100));
        ligo100confs.add(new Pair(1000,1));
        ligo100confs.add(new Pair(1000,100));
        ligo100confs.add(new Pair(1000,1500));
        ligo100confs.add(new Pair(10,1));
        ligo100confs.add(new Pair(10,100));

        for(Pair<Integer,Integer> p:ligo100confs) {
            System.out.println("java  -Dflow="+fl+" -Dmt="+p.a+" -Dmd="+p.b+" -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        }
        ////////////////////////////////////////////////////////////////

        ///////////MONTAGE//////////////////////////////////////////////
        fl = path+"MONTAGE.n.100.0.dax";
        ArrayList<Pair<Integer,Integer>> montage100confs = new ArrayList<>();
        montage100confs.add(new Pair(30,1));
        montage100confs.add(new Pair(100,1));
        montage100confs.add(new Pair(500,1));
        montage100confs.add(new Pair(500,100));
        montage100confs.add(new Pair(1000,100));
        montage100confs.add(new Pair(1000,1000));

        for(Pair<Integer,Integer> p:montage100confs) {
            System.out.println("java  -Dflow="+fl+" -Dmt="+p.a+" -Dmd="+p.b+" -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        }
        ////////////////////////////////////////////////////////////////

        ///////////CYBERSHAKE///////////////////////////////////////////
        fl = path+"CYBERSHAKE.n.100.0.dax";
        ArrayList<Pair<Integer,Integer>> cybershake100confs = new ArrayList<>();
        cybershake100confs.add(new Pair(10,1));
        cybershake100confs.add(new Pair(50,1));
        cybershake100confs.add(new Pair(50,100));
        cybershake100confs.add(new Pair(100,100));
        cybershake100confs.add(new Pair(100,500));

        for(Pair<Integer,Integer> p:cybershake100confs) {
            System.out.println("java  -Dflow="+fl+" -Dmt="+p.a+" -Dmd="+p.b+" -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        }
        ////////////////////////////////////////////////////////////////

        ///////////SIPHT////////////////////////////////////////////////
        fl = path+"SIPHT.n.100.0.dax";
        ArrayList<Pair<Integer,Integer>> spiht100confs = new ArrayList<>();
        spiht100confs.add(new Pair(50,100));
        spiht100confs.add(new Pair(500,5000));
        spiht100confs.add(new Pair(1000,100));
        spiht100confs.add(new Pair(1000,1500));
        spiht100confs.add(new Pair(10,1));
        spiht100confs.add(new Pair(10,100));

        for(Pair<Integer,Integer> p:spiht100confs) {
            System.out.println("java  -Dflow="+fl+" -Dmt="+p.a+" -Dmd="+p.b+" -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        }
        ////////////////////////////////////////////////////////////////

        ///////////Lattice//////////////////////////////////////////////
        ArrayList<Pair<Integer,Integer>> latticeConf = new ArrayList<>();
        latticeConf.add(new Pair(11,3));
        latticeConf.add(new Pair(9,4));
        latticeConf.add(new Pair(7,7));
        latticeConf.add(new Pair(5,21));
        latticeConf.add(new Pair(3,498));

        for(Pair<Integer,Integer> p:latticeConf) {
            System.out.println("java -Dflow=lattice -Dd="+p.a+" -Db="+p.b+" -jar MyScheduler.jar -Xms5g -Xmx240g &");
        }
        /////////////////////////////////////////////////////////////////

        ///////////sciBig//////////////////////////////////////////////

        System.out.println("java  -Dflow="+path+   "CYBERSHAKE.n.300.0.dax"+" -Dmt="+  50   +" -Dmd="+  10  +"  -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        System.out.println("java  -Dflow="+path+   "LIGO.n.300.0.dax"      +" -Dmt="+  500   +" -Dmd="+  500  +"  -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        System.out.println("java  -Dflow="+path+   "MONTAGE.n.300.0.dax"   +" -Dmt="+  500  +" -Dmd="+  500  +"  -jar MyScheduler.jar -Xms5g -Xmx240g &" );
        System.out.println("java  -Dflow="+path+   "LIGO.n.500.0.dax"      +" -Dmt="+  500   +" -Dmd="+  500  +"  -jar MyScheduler.jar -Xms5g -Xmx240g &" );



        /////////////////////////////////////////////////////////////////




    }




    }
