package slurpp.faces_lib.models;

import java.util.HashMap;

import slurpp.faces_lib.Global;

/**
 * Created by kylemontague on 09/04/16.
 */
public class WearSettings implements IMap {



    public boolean isRound;
    public boolean isActive;

    public WearSettings(boolean isActive, boolean isRound){
        this.isActive = isActive;
        this.isRound = isRound;
    }

    @Override
    public HashMap<String, String> toMap() {
        HashMap<String,String> map = new HashMap<>();
        map.put(Global.WEAR_KEYS.IS_ACTIVE.name(),String.valueOf(isActive));
        map.put(Global.WEAR_KEYS.SHAPE.name(),String.valueOf(isRound));
        return map;
    }
}
