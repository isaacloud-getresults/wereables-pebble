package com.sointeractive.getresults.pebble.isaacloud.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.getresults.pebble.utils.Application;

public class IsaacloudNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = IsaacloudNotificationReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Event: New notification to send");

        final String title = intent.getStringExtra("title");
        final String body = intent.getStringExtra("body");
        Application.pebbleConnector.sendNotification(title, body);
    }
}
