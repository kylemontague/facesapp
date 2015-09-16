package montague.faces_lib;

import android.graphics.Paint;
import android.graphics.Typeface;


/**
 * Created by Kyle Montague on 19/12/14.
 */
public interface Face {

    enum Mode {
        ANALOG,
        DIGITAL
    }

    enum Shape {
        ROUND,
        SQUARE
    }

    enum State {
        ACTIVE,
        AMBIENT
    }

    public int xOffset(Shape faceShape);

    public int yOffset(Shape faceShape);

    public void setAsCurrentFace();
    public String getFaceName();


    public Mode mode();


    public Paint datePaint(State faceState);

    public Paint hoursPaint(State faceState);

    public Paint minutesPaint(State faceState);

    public Paint secondsPaint(State faceState);

    public Paint meridianPaint(State faceState);

    public Paint backgroundPaint(State faceState);

    public Paint accentPaint(State faceState);


    public Typeface dateType(State faceState);

    public Typeface hoursType(State faceState);

    public Typeface minutesType(State faceState);

    public Typeface secondsType(State faceState);

    public Typeface meridianType(State faceState); //am pm

    public int dateTextSize();

    public int hoursTextSize();

    public int minutesTextSize();

    public int secondsTextSize();

    public int meridianTextSize();

    public int dateTextColour(State faceState);

    public int hoursTextColour(State faceState);

    public int minutesTextColour(State faceState);

    public int secondsTextColour(State faceState);

    public int meridianTextColour(State faceState);

    public int backgroundColour(State faceState);

    public int accentColour(State faceState);

    public String makeString(int stringID);

    public void changedState(State faceState);

}
