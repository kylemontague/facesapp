package slurpp.faces.objects;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import slurpp.faces.R;

/**
 * Created by Kyle Montague on 19/06/15.
 */
public class SwatchView extends View {


    private static final int DEFAULT_VIEW_SIZE = 96;
    Paint mFillPaint;
    Paint mStrokePaint = null;
    RectF mInnerBounds;

    public SwatchView(Context context) {
        super(context);
        init(null, 0);



    }

    public SwatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SwatchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SwatchView, defStyle, 0);
        mInnerBounds = new RectF();
        setFillColor(R.color.green);
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

    }

    public void setFillColor(int color){
        mFillPaint = new Paint();
        mFillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(color);
        mFillPaint.setShadowLayer(10,3,3,R.color.black);
    }

    public void setFillResource(int resource){
        setFillColor(this.getResources().getColor(resource));
    }

    public void setStrokeColor(int color){
        if(color == -2)
            mStrokePaint = null;
        else {
            mStrokePaint = new Paint();
            mStrokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setStrokeWidth(5.0f);
            mStrokePaint.setStrokeCap(Paint.Cap.ROUND);
            mStrokePaint.setColor(color);
        }
    }

    public void setStrokeResource(int resource){
        setStrokeColor(this.getResources().getColor(resource));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = resolveSize(DEFAULT_VIEW_SIZE, widthMeasureSpec);
        int height = resolveSize(DEFAULT_VIEW_SIZE, heightMeasureSpec);
        int min = Math.min(width,height);
        mInnerBounds = new RectF(0,0,min,min);
        setMeasuredDimension(width, height);
    }


    @Override
    public void onDraw(Canvas canvas){
        canvas.drawCircle(mInnerBounds.centerX(),mInnerBounds.centerY(),(mInnerBounds.width()/2)-12,mFillPaint);
        if(mStrokePaint!=null) {
            canvas.drawCircle(mInnerBounds.centerX(), mInnerBounds.centerY(), (mInnerBounds.width() / 2)-2, mStrokePaint);
        }
    }






}

