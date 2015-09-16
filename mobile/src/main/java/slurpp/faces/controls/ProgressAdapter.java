package slurpp.faces.controls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import slurpp.faces.R;
import slurpp.faces.objects.ProgressItem;
import slurpp.faces.objects.SwatchView;

/**
 * Created by kylemontague on 15/12/14.
 */
public class ProgressAdapter extends BaseAdapter {

    private ArrayList<ProgressItem> mPreferences;
    private Context mContext;
    private int currentIndex=0;

    public ProgressAdapter(Context context){
        mContext = context;
        mPreferences = new ArrayList<ProgressItem>();
        mPreferences.add(new ProgressItem(mContext,R.string.info_background_colour,true));
        mPreferences.add(new ProgressItem(mContext,R.string.info_text_colour,false));
        mPreferences.add(new ProgressItem(mContext,R.string.info_line_colour,false));
    }


    public ProgressAdapter(Context context, ArrayList<ProgressItem> values){
        mContext = context;
        setSwatches(values);
    }



    public void setSwatches(ArrayList<ProgressItem> values){
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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridItem = (convertView == null)?inflater.inflate(R.layout.swatchgridview, null):convertView;
        SwatchView swatch = (SwatchView) gridItem.findViewById(R.id.grid_swatch);
        ProgressItem progressItem = mPreferences.get(position);
        if(position == currentIndex){
            swatch.setStrokeResource(R.color.green);
            swatch.setFillColor(progressItem.color());
        }else{
            swatch.setFillResource(R.color.grey);
            if(progressItem.color()!=-2)
                swatch.setFillColor(progressItem.color());
            swatch.setStrokeColor(-2);
        }
        return gridItem;
    }

    public void next() {
        if (currentIndex < mPreferences.size() - 1) {
            currentIndex++;
        }
    }

    public int currentPreference(){
        return mPreferences.get(currentIndex).text();
    }

    public int getCurrentIndex(){
        return currentIndex;
    }

}
