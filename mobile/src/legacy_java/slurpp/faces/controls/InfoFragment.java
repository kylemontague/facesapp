package slurpp.faces.controls;


import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import slurpp.faces.R;
import slurpp.faces_lib.utils.Font;

/**
 * Created by kylemontague on 20/06/15.
 */
public class InfoFragment extends Fragment {

    TextView mText;
    ImageView mImageView;
    int mType;

    int textResource = R.string.info_background_colour;
    int imageResource = android.R.drawable.ic_menu_camera;



    public static final String ARG_TEXT = "ARG.TEXT";
    public static final String ARG_IMAGE = "ARG.IMAGE";
    public static final String ARG_TYPE = "ARG.TYPE";


    public enum type{
        BOTTOM,
        TOP,
        FULLSCREEN
    };

    public static Bundle argumentBuilder(int textRes,int imageRes, type overlayType){
        Bundle b = new Bundle();
        b.putInt(ARG_TEXT,textRes);
        b.putInt(ARG_IMAGE,imageRes);
        b.putInt(ARG_TYPE, overlayType.ordinal());
        return b;
    }


    public InfoFragment(){
        super();
    }

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        textResource = args.getInt(ARG_TEXT,R.string.info_background_colour);
        imageResource = args.getInt(ARG_IMAGE,android.R.drawable.ic_menu_camera);
        mType = args.getInt(ARG_TYPE, 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int layout = getLayoutType(mType);
        ViewGroup rootView = (ViewGroup) inflater.inflate(layout, container, false);
        mText = (TextView)rootView.findViewById(R.id.info_text);
        mText.setText(textResource);
        mText.setTypeface(Font.get(getActivity().getApplicationContext(), Font.QUICKSAND_BOLD), Typeface.BOLD);
        mImageView = (ImageView)rootView.findViewById(R.id.info_image);
        if(imageResource == -1){
            mImageView.setVisibility(View.INVISIBLE);
        }else {
            mImageView.setImageResource(imageResource);
            mImageView.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    protected int getLayoutType(int type){
        switch (type){
            case 0: // TYPE_BOTTOM
                return R.layout.bottom_info_overlay;
            case 1: // TYPE_TOP
                return R.layout.top_info_overlay;
            case 2: // TYPE_FULLSCREEN
                return R.layout.full_info_overlay;
        }
        return R.layout.bottom_info_overlay;
    }

    public void setText(int text){
        textResource = text;
        mText.setText(textResource);
        mText.invalidate();
    }

    public void setTextString(String text){
        textResource = -1;
        mText.setText(text);
        mText.invalidate();
    }



    public void setImage(int image){
        imageResource = image;
        if(imageResource == -1){
            mImageView.setVisibility(View.INVISIBLE);
        }else {
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageResource(imageResource);
            mImageView.invalidate();
        }

    }

    public void setImage(Uri imageUri){
        mImageView.setImageURI(imageUri);
        mImageView.invalidate();
    }


}
