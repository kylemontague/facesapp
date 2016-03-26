package slurpp.faces.watchfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import slurpp.faces_lib.BusinessFaceConfig;
import slurpp.faces_lib.Face;
import slurpp.faces_lib.FillFaceConfig;
import slurpp.faces_lib.Global;
import slurpp.faces_lib.OUATFace;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.WordsmithFace;

/**
 * Created by kyle montague on 19/12/14.
 */
public class MorphWatchFaceService extends CanvasWatchFaceService {





    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */

        return new Engine();


    }


    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine {

        SharedPreferences sp;

        private static final int MSG_UPDATE_TIME = 0;
        private static final long INTERACTIVE_UPDATE_RATE_MS = 1000;


        private GregorianCalendar mCalendar;

        /* device features */
        boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mAmbient =false;
        WatchDraw.FACE watchType;
        private Rect mPeekCardBounds = new Rect(0,0,0,0);


        /* Watch Features */
        Face watchConfig;
        Face.Shape shape = Face.Shape.SQUARE;

        boolean mRegisteredTimeZoneReceiver=false;

        FaceListener listener;

        /* handler to update the time once a second in interactive mode */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };



        boolean mIsRound;
        int mChinSize;

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();

            if(mIsRound){
                shape = Face.Shape.ROUND;
            }

            sp.edit().putBoolean(Global.DataMap.WEAR_SHAPE.name(),mIsRound);
            sp.edit().commit();

            //Log.d("PREF_TEST", "WINDOWINSETS" + sp.getBoolean(Global.DataMap.WEAR_SHAPE.name(), mIsRound));
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode() && !mAmbient;
        }



        /* receiver to update the time zone */
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                mCalendar.setTime(new Date());
            }
        };


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            sp.edit().putBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), true).commit();

            Log.d("PREF_TEST","CREATE "+sp.getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(),false));
            /* initialize your watch face */

//             /* load the background image */
//            Resources resources = OUATWatchFaceService.this.getResources();

            //TODO here defines the colour and layout properties of the face (ADD SWITCH)
            setWatchFaceType(WatchDraw.FACE.BUSINESS_TIME);


            /* allocate an object to hold the time */
            mCalendar = new GregorianCalendar();

            /* configure the system UI */
            setWatchFaceStyle(new WatchFaceStyle.Builder(MorphWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setHotwordIndicatorGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE)
                    .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR | WatchFaceStyle.PROTECT_HOTWORD_INDICATOR)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            listener = new FaceListener(getApplicationContext(), watchConfig.getFaceName());

        }


        @Override
        public void onDestroy(){
            super.onDestroy();
            sp.edit().putBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), false).commit();
            Log.d("PREF_TEST", "DESTROY" + sp.getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), true));
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            /* get device features (burn-in, low-bit ambient) */
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
        }

        @Override
        public void onTimeTick() {
            /* the time changed */
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
            boolean wasInAmbientMode = isInAmbientMode();
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;
            watchConfig.changedState(mAmbient ? Face.State.AMBIENT : Face.State.ACTIVE);
            if (inAmbientMode != wasInAmbientMode) {
                if (mLowBitAmbient) {
                    boolean antiAlias = !inAmbientMode;
                }
            }
            invalidate();
            updateTimer();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            //Log.d("TAP_WATCH","type:"+tapType+" location:["+x+","+y+"] timestamp:"+eventTime);
            if(tapType == 2) {
                onAmbientModeChanged((mAmbient)?false:true);
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mCalendar.setTime(new Date());
            switch (watchType){
                case BUSINESS_TIME:
                    WatchDraw.BusinessTime(canvas,bounds,watchConfig,mCalendar,mAmbient,shape, null, mChinSize);
                    break;
                case FILL_TIME:
                    WatchDraw.FillTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds);
                    break;
                case OUA_TIME:
                    WatchDraw.OUATime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds);
                    break;
                case TWO_TIME:
                    WatchDraw.TwoTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds);
                default:
                    //WatchDraw.FillTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds);
                    break;
            }
        }

        @Override
        public void onPeekCardPositionUpdate (Rect rect){
            super.onPeekCardPositionUpdate(rect);
            mPeekCardBounds = rect;
            invalidate();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }
            // Whether the timer should be running depends on whether we're visible and
            // whether we're in ambient mode), so we may need to start or stop the timer
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MorphWatchFaceService.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MorphWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
        }



        public void setWatchFaceType(WatchDraw.FACE face){

            watchType = face;
            switch (watchType){
                case BUSINESS_TIME:
                    watchConfig = new BusinessFaceConfig(getApplicationContext());
                    break;
                case FILL_TIME:
                    watchConfig = new FillFaceConfig(getApplicationContext());
                    break;
                case TWO_TIME:
                    watchConfig = new WordsmithFace(getApplicationContext());
                    break;
                case OUA_TIME:
                    watchConfig = new OUATFace(getApplicationContext());
                default:
                    //watchConfig = new WordsmithFace(getApplicationContext());
                    break;
            }
            watchConfig.setAsCurrentFace();
        }



    }



}
