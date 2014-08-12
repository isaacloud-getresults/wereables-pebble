package com.sointeractive.getresults.pebble.socket;

import android.util.Log;

public class SocketIOClient extends SocketIO {
    public SocketIOClient(final String address) {
        super(address);
    }

    @Override
    void onConnect(final String message) {
        Log.i(TAG, "Event: Connected, message: " + message);
    }

    @Override
    void onReconnecting(final String message) {
        Log.d(TAG, "Event: Reconnecting, message: " + message);
    }

    @Override
    void onReconnectFailed(final String message) {
        Log.e(TAG, "Event: Reconnection failed, message: " + message);
    }

    @Override
    void onReconnectError(final String message) {
        Log.e(TAG, "Error: Reconnection error, message: " + message);
    }

    @Override
    void onReconnectAttempt(final String message) {
        Log.d(TAG, "Event: Attempt to reconnect, message: " + message);
    }

    @Override
    void onReconnect(final String message) {
        Log.i(TAG, "Event: Reconnected, message: " + message);
    }

    @Override
    void onMessage(final String message) {
        Log.i(TAG, "Event: Message received: " + message);
    }

    @Override
    void onError(final String message) {
        Log.e(TAG, "Error: Message: " + message);
    }

    @Override
    void onDisconnect(final String message) {
        Log.i(TAG, "Event: Disconnected, message: " + message);
    }

    @Override
    void onConnectionTimeout(final String message) {
        Log.i(TAG, "Event: Connection timeout, message: " + message);
    }

    @Override
    void onConnectionError(final String message) {
        Log.e(TAG, "Error: Connection, message: " + message);
    }
}
