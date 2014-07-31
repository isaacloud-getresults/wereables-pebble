package com.sointeractive.getresults.pebble.pebble.communication;

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

    public NotificationSender(final Context context) {
        Log.i(TAG, "Action: Initialize notification sender");
        this.context = context;
    }

    public void send(final String title, final String body) {
        final Map<String, String> map = getDataMap(title, body);
        final String data = getData(map);

        sendData(data);
    }

    private Map<String, String> getDataMap(final String title, final String body) {
        final Map<String, String> data = new HashMap<String, String>();
        data.put("title", title);
        data.put("body", body);

        return data;
    }

    private String getData(final Map<String, String> map) {
        final JSONObject jsonData = new JSONObject(map);

        return new JSONArray().put(jsonData).toString();
    }

    private void sendData(final String data) {
        Log.i(TAG, "Action: Sending notification: " + data);
        final Intent i = getIntent(data);

        context.sendBroadcast(i);
    }

    private Intent getIntent(final String data) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", Settings.APP_NAME);
        i.putExtra("notificationData", data);

        return i;
    }
}
