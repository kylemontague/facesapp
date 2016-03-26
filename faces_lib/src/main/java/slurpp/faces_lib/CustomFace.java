package slurpp.faces_lib;

import java.io.Serializable;

/**
 * Created by Kyle Montague on 31/07/15.
 */
public class CustomFace implements Serializable{

    private long created;
    private long lastOn;
    private WatchDraw.FACE base;

    private static int TOTAL_COLOURS = 5;
    private int backgroundColor;
    private int accentColor;
    private int hourColor;
    private int minuteColor;
    private int secondColor;

    private String name;
    private boolean is24Hour;


    public CustomFace(long created, WatchDraw.FACE base, int[] colours, String name){
        this.created = created;
        this.base = base;
        this.name = name;
        setColours(colours);
    }


    public void setColours(int[] colours){
        if(colours!= null && colours.length == TOTAL_COLOURS){
            this.backgroundColor = colours[0];
            this.accentColor = colours[1];
            this.hourColor = colours[2];
            this.minuteColor = colours[3];
            this.secondColor = colours[4];
        }
    }

    @Override
    public String toString(){
        return "CustomFace [ created="+created+", lastOn="+lastOn+", name="+name+", base="+base.toString()+", is24Hour="+is24Hour+", background="+backgroundColor+", accent="+accentColor+", hour"+hourColor+", " +
                "minute="+minuteColor+", second="+secondColor+" ]";
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getLastOn() {
        return lastOn;
    }

    public void setLastOn(long lastOn) {
        this.lastOn = lastOn;
    }

    public WatchDraw.FACE getBase() {
        return base;
    }

    public void setBase(WatchDraw.FACE base) {
        this.base = base;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public int getHourColor() {
        return hourColor;
    }

    public void setHourColor(int hourColor) {
        this.hourColor = hourColor;
    }

    public int getMinuteColor() {
        return minuteColor;
    }

    public void setMinuteColor(int minuteColor) {
        this.minuteColor = minuteColor;
    }

    public int getSecondColor() {
        return secondColor;
    }

    public void setSecondColor(int secondColor) {
        this.secondColor = secondColor;
    }

    /** Optional parameters **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean is24Hour() {
        return is24Hour;
    }

    public void setIs24Hour(boolean is24Hour) {
        this.is24Hour = is24Hour;
    }
}

