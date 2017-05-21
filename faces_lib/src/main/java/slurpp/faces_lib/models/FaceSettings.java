package slurpp.faces_lib.models;


import com.google.gson.GsonBuilder;

/**
 * Created by Kyle Montague on 30/05/16.
 */
public class FaceSettings extends Object {



    enum TapMode{
        DATE,
        COLOR
    };

    int base;
    int background;
    int accent;
    int hour;
    int minute;
    int second;
    TapMode mode;


    public FaceSettings(int base, int[]colours, TapMode mode){
        setBase(base);
        setColours(colours);
        setMode(mode);
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }

    public static FaceSettings fromJson(String json){
        return new GsonBuilder().create().fromJson(json,FaceSettings.class);
    }

    public FaceSettings(int base, int[] colours){
        setBase(base);
        setColours(colours);
        setMode(TapMode.DATE);
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getAccent() {
        return accent;
    }

    public void setAccent(int accent) {
        this.accent = accent;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public TapMode getMode() {
        return mode;
    }

    public void setMode(TapMode mode) {
        this.mode = mode;
    }



    public int[] getColours(){
        return new int[]{getBackground(),getAccent(),getHour(),getMinute(),getSecond()};
    }

    public void setColours(int[] colours){
        if(colours!=null && colours.length>4){
            setBackground(colours[0]);
            setAccent(colours[1]);
            setHour(colours[2]);
            setMinute(colours[3]);
            setSecond(colours[4]);
        }
    }
}
