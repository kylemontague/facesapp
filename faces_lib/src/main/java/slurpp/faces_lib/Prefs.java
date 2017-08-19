package slurpp.faces_lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;


/**
 * Created by Kyle Montague on 29/10/2014.
 */
public class Prefs {

    private Context mContext;
    private SharedPreferences mPreferences;

    public Prefs(Context context){
        mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }



    public void set(String key, String value){
        mPreferences.edit().putString(key, value).apply();
    }

    public void set(String key, int value){
        mPreferences.edit().putInt(key, value).apply();
    }

    public void set(String key, long value){
        mPreferences.edit().putLong(key, value).apply();
    }

    public String getString(String key){
        return mPreferences.getString(key,"");
    }

    public String getString(String key, String defaultValue){
        return mPreferences.getString(key,defaultValue);
    }

    public long getLong(String key){
        return mPreferences.getLong(key,0);
    }

    public long getLong(String key, long defaultValue){
        return mPreferences.getLong(key,defaultValue);
    }

    public int getInt(String key, int defaultValue){
        return mPreferences.getInt(key,defaultValue);
    }


    public int getColourString(String key, int defaultValue){
        String value = mPreferences.getString(key,"EMPTY");
        if(value.equals("EMPTY"))
            return defaultValue;
        return Color.parseColor(value);
    }

    public static void setCurrentFace(Context context, String name){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(context.getString(R.string.PREF_CURRENT_FACE),name).commit();
    }

    public static String getCurrentFace(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.PREF_CURRENT_FACE),null);
    }








}
