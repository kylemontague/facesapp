package slurpp.faces_lib;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;


/**
 * Created by kylemontague on 19/12/14.
 */
public class BusinessIFaceConfig implements IFace, SharedPreferences.OnSharedPreferenceChangeListener {



    private Context mContext;

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;

    private Paint mBackgroundPaint;
    private Paint mAccentPaint;

    private static String name = null;

    private int[] colours = null;
    int BACKGROUND_INDEX = 0;
    int HOUR_INDEX = 1;
    int MINUTE_INDEX = 2;
    int SECOND_INDEX = 3;
    int ACCENT_INDEX = -1;
    int MERIDIAN_INDEX = -1;


    public BusinessIFaceConfig(Context context){
        mContext=context;
        getFaceName();
    }

    public BusinessIFaceConfig(Context context, int[] colours){
        this.mContext=context;
        this.colours = colours;
        getFaceName();
    }


    @Override
    public void setColours(int[] values) {
        colours = values;
    }

    @Override
    public int xOffset(Shape faceShape) {
        return ((faceShape == Shape.ROUND)?-80:-80);
    }

    @Override
    public int yOffset(Shape faceShape) {
        return ((faceShape == Shape.ROUND)?30:30);
    }

    @Override
    public void setAsCurrentFace() {
        Prefs.setCurrentFace(mContext,getFaceName());
        //FaceListener.shared(mContext, getFaceName()).sendMessage(mContext.getString(R.string.PREF_CURRENT_FACE),getFaceName());
    }

    @Override
    public String getFaceName() {
        if(name == null)
            name = mContext.getString(R.string.Business_FaceName);
        return name;
    }

    @Override
    public Mode mode() {
        return Mode.ANALOG;
    }

    @Override
    public Paint datePaint(State faceState) {
        return minutesPaint(faceState);
    }

    @Override
    public Paint hoursPaint(State faceState) {
        if(mHourPaint == null) {
            mHourPaint = new Paint();
            mHourPaint.setColor(hoursTextColour(faceState));
            mHourPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.business_hour_stroke));
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStyle((faceState!=State.AMBIENT)? Paint.Style.FILL_AND_STROKE: Paint.Style.STROKE);
            mHourPaint.setTypeface(hoursType(faceState));
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            mHourPaint.setTextSize(hoursTextSize());
        }
        return mHourPaint;
    }

    @Override
    public Paint minutesPaint(State faceState) {
        if(mMinutePaint == null) {
            mMinutePaint = new Paint();
            mMinutePaint.setColor(minutesTextColour(faceState));
            mMinutePaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.business_min_stroke));
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setTypeface(minutesType(faceState));
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutePaint.setTextSize(minutesTextSize());
        }
        return mMinutePaint;
    }

    @Override
    public Paint secondsPaint(State faceState) {
        if(mSecondPaint == null) {
            mSecondPaint = new Paint();
            mSecondPaint.setColor(secondsTextColour(faceState));
            mSecondPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.business_sec_stroke));
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStyle(Paint.Style.STROKE);
            mSecondPaint.setTypeface(secondsType(faceState));
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
            mSecondPaint.setTextSize(secondsTextSize());
        }
        return mSecondPaint;
    }

    @Override
    public Paint meridianPaint(State faceState) {
        return minutesPaint(faceState);
    }

    @Override
    public Paint backgroundPaint(State faceState) {
        if(mBackgroundPaint == null) {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(backgroundColour(faceState));
            mBackgroundPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.thin_stroke));
            mBackgroundPaint.setAntiAlias(faceState==State.AMBIENT?false:true);
        }
        return mBackgroundPaint;
    }

    @Override
    public Paint accentPaint(State faceState) {
        if(mAccentPaint == null) {
            mAccentPaint = new Paint();
            mAccentPaint.setColor(accentColour(faceState));
            mAccentPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.business_holo_stroke));
            mAccentPaint.setAntiAlias(true);
            mAccentPaint.setTypeface(secondsType(faceState));
            mAccentPaint.setStrokeCap(Paint.Cap.ROUND);
            mAccentPaint.setTextSize(secondsTextSize());
        }
        return mAccentPaint;
    }

    @Override
    public Typeface dateType(State faceState) {
        return Font.get(mContext,Font.PROXI_LIGHT);
    }

    @Override
    public Typeface hoursType(State faceState) {
        return (faceState == State.ACTIVE)?Font.get(mContext,Font.PROXI_SEMIBOLD):Font.get(mContext,Font.PROXI_LIGHT);
    }

    @Override
    public Typeface minutesType(State faceState) {
        return Font.get(mContext,Font.PROXI_LIGHT);
    }

    @Override
    public Typeface secondsType(State faceState) {
        return Font.get(mContext,Font.PROXI_LIGHT);
    }

    @Override
    public Typeface meridianType(State faceState) {
        return (faceState == State.ACTIVE)?Font.get(mContext,Font.PROXI_SEMIBOLD):Font.get(mContext,Font.PROXI_LIGHT);
    }

    @Override
    public int dateTextSize() {
        return 30;
    }

    @Override
    public int hoursTextSize() {
        return 80;
    }

    @Override
    public int minutesTextSize() {
        return 80;
    }

    @Override
    public int secondsTextSize() {
        return 53;
    }

    @Override
    public int meridianTextSize() {
        return 30;
    }

    @Override
    public int dateTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(SECOND_INDEX)) ? getColour(SECOND_INDEX) : mContext.getResources().getColor(R.color.business_default_second);
    }

    @Override
    public int hoursTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(HOUR_INDEX)) ? getColour(HOUR_INDEX) : mContext.getResources().getColor(R.color.business_default_hour);
    }

    @Override
    public int minutesTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(MINUTE_INDEX)) ? getColour(MINUTE_INDEX) : mContext.getResources().getColor(R.color.business_default_minute);
    }

    @Override
    public int secondsTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(SECOND_INDEX)) ? getColour(SECOND_INDEX) : mContext.getResources().getColor(R.color.business_default_second);
    }

    @Override
    public int meridianTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_background);
        return (hasColour(MERIDIAN_INDEX)) ? getColour(MERIDIAN_INDEX) : mContext.getResources().getColor(R.color.business_default_background);
    }

    @Override
    public int backgroundColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_background);
        return (hasColour(BACKGROUND_INDEX)) ? getColour(BACKGROUND_INDEX) : mContext.getResources().getColor(R.color.business_default_background);
    }

    @Override
    public int accentColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_background);
        return (hasColour(ACCENT_INDEX)) ? getColour(ACCENT_INDEX) : mContext.getResources().getColor(R.color.light);
    }


    private boolean hasColour(int index){
        return (colours != null && index < colours.length && index >= 0)?true:false;
    }
    private int getColour(int index){
        if(colours != null && index < colours.length && index >= 0){
            //request colour
            return colours[index];
        }
        return -1;
    }

    @Override
    public String makeString(int stringID){
        return name+"_"+mContext.getString(stringID);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.toUpperCase().contains(name)){
            if(key.toUpperCase().contains(mContext.getString(R.string.PREF_BACKGROUND_COLOUR))){
                mBackgroundPaint = null;
            }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_HOUR_COLOUR))){
                mHourPaint = null;
                mAccentPaint = null;
                mMinutePaint = null;
                mSecondPaint = null;
            }
        }
    }

    @Override
    public void changedState(State faceState) {
        mAccentPaint = null;
        mHourPaint = null;
        mBackgroundPaint = null;
        mSecondPaint = null;
        mMinutePaint = null;
    }
}
