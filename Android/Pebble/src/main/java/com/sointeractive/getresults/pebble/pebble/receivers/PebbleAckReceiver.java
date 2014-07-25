package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.communication.PebbleCommunicator;

public class PebbleAckReceiver extends PebbleKit.PebbleAckReceiver {
    private static final String TAG = PebbleAckReceiver.class.getSimpleName();

    public PebbleAckReceiver() {
        super(Settings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveAck(Context context, int transactionId) {
        Log.d(TAG, "Event: Received Ack from Pebble");
        PebbleCommunicator.getCommunicator(context).sendNext();
    }
}
