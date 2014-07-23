package com.sointeractive.getresults.pebble.pebble_communication.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PebbleConnectedReceiver extends BroadcastReceiver {
    private static final String TAG = PebbleConnectedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Event: Pebble is now connected");
    }
}
