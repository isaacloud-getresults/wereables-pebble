package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.checker.BeaconsInfoChangeChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class BeaconsCache {
    public static final BeaconsCache INSTANCE = new BeaconsCache();

    private Collection<ResponseItem> beaconsResponse = new LinkedList<ResponseItem>();

    private BeaconsCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (beaconsResponse.isEmpty()) {
            reload();
        }
        return beaconsResponse;
    }

    public void reload() {
        final Collection<ResponseItem> oldBeaconsResponse = beaconsResponse;
        final Collection<RoomIC> rooms = RoomsProvider.INSTANCE.getData();
        loadNewResponses(rooms);

        BeaconsInfoChangeChecker.check(oldBeaconsResponse, beaconsResponse);
    }

    private void loadNewResponses(final Iterable<RoomIC> rooms) {
        beaconsResponse = new LinkedList<ResponseItem>();
        for (final RoomIC room : rooms) {
            final int peopleNumber = PeopleCache.INSTANCE.getSize(room.getId());
            beaconsResponse.add(room.toBeaconResponse(peopleNumber));
        }
    }

    public int getSize() {
        return beaconsResponse.size();
    }

    public String getRoomName(final int roomId) {
        for (final ResponseItem responseItem : beaconsResponse) {
            final BeaconResponse beacon = (BeaconResponse) responseItem;
            if (beacon.getId() == roomId) {
                return beacon.getName();
            }
        }

        return IsaaCloudSettings.ROOM_NOT_FOUND_NAME;
    }

    public void clear() {
        RoomsProvider.INSTANCE.clear();
        beaconsResponse.clear();
    }
}
