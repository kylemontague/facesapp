package slurpp.faces.controls;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import io.realm.Realm;
import slurpp.faces.R;
import slurpp.faces_lib.PreviewCanvas;
import slurpp.faces_lib.WatchDraw;
import slurpp.faces_lib.templates.BusinessIFaceConfig;
import slurpp.faces_lib.templates.FillFaceConfig;
import slurpp.faces_lib.templates.IFace;
import slurpp.faces_lib.templates.OUATFace;
import slurpp.faces_lib.templates.WordsmithFace;
import slurpp.faces_lib.v2.models.FaceSettings;


/**
 * Created by kylemontague on 18/06/15.
 */
public class WatchFragment extends Fragment {


    public static final String ARG_WATCH_FACE = "ARG.WATCH_FACE";
    public static final String ARG_WATCH_AMBIENT_MODE = "ARG.WATCH_AMBIENT_MODE";
    public static final String ARG_WATCH_SETTINGS_ID = "ARG.WATCH_SETTINGS_ID";


    public FaceSettings faceSettings = null;

    public static WatchFragment fromStock(WatchDraw.FACE base){
        WatchFragment watchFragment = new WatchFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_WATCH_FACE, base.ordinal());
        b.putBoolean(ARG_WATCH_AMBIENT_MODE, false);
        watchFragment.setArguments(b);
        return watchFragment;
    }

    public static WatchFragment fromFaceSettings(FaceSettings faceSettings){
        WatchFragment watchFragment = new WatchFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_WATCH_FACE, faceSettings.getBase().ordinal());
        b.putBoolean(ARG_WATCH_AMBIENT_MODE, false);
        b.putString(ARG_WATCH_SETTINGS_ID,faceSettings.getId());
        watchFragment.setArguments(b);
        return watchFragment;
    }

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
        int id = args.getInt(ARG_WATCH_FACE,0);
        mFace = faces[id];

        String settingsID = args.getString(ARG_WATCH_SETTINGS_ID,null);
        if(settingsID!=null){
            faceSettings = FaceSettings.get(Realm.getDefaultInstance(),settingsID);
            mFace = faceSettings.getBase();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_preview_slide, container, false);
        watchPreview = (RelativeLayout) rootView.findViewById(R.id.watch);
        Context c= getActivity().getApplicationContext();

        IFace.Shape s = IFace.Shape.ROUND; //WearSettings.load(getContext()).isRound?IFace.Shape.ROUND:IFace.Shape.SQUARE;
        IFace iFace = getConfig(c,mFace);
        if(faceSettings!=null) {
            iFace.setColours(faceSettings.getColorsArray());
        }

        PreviewCanvas previewCanvas = new PreviewCanvas(c,mFace,iFace, s, true);
        previewCanvas.setAmbientMode(ambientMode);
        watchPreview.addView(previewCanvas);
        watchStraps = (ImageView) rootView.findViewById(R.id.watch_background);

        watchStraps.setImageResource(s== IFace.Shape.ROUND?R.drawable.watch_strap:R.drawable.watch_strap_square);
        return rootView;
    }

    private IFace getConfig(Context c, WatchDraw.FACE id){
        switch(id) {
            case OUA_TIME:
                return new OUATFace(c);
            case TWO_TIME:
                return new WordsmithFace(c);
            case FILL_TIME:
                return new FillFaceConfig(c);
            case BUSINESS_TIME:
                return new BusinessIFaceConfig(c);
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

    @Nullable
    public FaceSettings getSettings(){
        return faceSettings;
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
