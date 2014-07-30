package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetRoomsTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class RoomsProvider {
    public static final RoomsProvider INSTANCE = new RoomsProvider();

    private Collection<RoomIC> roomsIC;

    private RoomsProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<RoomIC> getData() {
        if (roomsIC == null) {
            reload();
        }
        return roomsIC;
    }

    public Collection<RoomIC> getUpToDateData() {
        reload();
        return roomsIC;
    }

    private void reload() {
        final GetRoomsTask getRooms = new GetRoomsTask();
        try {
            roomsIC = getRooms.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
