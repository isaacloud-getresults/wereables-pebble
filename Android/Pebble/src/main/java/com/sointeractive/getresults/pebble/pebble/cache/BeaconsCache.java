package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class BeaconsCache {
    public static final BeaconsCache INSTANCE = new BeaconsCache();

    private Collection<ResponseItem> beaconsResponse;

    private BeaconsCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (beaconsResponse == null) {
            reload();
        }
        return beaconsResponse;
    }

    public Collection<ResponseItem> getUpToDateData() {
        reload();
        return beaconsResponse;
    }

    public void reload() {
        final Collection<RoomIC> rooms = RoomsProvider.INSTANCE.getData();

        beaconsResponse = new LinkedList<ResponseItem>();
        for (final RoomIC room : rooms) {
            // TODO: Provide real beacon distance
            final int peopleNumber = PeopleCache.INSTANCE.getData(room.id).size();
            beaconsResponse.add(new BeaconResponse(room.id, room.name, 0, peopleNumber));
        }
    }
}