package Graph;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Data {

    public long size_B;
    public ArrayList<String> names;

    public Data(String name,long size_B){ //use only in dax parser
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
