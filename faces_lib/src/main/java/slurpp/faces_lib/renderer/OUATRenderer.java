package slurpp.faces_lib.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.Calendar;

import slurpp.faces_lib.IFaceRenderer;
import slurpp.faces_lib.models.ModelFace;
import slurpp.faces_lib.templates.FaceConfig;
import slurpp.faces_lib.utils.DrawUtils;
import slurpp.faces_lib.utils.NumbersToWords;

import static slurpp.faces_lib.utils.DrawUtils.scaleRect;

/**
 * Created by Kyle Montague on 21/05/2017.
 */

public class OUATRenderer implements IFaceRenderer {

    Rect peekCard = new Rect();
    int chinInset = 0;
    boolean isPreview = false;
    Context context;

    public OUATRenderer(Context context) {
        this.context = context;
    }


    public OUATRenderer(Context context, Rect peekCard, int chinInset, boolean isPreview) {
        this.peekCard = peekCard;
        this.chinInset = chinInset;
        this.isPreview = isPreview;
        this.context = context;
    }



    @Override
    public void setPeekCard(Rect peekCard) {
        this.peekCard = peekCard;
    }

    @Override
    public void setIsPreview(boolean isPreview) {
        this.isPreview = isPreview;
    }

    @Override
    public void setChinInset(int chinInset) {
        this.chinInset = chinInset;
    }

    @Override
    public Rect getPeekCard() {
        return this.peekCard;
    }

    @Override
    public boolean isPreview() {
        return this.isPreview;
    }

    @Override
    public int getChinInset() {
        return this.chinInset;
    }


    @Override
    public float xOffset() {
        return DrawUtils.pixelsInDp(this.context,60);
    }

    @Override
    public float yOffset() {
        return DrawUtils.pixelsInDp(this.context,35);
    }



    @Override
    public void onDraw(Canvas canvas, Rect bounds, FaceConfig faceConfig, FaceConfig.State mAmbient, boolean showDate) {


        if(faceConfig.getShape() == FaceConfig.Shape.SQUARE) {
            if(isPreview)
                bounds = scaleRect(bounds,.9f, .99f);
            canvas.drawRect(bounds, faceConfig.getPaint(ModelFace.FaceColour.BACKGROUND,mAmbient));
        }
        else
            canvas.drawCircle(bounds.centerX(),bounds.centerY(),bounds.width()/2,faceConfig.getPaint(ModelFace.FaceColour.BACKGROUND,mAmbient));


        int width = bounds.width();
        int height = bounds.height();

        float centerX =bounds.centerX();
        float centerY = bounds.centerY();
        String[] values;
        if(!showDate) {
            //draw second counter
            if (mAmbient == FaceConfig.State.ACTIVE) {
                int radius = (int) ((width / 2) * .9f);
                int angle = faceConfig.getCalendar().get(Calendar.SECOND) * 6;
                RectF rectF = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
                canvas.drawArc(rectF, -90, angle, false, faceConfig.getPaint(ModelFace.FaceColour.SECOND,mAmbient));
            }

            values=DrawUtils.getHoursMins(faceConfig.getCalendar().get(Calendar.HOUR_OF_DAY) % 12, faceConfig.getCalendar().get(Calendar.MINUTE));

        }else{
            String day = NumbersToWords.convert(faceConfig.getCalendar().get(Calendar.DAY_OF_MONTH));
            day = day.substring(1).replace(" ", "\n");
            values = new String[]{DrawUtils.datestring(faceConfig.getCalendar().getTime(), DrawUtils.MONTH_ONLY).toLowerCase(),day};
        }

        if (values.length > 1) {
            canvas.drawText(values[0], centerX + xOffset(), centerY + yOffset(), faceConfig.getPaint(ModelFace.FaceColour.HOUR,mAmbient));
            String[] minValues = values[1].split("\n");
            for (int x = 0; x < minValues.length; x++)
                canvas.drawText(minValues[x], centerX + xOffset(), centerY + (x * (height / 6)), faceConfig.getPaint(ModelFace.FaceColour.MINUTE,mAmbient));
        }

    }
}
