package Graph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Data {
//    public Long fromOpId;
//    public Long toOpId;
    public long size_B;
    public ArrayList<String> names;
//    public String name;


    public Data(String name,long size_B){ //use only in dax parser
//        fromOpId = -1L;
//        toOpId = -1L;
        this.size_B = size_B;
        this.names = new ArrayList<>();
        names.add(name);
    }

    public Data(Collection<String> names,long size_B){ //use only in dax parser

        this.size_B = size_B;
        this.names = new ArrayList<>();
        this.names.addAll(names);
    }



}
