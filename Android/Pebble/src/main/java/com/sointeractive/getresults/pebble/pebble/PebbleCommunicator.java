package com.sointeractive.getresults.pebble.pebble;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

public class PebbleCommunicator extends Observable {
    private static final String TAG = PebbleCommunicator.class.getSimpleName();
    private static volatile PebbleCommunicator instance = null;
    public boolean connectionState;
    private Context context;    public boolean connectionState;

    private PebbleCommunicator() {
    }

    public static PebbleCommunicator getCommunicator(Context context) {
        if (instance == null) {
            synchronized (PebbleCommunicator.class) {
                if (instance == null) {
                    instance = new PebbleCommunicator();
                    instance.connectionState = false;
                }
            }
        }
        instance.context = context;
        return instance;
    }

    public void sendResponse(Response response) {
        Log.d(TAG, response.getLogMessage());
        if (response != Response.UNKNOWN) {
            sendDataToPebble(response.getDataToSend());
        }
    }

    private void sendDataToPebble(PebbleDictionary data) {
        if (isPebbleConnected()) {
            Log.d(TAG, "Action: sending response: " + data.toJsonString());
            PebbleKit.sendDataToPebble(context, Settings.PEBBLE_APP_UUID, data);
        }
    }

    public boolean isPebbleConnected() {
        boolean currentState = PebbleKit.isWatchConnected(context);
        Log.d(TAG, "Check: Pebble is " + (currentState ? "connected" : "not connected"));

        if (this.connectionState != currentState) {
            this.connectionState = currentState;
            setChanged();
            notifyObservers();
        }

        return currentState;
    }

    public boolean areAppMessagesSupported() {
        boolean appMessagesSupported = PebbleKit.areAppMessagesSupported(context);
        Log.d(TAG, "Check: AppMessages " + (appMessagesSupported ? "are supported" : "are not supported"));

        return appMessagesSupported;
    }

    public void sendNotification(String title, String body) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map<String, String> data = new HashMap<String, String>();
        data.put("title", title);
        data.put("body", body);

        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", Settings.APP_NAME);
        i.putExtra("notificationData", notificationData);

        Log.d(TAG, "Action: Sending notification: " + notificationData);
        context.sendBroadcast(i);
    }
}
