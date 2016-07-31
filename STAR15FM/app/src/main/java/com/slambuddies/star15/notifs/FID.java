package com.slambuddies.star15.notifs;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.slambuddies.star15.tools.Tools;


public class FID extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        //Get Your Token to test the App Notifications From the Admin Panel.
        //This Token should be provided in the admin panel.

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i("Found FCM Token \n", token);
        if (token != null) {
            Tools.storePrefs(this, "token", token);
        }


    }
}
