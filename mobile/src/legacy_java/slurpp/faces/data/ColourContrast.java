package slurpp.faces.data;

import android.graphics.Color;
import android.support.v7.graphics.Palette;

/**
 * Created by Kyle Montague on 09/06/15.
 */
public class ColourContrast {


    private final static int COLOUR_THRESHOLD = 500;
    private final static int BRIGHTNESS_THRESHOLD = 125;


    public static boolean isValid(Palette.Swatch c1, Palette.Swatch c2){
        return (colourDifference(c1, c2) >= COLOUR_THRESHOLD && brightnessDifference(c1, c2) >= BRIGHTNESS_THRESHOLD);
    }

    private static int colourDifference(Palette.Swatch c1, Palette.Swatch c2){
        int r = Math.abs(Color.red(c1.getRgb())-Color.red(c2.getRgb()));
        int g = Math.abs(Color.green(c1.getRgb())-Color.green(c2.getRgb()));
        int b = Math.abs(Color.blue(c1.getRgb())-Color.blue(c2.getRgb()));
        return r+g+b;
    }

    private static float brightnessDifference(Palette.Swatch c1, Palette.Swatch c2){
        float b1 = colourBrightness(c1);
        float b2 = colourBrightness(c2);
        return Math.abs(b1-b2);
    }

    private static float colourBrightness(Palette.Swatch colour){
        int r = Color.red(colour.getRgb());
        int g = Color.green(colour.getRgb());
        int b = Color.blue(colour.getRgb());
        return ((r*299.0f) + ( g*587.0f) + (b*114.0f)) / 1000.0f;
    }





}
