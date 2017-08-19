package slurpp.faces_lib.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.GsonBuilder;

import java.util.HashMap;

import slurpp.faces_lib.utils.Global;

/**
 * Created by kylemontague on 09/04/16.
 */
public class WearSettings extends Object {


    public static String PREF_IS_ROUND = "PREF_IS_ROUND";
    public static String PREF_IS_ACTIVE = "PREF_IS_ACTIVE";

    public boolean isRound;
    public boolean isActive;

    public WearSettings(boolean isActive, boolean isRound){
        this.isActive = isActive;
        this.isRound = isRound;
    }

    public WearSettings() {
        this.isActive=true;
        this.isRound=true;
    }

    public HashMap<String, String> toMap() {
        HashMap<String,String> map = new HashMap<>();
        map.put(Global.WEAR_KEYS.IS_ACTIVE.name(),String.valueOf(isActive));
        map.put(Global.WEAR_KEYS.SHAPE.name(),String.valueOf(isRound));
        return map;
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }

    public static WearSettings fromJson(String json){
        return new GsonBuilder().create().fromJson(json,WearSettings.class);
    }


    public static WearSettings load(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        WearSettings settings = new WearSettings();

        settings.isRound = preferences.getBoolean(PREF_IS_ACTIVE,false);
        settings.isActive = preferences.getBoolean(PREF_IS_ROUND,true);
        return settings;
    }

    public static boolean save(Context context, WearSettings settings){
       return save(context,settings.isRound,settings.isActive);
    }

    public static boolean save(Context context, boolean isRound, boolean isActive){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(PREF_IS_ROUND,isRound).apply();
        preferences.edit().putBoolean(PREF_IS_ACTIVE,isActive).apply();
        return preferences.edit().commit();
    }
}
