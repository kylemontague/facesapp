package slurpp.faces.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import slurpp.faces_lib.templates.IFace;


/**
 * Created by kylemontague on 18/03/15.
 */
public class WatchSetting implements Serializable{

    private static final long serialVersionUID = 7526472295622776147L;

    int mID;
    String mName;
    IFace mIFace;
    long mTimestamp;

    public WatchSetting(int id, String name, long timestamp, IFace IFace) {
        mID = id;
        mName = name;
        mIFace = IFace;
        mTimestamp = timestamp;
    }


    private void validateState() {
    }

    /**
     * Always treat de-serialization as a full-blown constructor, by
     * validating the final state of the de-serialized object.
     */
    private void readObject(
            ObjectInputStream aInputStream
    ) throws ClassNotFoundException, IOException {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();

        //make defensive copy of the mutable Date field
        //fDateOpened = new Date(fDateOpened.getTime());

        //ensure that object state has not been corrupted or tampered with maliciously
        validateState();
    }


    /**
     * This is the default implementation of writeObject.
     * Customise if necessary.
     */
    private void writeObject(
            ObjectOutputStream aOutputStream
    ) throws IOException {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

}
