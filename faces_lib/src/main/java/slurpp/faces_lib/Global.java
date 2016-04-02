package slurpp.faces_lib;

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Kyle Montague on 18/06/15.
 */
public class Global {


    public enum Messages{
        IS_FACES_ACTIVE,
        SET_WATCH_FACE,
        GET_WATCH,
        OPEN_FACE_SELECTION,
        LAUNCH_CUSTOMISE_ACTIVITY,
        LAUNCH_WEAR_PREVIEW,
        VIBRATE_WATCH
    }

    public enum DataMap{
        WEAR_IS_ACTIVE,
        WEAR_SHAPE,
        TIMESTAMP, ASSET_FACE
    }

    public enum Preferences{
        CURRENT_FACE,
        PREVIEW_FACE,
        BACKGROUND,
        ACCENT,
        HOUR,
        MINUTE,
        SECOND
    }


    public static String DATA_MAP_WEAR_DEVICE = "/DATA_MAP_WEAR_DEVICE";
    public static String DATA_MAP_PREVIEW_FACE = "/DATA_MAP_PREVIEW_FACE";
    public static String DATA_MAP_CURRENT_FACE = "/DATA_MAP_CURRENT_FACE";



    public static String FACE_FACES_FACE = "FACE_FACES_FACE";
    public static String FACE_OUA_TIME = "FACE_OUA_TIME";
    public static String FACE_FILL_TIME = "FACE_FILL_TIME";
    public static String FACE_BUSINESS_TIME = "FACE_BUSINESS_TIME";
    public static final String[] ALL_FACES = new String[]{FACE_BUSINESS_TIME,FACE_FILL_TIME,FACE_OUA_TIME};



    public static String PREF_WATCH_FACE = "PREF_WATCH_FACE";


    public static Uri textFile(Context context, String filename, Object content){
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/"+context.getString(R.string.app_name)+"/");
        if(!tempDir.exists())
            tempDir.mkdirs();
        try {
            File tmpFile = new File(tempDir+"/"+filename+".txt");
            FileOutputStream fos = new FileOutputStream(tmpFile.getPath());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(content);
            oos.close();
            return Uri.fromFile(tmpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Object objectFromFile(Uri filepath) {
        ObjectInputStream in = null;
        FileInputStream fis = null;
        Object tmp = null;
        try {
            fis = new FileInputStream(filepath.getPath());
            in = new ObjectInputStream(fis);
            tmp = in.readObject();
            in.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }


    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public static String FULL_DATE = "EEE, dd MMM yyyy";
    public static String SHORT_DATE = "dd.MM.yy";

    public static String dateString(long timestamp, String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(timestamp));
    }
}
