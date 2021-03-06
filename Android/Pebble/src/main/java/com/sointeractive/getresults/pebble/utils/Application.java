package com.sointeractive.getresults.pebble.utils;

import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;

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
            Log.e(TAG, "Error: Invalid IsaaCloud config");
        }
    }

    private Map<String, String> getIsaacloudConfig() {
        final Map<String, String> config = new HashMap<String, String>();

        config.put("instanceId", IsaaCloudSettings.INSTANCE_ID);
        config.put("appSecret", IsaaCloudSettings.APP_SECRET);

        return config;
    }
}
