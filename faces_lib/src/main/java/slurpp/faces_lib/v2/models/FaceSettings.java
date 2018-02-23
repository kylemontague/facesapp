package slurpp.faces_lib.v2.models;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;
import slurpp.faces_lib.CustomFace;
import slurpp.faces_lib.WatchDraw;

/**
 * Created by Kyle Montague on 19/08/2017.
 */

public class FaceSettings extends RealmObject{




    @PrimaryKey
    String id = UUID.randomUUID().toString();

    long createdOn = System.currentTimeMillis();
    String name;
    RealmList<FaceColor> colors = new RealmList<>();
    int base;
    int tapMode = WatchDraw.FaceMode.DATE.ordinal();

    boolean favourite = false;

    public FaceSettings() {
    }

    public FaceSettings(RealmList<FaceColor> colors, WatchDraw.FACE base) {
        this.colors = colors;
        setBase(base);
    }


    public FaceSettings(String name, RealmList<FaceColor> colors, WatchDraw.FACE base) {
        this.name = name;
        this.colors = colors;
        setBase(base);
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<FaceColor> getColors() {
        return colors;
    }

    public int[] getColorsArray(){
        if(colors==null||colors.size() == 0)
            return new int[]{};

        int[] values = new int[colors.size()];
        int i=0;
        for(FaceColor color:colors){
            values[i]=color.normal;
            i++;
        }
        return values;
    }

    public void setColors(RealmList<FaceColor> colors) {
        this.colors = colors;
    }


    public void addColor(FaceColor color){
        this.colors.add(color);
    }

    public WatchDraw.FaceMode getTapMode() {
        return WatchDraw.FaceMode.fromOrdinal(this.tapMode);
    }

    public void setTapMode(WatchDraw.FaceMode tapMode) {
        this.tapMode = tapMode.ordinal();
    }

    public void setBase(WatchDraw.FACE base){
        this.base = base.ordinal();
    }

    public WatchDraw.FACE getBase(){
        return WatchDraw.FACE.fromOrdinal(this.base);
    }

    public static FaceSettings fromJson(String json){
        return new GsonBuilder().create().fromJson(json,FaceSettings.class);
    }

    public String toJson(){
        return new GsonBuilder().create().toJson(this);
    }


    public static FaceSettings current(Realm realm){
        RealmResults<FaceSettings> realmResults = all(realm, Sort.DESCENDING);
        if(realmResults.size() > 0)
            return realmResults.first();
        return null;
    }

    public static RealmResults<FaceSettings> all(Realm realm, Sort sort){
        return realm.where(FaceSettings.class).findAllSorted("createdOn",sort);
    }


    public static List<FaceSettings> stock(){
        List<FaceSettings> list = new ArrayList<>();
        for(WatchDraw.FACE face:WatchDraw.FACE.values()){
            list.add(new FaceSettings(null,face));
        }
        return list;
    }

    public static FaceSettings get(Realm realm, String id) {
        return realm.where(FaceSettings.class).equalTo("id",id).findFirst();
    }

    public static void fromCustomFace(Realm realm, final CustomFace customFace){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                FaceSettings faceSettings = new FaceSettings();
                for(int color:customFace.getColors()){
                    faceSettings.addColor(new FaceColor(color));
                }
                faceSettings.setBase(customFace.getBase());

                realm.insert(faceSettings);
            }
        });
    }
}
