package Graph.parsers;

import Graph.*;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

/**
 * @author herald
 * @since 1.0
 */
public class PegasusDaxParser {

    /**
     * A mapping of AbstractOperator names and their Concrete Operator names
     */
    public final  Map<String, List<String>> abstractConcreteOperatorsMap =
        Collections.unmodifiableMap(new HashMap<String, List<String>>() {

            {
                put("mAdd", Collections.unmodifiableList(Arrays.asList("mAdd")));
                put("mBackground", Collections.unmodifiableList(Arrays.asList("mBackground")));
                put("mBgModel", Collections.unmodifiableList(Arrays.asList("mBgModel")));
                put("mConcatFit", Collections.unmodifiableList(Arrays.asList("mConcatFit")));
                put("mDiffFit", Collections.unmodifiableList(Arrays.asList("mDiffFit")));
                put("mImgTbl", Collections.unmodifiableList(Arrays.asList("mImgTbl")));
                put("mJPEG", Collections.unmodifiableList(Arrays.asList("mJPEG")));
                put("mProjectPP", Collections.unmodifiableList(Arrays.asList("mProjectPP")));
                put("mShrink", Collections.unmodifiableList(Arrays.asList("mShrink")));
            }
        });
    private  final Logger LOG = Logger.getLogger(PegasusDaxParser.class);
//
//    static {
//    }

    long sumdata;

    double multiply_by_time = 1000.0;
    int multiply_by_data = 1;

    public PegasusDaxParser(double mulTIME,int mulDATA) {
        multiply_by_data = mulDATA;
        multiply_by_time = mulTIME;
        sumdata=0;

    }
    public PegasusDaxParser(){
        sumdata=0;
    }

