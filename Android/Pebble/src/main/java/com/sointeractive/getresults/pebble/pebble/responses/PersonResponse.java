package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.List;

public class PersonResponse implements ResponseItem {
    private static final int RESPONSE_HEADER_ID = 3;

    final int id;
    final String name;
    final int roomId;

    public PersonResponse(final int id, final String name, final int roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }

    @Override
    public List<PebbleDictionary> getData() {
        return new DictionaryBuilder(RESPONSE_HEADER_ID)
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
