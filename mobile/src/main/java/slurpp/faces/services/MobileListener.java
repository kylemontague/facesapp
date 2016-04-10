package slurpp.faces.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
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

import java.io.File;
import java.util.List;

import slurpp.faces.R;
import slurpp.faces.activities.Home;
import slurpp.faces_lib.Constants;
import slurpp.faces_lib.Global;


/**
 * Created by Kyle Montague on 08/01/15.
 */
public class MobileListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MOBILE LISTENER";
    private GoogleApiClient mGoogleApiClient;



    private static MobileListener sharedInstance;
    public static MobileListener shared(Context context){
        if(sharedInstance == null)
            sharedInstance = new MobileListener(context);
        return sharedInstance;
    }

    public MobileListener(Context context){
        setup(context);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        final String message = new String(messageEvent.getData());
        //Log.v("WATCH_MESSAGE",message+" path:"+messageEvent.getPath());
        if(messageEvent.getPath().equals(getString(R.string.PREF_CURRENT_FACE))) {
                Constants.getEditor(this).putString(messageEvent.getPath(), message).apply();
        }else if(messageEvent.getPath().equals(Global.Messages.LAUNCH_CUSTOMISE_ACTIVITY.name())) {
            //todo this might not work.
            startCustomiseActivity();
        }else{
            super.onMessageReceived(messageEvent);
        }
    }

    Context mContext;
    public void setup(Context context){
        mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }


    public void sendMessage(Context context, final String key, final String message){
        if(mGoogleApiClient == null)
            setup(context);
        else
            mGoogleApiClient.connect();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                                mGoogleApiClient,
                                node.getId(),
                                key,
                                message.getBytes()).await();
                        if (!result.getStatus().isSuccess()) {
                            Log.d(TAG, "error");
                        } else {
                            Log.d(TAG, "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            }).start();
    }

    public void sendMessages(Context context, final String name, final int[] keys, final String[] values){
        if(mGoogleApiClient==null)
            setup(context);
        else
            mGoogleApiClient.connect();

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/CONFIG");
        final DataMap map = putRequest.getDataMap();
        map.putString("face_name", name);
        for(int x =0;x< Math.min(keys.length,values.length);x++) {
            map.putString(context.getString(keys[x]),values[x]);
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }


    public void saveFace(Context context, final String name, final int[] keys, final String[] values){
        if(mGoogleApiClient==null)
            setup(context);
        else
            mGoogleApiClient.connect();

        final PutDataMapRequest putRequest = PutDataMapRequest.create(Global.DATA_MAP_CURRENT_FACE);
        final DataMap map = putRequest.getDataMap();
        map.putString("face_name", name);
        for(int x =0;x< Math.min(keys.length,values.length);x++) {
            map.putString(context.getString(keys[x]),values[x]);
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }

    public void launchWearPreview(Context context){
        this.sendMessage(context,Global.Messages.LAUNCH_WEAR_PREVIEW.name(),"");
    }

    public void startPreview(Context context, String filepath){
        if(mGoogleApiClient==null)
            setup(context);
        else
            mGoogleApiClient.connect();
        final PutDataMapRequest putRequest = PutDataMapRequest.create(Global.DATA_MAP_PREVIEW_FACE);
        final DataMap map = putRequest.getDataMap();

        File f = new File(filepath);
        Log.v(TAG, "Getting File: "+f.getAbsolutePath());
        if (!f.exists())
            return;
        Log.v(TAG, "Has File");
        Asset asset = Asset.createFromUri(Uri.fromFile(f));
        map.putAsset(Global.DataMap.ASSET_FACE.name(), asset);
        map.putLong(Global.DataMap.TIMESTAMP.name(),System.currentTimeMillis());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri != null ? uri.getPath() : null;
            if (path.equals("/"+Global.DATA_MAP_WEAR_DEVICE)) {
                updateWearSettings(DataMapItem.fromDataItem(event.getDataItem()).getDataMap());
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection Failed");
    }




    public void updateWearSettings(DataMap map){
        boolean isActive = map.getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), false);
        boolean isRound = map.getBoolean(Global.DataMap.WEAR_SHAPE.name(),true);
        Constants.getEditor(this).putBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), isActive);
        Constants.getEditor(this).putBoolean(Global.DataMap.WEAR_SHAPE.name(), isRound);
        Constants.getEditor(this).commit();
    }

    public void startCustomiseActivity(){
        Intent intent = new Intent(getApplicationContext(), Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Global.DataMap.WEAR_IS_ACTIVE.name(),true);
        intent.putExtra(Global.DataMap.WEAR_SHAPE.name(),Constants.isRound(this));
        startActivity(intent);
    }

}
