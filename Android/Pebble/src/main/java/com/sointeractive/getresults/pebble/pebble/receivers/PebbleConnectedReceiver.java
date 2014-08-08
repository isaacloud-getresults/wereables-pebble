package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sointeractive.getresults.pebble.utils.Application;
import com.sointeractive.getresults.pebble.utils.CacheManager;

public class PebbleConnectedReceiver extends BroadcastReceiver {
    private static final String TAG = PebbleConnectedReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Event: Pebble is now connected");
        Application.getPebbleConnector().isPebbleConnected();
        CacheManager.INSTANCE.setAutoReload(context);
    }
}
