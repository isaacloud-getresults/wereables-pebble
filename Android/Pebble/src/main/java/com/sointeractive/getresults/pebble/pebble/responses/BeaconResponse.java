package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class BeaconResponse implements ResponseItem {
    private final String name;
    private final int distance;
    private final int people;

    public BeaconResponse(final String name, final int distance, final int people) {
        this.name = name;
        this.distance = distance;
        this.people = people;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addInt(distance)
                .addInt(people)
                .build();
    }
}
