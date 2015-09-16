package slurpp.faces.controls;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import montague.faces_lib.BusinessFaceConfig;
import montague.faces_lib.Face;
import montague.faces_lib.FacesFaceConfig;
import montague.faces_lib.FillFaceConfig;
import montague.faces_lib.OUATFace;
import montague.faces_lib.PreviewCanvas;
import montague.faces_lib.WatchDraw;
import montague.faces_lib.WordsmithFace;
import slurpp.faces.R;


/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchFragment extends Fragment {

    private static final WatchDraw.FACE[] faces = {WatchDraw.FACE.FACES_FACE,WatchDraw.FACE.OUA_TIME, WatchDraw.FACE.FILL_TIME, WatchDraw.FACE.BUSINESS_TIME, WatchDraw.FACE.TWO_TIME};
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
        //todo load ID from dataset
        mFace = faces[id];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview_slide, container, false);
        watchPreview = (RelativeLayout) rootView.findViewById(R.id.watch);
        Context c= getActivity().getApplicationContext();
        Face.Shape s = Face.Shape.ROUND;
        PreviewCanvas previewCanvas = new PreviewCanvas(c,mFace,getConfig(c,mFace), s);
        previewCanvas.setAmbientMode(ambientMode);
        watchPreview.addView(previewCanvas);

        watchStraps = (ImageView) rootView.findViewById(R.id.watch_background);
        //todo need to change to square strap.
        watchStraps.setImageResource(s== Face.Shape.ROUND?R.drawable.watch_strap:R.drawable.watch_strap);
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
            case FACES_FACE:
                return new FacesFaceConfig(c);
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
            case FACES_FACE:
                name =  R.string.faces_face_title;
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
