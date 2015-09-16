package slurpp.faces.objects;

import android.content.Context;

import slurpp.faces.R;

/**
 * Created by kylemontague on 22/01/15.
 */
public class ProgressItem {

    private SwatchView swatch;
    private int mText;
    private int mRGB;
    private boolean mState = false;

    public ProgressItem(Context context, int text, boolean state){
        mText = text;
        swatch=new SwatchView(context);
        mRGB = context.getResources().getColor(R.color.grey);
        mState = state;
    }

    protected int getText(){
        return mText;
    }

    public void addColor(int rgb){
        mRGB = rgb;
        mState = true;
    }

    public void removeColor(){
        mState = false;
        mRGB = -2;

    }

    public int color(){
        return mRGB;
    }

    public int text(){
        return mText;
    }

    public boolean state(){
        return mState;
    }


}
