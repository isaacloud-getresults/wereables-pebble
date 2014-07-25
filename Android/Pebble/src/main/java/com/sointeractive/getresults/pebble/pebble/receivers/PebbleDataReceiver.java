package com.sointeractive.getresults.pebble.pebble.receivers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.PebbleCommunicator;
import com.sointeractive.getresults.pebble.pebble.Request;

public class PebbleDataReceiver extends PebbleKit.PebbleDataReceiver {
    private static final String TAG = PebbleDataReceiver.class.getSimpleName();
    private final Handler handler = new Handler();

    public PebbleDataReceiver() {
        super(Settings.PEBBLE_APP_UUID);
    }

    @Override
    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
        Log.d(TAG, "Event: Message received, value: " + data.toJsonString());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Action: Acknowledgement sent to Pebble, transactionId: " + transactionId);
                PebbleKit.sendAckToPebble(context, transactionId);

                Request request = Request.getByData(data);
                Log.d(TAG, "Request: " + request.getLogMessage());
                if (request != Request.UNKNOWN) {
                    PebbleCommunicator communicator = PebbleCommunicator.getCommunicator(context);
                    communicator.clearQueue();
                    communicator.sendDataToPebble(request.getDataToSend());
                }
            }
        });
    }
}