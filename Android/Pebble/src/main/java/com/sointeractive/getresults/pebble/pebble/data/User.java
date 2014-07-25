package com.sointeractive.getresults.pebble.pebble.data;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class User implements Sendable {
    private final String name;
    private final int points;
    private final int rank;

    public User(String name, int points, int rank) {
        this.name = name;
        this.points = points;
        this.rank = rank;
    }

    @Override
    public PebbleDictionary getDictionary(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addInt(points)
                .addInt(rank)
                .build();
    }
}
