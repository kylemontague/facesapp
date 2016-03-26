package slurpp.faces.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import slurpp.faces_lib.WatchDraw;
import slurpp.faces.R;

public class onboard_camera extends Activity {

    private static final int CAMERA_IMAGE_CAPTURE = 1337;

    int faceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard_camera);
        Bundle extras = getIntent().getExtras();
        faceID = extras.getInt(CustomiseActivity.EXTRA_WATCH_FACE, WatchDraw.FACE.TWO_TIME.ordinal());
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            public void run() {
                takePhoto();
            }
        }, 1500);
    }


    public void takePhoto(){
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(i, CAMERA_IMAGE_CAPTURE);
        overridePendingTransition(R.animator.fade_in, R.animator.fade_out);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CAMERA_IMAGE_CAPTURE:
                if(resultCode == 0)
                    return;
                Bundle extras = data.getExtras();
                Bitmap image = (Bitmap) extras.get("data");
                Intent customiseIntent = new Intent(this,CustomiseActivity.class);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_BITMAP_IMAGE,image);
                customiseIntent.putExtra(CustomiseActivity.EXTRA_WATCH_FACE,faceID);
                startActivity(customiseIntent);

                break;
        }
    }

}
