package slurpp.faces.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import slurpp.faces.Font;
import slurpp.faces_lib.Prefs;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;

import static slurpp.faces.R.animator;
import static slurpp.faces.R.drawable;
import static slurpp.faces.R.id;
import static slurpp.faces.R.layout;

/**
 * Created by kylemontague on 18/12/14.
 */
public class SplashActivity extends Activity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
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

        buildFitnessClient();
    }


    @Override
    protected void onResume(){
        super.onResume();

    }


    Runnable loadRunnable = new Runnable() {

        public void run() {
            if(ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                if(Prefs.hasGoogleSignIn(SplashActivity.this)){
                    hasLoaded();
                }else{
                    requestSignIn();
                }

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

    private GoogleApiClient client;

    private void buildFitnessClient(){

        GoogleSignInOptions signInConfig = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.FITNESS_LOCATION_READ),new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .build();

        if(client == null) {
            client = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Auth.GOOGLE_SIGN_IN_API, signInConfig)
                    .addApi(Fitness.HISTORY_API)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .build();
        }
    }


    private static final int GOOGLE_SIGNIN = 1234;

    private void requestSignIn(){
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(client), GOOGLE_SIGNIN);
    }


    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(client);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            if(result.isSuccess())
                hasLoaded();

        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    if(googleSignInResult.isSuccess())
                        hasLoaded();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGNIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            Prefs.setGoogleSignIn(SplashActivity.this, result.isSuccess());
        }
        hasLoaded();

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}