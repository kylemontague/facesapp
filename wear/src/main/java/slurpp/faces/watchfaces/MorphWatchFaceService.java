package slurpp.faces.watchfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.manolovn.trianglify.generator.point.RegularPointGenerator;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import slurpp.faces.services.WatchWearableService;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.templates.BusinessIFaceConfig;
import slurpp.faces_lib.templates.FacesTrianglifyDrawable;
import slurpp.faces_lib.templates.FillFaceConfig;
import slurpp.faces_lib.templates.IFace;
import slurpp.faces_lib.templates.OUATFace;
import slurpp.faces_lib.templates.PaletteColorGenerator;
import slurpp.faces_lib.templates.PrismFaceConfig;
import slurpp.faces_lib.templates.WordsmithFace;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;

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
    private class Engine extends CanvasWatchFaceService.Engine implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ResultCallback<DailyTotalResult> {

        private static final int MSG_UPDATE_TIME = 0;
        private static final long INTERACTIVE_UPDATE_RATE_MS = 1000;
        private static final String TAG = "MORPH";


        private GoogleApiClient client;

        private void buildFitnessClient(){


            if(client == null) {
                client = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(Fitness.HISTORY_API)
                        .addApi(Fitness.RECORDING_API)
                        .addOnConnectionFailedListener(this)
                        .addConnectionCallbacks(this)
                        .useDefaultAccount()
                        .build();
                client.connect();
            }
        }

        private void unregisterFitnessClient() {
            if(client!=null && client.isConnected())
                client.disconnect();
        }


        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d("FIT","Connected");

            mStepsRequested = false;
            // The subscribe step covers devices that do not have Google Fit installed.
            subscribeToSteps();
            updateSteps();
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e("FIT","Disconnected");

        }


        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.e("FIT","Connection failed: "+connectionResult.getErrorMessage()+" "+connectionResult.toString());
        }

        /*
        * Subscribes to step count (for phones that don't have Google Fit app).
        */
        private void subscribeToSteps() {
            Fitness.RecordingApi.subscribe(client, DataType.TYPE_STEP_COUNT_DELTA)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode()
                                        == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.i(TAG, "Existing subscription for activity detected.");
                                } else {
                                    Log.i(TAG, "Successfully subscribed!");
                                }
                            } else {
                                Log.i(TAG, "There was a problem subscribing.");
                            }
                        }
                    });
        }

        private boolean mStepsRequested = false;


        private void updateSteps() {
            if ((client != null) && (client.isConnected()) ) {
                PendingResult<DailyTotalResult> stepsResult = Fitness.HistoryApi.readDailyTotal(client, DataType.TYPE_STEP_COUNT_DELTA);
                stepsResult.setResultCallback(this);
            }
        }

        @Override
        public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
            mStepsRequested = false;

            if (dailyTotalResult.getStatus().isSuccess()) {
                List<DataPoint> points = dailyTotalResult.getTotal().getDataPoints();
                if (!points.isEmpty()) {
                    totalSteps = points.get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(TAG, "steps updated: " + totalSteps);
                }
            } else {
                Log.e(TAG, "onResult() failed! " + dailyTotalResult.getStatus().getStatusMessage());
            }
        }

        long totalSteps = 0;


        private GregorianCalendar mCalendar;

        /* device features */
        boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mAmbient =false;
        private WatchDraw.FaceMode contentMode = WatchDraw.FaceMode.TIME;
        WatchDraw.FACE watchType;
        private Rect mPeekCardBounds = new Rect(0,0,0,0);

        private boolean mTapShowDate = false;


        Handler dateTimeoutHandler = new Handler();
        long dateTimeout = 5000;
        Runnable dateTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                contentMode = WatchDraw.FaceMode.TIME;
                invalidate();
            }
        };


        /* Watch Features */
        IFace watchConfig;
        IFace.Shape shape = IFace.Shape.ROUND;

        CustomFace previewFace;

        boolean mRegisteredTimeZoneReceiver=false;


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
                shape = IFace.Shape.ROUND;
            }

            Constants.getEditor(getApplicationContext()).putBoolean(Global.OLD_DataMap.WEAR_SHAPE.name(),mIsRound).commit();

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


        GoogleApiClient mGoogleApiClient;
        private void start(){
            if(null == mGoogleApiClient) {
                mGoogleApiClient = new GoogleApiClient.Builder(MorphWatchFaceService.this)
                        .addApi(Wearable.API)
                        .build();
                Log.v(TAG, "GoogleApiClient created");
            }

            if(!mGoogleApiClient.isConnected()){
                mGoogleApiClient.connect();
                Log.v(TAG, "Connecting to GoogleApiClient..");
            }

            //listen for messages
            WatchWearableService.addListener(mGoogleApiClient,previewListener);

        }

        MessageApi.MessageListener previewListener = new MessageApi.MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {

                if(messageEvent.getPath().equals(Global.Messages.UPDATE_WEAR_PREVIEW.name())){
                    final String message = new String(messageEvent.getData());
                    Log.d(TAG,"Got preview update:"+ message);
                    previewFace = CustomFace.fromJson(message);
                    if(previewFace !=null){
                        setWatchFaceType(previewFace.getBase());
                        watchConfig.setColours(previewFace.getColors());
                    }
                    invalidate();
                    updateTimer();

                    //revert back to the original watch after 30 seconds.
                    previewHandler.removeCallbacks(previewRunnable);
                    previewHandler.postDelayed(previewRunnable,timeout);

                }else if(messageEvent.getPath().equals(Global.Messages.UPDATE_CURRENT_FACE.name())){
                    previewHandler.removeCallbacks(previewRunnable);
                }
            }
        };

        long timeout = 30*1000;
        Handler previewHandler = new Handler();
        Runnable previewRunnable = new Runnable() {
            @Override
            public void run() {

                String json = Constants.getPreferences(getApplicationContext()).getString(Global.PREF_WATCH_FACE,null);
                if(json != null)
                    loadFace(json);

            }
        };

        private void stop(){
            WatchWearableService.removeListener(mGoogleApiClient,previewListener);
        }



        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            mTapShowDate = Constants.getPreferences(MorphWatchFaceService.this).getBoolean(Constants.PREF_BOOLEAN_TAP_SHOW_DATE,true);

            /* initialize your watch face */

