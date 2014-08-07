package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.List;

public class PersonOutResponse implements ResponseItem {
    private static final int RESPONSE_ID = 6;

    private final int id;
    private final String name;
    private final int roomId;

    public PersonOutResponse(final PersonInResponse person) {
        this.id = person.id;
        this.name = person.name;
        this.roomId = person.roomId;
    }

    @Override
    public List<PebbleDictionary> getData() {
        return new DictionaryBuilder(RESPONSE_ID)
                .addInt(id)
                .addString(name)
                .addInt(roomId)
                .pack();
    }
}
