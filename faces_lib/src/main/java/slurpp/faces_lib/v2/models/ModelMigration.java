package slurpp.faces_lib.v2.models;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;

/**
 * Created by Kyle Montague on 19/08/2017.
 */

public class ModelMigration implements RealmMigration {

    private static final String TAG = "ModelMigration";
    public static long CurrentVersion = 1;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        if(oldVersion < CurrentVersion){
            //migrate to the latest version
            Log.d(TAG,"migrating to the latest");
        }
    }
}
