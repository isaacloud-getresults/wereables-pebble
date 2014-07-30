package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetPeopleTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class PeopleProvider implements Provider {
    public static final PeopleProvider INSTANCE = new PeopleProvider();
    private Collection<PersonIC> peopleIC;

    private PeopleProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<PersonIC> getData() {
        if (peopleIC == null) {
            reload();
        }
        return peopleIC;
    }

    public Collection<PersonIC> getUpToDateData() {
        reload();
        return peopleIC;
    }

    private void reload() {
        final GetPeopleTask getPeople = new GetPeopleTask();
        try {
            peopleIC = getPeople.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
