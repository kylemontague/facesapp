package slurpp.faces;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import slurpp.faces.utils.WatchFragment;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;

/**
 * Created by kylemontague on 13/06/15.
 */
public class PreviewActivity extends Activity {

    public static final String ARG_WATCH_SETTINGS = Global.PREF_WATCH_FACE;
    public static String ARG_WATCHFACE = "ARG.WATCH_FACE";
    public static String ARG_CHIN_INSET = "ARG.CHIN_INSET";


    private WatchFragment watchFragment;
    private RelativeLayout watchPreview;
    private Runnable previewRunnable;
    private Handler previewHandler;

    private CustomFace faceSettings;

    private boolean showPreview = true;

    private final BroadcastReceiver onCloseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Global.Messages.CLOSE_WEAR_PREVIEW.name()))
                finish();
            else if(intent.getAction().equals(Global.Messages.UPDATE_WEAR_PREVIEW.name())){
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String json = extras.getString(Global.PREF_WATCH_FACE, null);
                    if (json != null && !json.equals("")) {
                        faceSettings = CustomFace.fromJson(json);
                        Log.d("Preview","FaceSettings:"+json);
                        watchFragment.update(faceSettings);

                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_preview);



        Bundle extras = getIntent().getExtras();
        //get parameters from the bundle.
        int face = WatchDraw.FACE.OUA_TIME.ordinal();
        if(extras!=null) {
            face = extras.getInt(getString(R.string.PREVIEW_CURRENT_FACE), WatchDraw.FACE.FILL_TIME.ordinal());
            String json = extras.getString(Global.PREF_WATCH_FACE,null);
            if(json!=null && !json.equals("")){
                faceSettings = CustomFace.fromJson(json);
                if(faceSettings!=null)
                    face = faceSettings.getBase().ordinal();
            }
        }
        else{
            face = Constants.currentFace(this);
        }


        watchPreview = (RelativeLayout)findViewById(R.id.watch);
        watchFragment = new WatchFragment();
        Bundle args = new Bundle();
        args.putInt(PreviewActivity.ARG_WATCHFACE, face);
        if(faceSettings != null) {
            args.putString(PreviewActivity.ARG_WATCH_SETTINGS, faceSettings.toJson());
        }
        watchFragment.setArguments(args);


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.watch,watchFragment).commit();

        previewRunnable = new Runnable() {
            @Override
            public void run() {
                if(showPreview) {
                    watchFragment.redraw();
                    previewHandler.postDelayed(previewRunnable, 1000);
                }
            }
        };

        previewHandler = new Handler();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter =  new IntentFilter(Global.Messages.CLOSE_WEAR_PREVIEW.name());
        filter.addAction(Global.Messages.UPDATE_WEAR_PREVIEW.name());
        registerReceiver(onCloseReceiver,filter);


        if (!showPreview) {
            showPreview = true;
            previewHandler.postDelayed(previewRunnable, 0);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            String json = extras.getString(Global.PREF_WATCH_FACE, null);
            if (json != null && !json.equals("")) {
                faceSettings = CustomFace.fromJson(json);
                watchFragment.update(faceSettings);
            }
        }

    }

    protected void onPause(){
        super.onPause();
        showPreview = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(onCloseReceiver);
    }




}
