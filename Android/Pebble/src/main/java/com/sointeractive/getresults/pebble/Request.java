package com.sointeractive.getresults.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;

public enum Request {
    REQUEST_UNKNOWN(0, "Request: UNKNOWN REQUEST", Response.RESPONSE_UNKNOWN),
    REQUEST_BEACONS_IN_RANGE(12, "Request: Beacons in range list", Response.RESPONSE_BEACONS_IN_RANGE),
    REQUEST_BEACONS_OUT_OF_RANGE(13, "Request: Beacons out of range list", Response.RESPONSE_BEACONS_OUT_OF_RANGE),
    REQUEST_GAMES_ACTIVE(14, "Request: Active games list", Response.RESPONSE_GAMES_ACTIVE),
    REQUEST_GAMES_COMPLETED(15, "Request: Completed games list", Response.RESPONSE_GAMES_COMPLETED),
    REQUEST_LOGIN(16, "Request: Login", Response.RESPONSE_LOGIN),
    REQUEST_BEACON_DETAILS(17, "Request: Beacon details", Response.RESPONSE_BEACON_DETAILS),
    REQUEST_PROGRESS(18, "Request: Game progress", Response.RESPONSE_PROGRESS),
    REQUEST_GAME_DETAILS(19, "Request: Game details", Response.RESPONSE_GAME_DETAILS);

    private static final int REQUEST = 1;
    private static final int QUERY = 2;

    private final int id;
    private final Response response;
    private final String logMessage;

    private PebbleDictionary data;

    private Request(int id, String logMessage, Response response) {
        this.id = id;
        this.logMessage = logMessage;
        this.response = response;
    }

    public static Request getByData(PebbleDictionary data) {
        final int requestID = getRequestID(data);
        Request request = Request.getById(requestID);
        request.data = data;
        return request;
    }

    private static int getRequestID(PebbleDictionary data) {
        Long requestID = data.getInteger(REQUEST);
        return requestID.intValue();
    }

    private static Request getById(int id) {
        for (Request e : values()) {
            if (e.id == id)
                return e;
        }
        return REQUEST_UNKNOWN;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public Response getResponse() {
        String query = getRequestQuery();
        response.setQuery(query);
        return response;
    }

    private String getRequestQuery() {
        if (data.contains(QUERY)) {
            return data.getString(QUERY);
        } else {
            return "";
        }
    }
}