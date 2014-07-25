package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.responses.DataProvider;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.LinkedList;
import java.util.List;

public enum Request implements Sendable {
    UNKNOWN(0, "UNKNOWN") {
        @Override
        public List<ResponseItem> getSendable(String query) {
            return null;
        }
    },

    USER(1, "User info") {
        @Override
        public List<ResponseItem> getSendable(String query) {
            return DataProvider.getUser();
        }
    },

    BEACONS(2, "Beacons list") {
        @Override
        public List<ResponseItem> getSendable(String query) {
            return DataProvider.getBeacons();
        }
    },

    GAMES(3, "Games list") {
        @Override
        public List<ResponseItem> getSendable(String query) {
            return DataProvider.getGames();
        }
    },

    ACHIEVEMENTS(4, "Achievements info") {
        @Override
        public List<ResponseItem> getSendable(String query) {
            return DataProvider.getAchievements();
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;

    private static final int REQUEST_TYPE = 1;
    private static final int REQUEST_QUERY = 2;

    private final int id;
    private final String logMessage;

    private String query;

    private Request(int id, String logMessage) {
        this.id = id;
        this.logMessage = logMessage;
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
        for (ResponseItem responseItem : getSendable(query)) {
            list.add(responseItem.getData(id));
        }
        return list;
    }
}