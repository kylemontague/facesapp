package slurpp.faces.services;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import slurpp.faces_lib.utils.Global;

/**
 * Created by Kyle Montague on 15/08/2017.
 */

public class MobileWearableService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    private static final long TIMEOUT_S = 10;
    private GoogleApiClient mGoogleApiClient;
    private boolean nodeConnected = false;




    @Override
    public void onCreate()
    {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onConnected(Bundle bundle)
    {
        nodeConnected = true;
        Wearable.MessageApi.addListener(mGoogleApiClient, this );

    }

    @Override
    public void onConnectionSuspended(int i)
    {
        nodeConnected = false;
        Wearable.MessageApi.removeListener(mGoogleApiClient,this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        nodeConnected = false;
    }


    /**
     *
     * new ResultCallback<DataApi.DataItemResult>()
        {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult)
            {
            Log.e("WEAR APP", "APPLICATION Result has come");
            }
        }
     *
     *
     * */

    public void sendData(final String path, final String dataToSend, final ResultCallback<DataApi.DataItemResult> resultResultCallback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!nodeConnected) {mGoogleApiClient.blockingConnect(TIMEOUT_S, TimeUnit.SECONDS);}

                if (!nodeConnected) {Log.e("WEAR APP", "Failed to connect to mGoogleApiClient within " + TIMEOUT_S + " seconds");return;}

                if (mGoogleApiClient.isConnected())
                {
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/"+path);
                    putDataMapRequest.getDataMap().putString("json", dataToSend);
                    PutDataRequest request = putDataMapRequest.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                    pendingResult.setResultCallback(resultResultCallback);
                } else {
                    Log.e("WEAR APP", "No Google API Client connection");
                }
            }
        }).start();
    }


    public static void sendData(final GoogleApiClient client, final String path, final String dataToSend, final ResultCallback<DataApi.DataItemResult> resultResultCallback)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(!client.isConnected())
                {client.blockingConnect(TIMEOUT_S, TimeUnit.SECONDS);}


                if (client.isConnected())
                {
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/"+path);
                    putDataMapRequest.getDataMap().putString("json", dataToSend);
                    PutDataRequest request = putDataMapRequest.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(client, request);
                    pendingResult.setResultCallback(resultResultCallback);
                } else {
                    Log.e("WEAR APP", "No Google API Client connection");
                }
            }
        }).start();
    }



    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final ArrayList<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.createFromDataMapItem(DataMapItem.fromDataItem(event.getDataItem()));

            String path = event.getDataItem().getUri().getPath();
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap data = putDataMapRequest.getDataMap();
                String dataString = data.getString("json");
            }
        }
    }


    public void sendMessage(final String key, final String message, final ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!nodeConnected) {mGoogleApiClient.blockingConnect(TIMEOUT_S, TimeUnit.SECONDS);}

                if (!nodeConnected) {Log.e("WEAR APP", "Failed to connect to mGoogleApiClient within " + TIMEOUT_S + " seconds");
                    resultResultCallback.onResult(new MessageApi.SendMessageResult() {
                        @Override
                        public int getRequestId() {
                            return MessageApi.UNKNOWN_REQUEST_ID;
                        }

                        @Override
                        public Status getStatus() {
                            return new Status(CommonStatusCodes.TIMEOUT);
                        }
                    });
                    return;}

                if (mGoogleApiClient.isConnected()) {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), key, message.getBytes()).setResultCallback(resultResultCallback);
                    }
                }
            }
        }).start();
    }


    public static void sendMessage(final GoogleApiClient client, final String key, final String message, final ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                if(!client.isConnected())
                    client.blockingConnect(TIMEOUT_S, TimeUnit.SECONDS);


                if (client.isConnected()) {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(client).await();
                    for (Node node : nodes.getNodes()) {
                        Wearable.MessageApi.sendMessage(client, node.getId(), key, message.getBytes()).setResultCallback(resultResultCallback);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        super.onMessageReceived(messageEvent);
        final String message = new String(messageEvent.getData());

    }


    /**
     * Send a MessageAPI request to the wear device for the current WearSettings,
     * including isActive and isRound.
     *
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void requestSettings(ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        sendMessage(Global.Messages.GET_WATCH.name(),"", resultResultCallback);
    }



    /**
     * Send a MessageAPI request to the wear device to start the WatchFace selection and point to the Faces app.
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void launchFaceSelection(ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        sendMessage(Global.Messages.OPEN_FACE_SELECTION.name(),"",resultResultCallback);
    }


    /**
     * Send a MessageAPI request to the wear device launch the Faces Preview Activity
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void launchFacePreview(ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        sendMessage(Global.Messages.LAUNCH_WEAR_PREVIEW.name(),"",resultResultCallback);
    }




    /**
     * Send a MessageAPI request to the wear device close the Faces Preview Activity
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void closeFacePreview(ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        sendMessage(Global.Messages.CLOSE_WEAR_PREVIEW.name(),"",resultResultCallback);
    }


    /**
     * Send a MessageAPI request to the wear device to vibrate
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void vibrateWear(ResultCallback<MessageApi.SendMessageResult> resultResultCallback){
        sendMessage(Global.Messages.VIBRATE_WATCH.name(),"",resultResultCallback);
    }

    /**
     * Make a DataMap update request to the wear device with the Preview Face settings
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void updateFacePreview(String faceJson, ResultCallback<DataApi.DataItemResult> resultResultCallback){
        sendData(Global.DataMaps.PREVIEW_FACE_SETTINGS.name(),faceJson, resultResultCallback);
    }

    /**
     * Make a DataMap update request to the wear device with the Current Face settings
     * @return true if the message was able to be sent (no confirmation of being received), or false if the message couldn't be sent.
     */
    public void updateFace(String faceJson,  ResultCallback<DataApi.DataItemResult> resultResultCallback){
        sendData(Global.DataMaps.PREVIEW_FACE_SETTINGS.name(),faceJson, resultResultCallback);
    }



    public static void addListener(GoogleApiClient client, MessageApi.MessageListener listener){
        Wearable.MessageApi.addListener(client,listener);
    }

    public static void removeListener(GoogleApiClient client, MessageApi.MessageListener listener){
        Wearable.MessageApi.removeListener(client,listener);
    }

}
