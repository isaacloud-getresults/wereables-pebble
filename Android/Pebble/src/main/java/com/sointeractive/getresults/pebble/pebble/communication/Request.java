package com.sointeractive.getresults.pebble.pebble.communication;

import android.util.Log;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;
import com.sointeractive.getresults.pebble.utils.Application;

import java.util.Collection;
import java.util.LinkedList;

public enum Request implements Sendable {
    UNKNOWN(0) {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return new LinkedList<ResponseItem>();
        }

        @Override
        void onRequest() {
            Log.d(TAG, "Error: Unknown request");
        }
    },

    LOGIN(1) {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return LoginCache.INSTANCE.getData();
        }

        @Override
        void onRequest() {
            super.onRequest();
            PeopleCache.INSTANCE.clearObservedRoom();
            Application.getPebbleConnector().clearSendingQueue();
        }
    },

    BEACONS(2) {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return BeaconsCache.INSTANCE.getData();
        }
    },

    PEOPLE_IN_ROOM(3) {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            PeopleCache.INSTANCE.setObservedRoom(query);
            return PeopleCache.INSTANCE.getData(query);
        }
    },

    ACHIEVEMENTS(4) {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return AchievementsCache.INSTANCE.getData();
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;

    private static final String TAG = Responder.class.getSimpleName();

    private static final int REQUEST_TYPE = 1;
    private static final int REQUEST_QUERY = 2;

    private final int id;

    private Request(final int id) {
        this.id = id;
    }

    public static Collection<ResponseItem> getResponse(final PebbleDictionary data) {
        final int requestId = getRequestId(data);
        final int query = getQuery(data);
        return getSendable(requestId, query);
    }

    private static int getRequestId(final PebbleDictionary data) {
        final Long requestID = data.getInteger(Request.REQUEST_TYPE);
        return requestID.intValue();
    }

    private static int getQuery(final PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            return data.getInteger(REQUEST_QUERY).intValue();
        } else {
            return -1;
        }
    }

    private static Collection<ResponseItem> getSendable(final int requestId, final int query) {
        final Request request = getRequest(requestId);
        return request.getSendable(query);
    }

    private static Request getRequest(final int requestId) {
        final Request request = getById(requestId);
        request.onRequest();
        return request;
    }

    private static Request getById(final int id) {
        for (final Request request : Request.values()) {
            if (request.id == id)
                return request;
        }
        return Request.UNKNOWN;
    }

    void onRequest() {
        Log.i(TAG, "Request: " + name());
    }
}