package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;
import com.sointeractive.getresults.pebble.utils.Application;

import java.util.Collection;
import java.util.LinkedList;

public class Responder {
    private static final String TAG = Responder.class.getSimpleName();
    private final PebbleDictionary data;

    public Responder(final PebbleDictionary data) {
        this.data = data;
    }

    public static void sendResponseItemsToPebble(final Collection<ResponseItem> data) {
        if (!data.isEmpty()) {
            final Collection<PebbleDictionary> responseData = makeResponseDictionary(data);
            Application.pebbleConnector.sendDataToPebble(responseData);
        }
    }

    private static Collection<PebbleDictionary> makeResponseDictionary(final Iterable<ResponseItem> data) {
        final Collection<PebbleDictionary> list = new LinkedList<PebbleDictionary>();
        for (final ResponseItem responseItem : data) {
            list.addAll(responseItem.getData());
        }
        return list;
    }

    public void sendRequestedResponse() {
        sendResponseItemsToPebble(getResponse());
    }

    private Collection<ResponseItem> getResponse() {
        final Request request = getRequest();
        return request.getSendable(data);
    }

    private Request getRequest() {
        final Request request = getById(getRequestId());
        Log.i(TAG, "Request: " + request.logMessage);
        request.onRequest();
        return request;
    }

    private int getRequestId() {
        final Long requestID = data.getInteger(Request.REQUEST_TYPE);
        return requestID.intValue();
    }

    private Request getById(final int id) {
        for (final Request request : Request.values()) {
            if (request.id == id)
                return request;
        }
        return Request.UNKNOWN;
    }
}
