package slurpp.faces.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import montague.faces_lib.Global;
import slurpp.faces.Font;
import slurpp.faces.MobileListener;
import slurpp.faces.R;

/**
 * Created by kylemontague on 18/12/14.
 */
public class SplashActivity extends Activity{
    ImageView iv1;
    TextView madeby;
    AnimationDrawable Anim;
//    PathView svgView;
    //todo need to update the background watch straps to be square also.
    boolean isRound = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        iv1 = (ImageView) findViewById(R.id.watch);
        madeby = (TextView)findViewById(R.id.madeby_text);
        madeby.setTypeface(Font.get(getApplicationContext(), Font.QUICKSAND_BOLD));

        MobileListener mobileListener = new MobileListener(getApplicationContext());
        mobileListener.sendMessage(getApplicationContext(), Global.Messages.GET_WATCH.toString(), "");


//        svgView =(PathView)findViewById(R.id.svg_spot);
//        svgView.setSvgResource(R.raw.round_watch_paths);
        //svgView.setBackgroundColor(getResources().getColor(R.color.white));


    }

    @Override
    protected void onResume(){
        super.onResume();

        try {
            if(isRound)
                Anim = (AnimationDrawable)getResources().getDrawable(R.drawable.loader_rounded);
            else
                Anim = (AnimationDrawable)getResources().getDrawable(R.drawable.loader_squared);
            Anim.setOneShot(true);
            iv1.setBackground(Anim);
            Anim.start();
            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {

                public void run() {
                    hasLoaded();
                }
            }, 2500);
//            svgView.reveal(iv1,400);

        } catch (Exception e) {
            // TODO: handle exception
        }



    }

    public void hasLoaded(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean active = sp.getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), false);
        boolean round = sp.getBoolean(Global.DataMap.WEAR_SHAPE.name(),false);

        Log.d("WEAR SETTINGS","Active:"+active+", Round:"+round);
        Intent i = new Intent(this,Home.class);
        startActivity(i);
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
    }



}