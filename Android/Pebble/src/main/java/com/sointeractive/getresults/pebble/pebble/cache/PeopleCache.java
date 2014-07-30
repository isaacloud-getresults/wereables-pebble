package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.PeopleProvider;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.ArrayList;
import java.util.Collection;

public class PeopleCache {
    public final static PeopleCache INSTANCE = new PeopleCache();
    private SparseArray<Collection<ResponseItem>> peopleResponses;

    private PeopleCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData(final int room) {
        if (peopleResponses == null) {
            reload();
        }
        return peopleResponses.get(room);
    }

    public Collection<ResponseItem> getUpToDateData(final int room) {
        reload();
        return peopleResponses.get(room);
    }

    private void reload() {
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();

        peopleResponses = new SparseArray<Collection<ResponseItem>>();
        int id;
        for (final PersonIC person : people) {
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
