package slurpp.faces_lib;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by kylemontague on 18/12/14.
 */
public class Font {

    public static String PROXI_LIGHT = "ProximaNova-Light.otf";
    public static String PROXI_SEMIBOLD = "ProximaNova-Semibold.otf";
    public static String QUICKSAND_BOLD = "Quicksand-Bold.otf";

    public static Typeface get(Context context, String font){
        return Typeface.createFromAsset(context.getAssets(), "fonts/"+font);
    }
}
