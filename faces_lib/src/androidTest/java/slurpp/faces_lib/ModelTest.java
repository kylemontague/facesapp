package slurpp.faces_lib;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import slurpp.faces_lib.models.ModelFace;

/**
 * Created by kyle montague on 20/05/2017.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ModelTest {


    Context context;
    Gson gson;

    @Before
    public void init(){
        context = InstrumentationRegistry.getTargetContext();
        gson= new GsonBuilder().create();
    }

    @Test
    public void testModelCreate(){
        ModelFace face = new ModelFace();
        face.setName("KYLE");
        String json = gson.toJson(face);

        Assert.assertNotNull(json);
    }

    @Test
    public void textJsonModel(){
        String json = "{\"baseFace\":\"OUA_TIME\",\"name\":\"KYLE\",\"colours\":{\"FOREGROUND\":-16777216,\"BACKGROUND\":-1},\"createdOn\":1495306758626,\"id\":0,\"tapMode\":\"COLOR\"}";
        ModelFace face = gson.fromJson(json,ModelFace.class);

        Assert.assertNotNull(face);

        Assert.assertEquals("KYLE",face.getName());
    }
}
