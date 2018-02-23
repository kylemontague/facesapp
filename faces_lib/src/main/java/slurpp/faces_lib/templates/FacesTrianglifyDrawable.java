package slurpp.faces_lib.templates;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.manolovn.trianglify.domain.Point;
import com.manolovn.trianglify.domain.Triangle;
import com.manolovn.trianglify.generator.color.ColorGenerator;
import com.manolovn.trianglify.generator.point.PointGenerator;
import com.manolovn.trianglify.renderer.TriangleRenderer;
import com.manolovn.trianglify.triangulator.DelaunayTriangulator;
import com.manolovn.trianglify.triangulator.Triangulator;

import java.util.Vector;

/**
 * Created by Kyle Montague on 21/08/2017.
 */

public class FacesTrianglifyDrawable extends Drawable {


    private PointGenerator pointGenerator;
    private Triangulator triangulator;
    private TriangleRenderer triangleRenderer;

    private int width = 200;
    private int height = 200;
    private int bleedX;
    private int bleedY;
    private int cellSize;
    private int variance;

    private Vector<Point> points;
    private Vector<Triangle> triangles;

    private final Object lock = new Object();
    // Used with triangulateInBackground
    Boolean ready = false;


    public FacesTrianglifyDrawable(
            int bleedX,
            int bleedY,
            int cellSize,
            int variance,
            ColorGenerator colorGenerator,
            PointGenerator pointGenerator, Rect bounds) {
        super();

        this.bleedX = bleedX;
        this.bleedY = bleedY;
        this.cellSize = cellSize;
        this.variance = variance;
        this.pointGenerator = pointGenerator;
        this.width = bounds.width();
        this.height = bounds.height();

        triangulator = new DelaunayTriangulator();
        triangleRenderer = new TriangleRenderer(colorGenerator);
    }


    public FacesTrianglifyDrawable(
            int bleedX,
            int bleedY,
            int cellSize,
            int variance,
            ColorGenerator colorGenerator,
            PointGenerator pointGenerator) {
        super();

        this.bleedX = bleedX;
        this.bleedY = bleedY;
        this.cellSize = cellSize;
        this.variance = variance;
        this.pointGenerator = pointGenerator;

        triangulator = new DelaunayTriangulator();
        triangleRenderer = new TriangleRenderer(colorGenerator);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.width = bounds.width();
        this.height = bounds.height();
    }

    @Override
    public void draw(Canvas canvas) {
        if (ready) {
            triangleRenderer.render(triangles, canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    public void setVariance(int variance) {
        this.variance = variance;
        triangulateInBackground();
    }

    public void setCellSize(int cellSize) {
        this.cellSize = cellSize;
        triangulateInBackground();
    }

    public void setColorGenerator(ColorGenerator colorGenerator) {
        triangleRenderer = new TriangleRenderer(colorGenerator);
        triangulateInBackground();
    }

    public void setPointGenerator(PointGenerator pointGenerator) {
        this.pointGenerator = pointGenerator;
        triangulateInBackground();
    }

    public void triangulateInBackground() {
        FacesTrianglifyDrawable.TriangulateAsyncTask task = new FacesTrianglifyDrawable.TriangulateAsyncTask();
        task.execute();
    }


    public void triangulate(){
        synchronized (lock) {
            ready = false;
            pointGenerator.setBleedX(bleedX);
            pointGenerator.setBleedY(bleedY);
            points = pointGenerator.generatePoints(width, height, cellSize, variance);
            triangles = triangulator.triangulate(points);
            ready = true;
        }
    }

    private class TriangulateAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (lock) {
                ready = false;
                pointGenerator.setBleedX(bleedX);
                pointGenerator.setBleedY(bleedY);
                points = pointGenerator.generatePoints(width, height, cellSize, variance);
                triangles = triangulator.triangulate(points);
                ready = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            invalidateSelf();
        }
    }



}
