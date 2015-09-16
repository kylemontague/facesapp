package montague.faces_lib;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import java.util.ArrayList;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TestCustomFace();
        TestCustomFaceArray();
    }

    int[] colours = new int[]{R.color.black,R.color.green,R.color.white,R.color.white,R.color.white};

    public void TestCustomFace(){

        CustomFace cf = new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.BUSINESS_TIME,colours,"Test Watch");

        Assert.assertEquals(cf.getName(),"Test Watch");
        Assert.assertEquals(cf.getBackgroundColor(), R.color.black);

        Context c = getContext();
        Assert.assertNotNull(c);

        Uri file = Global.textFile(c, "watch", cf);
        Assert.assertNotNull(file);

        CustomFace cf2 = (CustomFace) Global.objectFromFile(file);
        Assert.assertNotNull(cf2);
    }


    public void TestCustomFaceArray(){
        ArrayList<CustomFace> faces = new ArrayList<>();
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.BUSINESS_TIME,colours,"Test Watch 1"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.FACES_FACE,colours,"Test Watch 2"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.OUA_TIME,colours,"Test Watch 3"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.FILL_TIME,colours,"Test Watch 4"));
        faces.add(new CustomFace(System.currentTimeMillis(), WatchDraw.FACE.BUSINESS_TIME, colours, "Test Watch 5"));

        Uri file = Global.textFile(getContext(), "watches", faces);
        Assert.assertNotNull(file);

        ArrayList<CustomFace> loadedFaces = (ArrayList<CustomFace>) Global.objectFromFile(file);
        Assert.assertNotNull(loadedFaces);
    }
}