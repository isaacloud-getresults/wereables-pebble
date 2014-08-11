package com.sointeractive.getresults.pebble.isaacloud.receivers;

import android.util.Log;

import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.DisconnectCallback;
import com.koushikdutta.async.http.socketio.ErrorCallback;
import com.koushikdutta.async.http.socketio.EventCallback;
import com.koushikdutta.async.http.socketio.ExceptionCallback;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;

import org.json.JSONArray;
import org.json.JSONObject;

public class AndroidAsyncSocketIOReceiver implements ConnectCallback {
    private static final String TAG = AndroidAsyncSocketIOReceiver.class.getSimpleName();

    @Override
    public void onConnectCompleted(final Exception e, final SocketIOClient client) {
        if (e != null) {
            Log.e(TAG, "Error: Connection, reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        Log.i(TAG, "Event: Connected");

        client.setStringCallback(new StringCallback() {
            @Override
            public void onString(final String message, final Acknowledge ack) {
                Log.i(TAG, "Event: Message received: " + message);
            }
        });

        client.setJSONCallback(new JSONCallback() {
            @Override
            public void onJSON(final JSONObject message, final Acknowledge ack) {
                Log.i(TAG, "Event: Message received: " + message.toString());
            }
        });

        client.on("event", new EventCallback() {
            @Override
            public void onEvent(final JSONArray arguments, final Acknowledge ack) {
                Log.i(TAG, "Event: 'event' with arguments: " + arguments.toString());
            }
        });

        client.setDisconnectCallback(new DisconnectCallback() {
            @Override
            public void onDisconnect(final Exception e) {
                Log.e(TAG, "Error: Disconnected, reason: " + e.getMessage());
            }
        });

        client.setExceptionCallback(new ExceptionCallback() {
            @Override
            public void onException(final Exception e) {
                Log.e(TAG, "Error: Exception, message: " + e.getMessage());
            }
        });

        client.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(final String message) {
                Log.e(TAG, "Error: " + message);
            }
        });
    }
}
