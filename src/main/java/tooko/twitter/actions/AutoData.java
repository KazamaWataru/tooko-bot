package tooko.twitter.actions;

import org.bson.codecs.pojo.annotations.BsonId;
import tooko.td.core.Table;

import java.util.ArrayList;

public class AutoData {

    public static Table<Long, AutoData> DATA = new Table<>("auto", AutoData.class);

    @BsonId
    public long accountId;

    public ArrayList<AutoFollowed> autoFollowed;
    public ArrayList<Long> autoFollowedIDs;

    public static class AutoFollowed {

        public long accountId;
        public long followedAt;
        public long unFollowedAt;


    }

}
