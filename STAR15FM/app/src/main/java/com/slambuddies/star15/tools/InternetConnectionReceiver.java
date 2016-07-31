package com.slambuddies.star15.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.slambuddies.star15.backs.PostService;
import com.slambuddies.star15.backs.StreamService;
import com.slambuddies.star15.widget.FMWidgetProvider;

import java.util.ArrayList;

public class InternetConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Tools.isNetConn(context)) {
            context.sendBroadcast(new Intent(Tools.WIDGET_NET));
            ArrayList<String> data = Tools.slams(context);
            if (data != null) {
                if (data.size() > 0) {
                    Intent service = new Intent(context, PostService.class);
                    String json = Tools.getSlamMultiple(context, data);
                    service.putExtra("data", json);
                    context.startService(service);
                }
            }
        } else {
            FMWidgetProvider.ERROR = 5;
            //Setting for the service to check and send the un-send slams (Messages, a Slang used by me (THE APP))...
            // If service not already called once
            //Check PostService for the same detail
            Tools.storeBoolPrefs(context, "called_once", false);
            context.sendBroadcast(new Intent(Tools.WIDGET_NET));
            if (Tools.loadPrefs(context, "playing")) {
                context.stopService(new Intent(context, StreamService.class));
            }


        }
    }
}
