package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;
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

    public static Collection<PebbleDictionary> makeResponseDictionary(final Iterable<ResponseItem> data) {
        final Collection<PebbleDictionary> list = new LinkedList<PebbleDictionary>();
        for (final ResponseItem responseItem : data) {
            list.addAll(responseItem.getData());
        }
        return list;
    }

    public void sendRequestedResponse() {
        final Request request = getRequest();
        // TODO: Polymorphic request action
        if (request != Request.UNKNOWN) {
            if (request == Request.LOGIN) {
                PeopleCache.INSTANCE.clearObservedRoom();
                Application.pebbleConnector.clearSendingQueue();
            }
            request.sendResponse();
        }
    }

    private Request getRequest() {
        final Request request = getByData(data);
        Log.i(TAG, "Request: " + request.logMessage);
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
}
