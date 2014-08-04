package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
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

            if (roomsIC == null) {
                roomsIC = new LinkedList<RoomIC>();
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public int getSize() {
        if (roomsIC == null) {
            return 0;
        } else {
            return roomsIC.size();
        }
    }

    public String getRoomName(final int id) {
        final Collection<RoomIC> roomsIC = getData();
        for (final RoomIC roomIC : roomsIC) {
            if (roomIC.id == id) {
                return roomIC.name;
            }
        }

        return IsaaCloudSettings.ROOM_NOT_FOUND_NAME;
    }
}
