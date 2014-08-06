package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

public class PersonResponse implements ResponseItem {
    private final int id;
    private final String name;
    private final int roomId;

    public PersonResponse(final int id, final String name, final int roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addInt(id)
                .addString(name)
                .addInt(roomId)
                .build();
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
