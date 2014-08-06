package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.List;

public class PersonResponse implements ResponseItem {
    private static final int RESPONSE_HEADER_ID = 3;
    private int responseType = RESPONSE_HEADER_ID;
    private static final int RESPONSE_PERSON_POP = 5;
    private final int id;
    private final String name;
    private final int roomId;

    public PersonResponse(final int id, final String name, final int roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }

    public PersonResponse(final PersonResponse person) {
        this.id = person.id;
        this.name = person.name;
        this.roomId = person.roomId;
    }

    public void setPersonPop() {
        responseType = RESPONSE_PERSON_POP;
    }

    @Override
    public List<PebbleDictionary> getData() {
        return new DictionaryBuilder(responseType)
                .addInt(id)
                .addString(name)
                .addInt(roomId)
                .pack();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PersonResponse)) {
            return false;
        }

        final PersonResponse personResponse = (PersonResponse) obj;
        return id == personResponse.id;
    }

    public int hashCode() {
        return id;
    }
}
