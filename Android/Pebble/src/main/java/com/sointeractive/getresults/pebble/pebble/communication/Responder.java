package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.util.List;

public class Responder {
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
        request.log();
        return request;
    }

    private void sendResponseToPebble(final List<PebbleDictionary> data) {
        Application.pebbleConnector.sendNewDataToPebble(data);
    }
}
