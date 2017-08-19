package slurpp.faces;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by kylemontague on 08/06/15.
 */
public class ColourUtil {


    /**
     *
     * Generate a new colour at a random interval / offset from the input colour. Offsets the R,G,B values by the same random offset.
     *
     * @param rgb
     * @return the new colour integer value.
     */
    public static int randomOffset(int rgb){
        int red = Color.red(rgb);
        int green = Color.green(rgb);
        int blue = Color.blue(rgb);

        int offset = 20;
        Random ran = new Random();
        float value = (red+green+blue)/3;
        float newValue = value + 2*ran.nextInt() * offset - offset;
        float valueRatio = newValue / value;
        int r = (int) (red * valueRatio);
        int g = (int) (green * valueRatio);
        int b = (int) (blue * valueRatio);

        return android.graphics.Color.rgb(r, g, b);


    }


    private static float[] hsl(int colour){
        float[] hsv = new float[3];
        Color.colorToHSV(colour,hsv);
        return hsv;
    }

    public static int[] analogicOffset(int colour, int variation, int count){
        count = Math.max(3,count);//return at least 3 colours.
        variation = Math.max(10,variation); //shift the colours by a minimum hue of 10;
        float[] base = hsl(colour);
        int[] colours = new int[count];
        int max = count/2;
        int i=0;
        for(int x=(0-max);x<max;x++){
            colours[i] = Color.HSVToColor(new float[]{normalizeAngle(base[0]+(variation*x)),base[1],base[2]});
            i++;
        }
        return colours;
    }

    private static int normalizeAngle(float angle)
    {
        if(angle >= 360)
            angle-=360;
        return (int)angle;
    }

    public static ArrayList<Integer> GenerateAnalogous(int colour, int count){
        return GenerateColors_Harmony(count,colour,15f,30f,80f,0f,0f,.8f,.9f);
    }

    public static ArrayList<Integer> GenerateTriad(int colour, int count){
        return GenerateColors_Harmony(count,colour,120f,240f,40f,40f,40f,1f,1f);
    }

    private static ArrayList<Integer> GenerateColors_Harmony(int colorCount, int referenceColour, float offsetAngle1, float offsetAngle2, float rangeAngle0,
            float rangeAngle1, float rangeAngle2, float saturationRange, float luminanceRange)
    {
        float[] hsb = new float[3];
        Color.colorToHSV(referenceColour,hsb);
        float referenceAngle = hsb[0];
        float saturation = hsb[1];
        float luminance = hsb[2];

        ArrayList<Integer> colors = new ArrayList();
        Random ran = new Random(System.currentTimeMillis());
        for (int i = 0; i < colorCount; i++)
        {
            float randomAngle = ran.nextFloat() * (rangeAngle0 + rangeAngle1 + rangeAngle2);
            if (randomAngle > rangeAngle0)
            {
                if (randomAngle < rangeAngle0 + rangeAngle1)
                {
                    randomAngle += offsetAngle1;
                }
                else
                {
                    randomAngle += offsetAngle2;
                }
            }

            float newSaturation = saturation + (ran.nextFloat() - 0.5f) * saturationRange;
            float newLuminance = luminance + (ran.nextFloat() - 0.5f) * luminanceRange;


            float hue = normalizeAngle(referenceAngle + randomAngle);
            int c = Color.HSVToColor(new float[]{hue, newSaturation, newLuminance});
            Log.d("COLOURS","OUTPUT ["+i+"]:"+hue+", "+newSaturation+", "+newLuminance+" = "+c);
            colors.add(c);
        }

        return colors;
    }

//    public static int[] goldenOffset(int rgb, int count){
//        int[] colours = new int[count];
//        Random ran = new Random();
//        float offset = ran.nextFloat();
//
//        for (int i = 0; i < count; i++)
//            colours[i] = gradient.GetColor(offset + (0.618033988749895f * i) % 1);
//
//        return colours;
//    }




    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }

    private static String colourFromRGB(int rgb){
        return String.format("#%06X", 0xFFFFFF & rgb);
    }

}
