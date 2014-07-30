package com.sointeractive.getresults.pebble.pebble.connection;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;
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
    private ConnectionState state;

    public PebbleConnector(final Context context) {
        this.context = context;
        sender = new NotificationSender(context);
        setInitialState();
    }

    private void setInitialState() {
        if (PebbleKit.isWatchConnected(context)) {
            state = ConnectionState.CONNECTED;
        } else {
            state = ConnectionState.DISCONNECTED;
        }
    }

    public void sendNewDataToPebble(final Collection<PebbleDictionary> data) {
        synchronized (sendingQueue) {
            clearSendingQueue();
            sendDataToPebble(data);
        }
    }

    void sendDataToPebble(final Collection<PebbleDictionary> data) {
        synchronized (sendingQueue) {
            if (isPebbleConnected()) {
                final boolean wasEmpty = sendingQueue.isEmpty();
                sendingQueue.addAll(data);
                if (wasEmpty) {
                    sendNext();
                }
            }
        }
    }

    public void sendNext() {
        synchronized (sendingQueue) {
            if (sendingQueue.isEmpty()) {
                Log.d(TAG, "Event: Nothing to send, empty sendingQueue");
            } else {
                final PebbleDictionary data = sendingQueue.poll();
                Log.d(TAG, "Action: sending response: " + data.toJsonString());
                PebbleKit.sendDataToPebble(context, Settings.PEBBLE_APP_UUID, data);
            }
        }
    }

    private void clearSendingQueue() {
        sendingQueue.clear();
    }

    public boolean isPebbleConnected() {
        Log.d(TAG, "Check: Pebble is " + (state.isConnected() ? "connected" : "not connected"));
        return state.isConnected();
    }

    public void setState(final ConnectionState newState) {
        if (state != newState) {
            state = newState;
            setChanged();
            notifyObservers();
        }
    }

    public boolean areAppMessagesSupported() {
        final boolean appMessagesSupported = PebbleKit.areAppMessagesSupported(context);
        Log.d(TAG, "Check: AppMessages " + (appMessagesSupported ? "are supported" : "are not supported"));

        return appMessagesSupported;
    }

    public void sendNotification(final String title, final String body) {
        sender.send(title, body);
    }
}
