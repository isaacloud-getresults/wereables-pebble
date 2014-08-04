package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;

public class PebbleDataReceiver extends PebbleKit.PebbleDataReceiver {
    private static final String TAG = PebbleDataReceiver.class.getSimpleName();

    private final Handler handler = new Handler();

    public PebbleDataReceiver() {
        super(Settings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
        Log.i(TAG, "Event: Message received, value: " + data.toJsonString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                sendAckToPebble(context, transactionId);
                response(data);
            }
        });
    }

    private void sendAckToPebble(final Context context, final int transactionId) {
        Log.i(TAG, "Action: Acknowledgement sent to Pebble, transactionId: " + transactionId);
        PebbleKit.sendAckToPebble(context, transactionId);
    }

    private void response(final PebbleDictionary data) {
        final Responder responder = new Responder(data);
        responder.sendRequestedResponse();
    }
}