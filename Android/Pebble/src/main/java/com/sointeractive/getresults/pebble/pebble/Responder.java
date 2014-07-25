package com.sointeractive.getresults.pebble.pebble;

import android.content.Context;
import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;

import java.util.List;

public class Responder {
    private static final String TAG = Responder.class.getSimpleName();

    private final Context context;
    private final PebbleDictionary data;

    private Responder(Context context, PebbleDictionary data) {
        this.context = context;
        this.data = data;
    }

    public static void response(Context context, PebbleDictionary data) {
        Responder responder = new Responder(context, data);
        responder.processRequest();
    }

    public void processRequest() {
        Request request = getRequest();
        if (request != Request.UNKNOWN) {
            sendResponseToPebble(request.getDataToSend());
        }
    }

    private Request getRequest() {
        Request request = Request.getByData(data);
        Log.d(TAG, "Request: " + request.getLogMessage());
        return request;
    }

    private void sendResponseToPebble(List<PebbleDictionary> dataToSend) {
        PebbleCommunicator communicator = PebbleCommunicator.getCommunicator(context);
        communicator.clearSendingQueue();
        communicator.sendDataToPebble(dataToSend);
    }
}
