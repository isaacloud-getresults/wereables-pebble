package com.sointeractive.getresults.pebble.pebble.communication;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class PebbleConnector extends Observable {
    private static final String TAG = PebbleConnector.class.getSimpleName();
    private final LinkedList<PebbleDictionary> sendingQueue = new LinkedList<PebbleDictionary>();
    private final Context context;
    public boolean connectionState;

    public PebbleConnector(Context context) {
        this.context = context;
    }

    public synchronized void sendNewDataToPebble(List<PebbleDictionary> data) {
        clearSendingQueue();
        sendDataToPebble(data);
    }

    public synchronized void sendDataToPebble(List<PebbleDictionary> data) {
        if (isPebbleConnected()) {
            boolean wasEmpty = sendingQueue.isEmpty();
            sendingQueue.addAll(data);
            if (wasEmpty) {
                sendNext();
            }
        }
    }

    public synchronized void sendNext() {
        if (sendingQueue.isEmpty()) {
            Log.d(TAG, "Event: Nothing to send, empty sendingQueue");
        } else {
            PebbleDictionary data = sendingQueue.poll();
            Log.d(TAG, "Action: sending response: " + data.toJsonString());
            PebbleKit.sendDataToPebble(context, Settings.PEBBLE_APP_UUID, data);
        }
    }

    private synchronized void clearSendingQueue() {
        sendingQueue.clear();
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
        NotificationSender.send(context, title, body);
    }
}
