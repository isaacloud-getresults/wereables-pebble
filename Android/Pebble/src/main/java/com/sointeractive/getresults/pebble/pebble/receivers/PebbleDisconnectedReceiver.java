package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.getresults.pebble.pebble.utils.Application;

public class PebbleDisconnectedReceiver extends BroadcastReceiver {
    private static final String TAG = PebbleConnectedReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "Event: Pebble is now disconnected");
        Application.pebbleConnector.isPebbleConnected();
    }
}
