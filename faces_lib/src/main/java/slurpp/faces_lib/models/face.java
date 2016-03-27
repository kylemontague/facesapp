package slurpp.faces_lib.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import slurpp.faces_lib.IFace;

/**
 * Created by kylemontague on 27/03/16.
 */
@Table(name = "Face", id="_id")
public class Face extends Model {

    @Column(name = "name")
    public String name;

    //todo can't store this, needs to be serialised
    @Column(name = "configuration")
    public IFace configuration;

    @Column(name = "created")
    public long created;

    @Column(name = "worn")
    public long worn;

    @Column(name = "baseFace", index = true)
    public int base;

    @Column(name = "colors")
    public ArrayList<Integer> colors;


    public int[] getColors(){
        int[] result = new int[colors.size()];
        for (int i=0;i<colors.size();i++){result[i] = colors.get(i);}
        return result;
    }

    public List<Face> allFaces(){
        return new Select().from(Face.class).orderBy("created DSC").execute();
    }

    public Face latestFace(){
        return new Select().from(Face.class).orderBy("lastWorn DSC").executeSingle();
    }

    public List<Face> latestFaces(){
        return new Select().from(Face.class).orderBy("lastWorn DSC").groupBy("baseFace").execute();
    }
}
