package com.slambuddies.star15.backs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.ListenAct;
import com.slambuddies.star15.acts.Startup;
import com.slambuddies.star15.tools.Tools;
import com.slambuddies.star15.widget.FMWidgetProvider;

import java.io.IOException;

public class StreamService extends Service implements OnPreparedListener, MediaPlayer.OnErrorListener {
    Intent broadcas;
    public static boolean playing = false;
    MediaPlayer mPlayer = null;
    private static int NOTIFICATION_ID = 2;
    Intent widget_intent;
    String url3 = "";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        broadcas = new Intent(Tools.SONG);
        widget_intent = new Intent(Tools.WIDGET_SERVICE);
        super.onCreate();
    }

    public void initMP(String url) {

        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mPlayer.setDataSource(url);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnErrorListener(this);
            mPlayer.prepareAsync();

        } catch (IllegalArgumentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalStateException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //If Tools store prefs called this Service to start on url_change
        //then as the former also stops the service, and stopping service may take some delay setting playing as false
        // setting playing true here so as stop any delay conflicts with UI
        //better to be set twice then to not at all :P
        Tools.storeBoolPrefs(this, "playing", true);
        url3 = Tools.getPrefs(this, "fm_url");
        if (url3.equals("")) {
            //From the Reviewer Notes
            url3 = "";
        }
        initMP(url3);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                playing = mPlayer.isPlaying();
            }
            mPlayer.release();
            mPlayer = null;
        }
        Tools.storeBoolPrefs(this, "playing", playing);
        CancelNotif();
        sendBroadcast(widget_intent);
        if (ListenAct.isAlive) {
            broadcas.putExtra("from", "SERVICE");
            sendBroadcast(broadcas);
        }
        super.onDestroy();
    }

    public void Notify() {
        //This Notification will be running Foreground... and so Not added to Stack.
        Intent notificationIntent = new Intent(this, Startup.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this);
        mBuilder.setSmallIcon(R.mipmap.star15_logo_final_save);
        mBuilder.setContentTitle("STAR15 FM's LIVE");
        mBuilder.setContentText("Currently Playing.....");
        mBuilder.setOngoing(true);

        mBuilder.setContentIntent(pendingIntent);

        Notification notification = mBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    public void CancelNotif() {
        stopForeground(true);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        try {
            if (mp != null) {
                mp.start();
                Notify();
                broadcas.putExtra("from", "SERVICE");
                FMWidgetProvider.ERROR = 0;
                sendBroadcast(widget_intent);
                if (ListenAct.isAlive) {
                    sendBroadcast(broadcas);
                }
            }
        } catch (Exception e) {
            this.stopSelf();
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        FMWidgetProvider.ERROR = 1;
        this.stopSelf();
        return false;
    }
}