    public  DAG parseDax(String url) throws Exception {
        sumdata=0;
        DAG graph = new DAG();
        graph.name = url+multiply_by_time+"_"+multiply_by_data;
        ArrayList<Edge> edges = new ArrayList<>();
        HashMap<Long,HashMap<Long,Edge>> fEdges = new HashMap<>();


        ////
        HashMap<String,Long> nameOut = new HashMap<>();
        HashMap<String,ArrayList<Long>> nameIn = new HashMap<>();
        HashMap<String,Long> nameToSize = new HashMap<>();
        /////


        //elaaaa
        HashMap<String,HashMap<String,tempFile>>  opin = new HashMap<>();
        HashMap<String,HashMap<String,tempFile>>  opout = new HashMap<>();
        HashMap<String,ArrayList<String>> optoop = new HashMap<>();


        //////


        int count=0;

        HashMap<String,Edge> filenameToEdge = new HashMap<>();

        HashMap<String, Data> filenameToFile = new HashMap<>();
        LinkedHashMap<String, Long> operatorNameMap = new LinkedHashMap<>();


        HashMap<String,tempFile> nameTotempFile = new HashMap<>();
        HashMap<Long,tempFile> fTotempFile = new HashMap<>();


        LinkedHashMap<String, Data> operatorDataMap = new LinkedHashMap<>(16);

        LinkedHashMap<String, LinkedHashMap<String, Integer>> operatorDataInputMap =
            new LinkedHashMap<>(16);
        LinkedHashMap<String, LinkedHashMap<String, Integer>> operatorDataOutputMap =
            new LinkedHashMap<>(16);

        HashMap<String, Data> inFileDataMap = new HashMap<>();
        HashMap<String, Data> outFileDataMap = new HashMap<>();
        HashMap<String, String> inDataOpMap = new HashMap<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(url);
        doc.getDocumentElement().normalize();

        Random rand = new Random();

        NodeList jobList = doc.getElementsByTagName("job");
        for (int s = 0; s < jobList.getLength(); s++) {

            double runTimeValue = 0;
            double cpuUtilizationValue = 1.0;
            int memoryValue = 40;

            Node job = jobList.item(s);
            Element jobElement = (Element) job;

            String name = jobElement.getAttribute("id");
            String application = jobElement.getAttribute("namespace");
            String className = jobElement.getAttribute("name");

            runTimeValue =
                Double.parseDouble(jobElement.getAttribute("runtime")) * multiply_by_time;

            long runtime_MS = (long) (runTimeValue * 1000);

            ResourcesRequirements res = new ResourcesRequirements(runtime_MS,memoryValue);

            Long opid = graph.addOperator(new Operator(name,res));
//            System.out.println("opcreated "+opid+"  "+runtime_MS);

            Operator op = graph.getOperator(opid);
            operatorNameMap.put(name, opid);
            op.className=className;
            op.cpuBoundness = getCpuBoundness(application,className);


            NodeList useElementList = jobElement.getElementsByTagName("uses");
            for (int i = 0; i < useElementList.getLength(); i++) {

                Node use = useElementList.item(i);
                Element useElement = (Element) use;


                long dataSize = (long) Double.parseDouble(useElement.getAttribute("size"));



                long data_B = dataSize*multiply_by_data;

                sumdata+=data_B;

                String filename = useElement.getAttribute("file");
                //                if(!filenameToFile.containsKey(filename)){
//                    data = new Data(filename,data_B);
//                    filenameToFile.put(filename,data);
//                }else{
//                    data = filenameToFile.get(filename);
//                }

//                Edge e = null;
//                tempFile tf = null;
//
////                if(!filenameToEdge.containsKey(filename)) {
////                    e = new Edge(-1, -1, new Data(filename, data_B));
////                    filenameToEdge.put(filename,e);
////                }else{
////                    e = filenameToEdge.get(filename);
////                }
//                if(!nameTotempFile.containsKey(filename)) {
//                    count++;
//                    tf = new tempFile(-1, -1, data_B, filename);
//                    nameTotempFile.put(filename,tf);
//                }else{
//                    tf = nameTotempFile.get(filename);
//                }

//                if(!nameToSize.containsKey(filename)){
//                    nameToSize.put(filename,data_B);
//                }

                if (useElement.getAttribute("link").equals("output")) {

                    if(!opout.containsKey(name)){
                        opout.put(name,new HashMap<String, tempFile>());
                    }
                    opout.get(name).put(filename,new tempFile(null,name,data_B,filename));
//                      tf.from = opid;
//                    op.addInFile(filename);
//                    data.fromOpId = opid;
                }
                else {
//                    if(!nameIn.containsKey(filename)){
//                        nameIn.put(filename,new ArrayList<Long>());
//                    }
//                    nameIn.get(filename).add(opid);
                    if(!opin.containsKey(name)){
                        opin.put(name,new HashMap<String, tempFile>());
                    }
                    opin.get(name).put(filename,new tempFile(name,"",data_B,filename));
//                    tf.to = opid;
//                    op.addOutFile(filename);
//                    data.toOpId = opid;
                }

            }

        }
        //all operator added
//        for(Data f:filenameToFile.values()){
//            graph.addFile(f);
//        }//add all files


        //////cleanup files with only from or to

//        HashSet<String> rmNames = new HashSet<>();
//        for(String s:nameTotempFile.keySet()){
//            tempFile tf = nameTotempFile.get(s);
//            if(tf.to.equals("") || tf.from.equals("")){
//                rmNames.add(tf.name);
//            }
//        }
//        for(String n:rmNames){
//            nameTotempFile.remove(n);
//        }

//        HashMap<Long,HashMap<Long,Long>> fTotfs = new HashMap<>();//from->to,filesize_MB
//
//        for(tempFile tf:nameTotempFile.values()){
//            if(!fTotfs.containsKey(tf.from)){
//                fTotfs.put(tf.from,new HashMap<Long,Long>());
//            }
//            if(!fTotfs.get(tf.from).containsKey(tf.to)){
//                fTotfs.get(tf.from).put(tf.to,0L);
//            }
//            fTotfs.get(tf.from).put(tf.to, fTotfs.get(tf.from).get(tf.to)+tf.file_MB);
//        }

//        for(Long from: fTotfs.keySet()) {  //add all edges
//            HashMap<Long,Long> innermap = fTotfs.get(from);
//            for(Long to:innermap.keySet()){
//                graph.addEdge(new Edge(from,to,new Data(from+"->"+to,innermap.get(to))));
//            }
//        }
//
//        graph.printEdges();

        //////////////////////////////////////

//        for(Edge e:filenameToEdge.values()) {  //add all edges
//            System.out.println(e.from+" "+e.to);
//            graph.addEdge(e);
//        }

        /////



        ////////check edges from files to edges from xml/////////

//        for(Edge e:filenameToEdge.values()) {
//            fEdges.put(e.from,new HashSet<Long>());
//        }
//        for(Edge e:filenameToEdge.values()) {
//            fEdges.get(e.from).add(e.to);
//        }
//        // Create the links


        NodeList childList = doc.getElementsByTagName("child");
        for (int c = 0; c < childList.getLength(); c++) {
            Node child = childList.item(c);
            Element childElement = (Element) child;

            String to = childElement.getAttribute("ref");
            Long toOpId = operatorNameMap.get(to);
            NodeList parentList = childElement.getElementsByTagName("parent");

      /* Input port names */
//            LinkedHashMap<String, Integer> inMap = operatorDataInputMap.get(to);
            for (int p = 0; p < parentList.getLength(); p++) {
                Node parent = parentList.item(p);
                Element parentElement = (Element) parent;

                String from = parentElement.getAttribute("ref");
                Long fromOpId = operatorNameMap.get(from);

                edges.add(new Edge(fromOpId,toOpId,new Data("",-1)));

                if(!optoop.containsKey(from)){
                    optoop.put(from,new ArrayList<String>());
                }
                optoop.get(from).add(to);


                //
//                if(!fEdges.containsKey(fromOpId)){
//                    System.out.printf("1.PROBLEM IN DAX PARSER!!!");
//                }
//                if(!fEdges.get(fromOpId).contains(toOpId)  ){
//                    System.out.printf("2.PROBLEM IN DAX PARSER!!!");
//                }


            }//all the parents
        }


        Long f,t;
        for(String fs:optoop.keySet()){
            f = operatorNameMap.get(fs);
            for(String ts:optoop.get(fs)){
                t = operatorNameMap.get(ts);

                ArrayList<String> names = new ArrayList<>();
                long csize = 0;

                for(String StmpflOUT: opout.get(fs).keySet()){
                    if(opin.get(ts).containsKey(StmpflOUT)){
                        names.add(StmpflOUT);
                        csize+=opout.get(fs).get(StmpflOUT).file_B;
                    }
                }
                graph.addEdge(new Edge(f,t,new Data(names,csize)));


            }
        }

//        graph.printEdges();



//        System.out.println("/////edges from data");
//        for(Edge e:edges){
//            if(!fEdges.containsKey(e.from)){
//                fEdges.put(e.from,new HashMap<Long,Edge>());
//            }
//            fEdges.get(e.from).put(e.to,e);
//        }
////        for(Long fid:fEdges.keySet()){
////            System.out.printf(fid+": ");
////            for(Long tid: fEdges.get(fid).keySet()){
////                System.out.printf(" "+tid);
////            }
////            System.out.println();
////        }
////        System.out.println("////////");
//
//
//        ///add files to edges/////////
//
//        for(Long fid:fEdges.keySet()){
//            for(Long tid: fEdges.get(fid).keySet()){
//                if(!fTotfs.containsKey(fid)){continue;}
//                if(!fTotfs.get(fid).containsKey(tid)){continue;}
//                fEdges.get(fid).get(tid).data.name = fid+"->"+tid;
//                fEdges.get(fid).get(tid).data.size_B = fTotfs.get(fid).get(tid);
//            }
//        }
//        System.out.println("////////");
//
//
//        /////////////
//
//        for(Long fid:fEdges.keySet()){
//            System.out.printf(fid+": ");
//            for(Long tid: fEdges.get(fid).keySet()){
//                System.out.printf(" "+tid +"("+fEdges.get(fid).get(tid).data.size_B+")");
//            }
//            System.out.println();
//        }
//        System.out.println("////////");
//

        graph.sumdata_B = sumdata;
        return graph;
    }

