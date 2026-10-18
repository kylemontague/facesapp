package slurpp.faces.controls;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.ArrayList;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.v2.models.FaceSettings;

/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchPageAdapter extends FragmentPagerAdapter {


    private static final String TAG = "REALM";
    FragmentManager fragmentManager;
    ArrayList<WatchFragment> watches;
    Context mContext;
    RealmResults<FaceSettings> familiarFaces;
    OrderedRealmCollectionChangeListener familiarChangeListener = new OrderedRealmCollectionChangeListener<RealmResults<FaceSettings>>() {
        @Override
        public void onChange(RealmResults<FaceSettings> faceSettings, OrderedCollectionChangeSet changeSet) {

            faceSettings.load();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateWatches();
                }
            }, 500);
        }

    };



    public int getCurrentFaceIndex() {
        return currentFaceIndex;
    }



    int currentFaceIndex = 0;

    public WatchPageAdapter(FragmentManager fm, Context context) {
        super(fm);

        fragmentManager = fm;
        mContext = context;
        familiarFaces = FaceSettings.all(Realm.getDefaultInstance(), Sort.ASCENDING);
        updateWatches();
        familiarFaces.addChangeListener(familiarChangeListener);

    }


    private void deleteWatches(int index, int count){
        if(watches != null){
            for(int i = index; i < index+count; i++){
                fragmentManager.beginTransaction().remove(watches.get(i)).commit();
            }
        }
    }


    private void updateWatches(){

        if(watches!=null)
            deleteWatches(0,watches.size());

        currentFaceIndex = 0;
        watches = new ArrayList<>();
        int index=0;
        for(FaceSettings face:familiarFaces){
            if(index == familiarFaces.size()-1){
                Log.d(TAG,"Color: "+face.getColors().first().getNormal());
            }
            index++;
            watches.add(WatchFragment.fromFaceSettings(face));
        }

        if(watches.size() > 0){
            currentFaceIndex = watches.size()-1;
        }else if(watches.size() == 0){
            //no faces have been made yet.
            watches.add(WatchFragment.fromStock(WatchDraw.FACE.TWO_TIME));
        }


        for (slurpp.faces_lib.v2.models.FaceSettings face : slurpp.faces_lib.v2.models.FaceSettings.stock()) {
            watches.add(WatchFragment.fromStock(face.getBase()));
        }
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        //todo need to change this to factor in the familiar faces.
        if(position > watches.size()-1 || position < 0)
            return null;
        return watches.get(position);
    }

    @Override
    public int getCount() {
        return watches.size();
    }


    @Override
    public CharSequence getPageTitle(int position){
        return watches.get(position).getName(mContext);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    public WatchDraw.FACE faceAtPosition(int position){
        return watches.get(position).getFace();
    }


    @Nullable
    public FaceSettings settingsAtPosition(int position){
        return watches.get(position).getSettings();
    }

    public void setAmbientMode(boolean mode){
        if(watches!=null){
            for(WatchFragment wf: watches){
                wf.ambientMode(mode);
            }
        }
    }


    public void deleteWatchAtPosition(int position){
        if(position >= 0 && position < familiarFaces.size()){
            final String id = familiarFaces.get(position).getId();
            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    FaceSettings.get(realm,id).deleteFromRealm();
                }
            });
        }
    }

    public boolean isFavourite(int position) {
        return position >= 0 && position < familiarFaces.size() && familiarFaces.get(position).isFavourite();
    }

    public void toggleFavourite(int position){
        if(position >= 0 && position < familiarFaces.size()){
            final String id = familiarFaces.get(position).getId();
            Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    FaceSettings faceSettings = FaceSettings.get(realm,id);
                    if(faceSettings!=null)
                        faceSettings.setFavourite(!faceSettings.isFavourite());
                }
            });
        }
    }

}
