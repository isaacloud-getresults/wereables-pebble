package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

public class LoginResponse implements ResponseItem {
    private final String name;
    private final int points;
    private final int rank;
    private final int beaconsSize;

    public LoginResponse(final String name, final int points, final int rank, final int beaconsSize) {
        this.name = name;
        this.points = points;
        this.rank = rank;
        this.beaconsSize = beaconsSize;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addInt(points)
                .addInt(rank)
                .addInt(beaconsSize)
                .build();
    }
}
