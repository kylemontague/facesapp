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

import slurpp.faces_lib.Global;
import slurpp.faces.Font;
import slurpp.faces.MobileListener;

import static slurpp.faces.R.animator;
import static slurpp.faces.R.drawable;
import static slurpp.faces.R.id;
import static slurpp.faces.R.layout;

/**
 * Created by kylemontague on 18/12/14.
 */
public class SplashActivity extends Activity{
   // ImageView iv1;
    ImageView straps;
    TextView madeby;
    AnimationDrawable Anim;
//    PathView svgView;
    //todo need to update the background watch straps to be square also.
    boolean isRound = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.splash);
        //iv1 = (ImageView) findViewById(id.watch);
        straps = (ImageView)findViewById(id.watch_background);
        madeby = (TextView)findViewById(id.madeby_text);
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
            int draw = drawable.loader_rounded2;
            if(!isRound) {
                draw = drawable.loader_squared2;
                //straps.setImageResource(drawable.watch_strap_square);
            }
            Anim = (AnimationDrawable)getResources().getDrawable(draw);
            Anim.setOneShot(false);
            straps.setImageDrawable(Anim);
            Anim.start();
            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {

                public void run() {
                    hasLoaded();
                }
            }, 1200);
//            svgView.reveal(iv1,400);

        } catch (Exception e) {

        }



    }

    public void hasLoaded(){

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean active = sp.getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), false);
        boolean round = sp.getBoolean(Global.DataMap.WEAR_SHAPE.name(),false);

        Log.d("WEAR SETTINGS","Active:"+active+", Round:"+round);
        Intent i = new Intent(this,Home.class);
        startActivity(i);
        overridePendingTransition(animator.fade_in, animator.fade_out);
    }



}