package slurpp.faces.controls;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import slurpp.faces_lib.BusinessFaceConfig;
import slurpp.faces_lib.Face;
import slurpp.faces_lib.FillFaceConfig;
import slurpp.faces_lib.OUATFace;
import slurpp.faces_lib.PreviewCanvas;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.WordsmithFace;
import slurpp.faces.R;


/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchFragment extends Fragment {

    private static final WatchDraw.FACE[] faces = WatchDraw.FACE.values();
    private WatchDraw.FACE mFace;
    private RelativeLayout watchPreview;
    private ImageView watchStraps;

    private boolean ambientMode = false;
    public WatchFragment(){
        mFace = faces[0];
    }

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        int id = args.getInt("ARG.WATCH_FACE",0);
        mFace = faces[id];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview_slide, container, false);
        watchPreview = (RelativeLayout) rootView.findViewById(R.id.watch);
        Context c= getActivity().getApplicationContext();
        //TODO CHANGE TO THE CURRENT FACE SHAPE
        Face.Shape s = Face.Shape.SQUARE;
        PreviewCanvas previewCanvas = new PreviewCanvas(c,mFace,getConfig(c,mFace), s, true);
        previewCanvas.setAmbientMode(ambientMode);
        watchPreview.addView(previewCanvas);

        watchStraps = (ImageView) rootView.findViewById(R.id.watch_background);
        //TODO CHANGE TO THE NEW STRAPS
        watchStraps.setImageResource(s== Face.Shape.ROUND?R.drawable.watch_strap:R.drawable.watch_strap_square);
        return rootView;
    }

    private Face getConfig(Context c, WatchDraw.FACE id){
        switch(id) {
            case OUA_TIME:
                return new OUATFace(c);
            case TWO_TIME:
                return new WordsmithFace(c);
            case FILL_TIME:
                return new FillFaceConfig(c);
            case BUSINESS_TIME:
                return new BusinessFaceConfig(c);
        }
        return null;
    }

    public String getName(Context c){
        int name = R.string.unknown_face_title;
        switch (mFace){
            case OUA_TIME:
                name = R.string.wordsmith_face;
                break;
            case FILL_TIME:
                name =  R.string.fill_face_title;
                break;
            case BUSINESS_TIME:
                name =  R.string.business_face_title;
                break;
            case TWO_TIME:
                name =  R.string.two_times_face_title;
                break;
        }
        return c.getResources().getString(name);
    }

    public WatchDraw.FACE getFace(){
        return mFace;
    }


    public void ambientMode(boolean mode){
        ambientMode = mode;
        if(watchPreview!=null) {
            PreviewCanvas pc = (PreviewCanvas)watchPreview.getChildAt(0);
            if(pc !=null) {
                pc.setAmbientMode(ambientMode);
                pc.invalidate();
            }
        }
    }

}
