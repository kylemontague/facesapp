package slurpp.faces;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import slurpp.faces_lib.v2.models.ModelMigration;

/**
 * Created by Kyle Montague on 30/05/16.
 */
public class FacesApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();


        Realm.init(getApplicationContext());
        RealmConfiguration config = new RealmConfiguration.Builder().schemaVersion(ModelMigration.CurrentVersion).migration(new ModelMigration()).build();
        Realm.setDefaultConfiguration(config);

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
