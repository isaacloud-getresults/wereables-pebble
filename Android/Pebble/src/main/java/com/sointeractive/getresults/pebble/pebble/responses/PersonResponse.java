package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class PersonResponse implements ResponseItem {
    private final int id;
    private final String name;

    public PersonResponse(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addInt(id)
                .addString(name)
                .build();
    }
}
