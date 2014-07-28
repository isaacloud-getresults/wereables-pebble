package com.sointeractive.getresults.pebble.pebble.utils;

import android.content.Context;

import com.sointeractive.getresults.pebble.pebble.communication.PebbleConnector;

public class Application extends android.app.Application {
    public static PebbleConnector pebbleConnector;
    public static Context context;

    public Application() {
        context = this;
        pebbleConnector = new PebbleConnector(this);
    }
}
