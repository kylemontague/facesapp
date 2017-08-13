package slurpp.faces_lib.templates;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import slurpp.faces_lib.models.ModelFace;

/**
 * Created by Kyle Montague on 21/05/2017.
 */

public class FaceConfig {

    ModelFace face;

    public enum State {
        ACTIVE,
        AMBIENT
    }

    public enum Shape {
        ROUND,
        SQUARE,
        UNKNOWN
    }


    HashMap<String,Paint> paints;

    GregorianCalendar calendar;
    Bitmap background;
    Shape shape = Shape.SQUARE;

    public FaceConfig(ModelFace face){
        this.face = face;
        this.paints = new HashMap<>();
        this.calendar = new GregorianCalendar();
    }


    public Paint getPaint(ModelFace.FaceColour colour, State state){
        String name = String.format(Locale.ENGLISH,"%s_%s",colour.name(),state.name());
        if(paints.get(name) ==null) {
            Paint p = new Paint();

//            mHourPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.thin_stroke));
//            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
//            mHourPaint.setTextSize(hoursTextSize());

            p.setColor(getColor(colour,state));
            p.setAntiAlias(state!=State.AMBIENT);
            p.setTypeface(getTypeface(colour,state));
            switch (colour) {
                case BACKGROUND:
                    break;
                case HOUR:

                    break;
                default:
                    break;
            }
            return null;
        }
        return paints.get(name);
    }

    private @ColorInt int getColor(ModelFace.FaceColour colour, State state){
        switch (colour){

        }
        return 0;
    }


    private Typeface getTypeface(ModelFace.FaceColour colour, State state){
        return null;
    }


    public Bitmap getBackground() {
        return background;
    }

    public void setBackground(Bitmap background) {
        this.background = background;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public GregorianCalendar getCalendar() {
        return calendar;
    }
}
