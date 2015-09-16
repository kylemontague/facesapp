package slurpp.faces.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kyle Montague on 11/09/15.
 */
public class PathView extends View {


    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final SvgHelper mSvg = new SvgHelper(mPaint);
    private int mSvgResource;

    private final Object mSvgLock = new Object();
    private List<SvgHelper.SvgPath> mPaths = new ArrayList<SvgHelper.SvgPath>(0);
    private Thread mLoader;

    private float mPhase;
    private float mFadeFactor;
    private int mDuration;
    private float mParallax = 1.0f;
    private float mOffsetY;

    private ObjectAnimator mSvgAnimator;



    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public PathView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);

        mPaint.setStyle(Paint.Style.STROKE);


        mPaint.setStrokeWidth(2.0f);
        mPaint.setColor(0xffffffff);
        mPhase =  1.0f;
        mDuration = 2500;
        mFadeFactor = 100.0f;

    }

    private void updatePathsPhaseLocked() {
        final int count = mPaths.size();
        for (int i = 0; i < count; i++) {
            SvgHelper.SvgPath svgPath = mPaths.get(i);
            svgPath.renderPath.reset();
            svgPath.measure.getSegment(0.0f, svgPath.length * mPhase, svgPath.renderPath, true);
            // Required only for Android 4.4 and earlier
            svgPath.renderPath.rLineTo(0.0f, 0.0f);
        }
    }

    public float getParallax() {
        return mParallax;
    }

    public void setParallax(float parallax) {
        mParallax = parallax;
        invalidate();
    }

    public float getPhase() {
        return mPhase;
    }

    public void setPhase(float phase) {
        mPhase = phase;
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }


    public int getSvgResource() {
        return mSvgResource;
    }

    public void setSvgResource(int svgResource) {
        mSvgResource = svgResource;

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e("SVG", "Unexpected error", e);
            }
        }

        mLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                mSvg.load(getContext(), mSvgResource);
                synchronized (mSvgLock) {
                    mPaths = mSvg.getPathsForViewport(
                            300 - getPaddingLeft() - getPaddingRight(),
                            300 - getPaddingTop() - getPaddingBottom());
                    updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        mLoader.start();
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e("SVG", "Unexpected error", e);
            }
        }

        mLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                mSvg.load(getContext(), mSvgResource);
                synchronized (mSvgLock) {
                    mPaths = mSvg.getPathsForViewport(
                            w - getPaddingLeft() - getPaddingRight(),
                            h - getPaddingTop() - getPaddingBottom());
                    updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        mLoader.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mSvgLock) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop() + mOffsetY);
            final int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                SvgHelper.SvgPath svgPath = mPaths.get(i);

                // We use the fade factor to speed up the alpha animation
                int alpha = (int) (Math.min(mPhase * mFadeFactor, 1.0f) * 255.0f);
                svgPath.paint.setAlpha((int)(alpha * mParallax));

                canvas.drawPath(svgPath.renderPath, svgPath.paint);
            }
            canvas.restore();
        }
    }

    public void reveal(View parent, int parentBottom) {
        if (mSvgAnimator == null) {
            mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", 0.0f, 1.0f);
            mSvgAnimator.setDuration(mDuration);
            mSvgAnimator.start();
        }

        float previousOffset = mOffsetY;
        mOffsetY = Math.min(0, parent.getHeight() - (parentBottom - parent.getScrollY()));
        if (previousOffset != mOffsetY) invalidate();
    }
}
