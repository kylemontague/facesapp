package montague.faces_lib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.format.Time;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by kylemontague on 13/06/15.
 */
public class WatchDraw {

    public enum FACE {
        FACES_FACE,
        OUA_TIME,
        FILL_TIME,
        BUSINESS_TIME,
        TWO_TIME;

        private static FACE[] allValues = values();

        public static FACE fromOrdinal(int n) {
            return allValues[n];
        }

        public static int[] getPreferences(FACE face) {
            //load the watch from the preferences
            switch (face) {
                case OUA_TIME:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_HOUR_COLOUR, R.string.PREF_SECOND_COLOUR};
                case FILL_TIME:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_ACCENT_COLOUR};
                case BUSINESS_TIME:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_HOUR_COLOUR, R.string.PREF_MINUTE_COLOUR, R.string.PREF_SECOND_COLOUR};
                case TWO_TIME:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_HOUR_COLOUR, R.string.PREF_SECOND_COLOUR};
                case FACES_FACE:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_ACCENT_COLOUR, R.string.PREF_HOUR_COLOUR};
                default:
                    return new int[]{R.string.PREF_CURRENT_FACE,R.string.PREF_BACKGROUND_COLOUR, R.string.PREF_ACCENT_COLOUR, R.string.PREF_HOUR_COLOUR};
            }
        }

        public static int[] getPreviewPreferences(FACE face) {
            //load the watch from the preferences
            switch (face) {
                case OUA_TIME:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_HOUR_COLOUR, R.string.PREVIEW_SECOND_COLOUR};
                case TWO_TIME:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_HOUR_COLOUR, R.string.PREVIEW_SECOND_COLOUR};
                case FILL_TIME:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_ACCENT_COLOUR};
                case BUSINESS_TIME:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_HOUR_COLOUR, R.string.PREVIEW_MINUTE_COLOUR, R.string.PREVIEW_SECOND_COLOUR};
                case FACES_FACE:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_ACCENT_COLOUR, R.string.PREVIEW_HOUR_COLOUR};
                default:
                    return new int[]{R.string.PREVIEW_BACKGROUND_COLOUR, R.string.PREVIEW_ACCENT_COLOUR, R.string.PREVIEW_HOUR_COLOUR};
            }
        }
    }







    public static void BusinessTime(Canvas canvas, Rect bounds, Face watchConfig, GregorianCalendar mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset) {
        //canvas.drawBitmap(background,bounds.centerX()-(background.getWidth()/2),bounds.centerY()-(background.getHeight()/2),null);

        if(shape == Face.Shape.SQUARE)
            canvas.drawRect(bounds,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
        else
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, watchConfig.backgroundPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE));

        Paint mHourPaint = watchConfig.hoursPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        Paint mMinutePaint =watchConfig.minutesPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        Paint mSecondPaint = watchConfig.secondsPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        Paint mAccentPaint = watchConfig.accentPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);

        int width = bounds.width();
        int height = bounds.height();

        // Find the center. Ignore the window insets so that, on round watches
        // with a "chin", the watch face is centered on the entire screen, not
        // just the usable portion.
        float centerX = width / 2f;
        float centerY = height / 2f;

        //Compute rotations and lengths for the clock hands.

        float secRot = mTime.get(Calendar.SECOND) / 30f * (float) Math.PI;
        int minutes = mTime.get(Calendar.MINUTE);
        float minRot = minutes / 30f * (float) Math.PI;
        float hrRot = ((mTime.get(Calendar.HOUR) + (minutes / 60f)) / 6f ) * (float) Math.PI;

        float r = width/2;
        float secLength = r*.9f;
        float minLength = r*.7f;
        float hrLength = r*.5f;

        // Only draw the second hand in interactive mode.
        if (!mAmbient) {
            float secX = (float) Math.sin(secRot) * secLength;
            float secY = (float) -Math.cos(secRot) * secLength;
            canvas.drawLine(centerX, centerY, centerX + secX, centerY +
                    secY, mSecondPaint);
        }

        //Draw the minute and hour hands.
        float minX = (float) Math.sin(minRot) * minLength;
        float minY = (float) -Math.cos(minRot) * minLength;
        canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mMinutePaint);
        float hrX = (float) Math.sin(hrRot) * hrLength;
        float hrY = (float) -Math.cos(hrRot) * hrLength;
        canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHourPaint);
        canvas.drawCircle(centerX,centerY,mAccentPaint.getStrokeWidth()*.6f,mHourPaint);

        if(mAmbient){
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mAccentPaint);
            canvas.drawCircle(centerX,centerY,mAccentPaint.getStrokeWidth(),mAccentPaint);
        }

    }


    public static void FillTime(Canvas canvas, Rect bounds, Face watchConfig, GregorianCalendar mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset, Rect peekCard) {

        int accentColor = watchConfig.accentColour(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        int bgColor = watchConfig.backgroundColour(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        //todo fix the start and stop positions of the seconds tick.
        RectF bF= new RectF(bounds.left,bounds.top,bounds.right,bounds.bottom);

        int hours = mTime.get(Calendar.HOUR);
        if(hours == 0)
            hours = 12;

        String timeString = hours+":"+((mTime.get(Calendar.MINUTE)<10)?"0"+mTime.get(Calendar.MINUTE):mTime.get(Calendar.MINUTE));
        Paint base = watchConfig.hoursPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE);
        setTextSizeForWidth(base, (bounds.width() * .85f), "00:00");

        Paint txtPaint;
        if(!mAmbient) {
            float split = Math.min((((mTime.get(Calendar.SECOND)+1.0f)/61.0f)*bounds.width())/bounds.width(),1.0f);
            Bitmap back;
            if (mTime.get(Calendar.MINUTE) % 2 == 0) {
                back = makeToneGrad(bF, accentColor, bgColor, split, shape);
                txtPaint = makeTonePaint(bF, bgColor, accentColor, split, base);
            } else {
                back = makeToneGrad(bF, bgColor, accentColor, split, shape);
                txtPaint = makeTonePaint(bF, accentColor, bgColor, split, base);
            }

            canvas.drawBitmap(back, bounds, bF, watchConfig.backgroundPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE));
        }else{
            if(shape == Face.Shape.SQUARE)
                canvas.drawRect(bounds,watchConfig.backgroundPaint(Face.State.AMBIENT));
            else
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), bounds.width() / 2, watchConfig.backgroundPaint(Face.State.AMBIENT));
            txtPaint = base;
            txtPaint.setStyle(Paint.Style.STROKE);
        }
        Rect b = new Rect();
        float txtWidth = txtPaint.measureText(timeString);
        txtPaint.getTextBounds("00:00", 0, "00:00".length(), b);
        if(peekCard.top > 0) {
            bF.set(bF.left, bF.top, bF.right, peekCard.top);
            canvas.drawText(timeString, bF.centerX() - (txtWidth / 2), bF.centerY()+b.height(), txtPaint);
        }else {
            canvas.drawText(timeString, bF.centerX() - (txtWidth / 2), (bF.centerY() + (b.height() / 2)), txtPaint);
        }


    }

    public static void PrideTime(Canvas canvas, Rect bounds, Face watchConfig, Time mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset) {

        //purple, blue, green, yellow, orange, red
        int colours[]= new int[]{0xFF750787, 0xFF004dff, 0xff008026, 0xffffed00, 0xffff8c00, 0xffe40303};

        int accentColor = watchConfig.accentColour(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE);
        int bgColor = watchConfig.backgroundColour(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE);
        //todo fix the start and stop positions of the seconds tick.
        RectF bF= new RectF(bounds.left,bounds.top,bounds.right,bounds.bottom);
        RectF vertBF= new RectF(bounds.centerX(),bounds.top,bounds.centerX(),bounds.bottom);


        mTime.setToNow();
        float split = (float)Math.min((((mTime.second+1.0f)/61.0f)*bounds.width())/bounds.width(),1.0f);

        //Paint txtPaint = makeTonePaint(bF,bgColor,accentColor,split,watchConfig.hoursPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE));
        Bitmap back = makeToneGrad(bF,colours, shape);
        canvas.drawBitmap(back,bounds,bF,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
        String timeString = (mTime.hour%12)+":"+((mTime.minute<10)?"0"+mTime.minute:mTime.minute);
        canvas.drawText(timeString, bF.centerX() + watchConfig.xOffset(shape), bounds.centerY() + watchConfig.yOffset(shape), watchConfig.hoursPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE));


    }


    public static void OUATime(Canvas canvas, Rect bounds, Face watchConfig, GregorianCalendar mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset, Rect peekCard) {
        //canvas.drawBitmap(background,bounds.centerX()-(background.getWidth()/2),bounds.centerY()-(background.getHeight()/2),null);

        if(shape == Face.Shape.SQUARE)
            canvas.drawRect(bounds,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
        else
            canvas.drawCircle(bounds.centerX(),bounds.centerY(),bounds.width()/2,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));


        int width = bounds.width();
        int height = bounds.height();

        float centerX = width / 2f;
        float centerY = height / 2f;

        //draw second counter
        if (!mAmbient) {
            int radius = (int)((width/2)*.9f);//todo 128;
            int angle = mTime.get(Calendar.SECOND) * 6;
            RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawArc(rectF, -90, angle, false, watchConfig.secondsPaint(mAmbient? Face.State.AMBIENT:Face.State.ACTIVE));
        }

        String[] values = getHoursMins(mTime.get(Calendar.HOUR_OF_DAY)%12,mTime.get(Calendar.MINUTE));
        if(values.length > 1) {
            canvas.drawText(values[0], centerX + watchConfig.xOffset(shape), centerY +watchConfig.yOffset(shape), watchConfig.hoursPaint(mAmbient? Face.State.AMBIENT:Face.State.ACTIVE));
            String[] minValues = values[1].split("\n");
            for(int x=0; x<minValues.length;x++)
                canvas.drawText(minValues[x], centerX + watchConfig.xOffset(shape), centerY+(x*(height/6)), watchConfig.minutesPaint(mAmbient? Face.State.AMBIENT:Face.State.ACTIVE));
        }
    }

    public static void TwoTime(Canvas canvas, Rect bounds, Face watchConfig, GregorianCalendar mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset, Rect peekCard) {
        //canvas.drawBitmap(background,bounds.centerX()-(background.getWidth()/2),bounds.centerY()-(background.getHeight()/2),null);
        RectF bF= new RectF(bounds.left,bounds.top,bounds.right,bounds.bottom);

        Bitmap bgBit = makeLinearGrad(bF,watchConfig.backgroundColour(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE), watchConfig.accentColour(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE), shape);
        canvas.drawBitmap(bgBit, bounds, bounds, watchConfig.backgroundPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE));

        int width = bounds.width();
        int height = bounds.height()-chinInset;
        if(peekCard.top > 0)
            height = peekCard.top;

        float centerX = width / 2f;
        float centerY = height / 2f;

        //draw second counter