    private double getCpuBoundness(String application, String className){
            double cpuBoundness=1.0;

        if ("SIPHT".equals(application))
        {
            if(className.equals("Patser"))
                cpuBoundness = 0.8348;
            else if(className.equals("Patser_concate"))
                cpuBoundness = 0.1889;
            else if(className.equals("Transterm"))
                cpuBoundness =  0.9479;
            else if(className.equals("Findterm"))
                cpuBoundness = 0.9520;
            else if(className.equals("RNAMotif"))
                cpuBoundness = 0.9505;
            else if(className.equals("Blast"))
                cpuBoundness = 0.9387;
            else if(className.equals("SRNA"))
                cpuBoundness = 0.9348;
            else if(className.equals("FFN_Parse"))
                cpuBoundness = 0.8109;
            else if(className.equals("Blast_synteny"))
                cpuBoundness = 0.6101;
            else if(className.equals("Blast_candidate"))
                cpuBoundness = 0.4361;
            else if(className.equals("Blast_QRNA"))
                cpuBoundness = 0.8780;
            else if(className.equals("Blast_paralogues"))
                cpuBoundness = 0.4430;
            else if(className.equals("SRNA_annotate"))
                cpuBoundness = 0.5596;
        }

        if ("MONTAGE".equals(application))
        {
            if(className.equals("mProjectPP"))
                cpuBoundness = 0.8696;
            else if(className.equals("mDiffFit"))
                cpuBoundness = 0.2839;
            else if(className.equals("mConcatFit"))
                cpuBoundness = 0.5317;
            else if(className.equals("mBgModel"))
                cpuBoundness = 0.9989;
            else if(className.equals("mBackground"))
                cpuBoundness = 0.0846;
            else if(className.equals("mImgTbl"))
                cpuBoundness = 0.0348;
            else if(className.equals("mAdd"))
                cpuBoundness = 0.0848;
            else if(className.equals("mShrink"))
                cpuBoundness = 0.0230;
            else if(className.equals("mJPEG"))
                cpuBoundness = 0.7714;
        }

        if ("LIGO".equals(application))
        {
            if(className.equals("TmpltBank"))
                cpuBoundness = 0.9894;
            else if(className.equals("Inspiral"))
                cpuBoundness = 0.8996;
            else if(className.equals("Thinca"))
                cpuBoundness = 0.4390;
            else if(className.equals("Inca"))
                cpuBoundness = 0.3793;
            else if(className.equals("Data_Find"))
                cpuBoundness = 0.5555;
            else if(className.equals("Inspinj"))
                cpuBoundness = 0.0832;
            else if(className.equals("TrigBank"))
                cpuBoundness = 0.1744;
            else if(className.equals("Sire"))
                cpuBoundness = 0.1415;
            else if(className.equals("Coire"))
                cpuBoundness = 0.0800;

        }

        return cpuBoundness;
    }
}
