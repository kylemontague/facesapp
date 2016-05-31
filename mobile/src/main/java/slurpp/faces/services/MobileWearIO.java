package slurpp.faces.services;

import android.content.Context;

import com.google.android.gms.wearable.DataMap;

import slurpp.faces_lib.Global;
import slurpp.faces_lib.WearIO;
import slurpp.faces_lib.models.IMap;

/**
 * Created by kylemontague on 10/04/16.
 */
public class MobileWearIO extends WearIO {


    public MessageListener sharedListener = new MessageListener() {
        @Override
        public boolean waitingForKey(String key) {
            return true;
        }

        @Override
        public void received(String path, String message) {
            if(path.equals("/"+Global.Messages.LAUNCH_CUSTOMISE_ACTIVITY.name())) {
                //todo start the customise activity

            }
        }

        @Override
        public void updated(String path, DataMap map) {
            if (path.equals("/"+Global.DataMaps.WEAR_SETTINGS.name())) {
                //todo store the wear_settings to the shared prefs
            }
        }
    };


    private static MobileWearIO sharedInstance;
    public static MobileWearIO shared() {
        if(sharedInstance == null)
            sharedInstance = new MobileWearIO();
        return sharedInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void init(Context context, OnErrorListener onErrorListener) {
        super.init(context, onErrorListener);
        this.registerListener(sharedListener);
    }



    /**
     * Send a MessageAPI request to the wear device for the current WearSettings,
     * including isActive and isRound.
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean requestSettings(WearIO.MessageListener listener){
        return sendWithListener(listener, Global.Messages.GET_WATCH.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device to start the WatchFace selection and point to the Faces app.
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean launchFaceSelection(WearIO.MessageListener listener){
        return sendWithListener(listener,Global.Messages.OPEN_FACE_SELECTION.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device launch the Faces Preview Activity
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean launchFacePreview(WearIO.MessageListener listener){
        return sendWithListener(listener,Global.Messages.LAUNCH_WEAR_PREVIEW.name(),"");
    }

    /**
     * Send a MessageAPI request to the wear device close the Faces Preview Activity
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean closeFacePreview(WearIO.MessageListener listener){
        return sendWithListener(listener,Global.Messages.CLOSE_WEAR_PREVIEW.name(),"");
    }


    /**
     * Send a MessageAPI request to the wear device to vibrate
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean vibrateWear(WearIO.MessageListener listener){
        return sendWithListener(listener,Global.Messages.VIBRATE_WATCH.name(),"");
    }

    /**
     * Make a DataMap update request to the wear device with the Preview Face settings
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean updateFacePreview(WearIO.MessageListener listener, IMap face){
        return updateWithListener(listener,"/"+Global.DataMaps.PREVIEW_FACE_SETTINGS.name(),face);
    }

    /**
     * Make a DataMap update request to the wear device with the Current Face settings
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean updateFace(WearIO.MessageListener listener, IMap face){
        return updateWithListener(listener,"/"+Global.DataMaps.CURRENT_FACE_SETTINGS.name(),face);
    }

}
