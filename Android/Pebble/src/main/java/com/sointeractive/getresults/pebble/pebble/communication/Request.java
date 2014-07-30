package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public enum Request implements Sendable {
    UNKNOWN(0, "UNKNOWN") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return null;
        }
    },

    LOGIN(1, "Login info") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return DataProvider.getLogin();
        }
    },

    BEACONS(2, "Beacons list") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return DataProvider.getBeacons();
        }
    },

    GAMES(3, "Games list") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            // TODO: Replace with user list
            return null;
        }
    },

    ACHIEVEMENTS(4, "Achievements info") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return DataProvider.getAchievements();
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;
    private static final String TAG = Request.class.getSimpleName();
    private static final int REQUEST_TYPE = 1;
    private static final int REQUEST_QUERY = 2;
    private final String logMessage;
    private final int id;
    private String query;

    private Request(final int id, final String logMessage) {
        this.id = id;
        this.logMessage = logMessage;
    }

    public static Request getByData(final PebbleDictionary data) {
        final int requestID = getRequestId(data);
        final Request request = Request.getById(requestID);
        request.query = getRequestQuery(data);
        return request;
    }

    private static int getRequestId(final PebbleDictionary data) {
        final Long requestID = data.getInteger(REQUEST_TYPE);
        return requestID.intValue();
    }

    private static Request getById(final int id) {
        for (final Request e : values()) {
            if (e.id == id)
                return e;
        }
        return UNKNOWN;
    }

    private static String getRequestQuery(final PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            return data.getString(REQUEST_QUERY);
        } else {
            return "";
        }
    }

    public Collection<PebbleDictionary> getDataToSend() {
        final Collection<PebbleDictionary> list = new LinkedList<PebbleDictionary>();
        for (final ResponseItem responseItem : getSendable(query)) {
            list.add(responseItem.getData(id));
        }
        return list;
    }

    public void log() {
        Log.d(TAG, "Request: " + logMessage);
    }
}