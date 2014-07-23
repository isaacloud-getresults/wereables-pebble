package com.sointeractive.getresults.pebble.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;

public enum Request {
    UNKNOWN(0, "Request: UNKNOWN REQUEST", Response.UNKNOWN),
    USER_DETAILS(11, "Request: User details", Response.USER_DETAILS),
    BEACONS(12, "Request: Beacons list", Response.BEACONS),
    GAMES(14, "Request: Games list", Response.GAMES),
    USER_INFO(16, "Request: User info", Response.USER_INFO),
    BEACON_DETAILS(17, "Request: Beacon details", Response.BEACON_DETAILS),
    PROGRESS(18, "Request: Game progress", Response.GAME_PROGRESS),
    GAME_DETAILS(19, "Request: Game details", Response.GAME_DETAILS);

    private static final int REQUEST_TYPE = 1;
    private static final int REQUEST_QUERY = 2;

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
        Long requestID = data.getInteger(REQUEST_TYPE);
        return requestID.intValue();
    }

    private static Request getById(int id) {
        for (Request e : values()) {
            if (e.id == id)
                return e;
        }
        return UNKNOWN;
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
        if (data.contains(REQUEST_QUERY)) {
            return data.getString(REQUEST_QUERY);
        } else {
            return "";
        }
    }
}