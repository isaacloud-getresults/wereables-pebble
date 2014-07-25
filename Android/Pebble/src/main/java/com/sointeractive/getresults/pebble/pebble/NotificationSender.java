package com.sointeractive.getresults.pebble.pebble;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationSender {
    private static final String TAG = NotificationSender.class.getSimpleName();

    private final Context context;

    public NotificationSender(Context context) {
        this.context = context;
    }

    public static void send(Context context, String title, String body) {
        NotificationSender sender = new NotificationSender(context);
        sender.send(title, body);
    }

    private void send(String title, String body) {
        Map<String, String> data = getDataMap(title, body);
        String notificationData = getData(data);

        sendData(notificationData);
    }

    private Map<String, String> getDataMap(String title, String body) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("title", title);
        data.put("body", body);

        return data;
    }

    private String getData(Map<String, String> data) {
        JSONObject jsonData = new JSONObject(data);

        return new JSONArray().put(jsonData).toString();
    }

    private void sendData(String notificationData) {
        Log.d(TAG, "Action: Sending notification: " + notificationData);
        final Intent i = getIntent(notificationData);

        context.sendBroadcast(i);
    }

    private Intent getIntent(String notificationData) {
        Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", Settings.APP_NAME);
        i.putExtra("notificationData", notificationData);

        return i;
    }
}
