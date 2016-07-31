package com.slambuddies.star15.backs;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;

import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.Dedicate;
import com.slambuddies.star15.acts.RequestAct;
import com.slambuddies.star15.acts.Settings;
import com.slambuddies.star15.acts.StarMain;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.JsonParser;
import com.slambuddies.star15.tools.Tools;

import org.json.JSONException;
import org.json.JSONObject;

public class PostService extends IntentService {

    String POST_URL = "http://star15.890m.com/star15fm/star15_post.php", data_1, data_2, type, imei, data_3, data_4, date, filter;
    int pos, must;

    public PostService() {
        super("POST");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String data = intent.getStringExtra("data");
        try {
            JSONObject json = new JSONObject(data);
            type = json.getString("type");
            if (type.equals("REQ")) {
                must = 2;
                data_1 = json.getString("name");
                data_2 = json.getString("song");
                data_3 = json.getString("album");
                data_4 = json.getString("for_buddy");
                date = intent.getStringExtra("date");
                imei = json.getString("imei");
                filter = Tools.REQ;
            } else if (type.equals("DED")) {
                must = 1;
                data_1 = json.getString("name");
                data_2 = json.getString("for_buddy");
                date = intent.getStringExtra("date");
                imei = json.getString("imei");
                filter = Tools.DED;
            } else if (type.equals("SLAM")) {
                must = 0;
                pos = intent.getIntExtra("msg_id", 0);
                filter = Tools.SLAM;
            } else if (type.equals("DEL")) {
                must = -1;
                filter = Tools.DEL;
            } else if (type.equals("UPDATE_UN")) {
                must = 3;
                data_1 = json.getString("name");
                data_2 = json.getString("pre");
            } else if (type.equals("SLAMS")) {
                must = 4;
                if (Tools.loadPrefs(this, "called_once")) {
                    must = 5;
                }
            }

            sendPost(data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void sendPost(String data) {
        Intent intent;
        intent = new Intent(filter);
        intent.putExtra("from", "SERV");
        JsonParser jsonParser = new JsonParser();
        ContentValues values;
        if (must != 5) {
            try {
                JSONObject json = jsonParser.getJSONFromUrl(POST_URL, data);
                if (json != null) {
                    int success = json.getInt("success");
                    intent.putExtra("Success", success);
                    if (success != 0) {
                        if (must == 0) {
                            values = new ContentValues();
                            values.put(FMContract.MSG_STATUS, 2);
                            getContentResolver().update(FMContract.ChatData.CONTENT_URI, values, FMContract.MSG_ID + " =?", new String[]{"" + pos});
                        } else if (must == 1) {
                            values = new ContentValues();
                            values.put(FMContract.TYPE, 2);
                            values.put(FMContract.BUDDY_NAME, data_1);
                            values.put(FMContract.SONG, "NA");
                            values.put(FMContract.ALBUM, "NA");
                            values.put(FMContract.FOR_BUD, data_2);
                            values.put(FMContract.DATE_TIME, Tools.getFullTime());
                            values.put(FMContract.DATE, date);
                            getContentResolver().insert(FMContract.RDData.CONTENT_URI, values);
                        } else if (must == 2) {
                            values = new ContentValues();
                            values.put(FMContract.TYPE, 1);
                            values.put(FMContract.BUDDY_NAME, data_1);
                            values.put(FMContract.SONG, data_2);
                            values.put(FMContract.ALBUM, data_3);
                            values.put(FMContract.FOR_BUD, data_4);
                            values.put(FMContract.DATE_TIME, Tools.getFullTime());
                            values.put(FMContract.DATE, date);
                            getContentResolver().insert(FMContract.RDData.CONTENT_URI, values);

                            try {
                                String live = Tools.getPrefs(this, "live");
                                if (!live.equals("")) {
                                    int status = Integer.valueOf(live);
                                    if (status == 0 || status == 1) {
                                        // status 0 means FM NOT LIVE
                                        // status 1 means NOT REQUEST SHOW
                                        intent.putExtra("request", false);
                                    } else {
                                        intent.putExtra("request", true);
                                    }
                                }
                            } catch (Exception e) {

                            }

                        } else if (must == 3) {
                            sendBroadcast(new Intent(Tools.PREF_FRAG));
                            if (success == 10) {
                                Settings.Snack(data_2, getString(R.string.taken_name_2) + " " + data_2);
                            } else {
                                values = new ContentValues();
                                values.put(FMContract.USER_NAME, data_1);
                                int count = getContentResolver().update(FMContract.UserData.CONTENT_URI, values, FMContract.MSG_ID + "=?", new String[]{"" + 1});
                                if (count > 0) {
                                    Settings.Snack(data_1, getString(R.string.name_updated) + " " + data_1);
                                } else {
                                    Settings.Snack(data_2, getString(R.string.name_update_error) + " " + data_2);
                                }
                            }
                        } else if (must == 4) {
                            //Storing the flag that Internet Connection Receiver Already Called the Service with Success Result
                            //And so it does not get called twice...
                            Tools.storeBoolPrefs(this, "called_once", true);
                            values = new ContentValues();
                            values.put(FMContract.MSG_STATUS, 2);
                            getContentResolver().update(FMContract.ChatData.CONTENT_URI, values, FMContract.MSG_STATUS + "=?", new String[]{"" + 1});

                        }
                    }
                } else {
                    intent.putExtra("Success", 0);
                    if (must == 3) {
                        sendBroadcast(new Intent(Tools.PREF_FRAG));
                        Settings.Snack(data_2, getString(R.string.name_update_error) + " " + data_2);
                    }
                }
            } catch (Exception e) {
                if (must == 3) {
                    sendBroadcast(new Intent(Tools.PREF_FRAG));
                    Settings.Snack(data_2, getString(R.string.name_update_error) + " " + data_2);
                }
            }


            if (type.equals("SLAM")) {
                sendBroadcast(intent);
            } else if (type.equals("REQ") && RequestAct.alive) {
                sendBroadcast(intent);
            } else if (type.equals("DED") && Dedicate.alive) {
                sendBroadcast(intent);
            } else if (type.equals("DEL") && StarMain.alive) {
                sendBroadcast(intent);
            }

        }
    }
}
