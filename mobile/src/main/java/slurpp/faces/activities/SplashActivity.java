package slurpp.faces.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
    // ImageView iv1;
    ImageView straps;
    TextView madeby;
    AnimationDrawable Anim;

    boolean googleApi = true;

    boolean shouldOnBoard = true;
    //todo need to update the background watch straps to be square also.
    boolean isRound = true;
    boolean isActive = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.splash);
//        initialiseDB();

//        splashListener = new WearIO.MessageListener() {
//            @Override
//            public boolean waitingForKey(String key) {
//                return (key.equals(Global.DATA_MAP_WEAR_DEVICE));
//            }
//
//            @Override
//            public void received(String path, String message) {
//                Toast.makeText(getApplicationContext(),"Message: "+message,Toast.LENGTH_LONG).show();
//
//            }
//
//            @Override
//            public void updated(String path, DataMap map) {
//                WearSettings settings = map.get(Global.OLD_DataMap.WEAR_SETTINGS.name());
//                isActive = settings.isActive;
//                isRound = settings.isRound;
//            }
//        };
        //fixme uncomment once fixed
        //wearIO.requestSettings(splashListener);


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                shouldOnBoard = Constants.shouldOnBoard(SplashActivity.this);
                isActive = Constants.isActive(SplashActivity.this);
                isRound = Constants.isRound(SplashActivity.this);
                //mobileListener.sendMessage(getApplicationContext(), Global.Messages.GET_WATCH.toString(), "");
            }
        });
        straps = (ImageView)findViewById(id.watch_background);
        madeby = (TextView)findViewById(id.madeby_text);
        madeby.setTypeface(Font.get(getApplicationContext(), Font.QUICKSAND_BOLD));
    }


    @Override
    protected void onResume(){
        super.onResume();
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
            handler.postDelayed(new Runnable() {

                public void run() {
                    hasLoaded();
                }
            }, 1200);

        } catch (Exception e) {

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