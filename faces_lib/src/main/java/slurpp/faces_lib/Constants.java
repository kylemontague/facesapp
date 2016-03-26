package slurpp.faces_lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by kylemontague on 26/03/16.
 */
public class Constants {

    public static final String PREF_BOOLEAN_SHOULD_ON_BOARD = "PREF_SHOULD_ON_BOARD";
    public static final String PREF_INT_CURRENT_WATCH_FACE = "PREF_CURRENT_WATCH_FACE";

    public static final String PREF_BOOLEAN_TAP_SHOW_DATE = "PREF_TAP_SHOW_DATE";


    private static SharedPreferences sharedPreferences;
    public static SharedPreferences getPreferences(Context context){
        if(sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences;
    }

    public static SharedPreferences.Editor getEditor(Context context){
        return Constants.getPreferences(context).edit();
    }

    public static boolean shouldOnBoard(Context context){
        return Constants.getPreferences(context).getBoolean(PREF_BOOLEAN_SHOULD_ON_BOARD,true);
    }

    public static void setOnBoard(Context context, boolean value){
        getEditor(context).putBoolean(PREF_BOOLEAN_SHOULD_ON_BOARD,value).apply();
    }

    public static int currentFace(Context context){
        return Constants.getPreferences(context).getInt(PREF_INT_CURRENT_WATCH_FACE,0);
    }


    public static boolean isActive(Context context){
        return Constants.getPreferences(context).getBoolean(Global.DataMap.WEAR_IS_ACTIVE.name(),false);
    }

    public static boolean isRound(Context context){
        return Constants.getPreferences(context).getBoolean(Global.DataMap.WEAR_SHAPE.name(),true);
    }

}
