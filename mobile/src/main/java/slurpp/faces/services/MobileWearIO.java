package slurpp.faces.services;

import slurpp.faces_lib.Global;
import slurpp.faces_lib.WearIO;

/**
 * Created by kylemontague on 10/04/16.
 */
public class MobileWearIO extends WearIO {


    public static MobileWearIO shared() {
        return (MobileWearIO)WearIO.shared();
    }

    /**
     * Send a MessageAPI request to the wear device for the current WearSettings,
     * including isActive and isRound.
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean requestSettings(WearIO.MessageListener listener){
        return sendWithListener(listener, Global.Messages.GET_WATCH.toString(),"");//todo update parameters
    }

    /**
     * Send a MessageAPI request to the wear device to start the WatchFace selection and point to the Faces app.
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean launchFaceSelection(WearIO.MessageListener listener){
        return sendWithListener(listener,"","");//todo update parameters
    }

    /**
     * Send a MessageAPI request to the wear device launch the Faces Preview Activity
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean launchFacePreview(WearIO.MessageListener listener){
        return sendWithListener(listener,"","");//todo update parameters
    }

    /**
     * Send a MessageAPI request to the wear device close the Faces Preview Activity
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean closeFacePreview(WearIO.MessageListener listener){
        return sendWithListener(listener,"","");//todo update parameters
    }

    /**
     * Make a DataMap update request to the wear device with the Preview Face settings
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean updateFacePreview(WearIO.MessageListener listener, Object face){
        return updateWithListener(listener,"",face);//todo update parameters
    }

    /**
     * Make a DataMap update request to the wear device with the Current Face settings
     * @param listener that will receive the callback response for the message
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public boolean updateFace(WearIO.MessageListener listener, Object face){
        return updateWithListener(listener,"",face);//todo update parameters
    }

}
