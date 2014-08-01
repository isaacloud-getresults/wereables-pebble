package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.google.common.collect.Sets;
import com.sointeractive.getresults.pebble.pebble.communication.Request;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class NewPeopleChecker {
    private static final String TAG = NewPeopleChecker.class.getSimpleName();

    public static void check(Collection<ResponseItem> oldPeople, Collection<ResponseItem> newPeople) {
        if (oldPeople == null) {
            oldPeople = new LinkedList<ResponseItem>();
        }

        if (newPeople == null) {
            newPeople = new LinkedList<ResponseItem>();
        }

        final Set<ResponseItem> oldPeopleSet = new HashSet<ResponseItem>(oldPeople);
        final Set<ResponseItem> newPeopleSet = new HashSet<ResponseItem>(newPeople);
        final Set<ResponseItem> peopleIn = Sets.difference(newPeopleSet, oldPeopleSet).immutableCopy();
        final Set<ResponseItem> peopleOut = Sets.difference(oldPeopleSet, newPeopleSet).immutableCopy();

        notifyPeopleIn(peopleIn);
        notifyPeopleOut(peopleOut);
    }

    private static void notifyPeopleIn(final Collection<ResponseItem> people) {
        if (!people.isEmpty()) {
            Log.i(TAG, "Event: New people in room");
            Responder.sendResponseToPebble(Request.PEOPLE_IN_ROOM.id, people);
        }
    }

    private static void notifyPeopleOut(final Collection<ResponseItem> people) {
        if (!people.isEmpty()) {
            Log.i(TAG, "Event: New people out of room");
            Responder.sendResponseToPebble(Responder.PERSON_POP, people);
        }
    }
}
