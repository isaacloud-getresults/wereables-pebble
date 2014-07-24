package com.sointeractive.getresults.pebble.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.data.Achievement;
import com.sointeractive.getresults.pebble.pebble.data.Beacon;
import com.sointeractive.getresults.pebble.pebble.data.Game;
import com.sointeractive.getresults.pebble.pebble.data.Response;
import com.sointeractive.getresults.pebble.pebble.data.Sendable;
import com.sointeractive.getresults.pebble.pebble.data.User;

import java.util.LinkedList;
import java.util.List;

public enum Request {
    UNKNOWN(0, "UNKNOWN", null),
    USER(1, "User info", new User.GetData()),
    BEACONS(2, "Beacons list", new Beacon.GetData()),
    GAMES(3, "Games list", new Game.GetData()),
    ACHIEVEMENTS(4, "Achievements info", new Achievement.GetData());

    public static final int RESPONSE_TYPE = 1;

    private static final int REQUEST_TYPE = 1;
    private static final int REQUEST_QUERY = 2;

    private final int id;
    private final String logMessage;
    private final Response response;

    private String query;

    private Request(int id, String logMessage, Response response) {
        this.id = id;
        this.logMessage = logMessage;
        this.response = response;
    }

    public static Request getByData(PebbleDictionary data) {
        int requestID = getRequestId(data);
        Request request = Request.getById(requestID);
        request.query = getRequestQuery(data);
        return request;
    }

    private static int getRequestId(PebbleDictionary data) {
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

    private static String getRequestQuery(PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            return data.getString(REQUEST_QUERY);
        } else {
            return "";
        }
    }

    public String getLogMessage() {
        return logMessage;
    }

    public List<PebbleDictionary> getDataToSend() {
        List<PebbleDictionary> list = new LinkedList<PebbleDictionary>();
        for (Sendable sendable : response.get(query)) {
            list.add(sendable.getDictionary(id));
        }
        return list;
    }
}