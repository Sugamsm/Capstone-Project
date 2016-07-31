package com.slambuddies.star15.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.MultiAutoCompleteTextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.slambuddies.star15.R;
import com.slambuddies.star15.acts.StarMain;
import com.slambuddies.star15.acts.Startup;
import com.slambuddies.star15.backs.StreamService;
import com.slambuddies.star15.datab.FMContract;
import com.slambuddies.star15.datab.FMHelper;
import com.slambuddies.star15.infos.BudsInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Tools {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String AUTH = "com.slambuddies.star15.";
    public static final String SLAM = AUTH + "SLAM";
    public static final String SONG = AUTH + "SFETCH";
    public static final String REQ = AUTH + "REQ";
    public static final String DED = AUTH + "DED";
    public static final String BUDS = AUTH + "BUDS";
    public static final String RDI = AUTH + "RDI";
    public static final String DEL = AUTH + "DEL";
    public static final String PREF_FRAG = AUTH + "SERVICE";
    public static final String WIDGET_NET = AUTH + "NETWORK";
    public static final String WIDGET_SERVICE = AUTH + "WIDGET_INTERNAL_UPDATES";
    public static final String NOTIF_DELETED = AUTH + "NOTIF_DELETED";
    public static final int NOTIFICATION_ID = 3;

    //Set it From the Reviewer Notes
    public static final String POST_URL = "http://star15.890m.com/star15fm/star15_post.php";

    public static boolean checkPlayServices(Activity context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public static boolean isNetConn(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        return info != null && info.isConnectedOrConnecting();
    }

    public static int getRandomInteger(int maximum, int minimum) {
        return ((int) (Math.random() * (maximum - minimum))) + minimum;
    }

    public static void hiddenKeyboard(View vi) {
        InputMethodManager keyboard = (InputMethodManager) vi.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(vi.getWindowToken(), 0);
    }

    public static String getJson(String[] data, String[] men_array) {
        JSONObject json = null;
        String type = data[0];
        try {
            switch (type) {
                case "SNU":
                    json = new JSONObject();
                    json.put("type", type);
                    break;

                case "UPDATE":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("imei", data[1]);
                    json.put("token", data[2]);
                    break;

                case "REG_VER":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("imei", data[1]);
                    break;

                case "REG":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("name", data[1]);
                    json.put("imei", data[2]);
                    json.put("token", data[3]);
                    break;

                case "REQ":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("name", data[1]);
                    json.put("for_buddy", data[2]);
                    json.put("song", data[3]);
                    json.put("album", data[4]);
                    json.put("imei", data[5]);
                    int mentioned = Integer.valueOf(data[6]);
                    json.put("mentioned", mentioned);

                    if (mentioned == 1) {
                        JSONArray array = mentions(men_array);
                        if (array != null) {
                            json.put("men_array", array);
                        }
                    }
                    break;

                case "SLAM":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("buddy", data[1]);
                    json.put("slam", data[2]);
                    json.put("imei", data[3]);
                    int men = Integer.valueOf(data[4]);
                    json.put("mentioned", men);

                    if (men == 1) {
                        JSONArray array = mentions(men_array);
                        if (array != null) {
                            json.put("men_array", array);
                        }
                    }

                    break;

                case "DED":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("name", data[1]);
                    json.put("for_buddy", data[2]);
                    json.put("imei", data[3]);
                    int m = Integer.valueOf(data[4]);
                    json.put("mentioned", m);

                    if (m == 1) {
                        JSONArray array = mentions(men_array);
                        if (array != null) {
                            json.put("men_array", array);
                        }
                    }

                    break;

                case "BUDS":
                    json = new JSONObject();
                    json.put("type", type);
                    break;

                case "DEL":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("imei", data[1]);
                    break;

                case "SNU_FETCH":
                    json = new JSONObject();
                    json.put("type", type);
                    break;

                case "UPDATE_UN":
                    json = new JSONObject();
                    json.put("type", type);
                    json.put("name", data[1]);
                    json.put("pre", data[2]);
                    break;
            }
        } catch (Exception e) {
        }
        return json.toString();
    }

    public interface Switcher {
        void onSwitch(int id);
    }


    public static JSONArray mentions(String[] men_array) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < men_array.length; i++) {
            array.put(men_array[i]);
        }
        if (array.length() > 0) {
            return array;
        } else {
            return null;
        }
    }

    public static String[] getSelf(Cursor cur) {
        try {
            if (cur != null && cur.moveToFirst()) {
                String[] data = new String[]{cur.getString(2), cur.getString(1)};
                cur.close();
                return data;
            }
        } catch (Exception e) {
            return new String[]{"NA", "NA"};
        }
        return new String[]{"NA", "NA"};
    }


    public static String getSlamMultiple(Context context, ArrayList<String> mList) {
        try {
            String[] self = getSelf(context.getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null));
            ArrayList<String> list = new ArrayList<>();
            JSONObject json = new JSONObject();
            long previous = 0;
            json.put("type", "SLAMS");
            json.put("name", self[0]);
            json.put("imei", self[1]);
            String output = "";
            JSONArray times = new JSONArray();
            for (int i = 0; i < mList.size(); i++) {
                String msg = mList.get(i);
                long time = System.currentTimeMillis();
                String time_string = String.valueOf(time);
                if (previous == time) {
                    time_string += "153";
                }
                previous = time;

                times.put("" + time);
                output += time_string + msg + time_string;
                String[] names = Name(context, msg);
                if (names != null) {
                    for (int j = 0; j < names.length; j++) {
                        list.add(names[j]);
                    }
                }
            }
            if (!list.isEmpty() && list.size() > 0) {
                String[] men_array = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    men_array[i] = list.get(i);
                }
                JSONArray array = mentions(men_array);
                if (array != null) {
                    json.put("mentioned", 1);
                    json.put("men_array", array);
                }
            } else {
                json.put("mentioned", 0);
            }
            if (!output.equals("") && times.length() > 0) {
                json.put("all", output);
                json.put("times", times);
            }
            return json.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static ArrayList<String> slams(Context context) {
        ArrayList<String> array = null;
        try {
            Cursor data = context.getContentResolver().query(FMContract.ChatData.CONTENT_URI,
                    null,
                    FMContract.MSG_STATUS + "=?",
                    new String[]{"" + 1}, null);
            if (data != null && data.getCount() > 0) {
                if (data.moveToFirst()) {
                    array = new ArrayList<>();
                    do {
                        if (data.getInt(3) != 2) {
                            array.add(data.getString(2));
                        }
                    } while (data.moveToNext());
                }
                data.close();
            }
            return array;
        } catch (Exception e) {
            return array;
        }
    }

    public static void storePrefs(Context context, String key, String value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getPrefs(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, "");
    }

    public static class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {

        @Override
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        @Override
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        @Override
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();
            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }
            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }


    public static int getToday() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public static String getFullTime() {
        String time, pm;
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour > 12) {
            hour = hour - 12;
        }
        if (hour == 0) {
            hour = 12;
        }
        int am = cal.get(Calendar.AM_PM);
        if (am == 1) {
            pm = "PM";
        } else {
            pm = "AM";
        }
        int min = cal.get(Calendar.MINUTE);
        time = String.format(Locale.US, "%02d:%02d" + " " + pm, hour, min);
        return time;

    }

    public static int getDateParser(String mon) {
        String in = mon.substring(mon.indexOf("Q") + 1, mon.length() - 1);
        return Integer.parseInt(in);
    }

    public static String getTodays(String today) {
        String ret = today.replace("Q", " ");
        return ret;
    }

    public static List<BudsInfo> getBuds(Cursor cursor, String name) {
        List<BudsInfo> budsInfo = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String my = cursor.getString(1);
                    if (!my.equals(name)) {
                        BudsInfo current = new BudsInfo();
                        current.setName(my);
                        budsInfo.add(current);
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return budsInfo;
    }

    public static String[] setAdapter(Context context) {
        String[] names;
        try {

            List<BudsInfo> info = getBuds(context.getContentResolver().query(FMContract.BudsData.CONTENT_URI,
                    null,
                    null,
                    null,
                    null), getSelf(context.getContentResolver().query(FMContract.UserData.CONTENT_URI, null, null, null, null))[0]);

            if (info != null) {
                names = new String[info.size()];
                for (int i = 0; i < info.size(); i++) {
                    BudsInfo current = info.get(i);
                    names[i] = current.getName();
                }
                return names;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static void resetDB(Context context) {
        try {
            FMHelper helper = new FMHelper(context);
            final SQLiteDatabase db = helper.getWritableDatabase();
            for (String Table : FMContract.TABLES) {
                String drop = "DROP TABLE IF EXISTS " + Table;
                db.execSQL(drop);
            }
            helper.onCreate(db);
            db.close();
        } catch (Exception e) {

        }
    }


    public static String[] Name(Context context, String msg) {
        String[] names = setAdapter(context);
        ArrayList<String> mList = new ArrayList<>();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (msg.contains(names[i])) {
                    mList.add(names[i]);
                }
            }
            if (mList.size() == 0 || mList.isEmpty()) {
                return null;
            } else {
                String[] found = new String[mList.size()];
                for (int j = 0; j < mList.size(); j++) {
                    found[j] = mList.get(j);
                }
                return found;
            }
        } else {
            return null;
        }
    }

    public static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotifDeleteReceiver.class);
        intent.setAction(NOTIF_DELETED);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static int getNotifCount(Context context) {
        int counter = 0;
        String count = Tools.getPrefs(context, "notif_counter");
        if (count != null && !count.equals("")) {
            counter = Integer.valueOf(count);
        }

        return counter;
    }

    public static void Notify(Context context, String[] data) {
        if (!StarMain.alive) {
            int counter;
            Intent notificationIntent = new Intent(context, Startup.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            mBuilder.setTicker(data[2]);
            mBuilder.setSmallIcon(R.mipmap.star15_logo_final_save);
            mBuilder.setTicker(data[2]);
            mBuilder.setSmallIcon(R.mipmap.star15_logo_final_save);
            inboxStyle.setBigContentTitle(data[3]);
            counter = getNotifCount(context);
            if (counter < 2) {
                mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
            }
            if (loadPrefs(context, "NOTIFIED")) {
                if (counter != 0) {

                    mBuilder.setContentTitle(context.getString(R.string.fm_mentions));
                    mBuilder.setContentText(String.format(Locale.getDefault(), context.getString(R.string.fm_multiple_notifs), counter + 1));
                    mBuilder.setNumber(counter + 1);
                    for (int i = 0; i < counter; i++) {
                        inboxStyle.addLine(Tools.getPrefs(context, "notif_" + i));
                    }
                    inboxStyle.addLine(data[1]);
                    mBuilder.setStyle(inboxStyle);
                }
            } else {
                mBuilder.setContentTitle(context.getString(R.string.fm_live));
                mBuilder.setContentText(context.getString(R.string.mentioned));
                mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(data[1]));
            }
            storeBoolPrefs(context, "NOTIFIED", true);
            storePrefs(context, "notif_" + counter, data[1]);
            counter++;
            storePrefs(context, "notif_counter", String.valueOf(counter));
            mBuilder.setContentIntent(pendingIntent);

            mBuilder.setDeleteIntent(getDeleteIntent(context));
            Notification notification = mBuilder.build();
            managerCompat.notify(NOTIFICATION_ID, notification);

        } else {
            StarMain.Show(data[0], data[1]);
        }

    }

    public static void storeBoolPrefs(Context context, String key, boolean bool) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(key, bool).apply();
    }

    public static boolean loadPrefs(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (key.equals("notif_key")) {
            return preferences.getBoolean(key, true);
        } else if (key.equals("album_art_key")) {
            return preferences.getBoolean(key, true);
        }
        return preferences.getBoolean(key, false);
    }


    public static int getWidgetHeight(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return context.getResources().getDimensionPixelSize(R.dimen.widget_default_height);
        }
        return getWidgetHeightFromOptions(context, appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static int getWidgetHeightFromOptions(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)) {
            int minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minHeightDp,
                    displayMetrics);
        }
        return context.getResources().getDimensionPixelSize(R.dimen.widget_default_height);
    }

    public static void store(Context context, String[] data) {
        Intent broadcast = new Intent(Tools.SONG);


        if (data[2].equals("default")) {
            broadcast.putExtra("from", "SNU_NO_CHANGE");
            storePrefs(context, "pro", "");
            storePrefs(context, "buddies", "");
            storePrefs(context, "song", "");
            storePrefs(context, "album_art", "");
            storePrefs(context, "live", "0");
            storePrefs(context, "message", "");
        } else {
            broadcast.putExtra("from", "SNU_FETCH");
            storePrefs(context, "pro", data[0]);
            storePrefs(context, "buddies", data[1]);
            storePrefs(context, "song", data[2]);
            storePrefs(context, "album_art", data[3]);
            if (!getPrefs(context, "fm_url").equals("") && !getPrefs(context, "fm_url").equals(data[6])) {
                storePrefs(context, "fm_url", data[6]);
                if (loadPrefs(context, "playing")) {
                    context.stopService(new Intent(context, StreamService.class));
                    context.startService(new Intent(context, StreamService.class));
                    broadcast.putExtra("url_change_restart", true);
                }
            } else {
                broadcast.putExtra("url_change_restart", false);
                storePrefs(context, "fm_url", data[6]);
            }
            if (data[4].equals("0")) {
                if (loadPrefs(context, "playing")) {
                    context.stopService(new Intent(context, StreamService.class));
                }
            }else{
                if (!loadPrefs(context, "playing")) {
                    if(StarMain.alive){
                        StarMain.Show("", data[5]);
                    }
                }
            }
            storePrefs(context, "live", data[4]);
            storePrefs(context, "message", data[5]);

        }
        if (loadPrefs(context, "playing")) {
            context.sendBroadcast(broadcast);
            context.sendBroadcast(new Intent(Tools.WIDGET_SERVICE));
        }
    }


    public static boolean checkRtl(Context context) {
        boolean rtl = false;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                rtl = true;
            }
        } else {

            Set<String> lang = new HashSet<String>();
            lang.add("ar");
            lang.add("dv");
            lang.add("fa");
            lang.add("ha");
            lang.add("he");
            lang.add("iw");
            lang.add("ji");
            lang.add("ps");
            lang.add("ur");
            lang.add("yi");
            Set<String> RTL = Collections.unmodifiableSet(lang);

            Locale locale = Locale.getDefault();

            rtl = RTL.contains(locale.getLanguage());
        }

        return rtl;
    }

}
