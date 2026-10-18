package slurpp.faces.utils;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import slurpp.faces.R;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.templates.BusinessIFaceConfig;
import slurpp.faces_lib.templates.IFace;
import slurpp.faces_lib.templates.FillFaceConfig;
import slurpp.faces_lib.templates.OUATFace;
import slurpp.faces_lib.PreviewCanvas;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.templates.WordsmithFace;
import slurpp.faces.PreviewActivity;

/**
 * Created by Kyle Montague on 18/06/15.
 */
public class WatchFragment extends Fragment {

    private static final WatchDraw.FACE[] faces = WatchDraw.FACE.values();
    private WatchDraw.FACE mFace;
    private RelativeLayout watchPreview;
    private ImageView watchStraps;
    PreviewCanvas previewCanvas;
    CustomFace customFace;

    public WatchFragment(){
        mFace = faces[0];
    }

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        int wID = args.getInt(PreviewActivity.ARG_WATCHFACE,0);
        mFace = WatchDraw.FACE.fromOrdinal(wID);

        String json = args.getString(PreviewActivity.ARG_WATCH_SETTINGS,null);
        if(json != null && !json.equals("")){
            customFace = CustomFace.fromJson(json);
            mFace = customFace.getBase();
        }
    }



    public void update(CustomFace customFace){
        this.customFace = customFace;
        watchPreview.removeAllViews();

        mFace = customFace.getBase();
        Context c= getActivity().getApplicationContext();
        IFace.Shape s = IFace.Shape.ROUND;
        IFace iFace = getConfig(c,mFace);
        iFace.setColours(customFace.getColors());

        previewCanvas = new PreviewCanvas(c,mFace,iFace, s);
        watchPreview.addView(previewCanvas);
        watchPreview.invalidate();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview, container, false);
        watchPreview = (RelativeLayout) rootView.findViewById(R.id.watch);
        Context c= getActivity().getApplicationContext();
        IFace.Shape s = IFace.Shape.ROUND;
        IFace iFace = getConfig(getActivity().getApplicationContext(),mFace);
        if(customFace!=null){
            iFace.setColours(customFace.getColors());
        }
        previewCanvas = new PreviewCanvas(c,mFace,iFace, s);
        watchPreview.addView(previewCanvas);
        return rootView;
    }

    private IFace getConfig(Context c, WatchDraw.FACE id){
        switch(id) {
            case TWO_TIME:
                return new WordsmithFace(c);
            case OUA_TIME:
                return new OUATFace(c);
            case FILL_TIME:
                return new FillFaceConfig(c);
            case BUSINESS_TIME:
                return new BusinessIFaceConfig(c);
//            case FACES_FACE:
//                return new FacesFaceConfig(c);
        }
        return null;
    }

    public String getName(){
        int name = slurpp.faces_lib.R.string.unknown_face_title;
        switch (mFace){
            case OUA_TIME:
                name = slurpp.faces_lib.R.string.wordsmith_face;
                break;
            case FILL_TIME:
                name =  slurpp.faces_lib.R.string.fill_face_title;
                break;
            case BUSINESS_TIME:
                name =  slurpp.faces_lib.R.string.business_face_title;
                break;
//            case FACES_FACE:
//                name =  slurpp.faces_lib.R.string.faces_face_title;
//                break;
        }
        return getResources().getString(name);
    }

    public void redraw() {
        if(shouldPreview)
            watchPreview.invalidate();
    }


    boolean shouldPreview = true;

    public void stopPreview(){
        shouldPreview = false;
    }

    public void startPreview(){
        shouldPreview = true;
    }
}
