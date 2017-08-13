package slurpp.faces_lib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import slurpp.faces_lib.templates.IFace;

/**
 * Created by Kyle Montague on 21/05/2017.
 */

public class DrawUtils {


    public static Rect scaleRect(Rect bounds, float scale){
        float w = bounds.width()*scale;
        float h = bounds.height()*scale;
        float l = (bounds.width()-w)/2;
        float t = (bounds.height()-h)/2;
        return new Rect((int)(bounds.left+l),(int)(bounds.top+t),(int)(bounds.right-l),(int)(bounds.bottom-t));
    }

    public static Rect scaleRect(Rect bounds, float scaleX, float scaleY){
        float w = bounds.width()*scaleX;
        float h = bounds.height()*scaleY;
        float l = (bounds.width()-w)/2;
        float t = (bounds.height()-h)/2;
        return new Rect((int)(bounds.left+l), bounds.top,(int)(bounds.right-l),(int)(bounds.bottom-t));
    }

    public static String[] getHoursMins(int hours, int minutes){
        if(hours == 0)
            hours=12;
        String hour = NumbersToWords.convert(hours).substring(1);
        String min = NumbersToWords.convert(minutes);
        min = min.substring(1).replace(" ", "\n");

        //on the hour && mins less than ten
        if (minutes == 0)
            min = "on the\ndot";
        else if (minutes < 10)
            min = "oh\n" + min;
        else if(hours == 9 && minutes == 50){//cowboy time.
            hour = "cowboy";
            min = "time";
        }
        return new String[]{hour,min};
    }

    public static String getMins(int minutes){
        String min = NumbersToWords.convert(minutes).trim();

        //on the hour && mins less than ten
        if (minutes == 0)
            min = "on the dot";
        else if (minutes < 10)
            min = "oh " + min;
        return min;
    }

    public static Bitmap makeLinearGrad(RectF rectF, int startColour, int endColour, IFace.Shape shape){
        LinearGradient gradient = new LinearGradient(rectF.left,rectF.bottom,rectF.right,rectF.top,startColour,endColour, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);
        Bitmap bitmap = Bitmap.createBitmap((int)(rectF.width()),(int)(rectF.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        if(shape == IFace.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    public static Bitmap makeToneGrad(RectF rectF, int startColour, int endColour, float point, IFace.Shape shape){
        int[] colours = new int[]{startColour,startColour,endColour,endColour};
        float[] positions = new float[]{0f,point,point,1f};
        LinearGradient gradient = new LinearGradient(rectF.left,rectF.bottom,rectF.right,rectF.top,colours,positions, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);
        p.setStyle(Paint.Style.FILL);
        Bitmap bitmap = Bitmap.createBitmap((int)(rectF.width()),(int)(rectF.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        if(shape == IFace.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    public static Bitmap makeToneGrad(RectF rectF, int[] colours, IFace.Shape shape){
        int[] finalColours = new int[colours.length*2];
        float[] positions = new float[finalColours.length];

        int index=0;
        float v = 1.0f/colours.length;
        for(int x=0;x<colours.length;x++){
            finalColours[index]=colours[x];
            positions[index] =x*v;
            index++;
            finalColours[index]=colours[x];
            positions[index] = (x*v)+v;
            index++;
        }



        LinearGradient gradient = new LinearGradient(rectF.centerX(),rectF.bottom,rectF.centerX(),rectF.top,finalColours,positions, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);
        p.setStyle(Paint.Style.FILL);
        Bitmap bitmap = Bitmap.createBitmap((int)(rectF.width()),(int)(rectF.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        if(shape == IFace.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    public static Paint makeTonePaint(RectF rectF, int startColour, int endColour, float point, Paint base){
        int[] colours = new int[]{startColour,startColour,endColour,endColour};
        float[] positions = new float[]{0f,point,point,1f};
        LinearGradient gradient = new LinearGradient(rectF.left,rectF.bottom,rectF.right,rectF.top,colours,positions, Shader.TileMode.CLAMP);
        base.setDither(true);
        base.setShader(gradient);
        return base;
    }



    /**
     * Sets the text size for a Paint object so a given string of text will be a
     * given width.
     *
     * @param paint
     *            the Paint to set the text size for
     * @param desiredWidth
     *            the desired width
     * @param text
     *            the text that should be that width
     */
    public static void setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }



    public static String UK_DATE = "dd/MM/yy";
    public static String US_DATE = "MM/dd/yy";

    public static String SHORT_DATE = "dd MMM";
    public static String MONTH_ONLY = "MMMM";
    public static String DAY_ONLY = "dd";
    public static String YEAR_ONLY = "yyyy";

    public static String datestring(Date date, String dateFormat){
        SimpleDateFormat format = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        return format.format(date);
    }

    public static void drawDate(Canvas canvas, Rect bounds, Paint paint, Date date, String dateformat, int peekCardTop){
        String string = DrawUtils.datestring(date,dateformat);
        Paint tmp = new Paint(paint);
        tmp.setTextSize(paint.getTextSize()/1.5f);
        float txtWidth = tmp.measureText(string);
        Rect b = new Rect();
        tmp.getTextBounds(string, 0, string.length(), b);

        if(peekCardTop > 0) {
            bounds.set(bounds.left, bounds.top, bounds.right, peekCardTop);
            canvas.drawText(string, bounds.centerX() - (txtWidth / 2), bounds.centerY()+b.height(), tmp);
        }else {
            canvas.drawText(string, bounds.centerX() - (txtWidth / 2), (bounds.centerY() + (b.height() / 2)), tmp);
        }
    }

    public static int centerY(Rect bounds, Rect peekCard, int chinInset){
        int height = bounds.height()-chinInset;
        if(peekCard.top > 0)
            height = peekCard.top;
        return height/2;
    }

    public static int height(Rect bounds, Rect peekCard, int chinInset){
        int height = bounds.height()-chinInset;
        if(peekCard.top > 0)
            height = peekCard.top;
        return height;
    }


    public static float pixelsInDp(Context context, int pixels){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());

    }

    public static float pixelsInSp(Context context, float pixels) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return pixels/scaledDensity;
    }
}
