package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.Log;
import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewPeopleChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.PeopleProvider;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class PeopleCache {
    public static final PeopleCache INSTANCE = new PeopleCache();

    private static final String TAG = PeopleCache.class.getSimpleName();

    private int observedRoom = -1;

    private SparseArray<Collection<ResponseItem>> peopleResponses = new SparseArray<Collection<ResponseItem>>();

    private PeopleCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData(final int room) {
        if (peopleResponses.size() == 0) {
            reload();
        }
        return getPeopleRoomResponse(room);
    }

    public int getSize(final int room) {
        return getData(room).size();
    }

    private Collection<ResponseItem> getPeopleRoomResponse(final int room) {
        Collection<ResponseItem> response = peopleResponses.get(room);
        if (response == null) {
            response = new LinkedList<ResponseItem>();
        }
        return response;
    }

    public void reload() {
        final SparseArray<Collection<ResponseItem>> oldResponses = peopleResponses;

        peopleResponses = new SparseArray<Collection<ResponseItem>>();
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();
        updatePeopleList(people);

        findChanges(oldResponses);
    }

    private void updatePeopleList(final Iterable<PersonIC> people) {
        int room;
        for (final PersonIC person : people) {
            room = person.beacon;
            addPersonToRoom(room, person);
        }
    }

    private void addPersonToRoom(final int roomId, final PersonIC person) {
        if (peopleResponses.get(roomId) == null) {
            peopleResponses.put(roomId, new ArrayList<ResponseItem>());
        }
        peopleResponses.get(roomId).add(new PersonResponse(person.id, person.getFullName(), roomId));
    }

    private void findChanges(final SparseArray<Collection<ResponseItem>> oldResponses) {
        Log.d(TAG, "Check: Changes in roomId: " + observedRoom);
        final Collection<ResponseItem> oldResponsesRoom = oldResponses.get(observedRoom, new LinkedList<ResponseItem>());
        final Collection<ResponseItem> newResponsesRoom = peopleResponses.get(observedRoom, new LinkedList<ResponseItem>());
        NewPeopleChecker.check(oldResponsesRoom, newResponsesRoom);
    }

    public void clear() {
        PeopleProvider.INSTANCE.clear();
        peopleResponses.clear();
        clearObservedRoom();
    }

    public void setObservedRoom(final int observedRoom) {
        Log.i(TAG, "Action: Set observed room to: " + observedRoom);
        this.observedRoom = observedRoom;
    }

    public void clearObservedRoom() {
        observedRoom = -1;
    }
}
