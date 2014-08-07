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

    private int observedRoom = 0;

    private SparseArray<Collection<ResponseItem>> peopleResponses;

    private PeopleCache() {
        // Exists only to defeat instantiation.
    }

    public void setObservedRoom(final int observedRoom) {
        Log.i(TAG, "Action: Set observed room to: " + observedRoom);
        this.observedRoom = observedRoom;
    }

    public Collection<ResponseItem> getData(final int room) {
        if (peopleResponses == null) {
            reload();
        }
        return getPeopleRoomResponse(room);
    }

    private Collection<ResponseItem> getPeopleRoomResponse(final int room) {
        Collection<ResponseItem> response = peopleResponses.get(room);

        if (response == null) {
            response = new LinkedList<ResponseItem>();
        }

        return response;
    }

    public void reload() {
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();
        final SparseArray<Collection<ResponseItem>> oldResponses = peopleResponses;
        peopleResponses = new SparseArray<Collection<ResponseItem>>();

        updatePeopleList(people);
        findChanges(oldResponses);
    }

    private void addPersonToRoom(final int room, final PersonIC person) {
        if (peopleResponses.get(room) == null) {
            peopleResponses.put(room, new ArrayList<ResponseItem>());
        }
        peopleResponses.get(room).add(new PersonResponse(person.id, person.getFullName(), room));
    }

    private void updatePeopleList(final Iterable<PersonIC> people) {
        int room;
        for (final PersonIC person : people) {
            room = person.beacon;
            addPersonToRoom(room, person);
        }
    }

    private void findChanges(final SparseArray<Collection<ResponseItem>> oldResponses) {
        if (oldResponses != null) {
            Log.d(TAG, "Check: Changes in room " + observedRoom);
            NewPeopleChecker.checkSafe(oldResponses.get(observedRoom), peopleResponses.get(observedRoom));
        }
    }

    public void clear() {
        PeopleProvider.INSTANCE.clear();
        peopleResponses = null;
    }
}
