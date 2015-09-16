package slurpp.faces.activities;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import montague.faces_lib.WatchDraw;
import slurpp.faces.Font;
import slurpp.faces.MobileListener;
import slurpp.faces.R;
import slurpp.faces.controls.InfoFragment;
import slurpp.faces.controls.ProgressAdapter;
import slurpp.faces.controls.SwatchAdapter;
import slurpp.faces.objects.ProgressItem;
import slurpp.faces.objects.SwatchView;
import slurpp.faces.utils.BlurBuilder;


public class CustomiseActivity extends FragmentActivity{


    public static String EXTRA_BITMAP_IMAGE= "EXTRA_BITMAP_IMAGE";
    public static String EXTRA_WATCH_FACE = "EXTRA_WATCH_FACE";


    private static final int CAMERA_IMAGE_CAPTURE = 1337;
    private static final String TAG = "CAMERA";



    private InfoFragment mInfoOverlay;
    private View mOverlayView;
    private GridView swatchContainer;
    private HorizontalScrollView progressbarContainer;
    private LinearLayout progressbar;
    private Button faceButton;
    private View imageView;


    private SwatchAdapter swatchAdapter;
    private ProgressAdapter progressAdapter;

    private int[] preferences;
    private int[] markers;

    private int preferenceIndex=0;
    private int selectedSwatch = -1;
    private Bitmap mImage;
    private WatchDraw.FACE currentFace;
    private ArrayList<String> faceValues;

