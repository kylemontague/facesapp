package slurpp.faces_lib.v2.models;

import com.google.gson.GsonBuilder;

import io.realm.RealmObject;

/**
 * Created by Kyle Montague on 19/08/2017.
 */

public class FaceColor extends RealmObject {
    int normal;
    int ambient;

    public FaceColor(){

    }

    public FaceColor(int normal){
        this.normal = normal;
    }

    public FaceColor(int normal, int ambient) {
        this.normal = normal;
        this.ambient = ambient;
    }

    public int getNormal() {
        return normal;
    }

    public void setNormal(int normal) {
        this.normal = normal;
    }

    public int getAmbient() {
        return ambient;
    }

    public void setAmbient(int ambient) {
        this.ambient = ambient;
    }

    public int get(boolean ambientMode){
        return ambientMode?this.ambient:this.normal;
    }


    public static FaceColor fromJson(String json){
        return new GsonBuilder().create().fromJson(json,FaceColor.class);
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }
}
