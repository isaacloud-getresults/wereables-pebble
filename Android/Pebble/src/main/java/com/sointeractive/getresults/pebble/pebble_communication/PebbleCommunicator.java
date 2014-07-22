package com.sointeractive.getresults.pebble.pebble_communication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;

public class PebbleCommunicator extends Observable {
    private static final String TAG = PebbleCommunicator.class.getSimpleName();
    private final PebbleKit.PebbleDataReceiver receivedDataHandler;
    private final Handler handler = new Handler();
    private final Activity parentActivity;
    private final Context context;
    public boolean connectionState;

    public PebbleCommunicator(Activity parentActivity, Context context) {
        this.parentActivity = parentActivity;
        this.context = context;
        this.connectionState = false;
        this.receivedDataHandler = new PebbleDataReceiver(Settings.PEBBLE_APP_UUID);
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

    public void startListeningPebble() {
        Log.d(TAG, "Handlers: Registering received data handler");
        PebbleKit.registerReceivedDataHandler(parentActivity, receivedDataHandler);
    }

    public void stopListeningPebble() {
        Log.d(TAG, "Handlers: Unregistering received data handler");
        parentActivity.unregisterReceiver(receivedDataHandler);
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
        parentActivity.sendBroadcast(i);
    }

    private void receivedDataAction(PebbleDictionary data) {
        Request request = Request.getByData(data);
        Log.d(TAG, request.getLogMessage());
        if (request != Request.UNKNOWN) {
            sendResponse(request.getResponse());
        }
    }

    private void sendResponse(Response response) {
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

    class PebbleDataReceiver extends PebbleKit.PebbleDataReceiver {
        protected PebbleDataReceiver(UUID subscribedUuid) {
            super(subscribedUuid);
        }

        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
            Log.d(TAG, "Event: message received, value: " + data.toJsonString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Action: Acknowledgement sent to Pebble, transactionId: " + transactionId);
                    PebbleKit.sendAckToPebble(context, transactionId);
                    receivedDataAction(data);
                }
            });
        }
    }
}
