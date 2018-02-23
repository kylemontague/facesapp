package slurpp.faces_lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import com.manolovn.trianglify.generator.point.RegularPointGenerator;

import java.util.GregorianCalendar;

import slurpp.faces_lib.models.ModelFace;
import slurpp.faces_lib.templates.FacesTrianglifyDrawable;
import slurpp.faces_lib.templates.IFace;
import slurpp.faces_lib.templates.PaletteColorGenerator;
import slurpp.faces_lib.v2.models.FaceSettings;

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
    ModelFace face;

    public PreviewCanvas(Context context){
        super(context);
        mCal = new GregorianCalendar();
    }

    public PreviewCanvas(Context context, CustomFace customFace){
        super(context);
        mWatchFace = customFace.getBase();
        mCal = new GregorianCalendar();
    }


    public PreviewCanvas(Context context, ModelFace customFace){
        super(context);
        face = customFace;
        mWatchFace = face.getBaseFace();
        mCal = new GregorianCalendar();

    }

    public PreviewCanvas(Context context, FaceSettings settings, IFace.Shape shape){
        super(context);
        mWatchFace = settings.getBase();

        mCal = new GregorianCalendar();
        mShape = shape;
        ambientMode = false;
        clipBounds = false;

    }

    public PreviewCanvas(Context context, WatchDraw.FACE watch_face, IFace watch_config, IFace.Shape shape) {
        this(context, watch_face, watch_config, shape, shape == IFace.Shape.ROUND);
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


    WatchDraw.FACE currentFace;
    Bitmap backgroundImage;
    WatchDraw.FaceMode faceMode = WatchDraw.FaceMode.TIME;

    int stepCount = 0;


    @Override
    protected void onDraw(Canvas canvas){


        Rect card = new Rect(0,0,0,0);
        switch (mWatchFace){
            case BUSINESS_TIME:
                currentFace = WatchDraw.FACE.BUSINESS_TIME;
                WatchDraw.BusinessTime(canvas,canvas.getClipBounds(),mConfig,mCal,ambientMode,mShape,straps,0, card, clipBounds,faceMode,stepCount);
                break;
            case FILL_TIME:
                currentFace = WatchDraw.FACE.FILL_TIME;
                WatchDraw.FillTime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0, card, clipBounds,faceMode,stepCount);
                break;
//            case FACES_FACE:
//                Bitmap image = BitmapFactory.decodeResource(getResources(),(mShape == Face.Shape.ROUND)?R.drawable.rounded_49:R.drawable.square_49);
//                WatchDraw.FacesFace(image, canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0);
//                break;
            case OUA_TIME:
                currentFace = WatchDraw.FACE.OUA_TIME;
                WatchDraw.OUATime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps, 0, card, clipBounds,faceMode,stepCount);
                break;
            case TWO_TIME:
                currentFace = WatchDraw.FACE.TWO_TIME;
                WatchDraw.TwoTime(canvas, canvas.getClipBounds(), mConfig, mCal, ambientMode, mShape, straps,0, card, clipBounds,faceMode,stepCount);
                break;
            case PRISM_TIME:

                if(currentFace!= WatchDraw.FACE.PRISM_TIME){
                    IFace.State state = ambientMode? IFace.State.AMBIENT: IFace.State.ACTIVE;
                    int accentColor = mConfig.accentColour(state);
                    int bgColor = mConfig.backgroundColour(state);
                    int meridianColor = mConfig.meridianTextColour(state);
                    Rect bounds = canvas.getClipBounds();
                    FacesTrianglifyDrawable drawable = new FacesTrianglifyDrawable(5, 5, 200, 75, new PaletteColorGenerator(new int[]{bgColor,accentColor,meridianColor}),new RegularPointGenerator(),bounds);
                    drawable.setBounds(new Rect(0,0,bounds.width()*3, bounds.height()*3));
                    drawable.triangulate();
                    backgroundImage = WatchDraw.drawableToBitmap(drawable,bounds.width()*4, bounds.height()*4);
                }

                currentFace = WatchDraw.FACE.PRISM_TIME;
                WatchDraw.SpaceTimePrism(canvas,canvas.getClipBounds(),mConfig,mCal,ambientMode,mShape,straps,0,card,clipBounds,faceMode,backgroundImage,stepCount);
            default:
                break;
        }
    }
}
