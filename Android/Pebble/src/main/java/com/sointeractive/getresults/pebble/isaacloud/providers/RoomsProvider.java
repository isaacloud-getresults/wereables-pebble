package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetRoomsTask;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class RoomsProvider {
    public static final RoomsProvider INSTANCE = new RoomsProvider();

    private Collection<RoomIC> roomsIC = new LinkedList<RoomIC>();

    private RoomsProvider() {
        // Exists only to defeat instantiation.
    }

    @NotNull
    public Collection<RoomIC> getData() {
        if (roomsIC.isEmpty()) {
            reload();
        }
        return roomsIC;
    }

    private void reload() {
        final GetRoomsTask getRooms = new GetRoomsTask();
        try {
            @NotNull final Collection<RoomIC> newRoomsIC = getRooms.execute().get();
            if (newRoomsIC.size() > roomsIC.size()) {
                roomsIC = newRoomsIC;
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
