package main.java.Graph;

/**
 * Created by johnchronis on 2/18/17.
 */
public class Edge {
    public Long from;
    public Long to;
    public Data data;

    public Edge(long fr,long t, Data f){
        from=fr;
        to=t;
        data =f;
    }

}
