package com.sointeractive.getresults.pebble.pebble.utils;

import android.content.Context;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.communication.PebbleConnector;

import java.util.HashMap;
import java.util.Map;

import pl.sointeractive.isaacloud.Isaacloud;
import pl.sointeractive.isaacloud.exceptions.InvalidConfigException;

public class Application extends android.app.Application {
    public static Isaacloud isaacloudConnector;
    public static PebbleConnector pebbleConnector;
    private static Context context;

    public Application() {
        context = this;
        pebbleConnector = new PebbleConnector(this);
        initIsaacloudConnector();
    }

    private void initIsaacloudConnector() {
        try {
            isaacloudConnector = new Isaacloud(getIsaacloudConfig());
        } catch (final InvalidConfigException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getIsaacloudConfig() {
        final Map<String, String> config = new HashMap<String, String>();

        config.put("instanceId", Settings.instanceId);
        config.put("appSecret", Settings.appSecret);

        return config;
    }
}