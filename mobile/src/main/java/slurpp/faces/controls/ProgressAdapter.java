package slurpp.faces.controls;

import android.content.Context;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import slurpp.faces.R;
import slurpp.faces.objects.SwatchView;

/**
 * Created by kylemontague on 15/12/14.
 */
public class ProgressAdapter extends BaseAdapter {

    private ArrayList<Palette.Swatch> mPreferences;
    private Context mContext;
    private int currentIndex=0;

    public ProgressAdapter(Context context){
        mContext = context;
        mPreferences = new ArrayList<>();
    }


    public ProgressAdapter(Context context, ArrayList<Palette.Swatch> values){
        mContext = context;
        setSwatches(values);
    }




    public void setSwatches(ArrayList<Palette.Swatch> values){
        mPreferences = values;
    }


    @Override
    public int getCount() {
        return (mPreferences!=null)?mPreferences.size():0;
    }

    @Override
    public Object getItem(int position) {
        return mPreferences.get(position);
    }


    public void setSwatch(int position, Palette.Swatch object){
        mPreferences.set(position,object);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridItem = (convertView == null)?inflater.inflate(R.layout.swatchgridview, null):convertView;
        SwatchView swatch = (SwatchView) gridItem.findViewById(R.id.grid_swatch);
        Palette.Swatch progressItem = mPreferences.get(position);
        swatch.setStrokeResource(progressItem.getBodyTextColor());//todo change this back to white
        swatch.setFillColor(progressItem.getRgb());
        return gridItem;
    }

    public void next() {
        if (currentIndex < mPreferences.size() - 1) {
            currentIndex++;
        }
    }




    public int getCurrentIndex(){
        return currentIndex;
    }

}
