package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class PersonResponse implements ResponseItem {
    private final String name;

    public PersonResponse(final String name) {
        this.name = name;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .build();
    }
}
