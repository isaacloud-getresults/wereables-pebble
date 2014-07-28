package com.sointeractive.getresults.pebble.pebble.utils;

import android.content.Context;

import com.sointeractive.getresults.pebble.pebble.communication.PebbleConnector;

public class Application extends android.app.Application {
    private static PebbleConnector pebbleConnector;
    private static Context context;

    public Application() {
        context = this;
        createPebbleCommunicator();
    }

    private static void createPebbleCommunicator() {
        pebbleConnector = new PebbleConnector(context);
    }

    public static PebbleConnector getPebbleConnector() {
        return pebbleConnector;
    }
}
