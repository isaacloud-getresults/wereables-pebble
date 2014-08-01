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
    public int observedRoom = 0;
    private SparseArray<Collection<ResponseItem>> peopleResponses;

    private PeopleCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData(final int room) {
        if (peopleResponses == null) {
            reload();
        }
        return peopleRoomResponse(room);
    }

    private Collection<ResponseItem> peopleRoomResponse(final int room) {
        Collection<ResponseItem> response = peopleResponses.get(room);

        if (response == null) {
            response = new LinkedList<ResponseItem>();
        }

        return response;
    }

    public void reload() {
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getUpToDateData();

        final SparseArray<Collection<ResponseItem>> oldResponses = peopleResponses;
        peopleResponses = new SparseArray<Collection<ResponseItem>>();
        int id;
        for (final PersonIC person : people) {
            id = person.beacon;
            if (peopleResponses.get(id) == null) {
                peopleResponses.put(id, new ArrayList<ResponseItem>());
            }
            peopleResponses.get(id).add(new PersonResponse(person.id, person.getFullName()));
        }

        if (oldResponses != null && peopleResponses != null) {
            Log.i(TAG, "Check: Changes in room: " + observedRoom);
            NewPeopleChecker.check(oldResponses.get(observedRoom), peopleResponses.get(observedRoom));
        }
    }
}
