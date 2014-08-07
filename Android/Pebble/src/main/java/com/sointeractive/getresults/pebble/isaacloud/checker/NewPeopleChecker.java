package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.google.common.collect.Sets;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class NewPeopleChecker {
    private static final String TAG = NewPeopleChecker.class.getSimpleName();

    public static void check(final Collection<ResponseItem> oldPeople, final Collection<ResponseItem> newPeople) {
        final Set<ResponseItem> oldPeopleSet = new HashSet<ResponseItem>(oldPeople);
        final Set<ResponseItem> newPeopleSet = new HashSet<ResponseItem>(newPeople);
        final Set<ResponseItem> peopleIn = Sets.difference(newPeopleSet, oldPeopleSet).immutableCopy();
        final Set<ResponseItem> peopleOut = Sets.difference(oldPeopleSet, newPeopleSet).immutableCopy();

        notifyPeopleIn(peopleIn);
        notifyPeopleOut(peopleOut);
    }

    private static void notifyPeopleIn(final Collection<ResponseItem> people) {
        if (!people.isEmpty()) {
            Log.i(TAG, "Checker: New people entered observed room");
            Responder.sendResponseItemsToPebble(people);
        }
    }

    private static void notifyPeopleOut(final Collection<ResponseItem> people) {
        if (!people.isEmpty()) {
            Log.i(TAG, "Check: New people exited observed room");
            final Collection<ResponseItem> response = getPeopleOutResponse(people);
            Responder.sendResponseItemsToPebble(response);
        }
    }

    private static Collection<ResponseItem> getPeopleOutResponse(final Iterable<ResponseItem> people) {
        final Collection<ResponseItem> peopleOut = new LinkedList<ResponseItem>();
        for (final ResponseItem responseItem : people) {
            final PersonResponse personResponse = (PersonResponse) responseItem;
            peopleOut.add(personResponse.toPersonOutResponse());
        }
        return peopleOut;
    }
}
