package com.slambuddies.star15.notifs;

import android.content.ContentValues;
import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.StarMain;
import com.slambuddies.star15.backs.LiveService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.tools.Tools;

import java.util.Locale;
import java.util.Map;


public class FNotifs extends FirebaseMessagingService {

    Map<String, String> data;
    String type, myName;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        myName = Tools.getSelf(getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0];
        data = remoteMessage.getData();
        type = data.get("type");
        switch (type) {
            case "SLAM":
                slam(data);
                break;

            case "REQ":
            case "DED":
                RD(data);
                break;

            case "BUD":
                Bud(data);
                break;

            case "BUD_REMOVE":
                BudDel(data);
                break;

            case "SNU":
                song(data);
                break;

            case "UPDATE":
                update();
                break;

            case "UPDATE_UN":
                update_un(data);
                break;

            case "MENTION_SLAM":
                slamMention(data);
                break;

            case "MENTION_REQ":
                reqMention(data);
                break;

            case "MENTION_DED":
                dedMention(data);
                break;
        }
    }


    public void reqMention(Map<String, String> map) {
        String from = map.get("from_buddy");
        if (!from.equals(myName)) {
            String song = map.get("song");
            String msg = String.format(Locale.getDefault(), getString(R.string.fm_notif_requested), from, song);
            if (Tools.loadPrefs(this, getString(R.string.notif_key))) {
                Tools.Notify(this, new String[]{from, msg, getString(R.string.mentioned), getString(R.string.fm_mentions)});
            } else {
                if (StarMain.alive) {
                    StarMain.Show(from, msg);
                }
            }
        }
    }

    public void dedMention(Map<String, String> map) {
        String from = map.get("from_buddy");
        if (!from.equals(myName)) {
            String msg = String.format(Locale.getDefault(), getString(R.string.fm_notif_dedicated), from);
            if (Tools.loadPrefs(this, "notif_key")) {
                Tools.Notify(this, new String[]{from, msg, getString(R.string.mentioned), getString(R.string.fm_mentions)});
            } else {
                if (StarMain.alive) {
                    StarMain.Show(from, msg);
                }
            }
        }
    }

    public void slamMention(Map<String, String> map) {
        String from = map.get("from_buddy");
        if (!from.equals(myName)) {
            String msg = String.format(Locale.getDefault(), getString(R.string.fm_notif_slam), from);
            if (Tools.loadPrefs(this, "notif_key")) {
                Tools.Notify(this, new String[]{from, msg, getString(R.string.mentioned), getString(R.string.fm_mentions)});
            } else {
                if (StarMain.alive) {
                    StarMain.Show(from, msg);
                }
            }
        }
    }

    public void update() {
        getContentResolver().delete(FMContract.BudsData.CONTENT_URI, null, null);
        Intent intent = new Intent(this, LiveService.class);
        startService(intent);
    }

    public void update_un(Map<String, String> map) {
        ContentValues values = new ContentValues();
        values.put(FMContract.BUDDY_NAME, map.get("from_buddy"));
        getContentResolver().update(FMContract.BudsData.CONTENT_URI, values, FMContract.BUDDY_NAME + "=?", new String[]{map.get("pre")});

    }


    public void song(Map<String, String> data) {
        String live = data.get("live");
        String song_ = data.get("song");
        String pro = data.get("pro_name");
        String buds = data.get("buddies");
        String album_art = data.get("album_art");
        String message = data.get("message");
        String fm_url = data.get("fm_url");
        Tools.store(this, new String[]{pro, buds, song_, album_art, live, message, fm_url});
    }

    public void BudDel(Map<String, String> data) {
        getContentResolver().delete(FMContract.BudsData.CONTENT_URI, FMContract.USER_IMEI + " =?", new String[]{data.get("imei")});
    }

    public void slam(Map<String, String> map) {
        ContentValues values = new ContentValues();
        values.put(FMContract.BUDDY_NAME, map.get("from_buddy"));
        values.put(FMContract.BUDDY_MSG, map.get("slam"));
        values.put(FMContract.MSG_STATUS, 0);
        values.put(FMContract.DATE_TIME, map.get("when"));
        values.put(FMContract.DATE, map.get("mon_day"));
        getContentResolver().insert(FMContract.ChatData.CONTENT_URI, values);
    }

    public void RD(Map<String, String> map) {
        ContentValues values = new ContentValues();
        values.put(FMContract.BUDDY_NAME, map.get("from_buddy"));
        values.put(FMContract.TYPE, Integer.valueOf(map.get("rd_type")));
        values.put(FMContract.SONG, map.get("song"));
        values.put(FMContract.ALBUM, map.get("album"));
        values.put(FMContract.FOR_BUD, map.get("for_buddy"));
        values.put(FMContract.DATE_TIME, map.get("when"));
        values.put(FMContract.DATE, map.get("mon_day"));
        getContentResolver().insert(FMContract.RDData.CONTENT_URI, values);
    }

    public void Bud(Map<String, String> map) {
        ContentValues values = new ContentValues();
        values.put(FMContract.BUDDY_NAME, map.get("from_buddy"));
        values.put(FMContract.USER_IMEI, map.get("imei"));
        values.put(FMContract.DATE_TIME, map.get("when"));
        getContentResolver().insert(FMContract.BudsData.CONTENT_URI, values);
    }

}
