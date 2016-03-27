package slurpp.faces_lib.models;

import com.activeandroid.serializer.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kylemontague on 27/03/16.
 */
public class ListTypeSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return ArrayList.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public String serialize(Object o) {
        if (o == null) {
            return null;
        }

        String str = "";
        for (Integer i : (List<Integer>) o) {
            str += i + "\t";
        }
        return str;
    }

    @Override
    public ArrayList<Integer> deserialize(Object o) {
        if (o == null) {
            return null;
        }

        ArrayList<Integer> integers = new ArrayList<>();
        String str = (String) o;
        for(String s:str.split("\t")){
            integers.add(Integer.valueOf(s));
        }
        return integers;
    }
}
