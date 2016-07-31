package com.slambuddies.star15.datab;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FMHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    public static final String DB_NAME = "star15_fm.db";

    private static final String DEVICE_CREATE = "create table " + FMContract.USER_TABLE + "(id integer primary key autoincrement, " + FMContract.USER_IMEI + " text not null, " + FMContract.USER_NAME + " text not null);";
    private static final String CHAT_CREATE = "create table " + FMContract.CHAT_TABLE + "(id integer primary key autoincrement, " + FMContract.BUDDY_NAME + " text not null, " + FMContract.BUDDY_MSG + " text not null, " + FMContract.MSG_STATUS + " integer, " + FMContract.DATE_TIME + " text, " + FMContract.DATE + " text);";
    private static final String REQS_CREATE = "create table " + FMContract.REQS_TABLE + "(id integer primary key autoincrement, " + FMContract.TYPE + " integer, " + FMContract.BUDDY_NAME + " text not null, " + FMContract.SONG + " text, " + FMContract.ALBUM + " text, " + FMContract.FOR_BUD + " text, " + FMContract.DATE_TIME + " text, " + FMContract.DATE + " text);";
    private static final String USER_CREATE = "create table " + FMContract.BUDDIES_TABLE + "(id integer primary key autoincrement, " + FMContract.BUDDY_NAME + " text not null, " + FMContract.DATE_TIME + " text);";
    private static final String[] ALL_TABLES_CREATE = {DEVICE_CREATE, CHAT_CREATE, REQS_CREATE, USER_CREATE};

    public FMHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String table : ALL_TABLES_CREATE) {
            db.execSQL(table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String Table : FMContract.TABLES) {
            String drop = "DROP TABLE IF EXISTS " + Table;
            db.execSQL(drop);
        }
        onCreate(db);
    }
}
