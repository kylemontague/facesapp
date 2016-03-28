package slurpp.faces;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mclemont.faces.R;
import slurpp.faces.utils.WatchFragment;
import slurpp.faces_lib.Constants;
import slurpp.faces_lib.WatchDraw;

/**
 * Created by kylemontague on 13/06/15.
 */
public class PreviewActivity extends Activity {

    public static String ARG_WATCHFACE = "ARG.WATCH_FACE";
    public static String ARG_CHIN_INSET = "ARG.CHIN_INSET";


    private PowerManager.WakeLock wakeLock;
    private WatchFragment watchFragment;
    private RelativeLayout watchPreview;
    private Runnable previewRunnable;
    private Handler previewHandler;
    private boolean showPreview = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_preview);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "Faces");


        Bundle extras = getIntent().getExtras();
        //get parameters from the bundle.
        int face = WatchDraw.FACE.OUA_TIME.ordinal();
        if(extras!=null)
            face = extras.getInt(getString(R.string.PREVIEW_CURRENT_FACE),WatchDraw.FACE.FILL_TIME  .ordinal());
        else{
            face = Constants.currentFace(this);
        }

        watchPreview = (RelativeLayout)findViewById(R.id.watch);
        watchFragment = new WatchFragment();
        Bundle args = new Bundle();
        args.putInt(PreviewActivity.ARG_WATCHFACE, face);
        watchFragment.setArguments(args);


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.watch,watchFragment).commit();

        previewRunnable = new Runnable() {
            @Override
            public void run() {
                if(showPreview) {
                    watchPreview.invalidate();
                    previewHandler.postDelayed(previewRunnable, 1000);
                }
            }
        };

        previewHandler = new Handler();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (wakeLock!=null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        if (!showPreview) {
            showPreview = true;
            previewHandler.postDelayed(previewRunnable, 0);
        }
    }


    protected void onPause(){
        super.onPause();
        showPreview = false;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(wakeLock!=null && wakeLock.isHeld())
            wakeLock.release();
    }




}
