package slurpp.faces.controls;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import slurpp.faces.utils.ColourConverter;

import static android.support.v7.graphics.Palette.Swatch;

/**
 * Created by kylemontague on 15/12/14.
 */
public class SwatchAdapter extends BaseAdapter {

    private Context mContext;
    private int MAX_COLOURS = 16;
    private int[] stockColours = new int[0];//{R.color.black,R.color.white};//R.color.green,R.color.red};

    private ArrayList<Swatch> colours;
    private ArrayList<Swatch> allColours;
    private ArrayList<Integer> markers;

    private int currentMarker =-1;
    private SwatchView markerView=null;

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

        Collections.sort(new ArrayList<>(values),new ChromaComparator());
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
        swatch.setOnTouchListener(new MarkerTouchListener());
        swatch.setFillColor(colours.get(position).getRgb());
        swatch.setStrokeColor(-2);
        if(markers.contains(Integer.valueOf(position)))
            swatch.setStrokeResource(R.color.white);
        swatch.setTag(R.string.Swatch_ID_ViewTag,position);
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


    public void clearMarkers(){
        markers = new ArrayList<>();
    }


    public void addMarker(int position){
        markers.add(Integer.valueOf(position));

        for(Integer m:markers)
            Log.d("Markers","Swatch:"+m.toString());
    }

    public void removeMarker(int position) {
        if(markers.contains(Integer.valueOf(position)))
            markers.remove(markers.indexOf(Integer.valueOf(position)));
    }

    public boolean containsMarker(int position){
        return markers.contains(Integer.valueOf(position));
    }

    public ArrayList<Integer> getMarkers(){
        return markers;
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

    public class ChromaComparator implements Comparator<Swatch>{

        @Override
        public int compare(Swatch s1, Swatch s2) {
            double c1 = chroma(s1);
            double c2 = chroma(s2);

            if(c1 > c2)
                return 1;
            else if(c1 < c2)
                return -1;
            return 0;
        }


        private double chroma(Swatch swatch){
            double[] lab = ColourConverter.rgb2lab(swatch.getRgb());
            return Math.sqrt(Math.pow(lab[1],2)+ Math.pow(lab[2],2));
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
                    View view = (View) event.getLocalState();

                    if(v.getTag(R.string.Swatch_ID_ViewTag)!=null) {
                        int index = (int) v.getTag(R.string.Swatch_ID_ViewTag);
                        Log.d("SWATCH", "DROPPER ON SWATCH:" + index);
                        removeMarker(currentMarker);
                        addMarker(index);
                        notifyDataSetChanged();
                        view.invalidate();
                        v.invalidate();
                        currentMarker = -1;
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    currentMarker = -1;
                default:
                    break;
            }
            return true;
        }
    }



    private final class MarkerTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(view.getTag(R.string.Swatch_ID_ViewTag)!=null) {
                int index = (int) view.getTag(R.string.Swatch_ID_ViewTag);
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && containsMarker(index) && currentMarker == -1) {
                    Log.d("SWATCH", "MARKER SELECTED:" +index);
                    currentMarker = index;
                    ClipData data = ClipData.newPlainText("", "");
                    markerView = (SwatchView)view;
                    markerView.setFillResource(R.color.transparent);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(markerView);
                    view.startDrag(data, shadowBuilder, view, 0);
                    return true;
                }
            }
            return false;
        }
    }
}
