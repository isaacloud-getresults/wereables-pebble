package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.util.List;

public class Responder {
    private static final String TAG = Responder.class.getSimpleName();

    private final PebbleDictionary data;

    private Responder(final PebbleDictionary data) {
        this.data = data;
    }

    public static void response(final PebbleDictionary data) {
        final Responder responder = new Responder(data);
        responder.processRequest();
    }

    private void processRequest() {
        final Request request = getRequest();
        if (request != Request.UNKNOWN) {
            sendResponseToPebble(request.getDataToSend());
        }
    }

    private Request getRequest() {
        final Request request = Request.getByData(data);
        Log.d(TAG, "Request: " + request.getLogMessage());
        return request;
    }

    private void sendResponseToPebble(final List<PebbleDictionary> data) {
        Application.getPebbleConnector().sendNewDataToPebble(data);
    }
}
