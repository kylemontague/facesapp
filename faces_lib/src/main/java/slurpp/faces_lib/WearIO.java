package slurpp.faces_lib;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import slurpp.faces_lib.models.IMap;

/**
 * Created by Kyle Montague on 09/04/16.
 */
public class WearIO extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    private Context context;
    private ArrayList<MessageListener> listeners;

    protected GoogleApiClient mGoogleApiClient;
    private OnErrorListener onErrorListener;
    private boolean initialised = false;

    public WearIO() {
    }


    public void init(Context context, OnErrorListener onErrorListener){
        this.context = context;
        if(!initialised) {
            initGoogleApi();
            this.listeners = new ArrayList<>();
            this.onErrorListener = onErrorListener;
            initialised = true;
        }
    }

    private void initGoogleApi(){

        mGoogleApiClient = new GoogleApiClient.Builder(this.context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void registerListener(MessageListener listener){
        if(listeners == null || listener == null || listeners.contains(listener))
            return;
        listeners.add(listener);
    }

    public void unregisterListener(MessageListener listener){
        if(listeners == null || listener == null)
            return;
        listeners.remove(listener);
    }


    public boolean sendWithListener(WearIO.MessageListener listener, String key, String value){
        if(this.sendMessage(key,value)){
            this.registerListener(listener);
            return  true;
        }
        return false;
    }

    public boolean updateWithListener(WearIO.MessageListener listener, String path, IMap object){
        if(this.update(path,object.toMap())){
            this.registerListener(listener);
            return true;
        }
        return false;
    }

    public boolean sendMessage(final String key, final String message){
        if(!initialised)
           return false;

        if(mGoogleApiClient == null)
            initGoogleApi();

        mGoogleApiClient.connect();
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), key, message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.d("WearIO", String.format("Msg: %s sent to: %s",key+" - "+message,node.getDisplayName()));
                    }
                }
            }
        }).start();
        return true;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        super.onMessageReceived(messageEvent);

        final String message = new String(messageEvent.getData());

        Log.d("WearIO", String.format("Received Path: %s Msg: %s",messageEvent.getPath(),message));


        if(listeners !=null) {
            for (MessageListener listener : listeners) {
                if (listener.waitingForKey(messageEvent.getPath()))
                    listener.received(messageEvent.getPath(), message);
            }
        }
    }


    public boolean update(String path, final List<String> keys, final List<String> values) {
        if(!initialised)
            return false;

        if(mGoogleApiClient == null)
            initGoogleApi();

        mGoogleApiClient.connect();

        final PutDataMapRequest putRequest = PutDataMapRequest.create(path);
        final DataMap map = putRequest.getDataMap();
        for (int x = 0; x < Math.min(keys.size(), values.size()); x++) {
            map.putString(keys.get(x), values.get(x));
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
        return true;
    }


    public boolean update(String path, final HashMap<String,String> hashMap) {
        if(!initialised)
            return false;

        if(mGoogleApiClient == null)
            initGoogleApi();

        mGoogleApiClient.connect();

        final PutDataMapRequest putRequest = PutDataMapRequest.create(path);
        final DataMap map = putRequest.getDataMap();

        for( String key : hashMap.keySet()){
            map.putString(key, hashMap.get(key));
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
        return true;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            for(MessageListener listener: listeners){
                if(listener.waitingForKey(path))
                    listener.updated(path, DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        this.context = WearIO.this;
        initGoogleApi();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        onErrorListener.onError(new Throwable("Connection Failed"));
    }



    public interface MessageListener{
        boolean waitingForKey(String key);
        void received(String path, String message);
        void updated(String path, DataMap map);
    }

    public interface OnErrorListener{
        void onError(Throwable throwable);
    }

}
