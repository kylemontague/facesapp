package slurpp.faces;

import android.util.Log;

import com.orm.SugarApp;

import slurpp.faces.services.MobileWearIO;
import slurpp.faces_lib.utils.WearIO;

/**
 * Created by Kyle Montague on 30/05/16.
 */
public class FacesApplication extends SugarApp {


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

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
