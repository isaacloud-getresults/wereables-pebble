package com.sointeractive.getresults.pebble.utils;

import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.config.WebsocketSettings;
import com.sointeractive.getresults.pebble.isaacloud.receivers.SocketIOReceiver;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import io.socket.SocketIO;
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
            SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
            final SocketIO socket = new SocketIO();
            socket.connect(WebsocketSettings.SERVER_ADDRESS, new SocketIOReceiver());
            socket.send("{ \"token\" : \"abc\", \"url\" : \"/queues/notifications\"}");
        } catch (final MalformedURLException e) {
            Log.e(TAG, "Error: Websocket server address not valid");
        } catch (final NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
