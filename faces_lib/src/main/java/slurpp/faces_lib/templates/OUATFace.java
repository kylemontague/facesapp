package slurpp.faces_lib.templates;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;

import slurpp.faces_lib.Prefs;
import slurpp.faces_lib.R;
import slurpp.faces_lib.utils.Font;


/**
 * Created by kyle montague on 19/12/14.
 */
public class OUATFace implements IFace, SharedPreferences.OnSharedPreferenceChangeListener {


    private Context mContext;
    private Prefs mP;

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;

    private Paint mBackgroundPaint;
    private Paint mAccentPaint;

    private double mTextScale = 1.0;



    private int[] colours = null;
    int BACKGROUND_INDEX = 0;
    int HOUR_INDEX = 1;
    int MINUTE_INDEX = -2;
    int SECOND_INDEX = 2;
    int ACCENT_INDEX = -1;
    int MERIDIAN_INDEX = -1;

    private boolean hasColour(int index){
        return (colours != null && index < colours.length && index >= 0 && colours[index] != -1);
    }


    private int getColour(int index){
        if(colours != null && index < colours.length && index >= 0){
            //request colour
            return colours[index];
        }
        return -1;
    }

    private static String name = null;
    public OUATFace(Context context){
        mContext=context;
        mP = new Prefs(context);
//        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.OUAT_FaceName);
    }


    public OUATFace(Context context, double textScale){
        mContext=context;
        mTextScale = textScale;
        mP = new Prefs(context);
//        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.OUAT_FaceName);
    }


    @Override
    public void invalidateColors() {
        mHourPaint = mAccentPaint = mMinutePaint = mSecondPaint = mBackgroundPaint = null;
    }


    @Override
    public void setColours(int[] values) {
        this.colours = values;
        invalidateColors();
    }

    @Override
    public int xOffset(Shape faceShape) {
        return (int)mContext.getResources().getDimension(R.dimen.ouat_xOffset);
    }

    @Override
    public int yOffset(Shape faceShape) {
        return (int)mContext.getResources().getDimension(R.dimen.ouat_yOffset);
    }

    @Override
    public void setAsCurrentFace() {
//        Prefs.setCurrentFace(mContext,getFaceName());
        //FaceListener.shared(mContext, getFaceName()).sendMessage(mContext.getString(R.string.PREF_CURRENT_FACE),getFaceName());
    }

    @Override
    public String getFaceName() {
        if(name == null)
            name = mContext.getString(R.string.OUAT_FaceName);
        return name;
    }

    @Override
    public Mode mode() {
        return Mode.DIGITAL;
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
            mHourPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.thin_stroke));
            mHourPaint.setAntiAlias(faceState != State.AMBIENT);
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
            mMinutePaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.thin_stroke));
            mMinutePaint.setAntiAlias(faceState != State.AMBIENT);
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
            mSecondPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.normal_stroke));
            mSecondPaint.setAntiAlias(faceState != State.AMBIENT);
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
            mBackgroundPaint.setAntiAlias(faceState != State.AMBIENT);
        }
        return mBackgroundPaint;
    }

    @Override
    public Paint accentPaint(State faceState) {
        if(mAccentPaint == null) {
            mAccentPaint = new Paint();
            mAccentPaint.setColor(accentColour(faceState));
            mAccentPaint.setStrokeWidth(mContext.getResources().getDimension(R.dimen.normal_stroke));
            mAccentPaint.setAntiAlias(faceState != State.AMBIENT);
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
        return (int)mContext.getResources().getDimension(R.dimen.large_font);
    }

    @Override
    public int hoursTextSize() {
        return (int)mContext.getResources().getDimension(R.dimen.ouat_fontsize);
    }

    @Override
    public int minutesTextSize() {
        return (int)mContext.getResources().getDimension(R.dimen.ouat_fontsize);
    }

    @Override
    public int secondsTextSize() {
        return (int)mContext.getResources().getDimension(R.dimen.ouat_fontsize);
    }

    @Override
    public int meridianTextSize() {
        return (int)mContext.getResources().getDimension(R.dimen.large_font);
    }

    @Override
    public int dateTextColour(State faceState) {
       return secondsTextColour(faceState);
    }

    @Override
    public int hoursTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(HOUR_INDEX)) ? getColour(HOUR_INDEX) : mContext.getResources().getColor(R.color.black);
    }

    @Override
    public int minutesTextColour(State faceState) {
        return hoursTextColour(faceState);
    }

    @Override
    public int secondsTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(SECOND_INDEX)) ? getColour(SECOND_INDEX) : mContext.getResources().getColor(R.color.black);    }

    @Override
    public int meridianTextColour(State faceState) {
        return hoursTextColour(faceState);
    }

    @Override
    public int backgroundColour(State faceState) {

        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_background);
        return (hasColour(BACKGROUND_INDEX)) ? getColour(BACKGROUND_INDEX) : mContext.getResources().getColor(R.color.white);
    }

    @Override
    public int accentColour(State faceState) {
        return hoursTextColour(faceState);
    }

    @Override
    public String makeString(int stringID){
        return name+"_"+mContext.getString(stringID);
    }

    @Override
    public void changedState(State faceState) {
       invalidateColors();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if(key.toUpperCase().contains(name)){
//            if(key.toUpperCase().contains(mContext.getString(R.string.PREF_BACKGROUND_COLOUR))){
//                mBackgroundPaint = null;
//            }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_HOUR_COLOUR))){
//                mHourPaint = null;
//                mAccentPaint = null;
//                mMinutePaint = null;
//                mSecondPaint = null;
//            }
//        }
    }
}
