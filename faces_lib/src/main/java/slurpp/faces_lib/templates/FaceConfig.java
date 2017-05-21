package slurpp.faces_lib.templates;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import slurpp.faces_lib.models.ModelFace;

/**
 * Created by Kyle Montague on 21/05/2017.
 */

public class FaceConfig {

    ModelFace face;

    enum State {
        ACTIVE,
        AMBIENT
    }

    enum Shape {
        ROUND,
        SQUARE,
        UNKNOWN
    }

    public FaceConfig(ModelFace face){
        this.face = face;
    }


    public Paint getPaint(ModelFace.FaceColour colour, State state){
        switch (colour){
            case BACKGROUND:
                break;
            default:
                break;
        }
        return null;
    }

    private Color getColor(ModelFace.FaceColour colour, State state){
        switch (colour){

        }
        return null;
    }


    private Typeface getTypeface(ModelFace.FaceColour colour, State state){
        return null;
    }

    public int xOffset(){
        return 0;
    }

    public int yOffset(){
        return 0;
    }
}
