package slurpp.faces.controls;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import slurpp.faces.R;
import slurpp.faces.data.ColourContrast;
import slurpp.faces.objects.SwatchView;

import static android.support.v7.graphics.Palette.Swatch;

/**
 * Created by kylemontague on 15/12/14.
 */
public class SwatchAdapter extends BaseAdapter {

    private Context mContext;
    private int MAX_COLOURS = 16;
    private int[] stockColours = new int[0];//{R.color.black,R.color.white};//R.color.green,R.color.red};
    private int selected = -2;

    private ArrayList<Swatch> colours;
    private ArrayList<Swatch> allColours;
    private ArrayList<Integer> markers;

    public SwatchAdapter(Context context, List<Swatch> values){
        mContext = context;
        colours = new ArrayList<>();
        allColours = new ArrayList<>();
        markers = new ArrayList<>();
        setSwatches(values);
    }


    public void setSwatches(List<Swatch> values){
        //colours.addAll(ColourUtil.GenerateTriad(values.get(0).getRgb(), 5));
        //colours.addAll(ColourUtil.GenerateAnalogous(values.get(0).getRgb(), 5));
        int totalColours =0;

        Collections.sort(new ArrayList<>(values),new HSLComparator());
        for(Swatch s:values){
            if(totalColours == (MAX_COLOURS-stockColours.length))
                break;
            colours.add(s);
            totalColours++;
        }

        allColours.addAll(colours);

    }

    @Override
    public int getCount() {
        return (colours!=null)?colours.size():0;
    }

    @Override
    public Object getItem(int position) {
        return colours.get(position);
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
        swatch.setOnDragListener(new SwatchDragListener());
        swatch.setFillColor(colours.get(position).getRgb());
        if(position == selected || markers.contains(Integer.valueOf(position)))
            swatch.setStrokeResource(R.color.white);
        return gridItem;
    }

    public int getColour(int position){
        return colours.get(position).getRgb();
    }

    public void subsetColours(Swatch swatch){
        ArrayList<Swatch> subset = new ArrayList<>();
        for(Swatch c:colours){
            if(ColourContrast.isValid(swatch,c)) {
                subset.add(c);
            }
        }
        colours = subset;
    }

    public void setSelected(int position){
        if(selected != position) {
            selected = position;
            notifyDataSetChanged();
        }
    }

    public int getSelected(){
        if(selected !=-2)
            return getColour(selected);
        return -2;
    }


    public void addMarker(int position){
        markers.add(Integer.valueOf(position));
    }

    public void removeMarker(int position) {
        if(markers.contains(Integer.valueOf(position)))
            markers.remove(markers.indexOf(Integer.valueOf(position)));
    }

    public boolean containsMarker(int position){
        return markers.contains(Integer.valueOf(position));
    }

    public class PopulationComparator implements Comparator<Swatch>{

        @Override
        public int compare(Swatch o1, Swatch o2) {
            int p = o1.getPopulation()-o2.getPopulation();
            if(p > 0)
                return 1;
            else if (p < 0)
                return -1;
            else
                return 0;
        }
    }

    public class HSLComparator implements Comparator<Swatch>{

        @Override
        public int compare(Swatch o1, Swatch o2) {
            float h = (o1.getHsl()[0]-o2.getHsl()[0]);
            float s = (o1.getHsl()[1]-o2.getHsl()[1]);
            float l = (o1.getHsl()[2]-o2.getHsl()[2]);
            if(h >0)
                return 1;
            if(h < 0)
                return -1;
            if(l > 0)
                return 1;
            if(l < 0)
                return -1;
//            if(s >0)
//                return 1;
//            if(s < 0)
//                return -1;
            return 0;
        }
    }

    class SwatchDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    //v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
//                    View view = (View) event.getLocalState();
//                    ViewGroup owner = (ViewGroup) view.getParent();
//                    owner.removeView(view);
//                    GridView container = (GridView) v;
//                    container.addView(view);
//                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }


}
