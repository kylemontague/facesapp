package slurpp.faces;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;

import slurpp.faces.watchfaces.MorphWatchFaceService;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;
import slurpp.faces_lib.utils.WearIO;
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
            }else if(path.equals(Global.Messages.LAUNCH_WEAR_PREVIEW.name())) {
                startFacePreview();
            }else if(path.equals(Global.Messages.CLOSE_WEAR_PREVIEW.name())){
                stopFacePreview();
            }else if(path.equals(Global.Messages.VIBRATE_WATCH.name())){
                vibrate();
            }else if(path.equals(Global.Messages.GET_WATCH.name())){
                updateSettings(WearSettings.load(FaceWearIO.this.getApplicationContext()));
            }
        }

        @Override
        public void updated(String path, DataMap map) {
            if (path.equals("/"+Global.DataMaps.CURRENT_FACE_SETTINGS.name())) {
                updateCurrentFace(map);
            }else if(path.equals("/"+Global.DataMaps.PREVIEW_FACE_SETTINGS.name())){
                updatePreview(map);
            }
        }
    };



    private static FaceWearIO sharedInstance;
    public static FaceWearIO shared() {
        if(sharedInstance == null)
            sharedInstance = new FaceWearIO();
        return sharedInstance;
    }


    @Override
    public void init(Context context, OnErrorListener onErrorListener) {
        super.init(context, onErrorListener);
        this.registerListener(sharedListener);
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        sharedListener.received(messageEvent.getPath(),new String(messageEvent.getData()));
    }

    public boolean updateSettings(WearSettings settings){
        return updateWithListener(null, "/"+Global.DataMaps.WEAR_SETTINGS.name(),settings.toJson());
    }

    public boolean launchCustomisation(WearIO.MessageListener listener){
        return sendWithListener(listener,Global.Messages.LAUNCH_CUSTOMISE_ACTIVITY.name(),"");
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


    private void stopFacePreview() {
        sendBroadcast(new Intent(Global.Messages.CLOSE_WEAR_PREVIEW.name()));
    }

    protected void vibrate(){
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        if(vibrator.hasVibrator())
            vibrator.vibrate(new long[]{0,200,100,200},-1);
    }

    private void updateCurrentFace(DataMap map) {

        String json = map.getString("json",null);
        if(json!=null){
            Constants.getEditor(FaceWearIO.this).putString(Global.PREF_WATCH_FACE,json).commit();
        }

        //todo current face has been updated
        //create the object and over write the existing watch face.
    }

    private void updatePreview(DataMap map) {

        //fixme this should already be open, therefore the preview activity should also listen for these changes and update the draw.
        Intent i = new Intent(FaceWearIO.this, PreviewActivity.class);
        String json = map.getString("json",null);
        i.putExtra(Global.PREF_WATCH_FACE,json);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);

    }







    public boolean sentWearInfo(WearIO.MessageListener messageListener){
        return false;
    }
}
