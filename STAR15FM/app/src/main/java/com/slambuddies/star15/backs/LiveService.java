package com.slambuddies.star15.backs;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.Dedicate;
import com.slambuddies.star15.acts.RequestAct;
import com.slambuddies.star15.acts.SlamLiveChat;
import com.slambuddies.star15.acts.StarMain;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.infos.BudsInfo;
import com.slambuddies.star15.tools.JsonParser;
import com.slambuddies.star15.tools.Tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

public class LiveService extends IntentService {
    String Title, Text, FirstLine, SecondLine;
    private static final String TAG = "Fetcher";
    List<BudsInfo> mList;
    String name, imei, date;
    boolean status = false;
    static final String GET = "http://star15.890m.com/star15fm/star15_post.php";
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);


    public LiveService() {
        super(TAG);
    }


    public void Notifier() {
        mBuilder.setSmallIcon(R.mipmap.star15_logo_final_save);
        mBuilder.setContentTitle(Title);
        mBuilder.setContentText(Text);
        mBuilder.setTicker("STAR15 FM LIVE!");

        if (status) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            String[] events = new String[6];
            events[0] = FirstLine;
            events[1] = SecondLine;

            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle(Title);
            // Moves events into the big view
            for (int i = 0; i < events.length; i++) {

                inboxStyle.addLine(events[i]);
            }
            mBuilder.setStyle(inboxStyle);
        }

        mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
        mBuilder.setVibrate(new long[]{1000, 500, 200, 500, 500});

        PendingIntent result = PendingIntent.getActivity(this, 0, new Intent(
                this, StarMain.class), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(result);

        mBuilder.setAutoCancel(true);

        Notification notif = mBuilder.build();
        notif.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(15, notif);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        JsonParser jsonParser = new JsonParser();
        JSONObject json = jsonParser.getJSONFromUrl(GET, Tools.getJson(new String[]{"BUDS"}, null));
        int success;
        int count = 0;
        try {
            if (json != null) {
                success = json.getInt("success");
                if (success == 1) {
                    JSONArray array = json.getJSONArray("INFO");
                    int size = array.length();
                    Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(size);

                    for (int j = 0; j < size; j++) {
                        JSONObject c = array.getJSONObject(j);
                        name = c.getString("name");
                        imei = c.getString("imei");
                        date = c.getString("dateTime");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(FMContract.BUDDY_NAME, name);
                        contentValues.put(FMContract.DATE_TIME, date);
                        contentValuesVector.add(contentValues);
                    }


                    if (contentValuesVector.size() > 0) {
                        ContentValues[] values = new ContentValues[contentValuesVector.size()];
                        contentValuesVector.toArray(values);
                        count = getContentResolver().bulkInsert(FMContract.BudsData.CONTENT_URI, values);
                    }

                }
            }

        } catch (Exception e) {
        }
        if (count > 0) {
            Tools.storeBoolPrefs(this, "reg_buds", true);
            if (SlamLiveChat.alive) {
                Intent intent1 = new Intent(Tools.SLAM);
                intent1.putExtra("from", Tools.BUDS);
                sendBroadcast(intent1);
            }
            if (Dedicate.alive) {
                Intent intent1 = new Intent(Tools.DED);
                intent1.putExtra("from", Tools.BUDS);
                sendBroadcast(intent1);
            }
            if (RequestAct.alive) {
                Intent intent1 = new Intent(Tools.REQ);
                intent1.putExtra("from", Tools.BUDS);
                sendBroadcast(intent1);
            }
            Intent intent1 = new Intent(Tools.BUDS);
            intent1.putExtra("from", "ALL");
            sendBroadcast(intent1);

        }
    }

}