//        if (!mAmbient) {
//            int radius = (int)((width/2)*.9f);//todo 128;
//            int angle = mTime.second * 6;
//            RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
//            canvas.drawArc(rectF, -90, angle, false, watchConfig.secondsPaint(mAmbient? Face.State.AMBIENT:Face.State.ACTIVE));
//        }

        int hours = mTime.get(Calendar.HOUR_OF_DAY)%12;
        if(hours == 0)
            hours = 12;
        String hourStr = "";
        if(hours < 10)
            hourStr+="0";
        hourStr+=hours;

        float yPos = centerY;
        String mintues = getMins(mTime.get(Calendar.MINUTE)).replace("-\n","");

        Paint hourPaint =  watchConfig.hoursPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE);

        float hourWidth = hourPaint.measureText(hourStr);

        Paint minPaint = watchConfig.minutesPaint(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE);
        float minWidth = minPaint.measureText(mintues);
        if (mAmbient) {
            Path textPath = new Path();
            hourPaint.getTextPath(hourStr, 0, hourStr.length(), centerX - (hourWidth / 2), yPos, textPath);
            hourPaint.setStyle(Paint.Style.STROKE);
            // Measure the path
            // PathEffect effect = new DashPathEffect(new float[] { 10, 5 }, 1.0f);
            // hourPaint.setPathEffect(effect);
            //canvas.drawPath(textPath, hourPaint);
            canvas.drawText(hourStr, centerX - (hourWidth / 2), yPos, hourPaint);
        }else{
            hourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawText(hourStr, centerX - (hourWidth/2), yPos,hourPaint);
        }





        int shift = (watchConfig.hoursTextSize()/5);

        canvas.drawLine(centerX-Math.max(hourWidth, minWidth)/2,yPos+shift,centerX+Math.max(hourWidth, minWidth)/2,yPos+shift,hourPaint);
        yPos+=(hourPaint.descent()-hourPaint.ascent())/2+10;

        canvas.drawText(mintues, centerX - (minWidth/2), yPos, watchConfig.minutesPaint(mAmbient? Face.State.AMBIENT:Face.State.ACTIVE));
        yPos+=(minPaint.descent()-minPaint.ascent())/2;
        canvas.drawLine(centerX-Math.max(hourWidth, minWidth)/2,yPos,centerX+Math.max(hourWidth, minWidth)/2,yPos,hourPaint);

    }



    public static void FacesFace(Bitmap image, Canvas canvas, Rect bounds, Face watchConfig, GregorianCalendar mTime, Boolean mAmbient, Face.Shape shape, Bitmap background, int chinInset) {
        //canvas.drawBitmap(background,bounds.centerX()-(background.getWidth()/2),bounds.centerY()-(background.getHeight()/2),null);

        if(shape == Face.Shape.SQUARE)
            canvas.drawRect(bounds,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
        else
            canvas.drawCircle(bounds.centerX(),bounds.centerY(),bounds.width()/2,watchConfig.backgroundPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
        int angle = -10;
        canvas.drawBitmap(image,new Rect(0,0,image.getWidth(),image.getHeight()),new Rect(0,0,bounds.right+3,bounds.bottom+3),null);
//        String timeString = "Faces";//(mTime.hour%12)+":"+((mTime.minute<10)?"0"+mTime.minute:mTime.minute);
//        canvas.rotate(angle,centerX,centerY);
//        canvas.drawText(timeString, centerX + watchConfig.xOffset(shape), centerY + watchConfig.yOffset(shape), watchConfig.hoursPaint(mAmbient?Face.State.AMBIENT:Face.State.ACTIVE));
//        canvas.restore();

    }


    //***** DRAWING UTILITIES ******//
    private static String[] getHoursMins(int hours, int minutes){
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

    private static String getMins(int minutes){
        String min = NumbersToWords.convert(minutes).trim();

        //on the hour && mins less than ten
        if (minutes == 0)
            min = "on the dot";
        else if (minutes < 10)
            min = "oh " + min;
        return min;
    }

    private static Bitmap makeLinearGrad(RectF rectF, int startColour, int endColour, Face.Shape shape){
        LinearGradient gradient = new LinearGradient(rectF.left,rectF.bottom,rectF.right,rectF.top,startColour,endColour, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);
        Bitmap bitmap = Bitmap.createBitmap((int)(rectF.width()),(int)(rectF.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        if(shape == Face.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    private static Bitmap makeToneGrad(RectF rectF, int startColour, int endColour, float point, Face.Shape shape){
        int[] colours = new int[]{startColour,startColour,endColour,endColour};
        float[] positions = new float[]{0f,point,point,1f};
        LinearGradient gradient = new LinearGradient(rectF.left,rectF.bottom,rectF.right,rectF.top,colours,positions, Shader.TileMode.CLAMP);
        Paint p = new Paint();
        p.setDither(true);
        p.setShader(gradient);
        p.setStyle(Paint.Style.FILL);
        Bitmap bitmap = Bitmap.createBitmap((int)(rectF.width()),(int)(rectF.height()), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        if(shape == Face.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    private static Bitmap makeToneGrad(RectF rectF, int[] colours, Face.Shape shape){
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
        if(shape == Face.Shape.SQUARE)
            c.drawRect(rectF,p);
        else
            c.drawCircle(rectF.centerX(),rectF.centerY(),rectF.width()/2,p);
        return bitmap;
    }

    private static Paint makeTonePaint(RectF rectF, int startColour, int endColour, float point, Paint base){
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
    private static void setTextSizeForWidth(Paint paint, float desiredWidth,
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
}