    private MobileListener mobileListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_customise);
        Bitmap mTmpImage = (Bitmap)getIntent().getExtras().get(CustomiseActivity.EXTRA_BITMAP_IMAGE);
        int faceID = getIntent().getExtras().getInt(CustomiseActivity.EXTRA_WATCH_FACE,0);
        currentFace = WatchDraw.FACE.fromOrdinal(faceID);
        mImage = BlurBuilder.blur(this, mTmpImage, .95f, 2.0f);
        setup();


        updateInterface(mImage);
        //todo change this to get the id from the preferences
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        setCurrentPreviewWatch(currentFace);
        faceValues = new ArrayList<>();
        faceValues.add("" + currentFace.ordinal());

        mobileListener = new MobileListener(getApplicationContext());




    }

    @Override
    protected void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        startPreview();
    }

    private void setup(){
        faceButton = (Button) findViewById(R.id.TakePhoto);
        faceButton.setTypeface(Font.get(this, Font.QUICKSAND_BOLD));
        faceButton.setVisibility(View.INVISIBLE);
        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setColour();
            }
        });


        swatchContainer = (GridView) findViewById(R.id.swatchContainer);
        swatchContainer.setVisibility(View.VISIBLE);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView = findViewById(R.id.blur_image);
        Drawable img = new BitmapDrawable(getResources(), mImage);
        img.setAlpha(200);
        //todo remove b&w colour filter here.
        img.setColorFilter(filter);
        imageView.setBackground(img);




        mOverlayView = findViewById(R.id.info_overlay);
        if(mOverlayView != null){
            mInfoOverlay = new InfoFragment();
            //todo update to the correct image.
            mInfoOverlay.setArguments(InfoFragment.argumentBuilder(R.string.info_background_colour,-1, InfoFragment.type.TOP));
            getSupportFragmentManager().beginTransaction().add(R.id.info_overlay,mInfoOverlay).commit();
            View v = findViewById(R.id.info_overlay);
            v.bringToFront();
        }

        progressbarContainer = (HorizontalScrollView) findViewById(R.id.progressbarContainer);
        progressbarContainer.setVisibility(View.VISIBLE);
        progressbar = (LinearLayout) findViewById(R.id.progressbar);

        //todo add the images / icons in the correct states.




        //  Needed for communication between watch and device.
    }

    private void setColour() {
        //todo set the colour in the configuration file.
        ProgressItem item = (ProgressItem)progressAdapter.getItem(preferenceIndex);
        item.addColor(selectedSwatch);
        faceValues.add(colourFromRGB(selectedSwatch));
        progressAdapter.next();
        preferenceIndex++;
        progressbar.removeAllViews();
        for(int x=0;x<progressAdapter.getCount();x++){
            progressbar.addView(progressAdapter.getView(x,null,progressbar));
        }
        mInfoOverlay.setText(progressAdapter.currentPreference());
        swatchAdapter.setSelected(-2);
        updateSwatches();
        faceButton.setVisibility(View.INVISIBLE);

        if(preferenceIndex >= progressAdapter.getCount()){
            //finished
            faceButton.setText(R.string.save);
            faceButton.setVisibility(View.VISIBLE);
            faceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFace();
                }
            });
            mInfoOverlay.setText(R.string.info_colours_selected);
        }
    }

    private void updateProgressBar(int rgb){
        Log.d("PROGRESS_UPDATE", "" + rgb);
        View gridItem = progressbar.getChildAt(progressAdapter.getCurrentIndex());
        SwatchView view = (SwatchView) gridItem.findViewById(R.id.grid_swatch);
        view.setFillColor(rgb);
        view.invalidate();
        view.refreshDrawableState();
    }


    public void saveFace(){
       //todo store the json / xml face design.
//
//        WatchData wData = new WatchData(System.currentTimeMillis(),"name",mBaseFace);
//        wData.addColour("BACKGROUND","#ffffff");
//        wData.addColour("FOREGROUND","#00FF00");
//        Log.d(TAG,wData.toJSON());

        int[] prefIDs = WatchDraw.FACE.getPreferences(currentFace);
        String[] prefValues = new String[faceValues.size()];
        faceValues.toArray(prefValues);
        updateWatchFace(currentFace.toString(), prefIDs, prefValues);


    }

    private void updateInterface(Bitmap image){
        Palette p = Palette.generate(image,20);
        final List<Palette.Swatch> swatches = p.getSwatches();
        if(swatches != null && swatches.size()>0){
            swatchAdapter = new SwatchAdapter(this,swatches);
            swatchContainer.setVisibility(View.VISIBLE);
            swatchContainer.setAdapter(swatchAdapter);
            LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in), 0.4f); //0.5f == time between appearance of listview items.
            swatchContainer.setLayoutAnimation(lac);
            swatchContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    swatchAdapter.setSelected(position);
                    selectedSwatch = swatchAdapter.getColour(position);
                    updateProgressBar(selectedSwatch);
                    String tmp_colour = colourFromRGB(selectedSwatch);//tmp.getRgb());
                    //todo this should update the watch preview app.
                    if(preferenceIndex < preferences.length) {
                        updateWatchface(getString(preferences[preferenceIndex]), tmp_colour);
                    }
                    faceButton.setVisibility(View.VISIBLE);
                    updateSwatches();

                }
            });
            swatchContainer.startLayoutAnimation();
            faceButton.setVisibility(View.INVISIBLE);

        }else
        {
            faceButton.setVisibility(View.VISIBLE);
            swatchContainer.setVisibility(View.INVISIBLE);
        }

        //todo need to pass the configuration steps for this particular watch
        if(progressAdapter == null)
            progressAdapter = new ProgressAdapter(this.getApplicationContext());
        for(int x=0;x<progressAdapter.getCount();x++){
            progressbar.addView(progressAdapter.getView(x,null,progressbar));
        }

        //todo need to pass the correct number of markers to create
        randomSelection(3);
    }


    private void updateSwatches(){

        swatchContainer.setAdapter(swatchAdapter);
        swatchContainer.invalidateViews();
    }

    private String colourFromRGB(int rgb){
        return String.format("#%06X", 0xFFFFFF & rgb);
    }

    private void updateWatchface(final String key, final String message){
        mobileListener.sendMessage(getApplicationContext(), key, message);
    }


    private void updateWatchFace(String name, int[] keys, String[] values) {
        Log.d("SAVE","Saving Watch:"+name+", "+keys.toString()+", "+values.toString());
        mobileListener.saveFace(getApplicationContext(), name, keys, values);
    }




    public void setCurrentPreviewWatch(WatchDraw.FACE face) {
        preferences = WatchDraw.FACE.getPreviewPreferences(face);

    }


    private void startPreview(){
        //todo need to ass the file path to the serialised setup.
        mobileListener.startPreview(this, "");
    }


    public void randomSelection(final int count){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Random rn = new Random(System.currentTimeMillis());
                int n = 16 + 1;
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


}
