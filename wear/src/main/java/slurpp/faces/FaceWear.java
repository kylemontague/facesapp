package slurpp.faces;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.github.jrejaud.wear_socket.WearSocket;
import com.google.android.gms.wearable.DataMap;

import slurpp.faces.watchfaces.MorphWatchFaceService;
import slurpp.faces_lib.models.WearSettings;
import slurpp.faces_lib.utils.Constants;
import slurpp.faces_lib.utils.Global;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by kylemontague on 10/04/16.
 */
public class FaceWear implements WearSocket.MessageListener, WearSocket.DataListener, WearSocket.onErrorListener {


    private static FaceWear sharedInstance;
    private WearSocket wearSocket;
    private Context mContext;

    public FaceWear() {
        wearSocket = WearSocket.getInstance();
    }


    public static FaceWear shared() {
        if(sharedInstance == null)
            sharedInstance = new FaceWear();
        return sharedInstance;
    }


    public FaceWear init(Context context){
        if(mContext == null) {
            mContext = context;
            wearSocket.setupAndConnect(context, "watch_updates", this);
            wearSocket.startMessageListener(context, Global.Messages.LAUNCH_WEAR_PREVIEW.name());
            wearSocket.startMessageListener(context, Global.Messages.CLOSE_WEAR_PREVIEW.name());
            wearSocket.startMessageListener(context, Global.Messages.VIBRATE_WATCH.name());
            wearSocket.startMessageListener(context, Global.Messages.OPEN_FACE_SELECTION.name());
            wearSocket.startMessageListener(context, Global.Messages.GET_WATCH.name());
        }
        return this;
    }




    /**
     * Send a MessageAPI request to the wear device for the current WearSettings,
     * including isActive and isRound.
     *
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void requestSettings(){
        wearSocket.sendMessage(Global.Messages.GET_WATCH.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device to start the WatchFace selection and point to the Faces app.
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void launchFaceSelection(){
        wearSocket.sendMessage(Global.Messages.OPEN_FACE_SELECTION.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device launch the Faces Preview Activity
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void launchFacePreview(){
        wearSocket.sendMessage(Global.Messages.LAUNCH_WEAR_PREVIEW.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device close the Faces Preview Activity
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void closeFacePreview(){
        wearSocket.sendMessage(Global.Messages.CLOSE_WEAR_PREVIEW.name(),"");
    }


    /**
     * Send a MessageAPI request to the wear device to vibrate
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void vibrateWear(){
        wearSocket.sendMessage(Global.Messages.VIBRATE_WATCH.name(),"");
    }

    /**
     * Make a DataMap update request to the wear device with the Preview Face settings
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void updateFacePreview(String faceJson){
        wearSocket.updateDataItem("/"+Global.DataMaps.PREVIEW_FACE_SETTINGS.name(),"json",faceJson);
    }

    /**
     * Make a DataMap update request to the wear device with the Current Face settings
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void updateFace(String faceJson){
        wearSocket.updateDataItem("/"+Global.DataMaps.CURRENT_FACE_SETTINGS.name(),"json",faceJson);
    }

    @Override
    public void dataChanged(String s, Object o) {
        Log.v("WearSocket","dataChanged - "+s+" : "+o.toString());

    }

    @Override
    public void messageReceived(String path, String message) {
        Log.v("WearSocket","messageReceived - "+path+" : "+message);

        if(path.equals(Global.Messages.OPEN_FACE_SELECTION.name())){
            startFaceSelection();
        }else if(path.equals(Global.Messages.LAUNCH_WEAR_PREVIEW.name())) {
            startFacePreview();
        }else if(path.equals(Global.Messages.CLOSE_WEAR_PREVIEW.name())){
            stopFacePreview();
        }else if(path.equals(Global.Messages.VIBRATE_WATCH.name())){
            vibrate();
        }else if(path.equals(Global.Messages.GET_WATCH.name())){
            updateSettings(WearSettings.load(mContext));
        }

    }


    public void updateSettings(WearSettings settings){
        wearSocket.updateDataItem("/"+Global.DataMaps.WEAR_SETTINGS.name(),"json",settings.toJson());
    }


    protected void startFaceSelection(){
        vibrate();
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(mContext.getPackageName(), MorphWatchFaceService.class.getCanonicalName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(intent);
    }

    protected void startFacePreview(){
        vibrate();
        Intent intent = new Intent(mContext,PreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    private void stopFacePreview() {
        mContext.sendBroadcast(new Intent(Global.Messages.CLOSE_WEAR_PREVIEW.name()));
    }

    protected void vibrate(){
        Vibrator vibrator = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
        if(vibrator.hasVibrator())
            vibrator.vibrate(new long[]{0,200,100,200},-1);
    }

    private void updateCurrentFace(DataMap map) {

        String json = map.getString("json",null);
        if(json!=null){
            Constants.getEditor(mContext).putString(Global.PREF_WATCH_FACE,json).commit();
        }

        //todo current face has been updated
        //create the object and over write the existing watch face.
    }

    private void updatePreview(DataMap map) {

        //fixme this should already be open, therefore the preview activity should also listen for these changes and update the draw.
        Intent i = new Intent(mContext, PreviewActivity.class);
        String json = map.getString("json",null);
        i.putExtra(Global.PREF_WATCH_FACE,json);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(i);

    }





    @Override
    public void onError(Throwable throwable) {
        Log.e("WearSocket",throwable.toString());
    }

    public void sentWearInfo() {

    }
}
