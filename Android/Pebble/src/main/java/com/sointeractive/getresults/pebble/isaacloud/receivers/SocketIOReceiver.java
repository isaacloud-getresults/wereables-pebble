package com.sointeractive.getresults.pebble.isaacloud.receivers;

import android.util.Log;

import org.json.JSONObject;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIOException;

public class SocketIOReceiver implements IOCallback {
    private static final String TAG = SocketIOReceiver.class.getSimpleName();

    @Override
    public void onConnect() {
        Log.i(TAG, "Event: Connected");
    }

    @Override
    public void onDisconnect() {
        Log.i(TAG, "Event: Disconnected");
    }

    @Override
    public void onMessage(final String message, final IOAcknowledge ack) {
        Log.i(TAG, "Event: Message received: " + message);
    }

    @Override
    public void onMessage(final JSONObject message, final IOAcknowledge ack) {
        Log.i(TAG, "Event: Message received: " + message.toString());
    }

    @Override
    public void on(final String event, final IOAcknowledge ack, final Object... args) {
        Log.i(TAG, "Server triggered event: " + event);
    }

    @Override
    public void onError(final SocketIOException socketIOException) {
        Log.e(TAG, "Error: " + socketIOException.getMessage());
        socketIOException.printStackTrace();
    }
}
