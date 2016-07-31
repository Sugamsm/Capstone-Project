package com.slambuddies.star15.datab;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.slambuddies.star15.tools.Tools;

public class FMDataProvider extends ContentProvider {

    private static final int USER = 1;
    private static final int R_D = 2;
    private static final int BUDS_ALL = 3;
    private static final int CHAT = 4;

    private static UriMatcher mUriMatcher = matcher();

    FMHelper helper;

    @Override
    public boolean onCreate() {
        helper = new FMHelper(getContext());
        return false;
    }

    static UriMatcher matcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FMContract.AUTH, FMContract.USER_TABLE + "/", USER);
        uriMatcher.addURI(FMContract.AUTH, FMContract.CHAT_TABLE + "/", CHAT);
        uriMatcher.addURI(FMContract.AUTH, FMContract.REQS_TABLE + "/", R_D);
        uriMatcher.addURI(FMContract.AUTH, FMContract.BUDDIES_TABLE + "/", BUDS_ALL);

        return uriMatcher;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor ret = null;
        switch (mUriMatcher.match(uri)) {
            case USER:
                ret = helper.getReadableDatabase().query(FMContract.USER_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case CHAT:
                ret = helper.getReadableDatabase().query(FMContract.CHAT_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case BUDS_ALL:
                ret = helper.getReadableDatabase().query(FMContract.BUDDIES_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;

            case R_D:
                ret = helper.getReadableDatabase().query(FMContract.REQS_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return ret;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        int match = mUriMatcher.match(uri);

        switch (match) {
            case USER:
                return FMContract.UserData.CONTENT_ITEM;

            case CHAT:
                return FMContract.ChatData.CONTENT_ITEM;

            case BUDS_ALL:
                return FMContract.BudsData.CONTENT_ITEM;

            case R_D:
                return FMContract.RDData.CONTENT_ITEM;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        Uri return_uri;
        long id;
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case USER:
                id = helper.getWritableDatabase().insert(FMContract.USER_TABLE, null, values);
                return_uri = ContentUris.withAppendedId(FMContract.UserData.CONTENT_URI, id);
                break;

            case CHAT:
                id = helper.getWritableDatabase().insert(FMContract.CHAT_TABLE, null, values);
                return_uri = ContentUris.withAppendedId(FMContract.ChatData.CONTENT_URI, id);
                sendChatBroadcast(getContext());
                break;

            case BUDS_ALL:
                id = helper.getWritableDatabase().insert(FMContract.BUDDIES_TABLE, null, values);
                return_uri = ContentUris.withAppendedId(FMContract.BudsData.CONTENT_URI, id);
                sendBudBroadcast(getContext());
                break;

            case R_D:
                id = helper.getWritableDatabase().insert(FMContract.REQS_TABLE, null, values);
                return_uri = ContentUris.withAppendedId(FMContract.RDData.CONTENT_URI, id);
                Log.i("Inserted", "Received from " + values.getAsString(FMContract.BUDDY_NAME));
                sendRDBroadcast(getContext());
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return return_uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = helper.getWritableDatabase();
        int count = 0;
        try {
            db.beginTransaction();
            for (ContentValues contentValues : values) {
                long id = db.insert(FMContract.BUDDIES_TABLE, null, contentValues);
                if (id != -1) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = mUriMatcher.match(uri);
        int count = 0;
        switch (match) {

            case BUDS_ALL:
                count = helper.getWritableDatabase().delete(FMContract.BUDDIES_TABLE, selection, selectionArgs);
                if (count > 0) {
                    try {
                        getContext().sendBroadcast(new Intent(Tools.BUDS).putExtra("from", "DB_DEL"));
                    } catch (Exception e) {
                    }
                }
                break;

            case CHAT:
                count = helper.getWritableDatabase().delete(FMContract.CHAT_TABLE, selection, selectionArgs);
                if (count > 0) {
                    try {
                        getContext().sendBroadcast(new Intent(Tools.SLAM).putExtra("from", "DB_DEL"));
                    } catch (Exception e) {
                    }
                }
                break;

            case R_D:
                count = helper.getWritableDatabase().delete(FMContract.REQS_TABLE, selection, selectionArgs);
                if (count > 0) {
                    try {
                        getContext().sendBroadcast(new Intent(Tools.RDI).putExtra("from", "DB_DEL"));
                    } catch (Exception e) {
                    }
                }
                break;

        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        int match = mUriMatcher.match(uri);
        int count = 0;
        switch (match) {
            case CHAT:
                count = helper.getWritableDatabase().update(FMContract.CHAT_TABLE, values, selection, selectionArgs);
                if (count > 0) {
                    sendChatBroadcast(getContext());
                }
                break;

            case USER:
                count = helper.getWritableDatabase().update(FMContract.USER_TABLE, values, selection, selectionArgs);
                if (count > 0) {
                    getContext().sendBroadcast(new Intent(Tools.SONG).putExtra("from", Tools.BUDS));
                    sendBudBroadcast(getContext());
                }
                break;

            case BUDS_ALL:
                count = helper.getWritableDatabase().update(FMContract.BUDDIES_TABLE, values, selection, selectionArgs);
                if (count > 0) {
                    sendBudBroadcast(getContext());
                }
                break;
        }

        return count;
    }

    private void sendRDBroadcast(Context context) {
        Intent intent = new Intent(Tools.RDI);
        intent.putExtra("from", "NEW");
        context.sendBroadcast(intent);
    }

    private void sendChatBroadcast(Context context) {
        Intent intent = new Intent(Tools.SLAM);
        intent.putExtra("from", "NEW");
        context.sendBroadcast(intent);


    }

    private void sendBudBroadcast(Context context) {
        context.sendBroadcast(new Intent(Tools.SLAM).putExtra("from", Tools.BUDS));
        context.sendBroadcast(new Intent(Tools.DED).putExtra("from", Tools.BUDS));
        context.sendBroadcast(new Intent(Tools.REQ).putExtra("from", Tools.BUDS));
        context.sendBroadcast(new Intent(Tools.BUDS).putExtra("from", "NEW"));
    }

    @Override
    public void shutdown() {
        super.shutdown();
        helper.close();
    }
}
