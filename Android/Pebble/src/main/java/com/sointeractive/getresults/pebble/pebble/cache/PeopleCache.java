package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.Log;
import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewPeopleChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.PeopleProvider;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class PeopleCache {
    public static final PeopleCache INSTANCE = new PeopleCache();

    private static final String TAG = PeopleCache.class.getSimpleName();

    private int observedRoom = -1;

    private SparseArray<Collection<ResponseItem>> oldResponses;
    private SparseArray<Collection<ResponseItem>> peopleResponses = new SparseArray<Collection<ResponseItem>>();

    private PeopleCache() {
        // Exists only to defeat instantiation.
    }

    public void setObservedRoom(final int observedRoom) {
        Log.i(TAG, "Action: Set observed room to: " + observedRoom);
        this.observedRoom = observedRoom;
    }

    public void clearObservedRoom() {
        observedRoom = -1;
    }

    public Collection<ResponseItem> getData(final int room) {
        if (peopleResponses.size() == 0) {
            reload();
        }
        return getPeopleRoomResponse(room);
    }

    public void reload() {
        oldResponses = peopleResponses;
        peopleResponses = new SparseArray<Collection<ResponseItem>>();
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();
        updatePeopleList(people);
    }

    private void updatePeopleList(final Iterable<PersonIC> people) {
        int room;
        for (final PersonIC person : people) {
            room = person.getBeacon();
            addPersonToRoom(room, person);
        }
    }

    private void addPersonToRoom(final int roomId, final PersonIC person) {
        if (peopleResponses.get(roomId) == null) {
            peopleResponses.put(roomId, new ArrayList<ResponseItem>());
        }
        peopleResponses.get(roomId).add(person.toPersonInResponse(roomId));
    }

    public void findChanges() {
        if (oldResponses == null) {
            return;
        }

        Log.d(TAG, "Check: Changes in roomId: " + observedRoom);
        final Collection<ResponseItem> oldResponsesRoom = oldResponses.get(observedRoom, new LinkedList<ResponseItem>());
        final Collection<ResponseItem> newResponsesRoom = peopleResponses.get(observedRoom, new LinkedList<ResponseItem>());
        NewPeopleChecker.check(oldResponsesRoom, newResponsesRoom);
    }

    private Collection<ResponseItem> getPeopleRoomResponse(final int room) {
        Collection<ResponseItem> response = peopleResponses.get(room);
        if (response == null) {
            response = new LinkedList<ResponseItem>();
        }
        return response;
    }

    public int getSize(final int room) {
        return getData(room).size();
    }

    public void clear() {
        PeopleProvider.INSTANCE.clear();
        peopleResponses.clear();
        clearObservedRoom();
    }
}
