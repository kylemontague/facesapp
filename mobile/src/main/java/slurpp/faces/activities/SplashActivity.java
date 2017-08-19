package slurpp.faces.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

import slurpp.faces.Font;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;

import static slurpp.faces.R.animator;
import static slurpp.faces.R.drawable;
import static slurpp.faces.R.id;
import static slurpp.faces.R.layout;

/**
 * Created by kylemontague on 18/12/14.
 */
public class SplashActivity extends Activity{
    private static final String TAG = "SPLASH";
    private static final int FACES_PERMISSIONS = 1324;
    // ImageView iv1;
    ImageView straps;
    TextView madeby;
    AnimationDrawable Anim;

    boolean googleApi = true;

    boolean shouldOnBoard = false ;
    //todo need to update the background watch straps to be square also.
    boolean isRound = true;
    boolean isActive = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.splash);

        //fixme uncomment once fixed
        //wearIO.requestSettings(splashListener);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                shouldOnBoard = Constants.shouldOnBoard(SplashActivity.this);
                isActive = Constants.isActive(SplashActivity.this);
                isRound = Constants.isRound(SplashActivity.this);
            }
        });
        straps = (ImageView)findViewById(id.watch_background);
        madeby = (TextView)findViewById(id.madeby_text);
        madeby.setTypeface(Font.get(getApplicationContext(), Font.QUICKSAND_BOLD));


        try {
            int draw = drawable.loader_rounded2;
            if(!isRound) {
                draw = drawable.loader_squared2;
                //straps.setImageResource(drawable.watch_strap_square);
            }

            Anim = (AnimationDrawable)getResources().getDrawable(draw);
            if(Anim!=null) {
                Anim.setOneShot(false);
                straps.setImageDrawable(Anim);
                Anim.start();
            }
            final Handler handler = new Handler();
            handler.postDelayed(loadRunnable, 1200);

        } catch (Exception e) {

        }
    }


    @Override
    protected void onResume(){
        super.onResume();

    }


    Runnable loadRunnable = new Runnable() {

        public void run() {
            if(ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                hasLoaded();
            }else{
                //todo check, might not need external storage.
                ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, FACES_PERMISSIONS);
            }
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FACES_PERMISSIONS: {
                loadRunnable.run();
            }
        }
    }


    public void hasLoaded(){
        //if(googleApi) {
            Intent i = new Intent(SplashActivity.this, Home.class);
            i.putExtra(Constants.PREF_BOOLEAN_SHOULD_ON_BOARD, shouldOnBoard);
            i.putExtra(Global.OLD_DataMap.WEAR_SHAPE.name(), isRound);
            i.putExtra(Global.OLD_DataMap.WEAR_IS_ACTIVE.name(), isActive);
            startActivity(i);
            overridePendingTransition(animator.fade_in, animator.fade_out);
//        }else{
//            Dialog d = new Dialog(this);
//            d.setTitle("Unable to communicate with Wear device");
//            d.show();
//        }
    }
}