package slurpp.faces.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import io.realm.Realm;
import slurpp.faces.Font;
import slurpp.faces.R;
import slurpp.faces.controls.InfoFragment;
import slurpp.faces.controls.WatchPageAdapter;
import slurpp.faces.objects.DepthPageTransformer;
import slurpp.faces.services.MobileWearableService;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;
import slurpp.faces_lib.v2.models.FaceSettings;


public class Home extends FragmentActivity implements MessageApi.MessageListener {

    private static final String TAG = "Home";
    private boolean shouldOnBoard = false;
    private boolean isActive = true;
    private boolean isRound = true;


    private static final int CAMERA_IMAGE_CAPTURE = 1337;
    private Button takePhoto;
    private Button ambientMode;


    private ImageButton favouriteButton;
    private ImageButton deleteButton;
    private ImageButton todayButton;

    private ViewPager mPager;
    private WatchPageAdapter mPagerAdapter;
    private InfoFragment mTopInfoOverlay;
    private View mOverlayView;

    private View currentPreview = null;


    FaceSettings currentSettings;



    WatchDraw.FACE currentFace = WatchDraw.FACE.OUA_TIME;
    private int faceIndex =0;
    boolean firstLoad = true;

    private Handler previewHandler;
    private Runnable previewRunnable;
    private boolean showPreview = true;


    @Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        setContentView(R.layout.activity_selection);
        setup();

        previewRunnable = new Runnable() {
            @Override
            public void run() {
                if(showPreview) {
                    mPagerAdapter.notifyDataSetChanged();
                    previewHandler.postDelayed(previewRunnable, 1000);
                }
            }
        };

        previewHandler = new Handler();
        previewHandler.postDelayed(previewRunnable,0);

