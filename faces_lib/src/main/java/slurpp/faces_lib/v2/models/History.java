package slurpp.faces_lib.v2.models;

/**
 * Created by Kyle Montague on 19/08/2017.
 */

public class History {

    long timestamp = System.currentTimeMillis();
    int id; //todo swap for FaceSetting

    public History(){

    }

    public History(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
