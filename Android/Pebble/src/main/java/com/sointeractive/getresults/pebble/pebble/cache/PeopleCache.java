package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.PeopleProvider;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class PeopleCache {
    public static final PeopleCache INSTANCE = new PeopleCache();

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

    public Collection<ResponseItem> getUpToDateData(final int room) {
        reload();
        return peopleRoomResponse(room);
    }

    private Collection<ResponseItem> peopleRoomResponse(final int room) {
        final Collection<ResponseItem> response = peopleResponses.get(room);

        if (response == null) {
            return new LinkedList<ResponseItem>();
        } else {
            return response;
        }
    }

    public void reload() {
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();

        peopleResponses = new SparseArray<Collection<ResponseItem>>();
        int id;
        for (final PersonIC person : people) {
            // TODO: Remove this after adding id communication feature
            if (!person.getFullName().equals("null null")) {
                id = person.beacon;
                if (peopleResponses.get(id) == null) {
                    peopleResponses.put(id, new ArrayList<ResponseItem>());
                }
                peopleResponses.get(id).add(new PersonResponse(person.getFullName()));
            }
        }
    }
}
