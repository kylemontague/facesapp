package slurpp.faces_lib;

import android.graphics.Canvas;
import android.graphics.Rect;

import slurpp.faces_lib.templates.FaceConfig;

/**
 * Created by Kyle Montague on 21/05/2017.
 */

public interface IFaceRenderer {

    void setPeekCard(Rect peekCard);
    void setIsPreview(boolean isPreview);
    void setChinInset(int chinInset);

    Rect getPeekCard();
    boolean isPreview();
    int getChinInset();

    float xOffset();
    float yOffset();

    void onDraw(Canvas canvas, Rect bounds, FaceConfig faceConfig, FaceConfig.State state, boolean showDate);

}