//             /* load the background image */
//            Resources resources = OUATWatchFaceService.this.getResources();

            //TODO here defines the colour and layout properties of the face (ADD SWITCH)

            String json = Constants.getPreferences(MorphWatchFaceService.this).getString(Global.PREF_WATCH_FACE,null);
            loadFace(json);


            Intent intent = new Intent(getApplicationContext(), WatchWearableService.class);
            startService(intent);
            start();
            buildFitnessClient();
        }



        public void loadFace(String json){
            if(json == null || json.equals("")) {
                Log.e("Load","No JSON face");
                Toast.makeText(getApplicationContext(),"No JSON",Toast.LENGTH_SHORT).show();
                setWatchFaceType(WatchDraw.FACE.TWO_TIME);
            }else{
                CustomFace customFace = CustomFace.fromJson(json);
                setWatchFaceType(customFace.getBase());
                watchConfig.setColours(customFace.getColors());
            }


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

        }


        @Override
        public void onDestroy(){
            super.onDestroy();
            Intent intent = new Intent(getApplicationContext(), WatchWearableService.class);
            stopService(intent);
            stop();
            unregisterFitnessClient();
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
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
            boolean wasInAmbientMode = isInAmbientMode();
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;
            watchConfig.changedState(mAmbient ? IFace.State.AMBIENT : IFace.State.ACTIVE);
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
            if(tapType == 2L) {
                switch (contentMode){
                    case TIME:
                        contentMode = WatchDraw.FaceMode.DATE;
                        dateTimeoutHandler.postDelayed(dateTimeoutRunnable, dateTimeout);
                        break;
                    case DATE:
                        if(totalSteps == 0){
                            contentMode = WatchDraw.FaceMode.TIME;
                            dateTimeoutHandler.removeCallbacks(dateTimeoutRunnable);
                        }else {
                            updateSteps();
                            contentMode = WatchDraw.FaceMode.STEPS;
                            dateTimeoutHandler.removeCallbacks(dateTimeoutRunnable);
                            dateTimeoutHandler.postDelayed(dateTimeoutRunnable, dateTimeout);
                        }
                        break;
                    case STEPS:
                        contentMode = WatchDraw.FaceMode.TIME;
                        dateTimeoutHandler.removeCallbacks(dateTimeoutRunnable);
                        break;
                }
                invalidate();
//                }else {
//                    onAmbientModeChanged(!mAmbient);
//                }
            }
        }


        WatchDraw.FACE currentFace = WatchDraw.FACE.TWO_TIME;
        Bitmap backgroundImage = null;

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mCalendar.setTime(new Date());
            switch (watchType){
                case BUSINESS_TIME:
                    currentFace = WatchDraw.FACE.BUSINESS_TIME;
                    WatchDraw.BusinessTime(canvas,bounds,watchConfig,mCalendar,mAmbient,shape, null, mChinSize, mPeekCardBounds, contentMode,totalSteps);
                    break;
                case FILL_TIME:
                    currentFace = WatchDraw.FACE.FILL_TIME;
                    WatchDraw.FillTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds, contentMode, totalSteps);
                    break;
                case OUA_TIME:
                    currentFace = WatchDraw.FACE.OUA_TIME;
                    WatchDraw.OUATime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds, contentMode, totalSteps);
                    break;
                case TWO_TIME:
                    currentFace = WatchDraw.FACE.TWO_TIME;
                    WatchDraw.TwoTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds, contentMode, totalSteps);
                    break;
                case PRISM_TIME:
                    if(currentFace!= WatchDraw.FACE.PRISM_TIME && !mAmbient){
                        currentFace = WatchDraw.FACE.PRISM_TIME;
                        IFace.State state = mAmbient? IFace.State.AMBIENT: IFace.State.ACTIVE;
                        int accentColor = watchConfig.accentColour(state);
                        int bgColor = watchConfig.backgroundColour(state);
                        int meridianColor = watchConfig.meridianTextColour(state);

                        FacesTrianglifyDrawable drawable = new FacesTrianglifyDrawable(10, 10, 120, 75, new PaletteColorGenerator(new int[]{bgColor,accentColor,meridianColor}),new RegularPointGenerator(),bounds);
                        drawable.setBounds(new Rect(0,0,bounds.width()*3, bounds.height()*3));
                        drawable.triangulate();
                        backgroundImage = WatchDraw.drawableToBitmap(drawable,bounds.width()*4, bounds.height()*4);
                    }
                    WatchDraw.SpaceTimePrism(canvas,bounds,watchConfig,mCalendar,mAmbient,shape,null,mChinSize,mPeekCardBounds, contentMode,backgroundImage, totalSteps);
                    break;
                default:
                    //WatchDraw.FillTime(canvas, bounds, watchConfig, mCalendar, mAmbient, shape, null, mChinSize, mPeekCardBounds, mTapShowDate);
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


        //fixme I have multiple copies of this method across the mobile and wear apps. need to have a single entry point.
        private IFace getConfig(Context c, WatchDraw.FACE id){
            switch(id) {
                case OUA_TIME:
                    return new OUATFace(c);
                case TWO_TIME:
                    return new WordsmithFace(c);
                case FILL_TIME:
                    return new FillFaceConfig(c);
                case BUSINESS_TIME:
                    return new BusinessIFaceConfig(c);
                case PRISM_TIME:
                    return new PrismFaceConfig(c);
                default:
                    return new WordsmithFace(c);
            }
        }

        public void setWatchFaceType(WatchDraw.FACE face){
            watchType = face;
            watchConfig = getConfig(MorphWatchFaceService.this,face);
//            watchConfig.setAsCurrentFace();
        }



    }



}
