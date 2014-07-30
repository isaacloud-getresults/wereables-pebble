package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;
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
            return LoginCache.INSTANCE.getData();
        }
    },

    BEACONS(2, "Beacons list") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return BeaconsCache.INSTANCE.getData();
        }
    },

    PEOPLE_IN_ROOM(3, "People list") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return PeopleCache.INSTANCE.getData(getRoomId(query));
        }

        private int getRoomId(final String query) {
            final Collection<RoomIC> roomsIC = RoomsProvider.INSTANCE.getData();
            for (final RoomIC room : roomsIC) {
                if (room.name.equals(query)) {
                    return room.id;
                }
            }
            return -1;
        }
    },

    ACHIEVEMENTS(4, "Achievements info") {
        @Override
        public Collection<ResponseItem> getSendable(final String query) {
            return AchievementsCache.INSTANCE.getData();
        }
    };

    public static final int RESPONSE_TYPE = 1;
    public static final int RESPONSE_DATA_INDEX = 2;

    static final int REQUEST_TYPE = 1;
    static final int REQUEST_QUERY = 2;

    final int id;
    final String logMessage;
    private String query;

    private Request(final int id, final String logMessage) {
        this.id = id;
        this.logMessage = logMessage;
    }

    public Collection<PebbleDictionary> getDataToSend() {
        final Collection<PebbleDictionary> list = new LinkedList<PebbleDictionary>();
        for (final ResponseItem responseItem : getSendable(query)) {
            list.add(responseItem.getData(id));
        }
        return list;
    }

    public void setQuery(final PebbleDictionary data) {
        if (data.contains(REQUEST_QUERY)) {
            query = data.getString(REQUEST_QUERY);
        } else {
            query = "";
        }
    }
}