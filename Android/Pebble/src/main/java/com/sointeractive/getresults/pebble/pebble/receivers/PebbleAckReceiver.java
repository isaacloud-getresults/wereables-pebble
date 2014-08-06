package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.utils.Application;
import com.sointeractive.getresults.pebble.utils.PebbleConnector;

public class PebbleAckReceiver extends PebbleKit.PebbleAckReceiver {
    private static final String TAG = PebbleAckReceiver.class.getSimpleName();

    public PebbleAckReceiver() {
        super(PebbleSettings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveAck(final Context context, final int transactionId) {
        Log.i(TAG, "Event: Received Ack from Pebble, transactionId=" + transactionId);
        final PebbleConnector pebbleConnector = Application.pebbleConnector;
        pebbleConnector.onReceived();
        pebbleConnector.sendNext();
    }
}
