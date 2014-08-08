package com.sointeractive.getresults.pebble.pebble.communication;

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
    UNKNOWN(0, "UNKNOWN") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return new LinkedList<ResponseItem>();
        }
    },

    LOGIN(1, "Login info") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return LoginCache.INSTANCE.getData();
        }

        @Override
        public void onRequest() {
            PeopleCache.INSTANCE.clearObservedRoom();
            Application.pebbleConnector.clearSendingQueue();
        }
    },

    BEACONS(2, "Beacons list") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return BeaconsCache.INSTANCE.getData();
        }
    },

    PEOPLE_IN_ROOM(3, "People list") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            PeopleCache.INSTANCE.setObservedRoom(query);
            return PeopleCache.INSTANCE.getData(query);
        }
    },

    ACHIEVEMENTS(4, "Achievements info") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return AchievementsCache.INSTANCE.getData();
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;

    static final int REQUEST_TYPE = 1;
    static final int REQUEST_QUERY = 2;

    public final int id;
    public final String logMessage;

    private Request(final int id, final String logMessage) {
        this.id = id;
        this.logMessage = logMessage;
    }

    @Override
    public void onRequest() {
        // Default: no onRequest action
    }

    public Collection<ResponseItem> getSendable(final PebbleDictionary data) {
        return getSendable(getQuery(data));
    }

    private int getQuery(final PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            return data.getInteger(REQUEST_QUERY).intValue();
        } else {
            return -1;
        }
    }
}