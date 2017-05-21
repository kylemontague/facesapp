package slurpp.faces.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.wearable.DataMap;
import com.squareup.seismic.ShakeDetector;

import java.util.List;
import java.util.Random;

import slurpp.faces.Font;
import slurpp.faces.R;
import slurpp.faces.controls.InfoFragment;
import slurpp.faces.controls.ProgressAdapter;
import slurpp.faces.controls.SwatchAdapter;
import slurpp.faces.services.MobileWearIO;
import slurpp.faces.utils.BlurBuilder;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.utils.Global;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.utils.WearIO;

import static slurpp.faces_lib.utils.Global.Messages.LAUNCH_WEAR_PREVIEW;


public class CustomiseActivity extends FragmentActivity implements ShakeDetector.Listener, WearIO.MessageListener {


    public static String EXTRA_BITMAP_IMAGE= "EXTRA_BITMAP_IMAGE";
    public static String EXTRA_WATCH_FACE = "EXTRA_WATCH_FACE";


    private static final int CAMERA_IMAGE_CAPTURE = 1337;
    private static final String TAG = "CAMERA";


    private boolean shouldOnBoard = true;
    private boolean isActive = false;

    private View mOnBoardView;
    private GridView swatchContainer;

    private Button faceButton;


    private SwatchAdapter swatchAdapter;
    private ProgressAdapter progressAdapter;

    private int[] preferences;
    private int[] markers;

    private int preferenceIndex=0;
    private int selectedSwatch = -1;
    private Bitmap mImage;
    private WatchDraw.FACE currentFace;

    private MobileWearIO wearIO;
    private DataSetObserver dragObserver;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_customise);
        Bitmap mTmpImage = (Bitmap)getIntent().getExtras().get(CustomiseActivity.EXTRA_BITMAP_IMAGE);
        final int faceID = getIntent().getExtras().getInt(CustomiseActivity.EXTRA_WATCH_FACE,0);
        currentFace = WatchDraw.FACE.fromOrdinal(faceID);
        mImage = BlurBuilder.blur(this, mTmpImage, .95f, 2.0f);

        shouldOnBoard = Constants.shouldOnBoard(getApplicationContext());
        isActive = Constants.isActive(getApplicationContext());

        // listen for faces watchface on wear device. update ui if dropped out
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(key == Global.OLD_DataMap.WEAR_IS_ACTIVE.name()){
                    isActive = prefs.getBoolean(key,false);
                    Toast.makeText(CustomiseActivity.this,"WEAR ACTIVE:"+isActive,Toast.LENGTH_SHORT).show();
                }
            }
        };
        Constants.getPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        setup();



        if(shouldOnBoard) {
            dragObserver = new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    if (shouldOnBoard) {
                        if(mOnBoardView!=null)
                           mOnBoardView.setVisibility(View.INVISIBLE);
                        faceButton.setVisibility(View.VISIBLE);
                        shouldOnBoard = false;
                        swatchAdapter.unregisterDataSetObserver(dragObserver);
                    }
                }
            };
        }

        setCurrentPreviewWatch(currentFace);
        updateInterface(mImage);


        wearIO = MobileWearIO.shared();

        wearIO.launchFacePreview(this);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);



    }

    @Override
    protected void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startPreview();
    }

    private void setup() {
        faceButton = (Button) findViewById(R.id.TakePhoto);
        faceButton.setTypeface(Font.get(this, Font.QUICKSAND_BOLD));
        faceButton.setText(R.string.save);
        faceButton.setVisibility(View.VISIBLE);
        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo update the watch face on the wear device (DATAMAP)
                //todo close the wear preview.

                saveFace();
            }
        });


        swatchContainer = (GridView) findViewById(R.id.swatchContainer);
        swatchContainer.setVisibility(View.VISIBLE);


        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        View imageView = findViewById(R.id.blur_image);
        Drawable img = new BitmapDrawable(getResources(), mImage);
        img.setAlpha(150);
        img.setColorFilter(filter);
        imageView.setBackground(img);

        View mOverlayView = findViewById(R.id.top_info_overlay);
        if (mOverlayView != null) {
            InfoFragment mInfoOverlay = new InfoFragment();
            mInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.info_pick_colours, -1, InfoFragment.type.TOP));
            getSupportFragmentManager().beginTransaction().replace(R.id.top_info_overlay, mInfoOverlay).commit();
            mOverlayView.bringToFront();
        }


        if (shouldOnBoard) {
            mOnBoardView = findViewById(R.id.info_overlay);
            mOnBoardView.setVisibility(View.VISIBLE);
            faceButton.setVisibility(View.INVISIBLE);
            if (mOverlayView != null) {
                InfoFragment mOnBoardOverlay = new InfoFragment();
                //todo update to the correct image.
                mOnBoardOverlay.setArguments(InfoFragment.argumentBuilder(R.string.onboard_msg_drag_to_change, R.drawable.ic_info_circle, InfoFragment.type.BOTTOM));//todo change to circle icon
                getSupportFragmentManager().beginTransaction().replace(R.id.info_overlay, mOnBoardOverlay).commit();
                View v = findViewById(R.id.info_overlay);
                v.bringToFront();
            }
        }
    }


    public void saveFace(){
        //todo store the json / xml face design.
        CustomFace customFace = new CustomFace(System.currentTimeMillis(),currentFace,swatchAdapter.getColors(),null);
        MobileWearIO.shared().updateFace(this,customFace.toJson());
    }

    private void updateInterface(Bitmap image){
        Palette p = Palette.generate(image,20);
        final List<Palette.Swatch> swatches = p.getSwatches();
        if(swatches.size()>0){
            swatchAdapter = new SwatchAdapter(this,swatches);
            swatchContainer.setVisibility(View.VISIBLE);
            swatchContainer.setAdapter(swatchAdapter);
            swatchAdapter.registerDataSetObserver(dragObserver);
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in), 0.4f); //0.5f == time between appearance of listview items.
            swatchContainer.setLayoutAnimation(lac);