        if(!isActive){
            new AlertDialog.Builder(this).setTitle(getString(R.string.title_activate_faces_dialog)).setMessage(R.string.message_activate_faces)
                    .setPositiveButton(R.string.dialog_activate_ok_pick_face, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO launchFaceSelection();
                        }
                    }).setNegativeButton(R.string.dialog_activate_cancel_continute,null).show();
        }

        start();
    }


    GoogleApiClient mGoogleApiClient;

    private void start(){
        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }

        //listen for messages
        MobileWearableService.addListener(mGoogleApiClient, this);
        startService(new Intent(this, MobileWearableService.class));
   }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,MobileWearableService.class));
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            shouldOnBoard = extras.getBoolean(Constants.PREF_BOOLEAN_SHOULD_ON_BOARD, false);
            isActive = extras.getBoolean(Global.OLD_DataMap.WEAR_IS_ACTIVE.name(),false);
            isRound = extras.getBoolean(Global.OLD_DataMap.WEAR_SHAPE.name(),true);
        }else{
            isActive = Constants.isActive(this);
            isRound = Constants.isRound(this);
        }


        //todo hardcoding
        isRound = true;
        if(!showPreview) {
            showPreview = true;
            previewHandler.postDelayed(previewRunnable, 0);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPager.setCurrentItem(mPagerAdapter.getCurrentFaceIndex());

    }

    @Override
    public void onPause(){
        super.onPause();
        showPreview = false;
        mOverlayView.setVisibility(View.INVISIBLE);
        MobileWearableService.removeListener(mGoogleApiClient,this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Toast.makeText(getApplicationContext(),item.getTitle(),Toast.LENGTH_SHORT).show();
        return super.onMenuItemSelected(featureId, item);
    }

    private void setup(){
        //TODO LAUNCH FACE SELECTION
        //launchFaceSelection();

        currentFace = WatchDraw.FACE.OUA_TIME;

        takePhoto= (Button) findViewById(R.id.TakePhoto);
        takePhoto.setTypeface(Font.get(this, Font.QUICKSAND_BOLD));
        takePhoto.setVisibility(View.GONE);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(faceIndex < mPagerAdapter.getCurrentFaceIndex()){
                    //set watch
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insert(new FaceSettings(currentSettings.getColors(),currentSettings.getBase()));
                        }
                    });
                    setFace(FaceSettings.current(Realm.getDefaultInstance()));
                }else {
                    //customise
                    takePhoto();
                }
            }
        });



        favouriteButton = (ImageButton)findViewById(R.id.favouriteButton);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPagerAdapter.toggleFavourite(faceIndex);
                favouriteButton.setImageResource(currentSettings.isFavourite()?R.drawable.ic_favorite:R.drawable.ic_favorite_border);
            }
        });



        todayButton = (ImageButton)findViewById(R.id.todayButton);
        todayButton.setVisibility(View.GONE);
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              scrollToCurrent();
            }
        });

        deleteButton = (ImageButton)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPagerAdapter.deleteWatchAtPosition(faceIndex);
            }
        });



        ambientMode = (Button) findViewById(R.id.AmbientMode);
        ambientMode.setTypeface(Font.get(this, Font.QUICKSAND_BOLD));
        ambientMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ambientMode.getText().toString().contains(getResources().getString(R.string.text_active_mode))){
                    //currently active, change to ambient mode.
                    mPagerAdapter.setAmbientMode(true);
                    ambientMode.setText(R.string.text_ambient_mode);
                }else{
                    //currently ambient change to active
                    mPagerAdapter.setAmbientMode(false);
                    ambientMode.setText(R.string.text_active_mode);
                }
                mPager.invalidate();
            }
        });
        ambientMode.setVisibility(View.VISIBLE);

        mOverlayView = findViewById(R.id.info_overlay);
        if(mOverlayView != null){
            InfoFragment mInfoOverlay = new InfoFragment();
            mInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.info_swipe_to_select,R.drawable.ic_info_arrowleft, InfoFragment.type.BOTTOM));
            getSupportFragmentManager().beginTransaction().add(R.id.info_overlay, mInfoOverlay).commit();
            mOverlayView.bringToFront();
        }

        View mTopOverlayView = findViewById(R.id.top_info_overlay);
        if(mTopOverlayView != null){
            mTopInfoOverlay = new InfoFragment();
            mTopInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.faces_face_title,-1, InfoFragment.type.TOP));
            getSupportFragmentManager().beginTransaction().add(R.id.top_info_overlay,mTopInfoOverlay).commit();
            mTopOverlayView.bringToFront();
        }


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.watch_pager);
        mPagerAdapter = new WatchPageAdapter(getSupportFragmentManager(), this);


        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //scrolling, stop the page from invalidating the view.
            }
            @Override
            public void onPageSelected(int position) {

                if(firstLoad){
                    firstLoad = false;
                    takePhoto.setVisibility(View.VISIBLE);
                    mOverlayView.setVisibility(View.INVISIBLE);
                }

                faceIndex = position;
                currentPreview = mPager.getChildAt(position);
                currentFace = mPagerAdapter.faceAtPosition(position);
                currentSettings = mPagerAdapter.settingsAtPosition(position);


//                //todo testing.
//                if(currentSettings!=null && currentSettings.getColorsArray() != null) {
//                    TrianglifyDrawable drawable = new TrianglifyDrawable(150, 150, 175, 75, new PaletteColorGenerator(currentSettings.getColorsArray()), new CircularPointGenerator());
//                    takePhoto.setBackground(drawable);
//                }

                favouriteButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);


                if(position < mPagerAdapter.getCurrentFaceIndex()){


                    favouriteButton.setVisibility(View.VISIBLE);
                    favouriteButton.setImageResource(currentSettings.isFavourite()?R.drawable.ic_favorite:R.drawable.ic_favorite_border);
                    deleteButton.setVisibility(View.VISIBLE);
                    todayButton.setVisibility(View.VISIBLE);



                    //familiar faces
                    takePhoto.setVisibility(View.VISIBLE);
                    takePhoto.setText(R.string.button_set_face);
                    if(mTopInfoOverlay !=null) {

                        String text = getString(R.string.title_familiar_face);
                        if(currentSettings!=null)
                            text = DateUtils.getRelativeTimeSpanString(currentSettings.getCreatedOn(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();

                        mTopInfoOverlay.setTextString(text);
                    }
                    //todo change the onclick listener
                }else if(position == mPagerAdapter.getCurrentFaceIndex()){
                    //current face
                    favouriteButton.setVisibility(View.VISIBLE);
                    favouriteButton.setImageResource(currentSettings.isFavourite()?R.drawable.ic_favorite:R.drawable.ic_favorite_border);

                    todayButton.setVisibility(View.GONE);
                    takePhoto.setVisibility(View.GONE);

                    if(mTopInfoOverlay != null) {
                        mTopInfoOverlay.setTextString("On yer wrist!");
                    }

                }else{
                    takePhoto.setVisibility(View.VISIBLE);
                    takePhoto.setText(R.string.button_customise_face);
                    if(mTopInfoOverlay!=null) {
                        mTopInfoOverlay.setTextString((String) mPagerAdapter.getPageTitle(position));
                    }

                }





                int[] newcolors = (currentSettings!=null)?currentSettings.getColorsArray(): null;
                CustomFace customFace = new CustomFace(System.currentTimeMillis(),currentFace,newcolors,null);


                MobileWearableService.sendMessage(mGoogleApiClient, Global.Messages.UPDATE_WEAR_PREVIEW.name(), customFace.toJson(), new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        Log.d(TAG,sendMessageResult.getStatus().getStatusMessage());
                    }
                });
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        faceIndex = mPagerAdapter.getCurrentFaceIndex();

        favouriteButton.setImageResource(mPagerAdapter.isFavourite(faceIndex)?R.drawable.ic_favorite:R.drawable.ic_favorite_border);

    }

    private void scrollToCurrent() {
        int index = mPagerAdapter.getCurrentFaceIndex();
        mPager.setCurrentItem(index,true);
        todayButton.setVisibility(View.GONE);
    }


    public void takePhoto(){
        Intent i;
//        if(shouldOnBoard){
//            i = new Intent(this,onboard_camera.class);
//            i.putExtra(CustomiseActivity.EXTRA_WATCH_FACE, currentFace.ordinal());
//            startActivityForResult(i,CAMERA_IMAGE_CAPTURE);
//        }else {
            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i, CAMERA_IMAGE_CAPTURE);
//        }
        overridePendingTransition(R.animator.fade_in, R.animator.zoom_in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_IMAGE_CAPTURE:
                if(resultCode == 0)
                    return;

                Log.d(TAG,"FACE: "+currentFace.name());

                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap image = (Bitmap) extras.get("data");
                Intent customiseIntent = new Intent(this,CustomiseActivity.class);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_BITMAP_IMAGE,image);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_WATCH_FACE,currentFace.ordinal());
                startActivity(customiseIntent);
                break;
        }
    }


    public void setFace(FaceSettings currentSettings){
        Log.d(TAG,"SET: Begin");

        CustomFace customFace = new CustomFace(System.currentTimeMillis(),currentSettings.getBase(),currentSettings.getColorsArray(),null);

        MobileWearableService.sendMessage(mGoogleApiClient, Global.Messages.UPDATE_CURRENT_FACE.name(), customFace.toJson(), new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                if(!sendMessageResult.getStatus().isSuccess()){
                    Log.d(TAG,"SET: "+sendMessageResult.getStatus().getStatusMessage());
                    scrollToCurrent();
                }
            }
        });


    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG,"Got message"+ messageEvent.getPath());
    }
}
