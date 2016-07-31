package com.slambuddies.star15.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotifDeleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Tools.storePrefs(context, "notif_counter", "0");
        Tools.storeBoolPrefs(context, "NOTIFIED", false);
    }
}
