package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.NoSuchElementException;

class DataProvider {
    public final static DataProvider INSTANCE = new DataProvider();

    private DataProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getLogin() {
        return LoginCache.INSTANCE.getData();
    }

    public Collection<ResponseItem> getBeacons() {
        return BeaconsCache.INSTANCE.getData();
    }

    public Collection<ResponseItem> getAchievements() {
        return AchievementsCache.INSTANCE.getData();
    }

    public Collection<ResponseItem> getPeople(final String query) {
        return PeopleCache.INSTANCE.getData(getRoomId(query));
    }

    private int getRoomId(final String query) throws NoSuchElementException {
        final Collection<RoomIC> roomsIC = RoomsProvider.INSTANCE.getData();
        for (final RoomIC room : roomsIC) {
            if (room.name.equals(query)) {
                return room.id;
            }
        }
        return -1;
    }
}