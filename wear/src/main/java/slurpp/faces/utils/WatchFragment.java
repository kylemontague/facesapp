package slurpp.faces.utils;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

//todo update this to slurpp
import mclemont.faces.R;
import montague.faces_lib.BusinessFaceConfig;
import montague.faces_lib.Face;
import montague.faces_lib.FacesFaceConfig;
import montague.faces_lib.FillFaceConfig;
import montague.faces_lib.OUATFace;
import montague.faces_lib.PreviewCanvas;
import montague.faces_lib.WatchDraw;
import montague.faces_lib.WordsmithFace;
import slurpp.faces.PreviewActivity;

/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchFragment extends Fragment {

    private static final WatchDraw.FACE[] faces = {WatchDraw.FACE.FACES_FACE,WatchDraw.FACE.OUA_TIME, WatchDraw.FACE.FILL_TIME, WatchDraw.FACE.BUSINESS_TIME};
    private WatchDraw.FACE mFace;
    private RelativeLayout watchPreview;
    private ImageView watchStraps;

    public WatchFragment(){
        mFace = faces[0];
    }

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        int wID = args.getInt(PreviewActivity.ARG_WATCHFACE,0);
        mFace = WatchDraw.FACE.fromOrdinal(wID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview, container, false);
        watchPreview = (RelativeLayout) rootView.findViewById(R.id.watch);
        Context c= getActivity().getApplicationContext();
        Face.Shape s = Face.Shape.ROUND;
        PreviewCanvas previewCanvas = new PreviewCanvas(c,mFace,getConfig(c,mFace), s);
        watchPreview.addView(previewCanvas);


        return rootView;
    }

    private Face getConfig(Context c, WatchDraw.FACE id){
        switch(id) {
            case TWO_TIME:
                return new WordsmithFace(c, 1);
            case OUA_TIME:
                return new OUATFace(c, 1);
            case FILL_TIME:
                return new FillFaceConfig(c, 1);
            case BUSINESS_TIME:
                return new BusinessFaceConfig(c, 1);
            case FACES_FACE:
                return new FacesFaceConfig(c,1);
        }
        return null;
    }

    public String getName(){
        int name = montague.faces_lib.R.string.unknown_face_title;
        switch (mFace){
            case OUA_TIME:
                name = montague.faces_lib.R.string.wordsmith_face;
                break;
            case FILL_TIME:
                name =  montague.faces_lib.R.string.fill_face_title;
                break;
            case BUSINESS_TIME:
                name =  montague.faces_lib.R.string.business_face_title;
                break;
            case FACES_FACE:
                name =  montague.faces_lib.R.string.faces_face_title;
                break;
        }
        return getResources().getString(name);
    }

}
