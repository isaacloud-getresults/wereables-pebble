package com.sointeractive.getresults.pebble.pebble.data;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class Achievement implements Sendable {
    public String name;
    public String description;

    public Achievement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public PebbleDictionary getDictionary(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addString(description)
                .build();
    }
}
