package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetPeopleTask;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class PeopleProvider {
    public static final PeopleProvider INSTANCE = new PeopleProvider();

    private Collection<PersonIC> peopleIC = new LinkedList<PersonIC>();

    private PeopleProvider() {
        // Exists only to defeat instantiation.
    }

    @NotNull
    public Collection<PersonIC> getData() {
        reload();
        return peopleIC;
    }

    private void reload() {
        final GetPeopleTask getPeople = new GetPeopleTask();
        try {
            @NotNull final Collection<PersonIC> newPeopleIC = getPeople.execute().get();
            if (!newPeopleIC.isEmpty()) {
                peopleIC = newPeopleIC;
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        peopleIC.clear();
    }
}
