package slurpp.faces.controls;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import slurpp.faces_lib.WatchDraw;

/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchPageAdapter extends FragmentPagerAdapter {


    ArrayList<WatchFragment> watches;
    Context mContext;
    public WatchPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
        //todo load the dataset here and build an array of WatchFragment
        watches = new ArrayList<WatchFragment>();
        //TODO THIS IS THE NUMBER OF FACES THAT WILL BE SHOWN
        WatchDraw.FACE[] array = WatchDraw.FACE.values();
        for(int x=0;x<array.length;x++) {
            WatchFragment wf = new WatchFragment();
            Bundle b = new Bundle();
            b.putInt("ARG.WATCH_FACE", array[x].ordinal());
            b.putBoolean("ARG.WATCH_AMBIENT_MODE",false);
            wf.setArguments(b);
            watches.add(wf);
        }
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

    public void setAmbientMode(boolean mode){
        if(watches!=null){
            for(WatchFragment wf: watches){
                wf.ambientMode(mode);
            }
        }
    }

}
