package com.sointeractive.getresults.pebble.pebble.communication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

class NotificationSender {
    private static final String TAG = NotificationSender.class.getSimpleName();

    private final Context context;

    private NotificationSender(final Context context) {
        this.context = context;
    }

    public static void send(final Context context, final String title, final String body) {
        final NotificationSender sender = new NotificationSender(context);
        sender.send(title, body);
    }

    private void send(final String title, final String body) {
        final Map<String, String> data = getDataMap(title, body);
        final String notificationData = getData(data);

        sendData(notificationData);
    }

    private Map<String, String> getDataMap(final String title, final String body) {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("title", title);
        data.put("body", body);

        return data;
    }

    private String getData(final Map<String, String> data) {
        final JSONObject jsonData = new JSONObject(data);

        return new JSONArray().put(jsonData).toString();
    }

    private void sendData(final String notificationData) {
        Log.d(TAG, "Action: Sending notification: " + notificationData);
        final Intent i = getIntent(notificationData);

        context.sendBroadcast(i);
    }

    private Intent getIntent(final String notificationData) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", Settings.APP_NAME);
        i.putExtra("notificationData", notificationData);

        return i;
    }
}
