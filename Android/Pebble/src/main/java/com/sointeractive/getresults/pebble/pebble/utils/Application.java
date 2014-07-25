package com.sointeractive.getresults.pebble.pebble.utils;

import android.content.Context;

import com.sointeractive.getresults.pebble.pebble.communication.PebbleCommunicator;

public class Application extends android.app.Application {
    private static PebbleCommunicator pebbleCommunicator;
    private static Context context;

    public Application() {
        context = this;
        createPebbleCommunicator();
    }

    private static void createPebbleCommunicator() {
        pebbleCommunicator = new PebbleCommunicator(context);
    }

    public static PebbleCommunicator getPebbleCommunicator() {
        return pebbleCommunicator;
    }
}
