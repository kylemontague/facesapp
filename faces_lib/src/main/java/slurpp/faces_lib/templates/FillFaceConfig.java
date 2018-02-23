package slurpp.faces_lib.templates;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;

import slurpp.faces_lib.Prefs;
import slurpp.faces_lib.R;
import slurpp.faces_lib.utils.Font;

/**
 * Created by kylemontague on 19/12/14.
 */
public class FillFaceConfig implements IFace, SharedPreferences.OnSharedPreferenceChangeListener {


    private Context mContext;

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;

    private Paint mBackgroundPaint;
    private Paint mAccentPaint;

    private Prefs mP;
    private static String name = null;
    private double mTextScale = 1.0;



    private int[] colours = null;
    int BACKGROUND_INDEX = 0;
    int HOUR_INDEX = -1;
    int MINUTE_INDEX = -1;
    int SECOND_INDEX = -1;
    int ACCENT_INDEX = 1;
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


    public FillFaceConfig(Context context){
        mContext=context;
        mP = new Prefs(context);
//        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.FillUp_FaceName);
    }

    public FillFaceConfig(Context context, double textScale){
        mTextScale = textScale;
        mContext=context;
        mP = new Prefs(context);
//        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.FillUp_FaceName);
    }

    @Override
    public void invalidateColors() {
        mHourPaint = mAccentPaint = mMinutePaint = mSecondPaint = mBackgroundPaint = null;
    }

    @Override
    public void setColours(int[] values) {
        this.colours  = values;
        invalidateColors();
    }

    @Override
    public int xOffset(Shape faceShape) {
        return (int)mTextScale*((faceShape == Shape.ROUND)?-80:-80);
    }

    @Override
    public int yOffset(Shape faceShape) {
        return (int)mTextScale*((faceShape == Shape.ROUND)?30:30);
    }

    @Override
    public void setAsCurrentFace() {
//        Prefs.setCurrentFace(mContext,getFaceName());
        //FaceListener.shared(mContext, getFaceName()).sendMessage(mContext.getString(R.string.PREF_CURRENT_FACE),getFaceName());
    }

    @Override
    public String getFaceName() {
        if(name == null)
            name = mContext.getString(R.string.FillUp_FaceName);
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
        mHourPaint = new Paint();
        mHourPaint.setColor(hoursTextColour(faceState));
        mHourPaint.setStrokeWidth(1.0f);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setTypeface(hoursType(faceState));
        mHourPaint.setStrokeCap(Paint.Cap.ROUND);
        mHourPaint.setTextSize(hoursTextSize());
        mHourPaint.setStyle(faceState==State.AMBIENT? Paint.Style.STROKE: Paint.Style.FILL_AND_STROKE);
        return mHourPaint;
    }

    @Override
    public Paint minutesPaint(State faceState) {
        if(mMinutePaint == null) {
            mMinutePaint = new Paint();
            mMinutePaint.setColor(minutesTextColour(faceState));
            mMinutePaint.setStrokeWidth(1.0f);
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
            mSecondPaint.setStrokeWidth(2.0f);
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
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(backgroundColour(faceState));
        mBackgroundPaint.setStrokeWidth(1.0f);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAntiAlias(faceState==State.AMBIENT?false:true);
        return mBackgroundPaint;
    }

    @Override
    public Paint accentPaint(State faceState) {
        if(mAccentPaint == null) {
            mAccentPaint = new Paint();
            mAccentPaint.setColor(accentColour(faceState));
            mAccentPaint.setStyle(Paint.Style.FILL);
            mAccentPaint.setAntiAlias(faceState == State.AMBIENT ? false : true);
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
        return (int)mTextScale*30;
    }

    @Override
    public int hoursTextSize() {
        return (int)mTextScale*80;
    }

    @Override
    public int minutesTextSize() {
        return (int)mTextScale*80;
    }

    @Override
    public int secondsTextSize() {
        return (int)mTextScale*53;
    }

    @Override
    public int meridianTextSize() {
        return (int)mTextScale*30;
    }

    @Override
    public int dateTextColour(State faceState) {
        return secondsTextColour(faceState);
    }

    @Override
    public int hoursTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(HOUR_INDEX)) ? getColour(HOUR_INDEX) : mContext.getResources().getColor(R.color.white);
    }

    @Override
    public int minutesTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(MINUTE_INDEX)) ? getColour(MINUTE_INDEX) : mContext.getResources().getColor(R.color.black);
    }

    @Override
    public int secondsTextColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(SECOND_INDEX)) ? getColour(SECOND_INDEX) : mContext.getResources().getColor(R.color.black);
    }

    @Override
    public int meridianTextColour(State faceState) {
        return secondsTextColour(faceState);
    }

    @Override
    public int backgroundColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_background);
        return (hasColour(BACKGROUND_INDEX)) ? getColour(BACKGROUND_INDEX) : mContext.getResources().getColor(R.color.fill_default_background);
    }

    @Override
    public int accentColour(State faceState) {
        if (faceState == State.AMBIENT)
            return mContext.getResources().getColor(R.color.ambient_foreground);
        return (hasColour(ACCENT_INDEX)) ? getColour(ACCENT_INDEX) : mContext.getResources().getColor(R.color.fill_default_accent);
    }

    @Override
    public String makeString(int stringID){
        return name+"_"+mContext.getString(stringID);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

//        if(key.toUpperCase().contains(name)){
//           if(key.toUpperCase().contains(mContext.getString(R.string.PREF_BACKGROUND_COLOUR))){
//                mBackgroundPaint = null;
//           }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_ACCENT_COLOUR))){
//               mAccentPaint = null;
//           }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_HOUR_COLOUR))){
//               mHourPaint = null;
//           }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_MINUTE_COLOUR))){
//               mMinutePaint = null;
//           }else if(key.toUpperCase().contains(mContext.getString(R.string.PREF_SECOND_COLOUR))){
//               mSecondPaint = null;
//           }
//        }
    }

    @Override
    public void changedState(State faceState) {
      invalidateColors();
    }
}
