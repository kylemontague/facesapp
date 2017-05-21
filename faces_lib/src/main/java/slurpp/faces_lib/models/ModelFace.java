package slurpp.faces_lib.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;

import com.orm.SugarRecord;

import java.util.HashMap;

import slurpp.faces_lib.WatchDraw;

/**
 * Created by Kyle Montague on 20/05/2017.
 */

public class ModelFace extends SugarRecord{

    public enum FaceColour{
        BACKGROUND,
        FOREGROUND,
        ACCENT,
        HOUR,
        MINUTE,
        SECOND
    }

    enum TapMode{
        DATE,
        COLOR
    }




    String name;

    long createdOn;

    long lastWorn;

    HashMap<FaceColour,Integer> colours;

    TapMode tapMode = TapMode.COLOR;

    WatchDraw.FACE baseFace = WatchDraw.FACE.OUA_TIME;


    public ModelFace(){
        tapMode = TapMode.COLOR;
        createdOn = System.currentTimeMillis();
        colours = new HashMap<>();
        colours.put(FaceColour.BACKGROUND,Color.parseColor("#ffffff"));
        colours.put(FaceColour.FOREGROUND,Color.BLACK);
    }

    public static ModelFace OUATime(@ColorInt int textColour, @ColorInt int backgroundColor){
        ModelFace face = new ModelFace();
        face.baseFace = WatchDraw.FACE.OUA_TIME;
        face.name = "Once Upon a Time";
        face.colours.put(FaceColour.BACKGROUND,backgroundColor);
        face.colours.put(FaceColour.FOREGROUND,textColour);
        return face;
    }

    public static ModelFace FillTime(@ColorInt int textColour, @ColorInt int backgroundColor){
        ModelFace face = new ModelFace();
        face.baseFace = WatchDraw.FACE.FILL_TIME;
        face.name = "Filling Time";
        face.colours.put(FaceColour.BACKGROUND,backgroundColor);
        face.colours.put(FaceColour.FOREGROUND,textColour);
        return face;
    }
    //Todo add the reset of the fields.




    /**
     * Getters and Setters
     *
     */


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public HashMap<FaceColour, Integer> getColours() {
        return colours;
    }

    public void setColours(HashMap<FaceColour, Integer> colours) {
        this.colours = colours;
    }

    public TapMode getTapMode() {
        return tapMode;
    }

    public void setTapMode(TapMode tapMode) {
        this.tapMode = tapMode;
    }

    public WatchDraw.FACE getBaseFace() {
        return baseFace;
    }

    public void setBaseFace(WatchDraw.FACE baseFace) {
        this.baseFace = baseFace;
    }


    /**
     *  Helper Classes
     */

    public void setColor(FaceColour name, @ColorInt Integer color){
        this.colours.put(name,color);
    }

    public @ColorInt Integer getColor(FaceColour name){
        return this.colours.get(name);
    }
}
