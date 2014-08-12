package com.sointeractive.getresults.pebble.socket;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.config.WebsocketSettings;
import com.sointeractive.getresults.pebble.utils.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class SocketIONotifier extends SocketIOClient {
    public static final SocketIONotifier INSTANCE = new SocketIONotifier();
    private int userId;

    private SocketIONotifier() {
        super(WebsocketSettings.SERVER_ADDRESS);

        getSocket().on(WebsocketSettings.LISTEN_EVENT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onCustomEvent(getSafeString(args));
            }
        });
    }

    public void connect(final int userId) {
        this.userId = userId;
        connect();
    }

    @Override
    void onConnect(final String message) {
        super.onConnect(message);

        try {
            emit(getConfig());
        } catch (final JSONException e) {
            Log.e(TAG, "Error: Get JSON to send" + e.getMessage());
        } catch (final IOException e) {
            Log.e(TAG, "Error: Get token from IsaaCloudConnector" + e.getMessage());
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: Connection to IsaaCloud" + e.getMessage());
        }
    }

    private String getConfig() throws JSONException, IOException, IsaaCloudConnectionException {
        final JSONObject config = new JSONObject();
        config.put("token", getToken());
        config.put("url", getUrl());
        return config.toString();
    }

    private String getToken() throws IOException, IsaaCloudConnectionException {
        return Application.getIsaacloudConnector().getToken().split(" ")[1];
    }

    private String getUrl() {
        return String.format(WebsocketSettings.URL_TO_LISTEN, IsaaCloudSettings.PEBBLE_NOTIFICATION_ID, userId);
    }

    private void onCustomEvent(final String response) {
        Log.i(TAG, "Event: received " + WebsocketSettings.LISTEN_EVENT + ": " + response);
        try {
            final JSONObject notification = new JSONObject(response);
            final String message = notification.getJSONObject("data").getJSONObject("body").getString("message");
            Application.getPebbleConnector().sendNotification(PebbleSettings.IC_NOTIFICATION_HEADER, message);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: Not valid notification response");
        }

    }

    private void emit(final String message) {
        Log.d(TAG, "Action: Emit message: " + message);
        getSocket().emit(WebsocketSettings.EMIT_EVENT, message, new Ack() {
            @Override
            public void call(final Object... args) {
                Log.d(TAG, "Event: Ack, message: " + getSafeString(args));
            }
        });
    }
}
