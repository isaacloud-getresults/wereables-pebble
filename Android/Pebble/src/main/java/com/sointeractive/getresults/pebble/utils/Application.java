package com.sointeractive.getresults.pebble.utils;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.config.WebsocketSettings;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import pl.sointeractive.isaacloud.Isaacloud;
import pl.sointeractive.isaacloud.exceptions.InvalidConfigException;

public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();

    private static Isaacloud isaacloudConnector;
    private static PebbleConnector pebbleConnector;

    @SuppressWarnings("WeakerAccess")
    public Application() {
        initPebbleConnector();
        initIsaacloudConnector();
        initWebsocketReceiver();
    }

    public static Isaacloud getIsaacloudConnector() {
        return isaacloudConnector;
    }

    public static PebbleConnector getPebbleConnector() {
        return pebbleConnector;
    }

    private void initPebbleConnector() {
        pebbleConnector = new PebbleConnector(this);
    }

    private void initIsaacloudConnector() {
        Log.i(TAG, "Action: Initialize IsaaCloud connector");

        try {
            isaacloudConnector = new Isaacloud(getIsaacloudConfig());
        } catch (final InvalidConfigException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getIsaacloudConfig() {
        final Map<String, String> config = new HashMap<String, String>();

        config.put("instanceId", IsaaCloudSettings.INSTANCE_ID);
        config.put("appSecret", IsaaCloudSettings.APP_SECRET);

        return config;
    }

    private void initWebsocketReceiver() {
        try {
            final Socket socket = IO.socket(WebsocketSettings.SERVER_ADDRESS);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Log.i(TAG, "Event: Connected");
                    socket.emit("chat message", "{ \"token\" : \"abc\", \"url\" : \"/queues/notifications\"}");
                }
            }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Log.i(TAG, "Event: message received");
                }
            }).on("chat message", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Log.i(TAG, "Event: chat message received: " + args[0].toString());
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    Log.i(TAG, "Event: Disconnected");
                }
            });

            socket.connect();
        } catch (final URISyntaxException e) {
            Log.e(TAG, "Error: Websocket server address not valid");
        }

    }
}
