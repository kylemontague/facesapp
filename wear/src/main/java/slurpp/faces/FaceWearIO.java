package slurpp.faces;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Vibrator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import slurpp.faces.watchfaces.MorphWatchFaceService;
import slurpp.faces_lib.Global;
import slurpp.faces_lib.WearIO;
import slurpp.faces_lib.models.WearSettings;

/**
 * Created by kylemontague on 10/04/16.
 */
public class FaceWearIO extends WearIO {


    public MessageListener sharedListener = new MessageListener() {
        @Override
        public boolean waitingForKey(String key) {
            return true;
        }

        @Override
        public void received(String path, String message) {
            if(path.equals(Global.Messages.OPEN_FACE_SELECTION.name())){
                startFaceSelection();
            }else if(path.equals(Global.Messages.LAUNCH_WEAR_PREVIEW.name())){
                startFacePreview();
            }else if(path.equals(Global.Messages.VIBRATE_WATCH.name())){
                vibrate();
            }else if(path.equals(Global.Messages.GET_WATCH.name())){
                updateSettings(new WearSettings(true,true));//todo need to get the actual values
            }
        }

        @Override
        public void updated(String path, DataMap map) {
            if (path.equals(Global.DATA_MAP_CURRENT_FACE)) {
                updateCurrentFace(map);
            }else if(path.equals(Global.DATA_MAP_PREVIEW_FACE)){
                updatePreview(map);
            }
        }
    };

    public static FaceWearIO shared() {
        return (FaceWearIO)WearIO.shared();
    }

    @Override
    public void init(Context context, OnErrorListener onErrorListener) {
        super.init(context, onErrorListener);
        this.registerListener(sharedListener);
    }

    public boolean updateSettings(WearSettings settings){
        return updateWithListener(null,Global.DataMap.WEAR_SETTINGS.name(),settings);
    }

    public boolean launchCustomisation(WearIO.MessageListener listener){
        return sendWithListener(listener,"","");//todo update parameters
    }

    protected void startFaceSelection(){
        vibrate();
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getPackageName(), MorphWatchFaceService.class.getCanonicalName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    protected void startFacePreview(){
        vibrate();
        Intent intent = new Intent(FaceWearIO.this,PreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    protected void vibrate(){
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if(vibrator.hasVibrator())
            vibrator.vibrate(new long[]{0,200,100,200},-1);
    }

    private void updateCurrentFace(DataMap map) {
        Asset asset = map.getAsset(Global.DataMap.ASSET_FACE.name());
        String faceFile = saveAsset(asset, "Current_Face");
        if(faceFile == null)
            return;
        //todo current face has been updated
        //create the object and over write the existing watch face.
    }

    private void updatePreview(DataMap map) {
        //todo preview face has been updated
        Asset asset = map.getAsset(Global.DataMap.ASSET_FACE.name());
        String faceFile = saveAsset(asset, "Preview_Face");
        if(faceFile == null)
            return;

        //fixme this should already be open, therefore the preview activity should also listen for these changes and update the draw.
        Intent i = new Intent(FaceWearIO.this, PreviewActivity.class);
        i.putExtra(Global.Preferences.PREVIEW_FACE.name(),faceFile);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);

    }



    //todo might just remove, or tidy up and move to WearIO
    private String saveAsset(Asset asset, String name){
        ConnectionResult result = mGoogleApiClient.blockingConnect(10000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {return null;}

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();
        if (assetInputStream == null) { return null; }

        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/Faces/");
        if (!dir.exists()) { dir.mkdirs(); } // Create folder if needed

        // Read data from the Asset and write it to a file on external storage
        final File file = new File(dir, name+".txt");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = assetInputStream.read(data, 0, data.length)) != -1) {
                fOut.write(data, 0, nRead);
            }

            fOut.flush();
            fOut.close();

            return file.getPath();
        }
        catch (Exception e)
        {
        }
        return null;
    }

}
