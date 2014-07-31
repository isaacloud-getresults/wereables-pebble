package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

public class BeaconResponse implements ResponseItem {
    private final int id;
    private final String name;
    private final int distance;
    private final int people;

    public BeaconResponse(final int id, final String name, final int distance, final int people) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.people = people;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addInt(id)
                .addString(name)
                .addInt(distance)
                .addInt(people)
                .build();
    }
}
