package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;

public enum Request implements Sendable {
    UNKNOWN(0, "UNKNOWN") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return null;
        }
    },

    LOGIN(1, "Login info") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return LoginCache.INSTANCE.getData();
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
    },

    ACHIEVEMENT_DESCRIPTION(5, "Achievement description") {
        @Override
        public Collection<ResponseItem> getSendable(final int query) {
            return AchievementsCache.INSTANCE.getDescriptionData(query);
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;

    static final int REQUEST_TYPE = 1;
    static final int REQUEST_QUERY = 2;

    public final int id;
    public final String logMessage;

    private int query;

    private Request(final int id, final String logMessage) {
        this.id = id;
        this.logMessage = logMessage;
    }

    public void sendResponse() {
        Responder.sendResponseItemsToPebble(getSendable(query));
    }

    public void setQuery(final PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            query = data.getInteger(REQUEST_QUERY).intValue();
        } else {
            query = 0;
        }
    }
}