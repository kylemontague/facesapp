package slurpp.faces.watchfaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mclemont.faces.R;
import montague.faces_lib.Global;
import montague.faces_lib.Prefs;
import slurpp.faces.PreviewActivity;


/**
 * Created by kyle montague on 23/12/14.
 */
public class FaceListener extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "Faces";
    private static FaceListener sharedInstance;

    public static FaceListener shared(Context context, String name){
        if(sharedInstance == null)
            sharedInstance = new FaceListener(context, name);
        return sharedInstance;
    }

    public FaceListener(){
        //setup(FaceListener.this, null);
    }

    private Prefs p;
    private String n = null;


    Context mContext;

    public FaceListener(Context context, String name){
      mContext = context;
      setup(context,name);
      mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        final String message = new String(messageEvent.getData());
        Log.d("MORPH",message+" path:"+messageEvent.getPath());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FaceListener.this);
        if(messageEvent.getPath().contains("PREF.")) {
            if (sp != null) {
                String currentFace = sp.getString(getString(R.string.PREF_CURRENT_FACE), "");
                sp.edit().putString(currentFace + "_" + messageEvent.getPath(), message).commit();
            }
        }else if(messageEvent.getPath().contains("FACES_PREVIEW")){
            preview(message);
        }else if (messageEvent.getPath().contains(Global.Messages.GET_WATCH.toString())) {
            sendWearSettings();
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    public void setup(Context context, String name){
        mContext = context;
        if(p == null) {
            p = new Prefs(context);
            n = (name == null && p != null) ? p.getString(mContext.getString(R.string.PREF_CURRENT_FACE),null) : name;
        }
    }


    // messages api
    private GoogleApiClient mGoogleApiClient;
    private Node mNode; // the connected device to send the message to
    public void sendMessage(final String key, final String message){
        if(mGoogleApiClient==null)
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        mGoogleApiClient.connect();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
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
                            Log.v(TAG, "error");
                        } else {
                            Log.v(TAG, "success!! sent to: " + node.getDisplayName());
                        }
                    }
                }
            },1000);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private Collection<Node> getNodes() {
        HashSet<Node> results = new HashSet<Node>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            Log.v(TAG, "Node: " + node.getId() + " " + node.getDisplayName());
            results.add(node);
        }
        return results;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
            if(path.equals("/PREVIEW")) {
                int face = map.getInt(Global.Preferences.CURRENT_FACE.name(),3);
                Intent i = new Intent(FaceListener.this,PreviewActivity.class);
                i.putExtra(Global.Preferences.PREVIEW_FACE.name(),face);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            }else if (path.equals(Global.DATA_MAP_CURRENT_FACE)) {

                updateCurrentFace(map);
            }else if(path.equals(Global.DATA_MAP_PREVIEW_FACE)){
                updatePreview(map);
            }
        }
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
        Intent i = new Intent(FaceListener.this, PreviewActivity.class);
        i.putExtra(Global.Preferences.PREVIEW_FACE.name(),faceFile);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);

    }

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


    private void preview(String state){
        if(state == "START"){
            Toast.makeText(FaceListener.this,"PREIVEW REQUESTED",Toast.LENGTH_LONG).show();
            Intent i = new Intent(FaceListener.this,PreviewActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.putExtra("FACE_TYPE", 2);
            //todo need to use actual face information not this.
            startActivity(i);
        }else{
            //todo close preview / stop the wakelock
        }
    }

    private boolean isFacesActive(){
        return Global.isMyServiceRunning(getApplicationContext(),MorphWatchFaceService.class);
    }


    public void sendWearSettings(){
        Log.d("MORPH","CHECKING IF CLIENT IS NULL");

        if(mGoogleApiClient==null)
            mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        Log.d("MORPH","SENDING WEAR SETTINGS");
        mGoogleApiClient.connect();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRound = sp.getBoolean(Global.DataMap.WEAR_SHAPE.name(),true);

        final PutDataMapRequest putRequest = PutDataMapRequest.create(Global.DATA_MAP_WEAR_DEVICE);
        final DataMap map = putRequest.getDataMap();
        map.putBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(), isFacesActive());
        map.putBoolean(Global.DataMap.WEAR_SHAPE.name(),isRound);
        map.putLong(Global.DataMap.TIMESTAMP.name(),System.currentTimeMillis());
        Wearable.DataApi.putDataItem(mGoogleApiClient, putRequest.asPutDataRequest());
    }



}
