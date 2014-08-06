package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.Collection;

public class AchievementResponse implements ResponseItem {
    private final int id;
    private final String name;
    private final String description;

    public AchievementResponse(final int id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public Collection<PebbleDictionary> getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addInt(id)
                .addString(name)
                .addString(description)
                .pack();
    }
}
