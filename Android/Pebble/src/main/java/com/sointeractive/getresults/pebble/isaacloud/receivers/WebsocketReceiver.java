package com.sointeractive.getresults.pebble.isaacloud.receivers;

import android.util.Log;

import com.sointeractive.getresults.pebble.config.WebsocketSettings;
import com.sointeractive.getresults.pebble.utils.Application;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class WebsocketReceiver extends WebSocketClient {
    private static final String TAG = WebsocketReceiver.class.getSimpleName();

    public WebsocketReceiver(final URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(final ServerHandshake handshakeData) {
        Log.i(TAG, "Event: Opened");
        sendConfig();
    }

    private void sendConfig() {
        try {
            final String configString = getConfig();
            Log.i(TAG, "Action: Sending config to websocket server: " + configString);
            send(configString);
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        }
    }

    private String getConfig() throws JSONException, IOException, IsaaCloudConnectionException {
        final JSONObject config = new JSONObject();
        config.put("token", Application.getIsaacloudConnector().getToken());
        config.put("url", WebsocketSettings.URL_TO_LISTEN);
        return config.toString();
    }

    @Override
    public void onMessage(final String message) {
        Log.i(TAG, "Event: Message received: " + message);
        //close();
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        Log.i(TAG, "Event: Closed, code: " + code + ", reason: " + reason);
    }

    @Override
    public void onError(final Exception ex) {
        Log.i(TAG, "Error: " + ex.getMessage());
    }


}
