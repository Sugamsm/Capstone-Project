package com.slambuddies.star15.datab;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class FMContract {

    public static final String AUTH = "com.slambuddies.star15";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTH);

    public static final String USER_NAME = "user_name";
    public static final String USER_IMEI = "imei";

    public static final String BUDDY_NAME = "buddy_name";
    public static final String BUDDY_MSG = "buddy_msg";
    public static final String MSG_STATUS = "status";
    public static final String DATE_TIME = "datetime";
    public static final String MSG_ID = "id";

    public static final String DATE = "date";
    public static final String TYPE = "type";
    public static final String FOR_BUD = "forBud";
    public static final String ALBUM = "album";
    public static final String SONG = "song";

    public static final String USER_TABLE = "dev_data";
    public static final String CHAT_TABLE = "chat_data";
    public static final String REQS_TABLE = "req_data";
    public static final String BUDDIES_TABLE = "buddies_data";

    public static String[] TABLES = {USER_TABLE, CHAT_TABLE, REQS_TABLE, BUDDIES_TABLE};

    public static class UserData implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(USER_TABLE).build();
        public static final String CONTENT_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTH + "/" + USER_TABLE;
    }

    public static class ChatData implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(CHAT_TABLE).build();
        public static final String CONTENT_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTH + "/" + CHAT_TABLE;
    }

    public static class RDData implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(REQS_TABLE).build();
        public static final String CONTENT_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTH + "/" + REQS_TABLE;
    }

    public static class BudsData implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(BUDDIES_TABLE).build();
        public static final String CONTENT_ITEM = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTH + "/" + BUDDIES_TABLE;
    }


}
