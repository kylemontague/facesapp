package slurpp.faces.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

import slurpp.faces.Font;
import slurpp.faces.services.MobileListener;
import slurpp.faces_lib.Constants;
import slurpp.faces_lib.Global;
import slurpp.faces_lib.models.Face;
import slurpp.faces_lib.models.ListTypeSerializer;

import static slurpp.faces.R.animator;
import static slurpp.faces.R.drawable;
import static slurpp.faces.R.id;
import static slurpp.faces.R.layout;

/**
 * Created by kylemontague on 18/12/14.
 */
public class SplashActivity extends Activity{
    private static final String TAG = "SPLASH";
    // ImageView iv1;
    ImageView straps;
    TextView madeby;
    AnimationDrawable Anim;
    MobileListener mobileListener;

    boolean shouldOnBoard = true;
    //todo need to update the background watch straps to be square also.
    boolean isRound = true;
    boolean isActive = false;


    long startTime = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.splash);
//        initialiseDB();

        mobileListener = new MobileListener(getApplicationContext());
        Log.v(TAG,"Pre-Async"+(System.currentTimeMillis()-startTime));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                shouldOnBoard = Constants.shouldOnBoard(SplashActivity.this);
                isActive = Constants.isActive(SplashActivity.this);
                isRound = Constants.isRound(SplashActivity.this);
                mobileListener.sendMessage(getApplicationContext(), Global.Messages.GET_WATCH.toString(), "");
                Log.v(TAG,"Post-Async"+(System.currentTimeMillis()-startTime));
            }
        });
        Log.v(TAG,"After-Async"+(System.currentTimeMillis()-startTime));
        straps = (ImageView)findViewById(id.watch_background);
        madeby = (TextView)findViewById(id.madeby_text);
        madeby.setTypeface(Font.get(getApplicationContext(), Font.QUICKSAND_BOLD));
        Log.v(TAG,"End-OnCreate"+(System.currentTimeMillis()-startTime));
    }

    private void initialiseDB() {
        //ActiveAndroid init.
        Configuration.Builder configurationBuilder = new Configuration.Builder(this);
        configurationBuilder.setDatabaseName("faces.db");
        configurationBuilder.setDatabaseVersion(1);
        configurationBuilder.addModelClass(Face.class);
        configurationBuilder.addTypeSerializer(ListTypeSerializer.class);
        ActiveAndroid.initialize(configurationBuilder.create());
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.v(TAG,"Start-OnCreate"+(System.currentTimeMillis()-startTime));
        try {
            int draw = drawable.loader_rounded2;
            if(!isRound) {
                draw = drawable.loader_squared2;
                //straps.setImageResource(drawable.watch_strap_square);
            }
            Log.v(TAG,"Start-Animate"+(System.currentTimeMillis()-startTime));

            Anim = (AnimationDrawable)getResources().getDrawable(draw);
            if(Anim!=null) {
                Anim.setOneShot(false);
                straps.setImageDrawable(Anim);
                Anim.start();
                Log.v(TAG,"End-Animate"+(System.currentTimeMillis()-startTime));
            }
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                public void run() {
                    hasLoaded();
                }
            }, 1200);

        } catch (Exception e) {

        }

        Log.v(TAG,"End-OnCreate"+(System.currentTimeMillis()-startTime));


    }

    public void hasLoaded(){
        Intent i = new Intent(SplashActivity.this,Home.class);
        i.putExtra(Constants.PREF_BOOLEAN_SHOULD_ON_BOARD,shouldOnBoard);
        i.putExtra(Global.DataMap.WEAR_SHAPE.name(),isRound);
        i.putExtra(Global.DataMap.WEAR_IS_ACTIVE.name(),isActive);
        startActivity(i);
        overridePendingTransition(animator.fade_in, animator.fade_out);
    }



}