package slurpp.faces_lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import java.util.GregorianCalendar;

/**
 * Created by Kyle Montague on 14/06/15.
 */
public class PreviewCanvas extends View {

    private IFace mConfig;
    private IFace.Shape mShape;
    WatchDraw.FACE mWatchFace;
    GregorianCalendar mCal;
    Bitmap straps;
    boolean ambientMode;
    boolean clipBounds = false;

    public PreviewCanvas(Context context){
        super(context);
        mCal = new GregorianCalendar();
    }

    public PreviewCanvas(Context context, WatchDraw.FACE watch_face, IFace watch_config, IFace.Shape shape) {
        this(context, watch_face, watch_config, shape, false);
    }

    public PreviewCanvas(Context context, WatchDraw.FACE watch_face, IFace watch_config, IFace.Shape shape, boolean shouldClipBounds) {
        super(context);
        mWatchFace = watch_face;
        mConfig = watch_config;
        mCal = new GregorianCalendar();
        mShape = shape;
        ambientMode = false;
        clipBounds = shouldClipBounds;
        //todo need to update to square straps also
        //this.setBackgroundResource(R.drawable.watch_strap);
        //straps = BitmapFactory.decodeResource(getResources(),(mShape == Face.Shape.ROUND)?R.drawable.watch_strap:R.drawable.watch_strap);
    }



    public void update(WatchDraw.FACE face, IFace config){
        mWatchFace = face;
        mConfig = config;
    }

    public void setAmbientMode(boolean mode){
        ambientMode = mode;
    }

    @Override
    protected void onDraw(Canvas canvas){


        Rect card = new Rect(0,0,0,0);
        switch (mWatchFace){
            case BUSINESS_TIME:
                WatchDraw.BusinessTime(canvas,canvas.getClipBounds(),mConfig,mCal,ambientMode,mShape,straps,0, card, clipBounds,false);
                break;
            case FILL_TIME:
                WatchDraw.FillTime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0, card, clipBounds,false);
                break;
//            case FACES_FACE:
//                Bitmap image = BitmapFactory.decodeResource(getResources(),(mShape == Face.Shape.ROUND)?R.drawable.rounded_49:R.drawable.square_49);
//                WatchDraw.FacesFace(image, canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0);
//                break;
            case OUA_TIME:
                WatchDraw.OUATime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0, card, clipBounds,false);
                break;
            case TWO_TIME:
                WatchDraw.TwoTime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps,0, card, clipBounds,false);
                break;
            default:
                break;
        }
    }
}
