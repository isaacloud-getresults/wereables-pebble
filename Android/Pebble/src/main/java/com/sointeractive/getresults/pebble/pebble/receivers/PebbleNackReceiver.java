package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.utils.Application;
import com.sointeractive.getresults.pebble.utils.PebbleConnector;

public class PebbleNackReceiver extends PebbleKit.PebbleNackReceiver {
    private static final String TAG = PebbleNackReceiver.class.getSimpleName();

    public PebbleNackReceiver() {
        super(PebbleSettings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveNack(final Context context, final int transactionId) {
        Log.e(TAG, "Event: Received Nack from Pebble");
        final PebbleConnector pebbleConnector = Application.pebbleConnector;
        pebbleConnector.onReceived();
        pebbleConnector.sendNext();
    }
}
