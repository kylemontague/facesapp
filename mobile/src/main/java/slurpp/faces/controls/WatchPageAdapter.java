package slurpp.faces.controls;

import android.content.Context;
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


    ArrayList<WatchFragment> watches;
    Context mContext;
    RealmResults<FaceSettings> familiarFaces;
    OrderedRealmCollectionChangeListener familiarChangeListener = new OrderedRealmCollectionChangeListener<RealmResults<FaceSettings>>() {
        @Override
        public void onChange(RealmResults<FaceSettings> faceSettings, OrderedCollectionChangeSet changeSet) {
            faceSettings.load();
            updateWatches();
        }
    };

    public int getCurrentFaceIndex() {
        return currentFaceIndex;
    }



    int currentFaceIndex = 0;

    public WatchPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        familiarFaces = FaceSettings.all(Realm.getDefaultInstance(), Sort.ASCENDING);
        familiarFaces.addChangeListener(familiarChangeListener);
        updateWatches();
    }


    private void updateWatches(){
        currentFaceIndex = 0;
        watches = new ArrayList<>();
        for(FaceSettings face:familiarFaces){
            Log.d("REALM",face.getId());
            watches.add(WatchFragment.fromFaceSettings(face));
        }

        if(watches.size() > 0){
            currentFaceIndex = watches.size()-1;
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

}
