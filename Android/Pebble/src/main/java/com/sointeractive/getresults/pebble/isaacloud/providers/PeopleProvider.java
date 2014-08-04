package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetPeopleTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class PeopleProvider {
    public static final PeopleProvider INSTANCE = new PeopleProvider();

    private Collection<PersonIC> peopleIC;

    private PeopleProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<PersonIC> getData() {
        reload();
        return safePeople();
    }

    private Collection<PersonIC> safePeople() {
        if (peopleIC == null) {
            return new LinkedList<PersonIC>();
        } else {
            return peopleIC;
        }
    }

    private void reload() {
        final GetPeopleTask getPeople = new GetPeopleTask();
        try {
            final Collection<PersonIC> newPeopleIC = getPeople.execute().get();

            if (newPeopleIC != null) {
                peopleIC = newPeopleIC;
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
