package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.util.Collection;

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
        final Request request = getByData(data);
        Log.d(TAG, "Request: " + request.logMessage);
        request.setQuery(data);
        return request;
    }

    private Request getByData(final PebbleDictionary data) {
        final int requestID = getRequestId(data);
        return getById(requestID);
    }

    private int getRequestId(final PebbleDictionary data) {
        final Long requestID = data.getInteger(Request.REQUEST_TYPE);
        return requestID.intValue();
    }

    private Request getById(final int id) {
        for (final Request e : Request.values()) {
            if (e.id == id)
                return e;
        }
        return Request.UNKNOWN;
    }

    private void sendResponseToPebble(final Collection<PebbleDictionary> data) {
        if (!data.isEmpty()) {
            Application.pebbleConnector.sendNewDataToPebble(data);
        }
    }
}
