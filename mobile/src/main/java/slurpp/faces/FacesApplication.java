package slurpp.faces;

import android.app.Application;
import android.util.Log;

import slurpp.faces.services.MobileWearIO;
import slurpp.faces_lib.WearIO;

/**
 * Created by Kyle Montague on 30/05/16.
 */
public class FacesApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        MobileWearIO.shared().init(this, new WearIO.OnErrorListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.d("WearIO","Failed to initialise");
            }
        });



    }
}
