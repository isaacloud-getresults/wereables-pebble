package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.List;

public class PersonInResponse implements ResponseItem {
    private static final int RESPONSE_ID = 3;

    final int id;
    final String name;
    final int roomId;

    public PersonInResponse(final int id, final String name, final int roomId) {
        this.id = id;
        this.name = name;
        this.roomId = roomId;
    }

    @Override
    public List<PebbleDictionary> getData() {
        return new DictionaryBuilder(RESPONSE_ID)
                .addInt(id)
                .addString(name)
                .addInt(roomId)
                .pack();
    }

    public ResponseItem toPersonOutResponse() {
        return new PersonOutResponse(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PersonInResponse)) {
            return false;
        }

        final PersonInResponse personInResponse = (PersonInResponse) obj;
        return id == personInResponse.id;
    }

    public int hashCode() {
        return id;
    }
}
