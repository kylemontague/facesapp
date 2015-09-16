package montague.faces_lib;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;


/**
 * Created by kylemontague on 19/12/14.
 */
public class BusinessFaceConfig implements Face, SharedPreferences.OnSharedPreferenceChangeListener {


    private Context mContext;

    private Paint mHourPaint;
    private Paint mMinutePaint;
    private Paint mSecondPaint;

    private Paint mBackgroundPaint;
    private Paint mAccentPaint;

    private Prefs mP;
    private static String name = null;
    private double mTextScale;

    public BusinessFaceConfig(Context context){
        mTextScale = 1.0;
        mContext=context;
        mP = new Prefs(context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.Business_FaceName);
    }

    public BusinessFaceConfig(Context context, double textScale){
        mTextScale = textScale;
        mContext=context;
        mP = new Prefs(context);
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
        name = mContext.getResources().getString(R.string.Business_FaceName);
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
        return mP.getColourString(makeString(R.string.PREF_SECOND_COLOUR), mContext.getResources().getColor(R.color.white));
    }

    @Override
    public int hoursTextColour(State faceState) {
        return (faceState!=State.AMBIENT)?mP.getColourString(makeString(R.string.PREF_HOUR_COLOUR), mContext.getResources().getColor(R.color.business_default_hour)):mContext.getResources().getColor(R.color.white);
    }

    @Override
    public int minutesTextColour(State faceState) {
        return (faceState!=State.AMBIENT)? mP.getColourString(makeString(R.string.PREF_MINUTE_COLOUR), mContext.getResources().getColor(R.color.business_default_minute)):mContext.getResources().getColor(R.color.white);
    }

    @Override
    public int secondsTextColour(State faceState) {
        return (faceState!=State.AMBIENT)? mP.getColourString(makeString(R.string.PREF_SECOND_COLOUR), mContext.getResources().getColor(R.color.business_default_second)):mContext.getResources().getColor(R.color.white);
    }

    @Override
    public int meridianTextColour(State faceState) {
        return mP.getColourString(makeString(R.string.PREF_SECOND_COLOUR), mContext.getResources().getColor(R.color.black));
    }

    @Override
    public int backgroundColour(State faceState) {
        return (faceState!=State.AMBIENT)? mP.getColourString(makeString(R.string.PREF_BACKGROUND_COLOUR), mContext.getResources().getColor(R.color.business_default_background)):mContext.getResources().getColor(R.color.black);
    }

    @Override
    public int accentColour(State faceState) {
        return (faceState!=State.AMBIENT)?mP.getColourString(makeString(R.string.PREF_ACCENT_COLOUR), mContext.getResources().getColor(R.color.light)):mContext.getResources().getColor(R.color.ambient_background);
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
