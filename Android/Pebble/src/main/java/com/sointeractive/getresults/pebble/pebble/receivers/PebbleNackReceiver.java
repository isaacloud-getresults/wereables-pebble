package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.getresults.pebble.config.Settings;

public class PebbleNackReceiver extends PebbleKit.PebbleNackReceiver {
    private static final String TAG = PebbleNackReceiver.class.getSimpleName();

    public PebbleNackReceiver() {
        super(Settings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveNack(final Context context, final int transactionId) {
        Log.d(TAG, "Event: Received Nack from Pebble");
    }
}
