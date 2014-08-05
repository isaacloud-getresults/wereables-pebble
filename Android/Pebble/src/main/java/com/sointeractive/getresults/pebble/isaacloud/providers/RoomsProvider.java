package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetRoomsTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class RoomsProvider {
    public static final RoomsProvider INSTANCE = new RoomsProvider();

    private Collection<RoomIC> roomsIC;

    private RoomsProvider() {
        // Exists only to defeat instantiation.
    }

    Collection<RoomIC> getData() {
        if (roomsIC == null) {
            reload();
        }
        return safeRooms();
    }

    public Collection<RoomIC> getUpToDateData() {
        reload();
        return safeRooms();
    }

    private Collection<RoomIC> safeRooms() {
        if (roomsIC == null) {
            return new LinkedList<RoomIC>();
        } else {
            return roomsIC;
        }
    }

    private void reload() {
        final GetRoomsTask getRooms = new GetRoomsTask();
        try {
            final Collection<RoomIC> newRoomsIC = getRooms.execute().get();

            if (newRoomsIC != null) {
                roomsIC = newRoomsIC;
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
