package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

public class AchievementResponse implements ResponseItem {
    private final String name;
    private final String description;

    public AchievementResponse(final String name, final String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addString(description)
                .build();
    }
}