//            swatchContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                    swatchAdapter.setSelected(position);
//                    selectedSwatch = swatchAdapter.getColour(position);
//                    //updateProgressBar(selectedSwatch);
//                    String tmp_colour = colourFromRGB(selectedSwatch);//tmp.getRgb());
//                    //todo this should update the watch preview app.
//                    if(preferenceIndex < preferences.length) {
//                        updateWatchface(getString(preferences[preferenceIndex]), tmp_colour);
//                    }
//                    faceButton.setVisibility(View.VISIBLE);
//                    updateSwatches();
//
//                }
//            });
            swatchContainer.startLayoutAnimation();
            //faceButton.setVisibility(View.INVISIBLE);

        }else
        {
            faceButton.setVisibility(View.VISIBLE);
            swatchContainer.setVisibility(View.INVISIBLE);
        }

        randomSelection(preferences.length);
    }


    private void updateSwatches(){


        CustomiseActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                swatchContainer.setAdapter(swatchAdapter);
                swatchContainer.invalidateViews();
            }
        });

    }

    private String colourFromRGB(int rgb){
        return String.format("#%06X", 0xFFFFFF & rgb);
    }

    private void updateWatchFace(String name, int[] keys, String[] values) {
        //todo dont forget the name
        //wearIO.update(Global.DATA_MAP_CURRENT_FACE,keys,values);
    }

    public void setCurrentPreviewWatch(WatchDraw.FACE face) {
        preferences = WatchDraw.FACE.getPreviewPreferences(face);

    }


    private void startPreview(){
        //todo need to pass the file path to the serialised setup.
//        mobileListener.startPreview(this, "");
    }


    public void randomSelection(final int count){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Random rn = new Random(System.currentTimeMillis());
                int n = swatchAdapter.getCount();
                markers = new int[count];
                int x=0;
                while (x < markers.length){
                    int i = rn.nextInt() % n;
                    if(i >= 0 && !swatchAdapter.containsMarker(i)){
                        Log.d("MARKER",x+" : "+i);
                        markers[x] = i;
                        swatchAdapter.addMarker(i);
                        x++;
                    }
                }
                updateSwatches();
            }
        };

        thread.start();



    }

    Vibrator vibrator = null;
    @Override
    public void hearShake() {
        if(vibrator == null)
            vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        swatchAdapter.clearMarkers();
        randomSelection(preferences.length);

        if (shouldOnBoard) {
            if(mOnBoardView!=null)
                mOnBoardView.setVisibility(View.INVISIBLE);
            faceButton.setVisibility(View.VISIBLE);
            shouldOnBoard = false;
            swatchAdapter.unregisterDataSetObserver(dragObserver);
        }
    }

    @Override
    public boolean waitingForKey(String key) {
        if(key.equals(LAUNCH_WEAR_PREVIEW.name()))
            return true;
        return false;
    }

    @Override
    public void received(String path, String message) {
        Log.d("WEAR-MESSAGE",String.format("%s %s",path,message));
    }

    @Override
    public void updated(String path, DataMap map) {
        Log.d("WEAR-MESSAGE",String.format("%s %s",path,map.toString()));
    }
}
