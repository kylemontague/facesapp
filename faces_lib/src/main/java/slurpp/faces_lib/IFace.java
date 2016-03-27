package slurpp.faces_lib;

import android.graphics.Paint;
import android.graphics.Typeface;


/**
 * Created by Kyle Montague on 19/12/14.
 */
public interface IFace {

    enum Mode {
        ANALOG,
        DIGITAL
    }

    enum Shape {
        ROUND,
        SQUARE,
        UNKNOWN
    }

    enum State {
        ACTIVE,
        AMBIENT
    }

    void setColours(int[] values);

    int xOffset(Shape faceShape);

    int yOffset(Shape faceShape);

    void setAsCurrentFace();
    String getFaceName();


    Mode mode();



    Paint datePaint(State faceState);

    Paint hoursPaint(State faceState);

    Paint minutesPaint(State faceState);

    Paint secondsPaint(State faceState);

    Paint meridianPaint(State faceState);

    Paint backgroundPaint(State faceState);

    Paint accentPaint(State faceState);


    Typeface dateType(State faceState);

    Typeface hoursType(State faceState);

    Typeface minutesType(State faceState);

    Typeface secondsType(State faceState);

    Typeface meridianType(State faceState); //am pm

    int dateTextSize();

    int hoursTextSize();

    int minutesTextSize();

    int secondsTextSize();

    int meridianTextSize();

    int dateTextColour(State faceState);

    int hoursTextColour(State faceState);

    int minutesTextColour(State faceState);

    int secondsTextColour(State faceState);

    int meridianTextColour(State faceState);

    int backgroundColour(State faceState);

    int accentColour(State faceState);

    String makeString(int stringID);

    void changedState(State faceState);

}
