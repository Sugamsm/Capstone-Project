package com.slambuddies.star15.backs;

import android.app.IntentService;
import android.content.Intent;

import com.slambuddies.star15.acts.ListenAct;
import com.slambuddies.star15.acts.StarMain;
import com.slambuddies.star15.tools.JsonParser;
import com.slambuddies.star15.tools.Tools;
import com.slambuddies.star15.widget.FMWidgetProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class SongFetch extends IntentService {
    private static final String SONG_URL = "http://star15.890m.com/star15fm/star15_post.php";
    String song = "", pro = "", buddies = "", album_art = "", message = "", fm_url = "";
    int live = 0;

    public SongFetch() {
        super("SFetch");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JsonParser jsonParser = new JsonParser();
        boolean refresh = intent.getBooleanExtra("refresh", false);
        String[] data = new String[]{"SNU_FETCH"};
        JSONObject json = jsonParser.getJSONFromUrl(SONG_URL, Tools.getJson(data, null));
        if (json != null) {
            try {
                int res = json.getInt("success");
                if (res != 0) {

                    JSONArray array = json.getJSONArray("INFO");
                    JSONObject object = array.getJSONObject(0);
                    song = object.getString("song");
                    pro = object.getString("pro");
                    buddies = object.getString("buddies");
                    album_art = object.getString("album_art");
                    live = object.getInt("live");
                    if (live == 0) {
                        FMWidgetProvider.ERROR = 3;
                    }
                    fm_url = object.getString("fm_url");
                    message = object.getString("message");

                }


                Tools.store(this, new String[]{pro, buddies, song, album_art, String.valueOf(live), message, fm_url});
                if (refresh) {
                    //If Refresh was done! Show A SnackBar if Activity live the messagestored in the database
                    if (StarMain.alive) {
                        StarMain.Show("", message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Intent broadcast = new Intent(Tools.SONG);
            broadcast.putExtra("from", "SNU_ERROR");
            if (ListenAct.isAlive) {
                sendBroadcast(broadcast);
            }
            FMWidgetProvider.ERROR = 1;
            sendBroadcast(new Intent(Tools.WIDGET_SERVICE));
        }

    }


}
