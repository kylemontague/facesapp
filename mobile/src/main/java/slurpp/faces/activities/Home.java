package slurpp.faces.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;
import java.util.Locale;

import slurpp.faces.Font;
import slurpp.faces.R;
import slurpp.faces.controls.InfoFragment;
import slurpp.faces.controls.WatchPageAdapter;
import slurpp.faces.objects.DepthPageTransformer;
import slurpp.faces.services.MobileWearIO;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.utils.Global;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.utils.WearIO;
import slurpp.faces_lib.models.WearSettings;

import static slurpp.faces_lib.utils.Global.Messages.LAUNCH_WEAR_PREVIEW;


public class Home extends FragmentActivity implements WearIO.MessageListener {

    private boolean shouldOnBoard = false;
    private boolean isActive = true;
    private boolean isRound = true;



    private static final int CAMERA_IMAGE_CAPTURE = 1337;
    private Button takePhoto;
    private Button ambientMode;
    private ViewPager mPager;
    private WatchPageAdapter mPagerAdapter;
    private InfoFragment mInfoOverlay;
    private InfoFragment mTopInfoOverlay;
    private View mOverlayView;
    private View mTopOverlayView;

    private View currentPreview = null;



    WatchDraw.FACE currentFace;
    private int faceIndex =0;
    boolean firstLoad = true;

    private Handler previewHandler;
    private Runnable previewRunnable;


    private boolean showPreview = true;
    private MobileWearIO wearIO;

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
                            wearIO.launchFaceSelection(Home.this);
                        }
                    }).setNegativeButton(R.string.dialog_activate_cancel_continute,null).show();
        }




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

        if(!showPreview) {
            showPreview = true;
            previewHandler.postDelayed(previewRunnable, 0);
        }


        //todo load watch preview.

        MobileWearIO.shared().launchFacePreview(new WearIO.MessageListener() {
            @Override
            public boolean waitingForKey(String key) {
                if(key.equals(LAUNCH_WEAR_PREVIEW.name()))
                    return true;
                return false;
            }

            @Override
            public void received(String path, String message) {
                Log.d("Listener",String.format(Locale.ENGLISH,"%s %s",path,message));

            }

            @Override
            public void updated(String path, DataMap map) {

            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        showPreview = false;
        mOverlayView.setVisibility(View.INVISIBLE);
    }





    private void setup(){
        wearIO = MobileWearIO.shared();

        wearIO.launchFaceSelection(new WearIO.MessageListener() {
            @Override
            public boolean waitingForKey(String key) {
                return true;
            }

            @Override
            public void received(String path, String message) {
                Log.d("WearIO",String.format("Path: %s Message: %s",path,message));
            }

            @Override
            public void updated(String path, DataMap map) {
                Log.d("WearIO",String.format("Path: %s Map: %s",path,map.toString()));

            }
        });

        currentFace = WatchDraw.FACE.OUA_TIME;

        takePhoto= (Button) findViewById(R.id.TakePhoto);
        takePhoto.setTypeface(Font.get(this, Font.QUICKSAND_BOLD));
        takePhoto.setVisibility(View.INVISIBLE);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MobileWearIO.shared().vibrateWear(null);
                takePhoto();
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

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.watch_pager);
        mPagerAdapter = new WatchPageAdapter(getSupportFragmentManager(), this);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if(firstLoad && faceIndex == 0){
                    firstLoad = false;
                    takePhoto.setVisibility(View.VISIBLE);
                    mOverlayView.setVisibility(View.INVISIBLE);
                }
                faceIndex = position;
                mTopInfoOverlay.setTextString((String) mPagerAdapter.getPageTitle(position));
                currentPreview = mPager.getChildAt(position);
                currentFace = mPagerAdapter.faceAtPosition(position);


            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

//        mPager.setOnTouchListener(new View.OnTouchListener() {
//
//            long lastTap = 0;
//            PointF lastXY = null;
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    if((System.currentTimeMillis() - lastTap) < 800 && lastXY != null){
//                        PointF curXY = new PointF(event.getX(),event.getY());
//                        if((Math.abs(curXY.x - lastXY.x)+ Math.abs(curXY.y - lastXY.y)) < 10)
//                            takePhoto();
//                    }
//                    lastTap = System.currentTimeMillis();
//                    lastXY = new PointF(event.getX(),event.getY());
//                }
//                return false;
//            }
//        });

        mOverlayView = findViewById(R.id.info_overlay);
        if(mOverlayView != null){
            mInfoOverlay = new InfoFragment();
            mInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.info_swipe_to_select,R.drawable.ic_info_arrowleft, InfoFragment.type.BOTTOM));
            getSupportFragmentManager().beginTransaction().add(R.id.info_overlay,mInfoOverlay).commit();
            mOverlayView.bringToFront();
        }

        mTopOverlayView = findViewById(R.id.top_info_overlay);
        if(mTopOverlayView != null){
            mTopInfoOverlay = new InfoFragment();
            mTopInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.faces_face_title,-1, InfoFragment.type.TOP));
            getSupportFragmentManager().beginTransaction().add(R.id.top_info_overlay,mTopInfoOverlay).commit();
            View v = findViewById(R.id.top_info_overlay);
            v.bringToFront();
        }


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
                Bundle extras = data.getExtras();
                Bitmap image = (Bitmap) extras.get("data");
                Intent customiseIntent = new Intent(this,CustomiseActivity.class);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_BITMAP_IMAGE,image);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_WATCH_FACE,currentFace.ordinal());
                startActivity(customiseIntent);
                break;
        }
    }

//    public void getCurrentWatch() {
//        //todo communicate with the watch face.
//        //todo or setup listener for messages.
//    }


    int[] colours = new int[]{slurpp.faces_lib.R.color.black, slurpp.faces_lib.R.color.green, slurpp.faces_lib.R.color.white, slurpp.faces_lib.R.color.white, slurpp.faces_lib.R.color.white};
    private void createFaces(){
        ArrayList<CustomFace> faces = new ArrayList<>();
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.BUSINESS_TIME,colours,"Test Watch 1"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.OUA_TIME,colours,"Test Watch 3"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.FILL_TIME,colours,"Test Watch 4"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.BUSINESS_TIME, colours, "Test Watch 5"));

    }



    @Override
    public boolean waitingForKey(String key) {
        return (key.equals(Global.Messages.OPEN_FACE_SELECTION.name()));
    }

    @Override
    public void received(String path, String message) {

    }

    @Override
    public void updated(String path, DataMap map) {
        Toast.makeText(getApplicationContext(),"UPDATED DATA",Toast.LENGTH_LONG).show();
        WearSettings settings = map.get(Global.OLD_DataMap.WEAR_SETTINGS.name());
        isActive = settings.isActive;
        isRound = settings.isRound;
        //todo update the preview if they are wrong.
    }
}
