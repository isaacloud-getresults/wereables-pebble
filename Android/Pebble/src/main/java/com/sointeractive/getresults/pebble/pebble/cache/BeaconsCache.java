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

    public void reload() {
        final Collection<RoomIC> rooms = RoomsProvider.INSTANCE.getUpToDateData();

        final Collection<ResponseItem> oldBeaconsResponse = beaconsResponse;
        beaconsResponse = new LinkedList<ResponseItem>();
        for (final RoomIC room : rooms) {
            final int peopleNumber = PeopleCache.INSTANCE.getData(room.id).size();
            beaconsResponse.add(new BeaconResponse(room.id, room.name, peopleNumber));
        }

        if (oldBeaconsResponse != null) {
            BeaconsInfoChangeChecker.check(oldBeaconsResponse, beaconsResponse);
        }
    }

    public int getSize() {
        if (beaconsResponse == null) {
            return 0;
        } else {
            return beaconsResponse.size();
        }
    }

    public String getRoomName(final int id) {
        for (final ResponseItem responseItem : beaconsResponse) {
            final BeaconResponse beacon = (BeaconResponse) responseItem;
            if (beacon.id == id) {
                return beacon.name;
            }
        }

        return IsaaCloudSettings.ROOM_NOT_FOUND_NAME;
    }
}
