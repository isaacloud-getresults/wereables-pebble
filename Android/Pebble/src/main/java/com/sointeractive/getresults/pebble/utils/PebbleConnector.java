package com.sointeractive.getresults.pebble.utils;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.pebble.communication.NotificationSender;

import java.util.Collection;
import java.util.Observable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PebbleConnector extends Observable {
    private static final String TAG = PebbleConnector.class.getSimpleName();

    private final Queue<PebbleDictionary> sendingQueue = new ConcurrentLinkedQueue<PebbleDictionary>();
    private final Context context;
    private final NotificationSender sender;

    private boolean connectionState;

    public PebbleConnector(final Context context) {
        Log.i(TAG, "Action: Initialize Pebble connector");

        this.context = context;
        sender = new NotificationSender(context);
    }

    public void clearSendingQueue() {
        Log.i(TAG, "Action: Clear sending queue");
        sendingQueue.clear();
    }

    public void onReceived() {
        Log.d(TAG, "Action: Poll queue");
        sendingQueue.poll();
    }

    public void sendDataToPebble(final Collection<PebbleDictionary> data) {
        synchronized (sendingQueue) {
            if (isPebbleConnected()) {
                final boolean wasEmpty = sendingQueue.isEmpty();
                sendingQueue.addAll(data);
                if (wasEmpty) {
                    Log.i(TAG, "Check: Queue was empty");
                    sendNext();
                }
            }
        }
    }

    public void sendNext() {
        synchronized (sendingQueue) {
            if (sendingQueue.isEmpty()) {
                Log.i(TAG, "Event: Nothing to send, sendingQueue is empty");
            } else {
                final PebbleDictionary data = sendingQueue.peek();
                Log.d(TAG, "Action: Sending response: " + data.toJsonString());
                PebbleKit.sendDataToPebble(context, PebbleSettings.PEBBLE_APP_UUID, data);
            }
        }
    }

    public boolean isPebbleConnected() {
        final boolean currentState = PebbleKit.isWatchConnected(context);
        if (currentState) {
            Log.d(TAG, "Check: Pebble is connected");
        } else {
            Log.w(TAG, "Check: Pebble is not connected");
        }

        if (connectionState != currentState) {
            connectionState = currentState;
            setChanged();
            notifyObservers();
        }

        return currentState;
    }

    public boolean areAppMessagesSupported() {
        final boolean appMessagesSupported = PebbleKit.areAppMessagesSupported(context);
        if (appMessagesSupported) {
            Log.d(TAG, "Check: AppMessages are supported");
        } else {
            Log.e(TAG, "Check: AppMessages are not supported");
        }

        return appMessagesSupported;
    }

    public void sendNotification(final String title, final String body) {
        sender.send(title, body);
    }
}
