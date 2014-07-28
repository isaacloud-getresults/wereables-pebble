package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class Login implements ResponseItem {
    private final String name;
    private final int points;
    private final int rank;
    private final int beaconsSize;
    private final int achievementsSize;

    public Login(String name, int points, int rank, int beaconsSize, int achievementsSize) {
        this.name = name;
        this.points = points;
        this.rank = rank;
        this.beaconsSize = beaconsSize;
        this.achievementsSize = achievementsSize;
    }

    @Override
    public PebbleDictionary getData(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addInt(points)
                .addInt(rank)
                .addInt(beaconsSize)
                .addInt(achievementsSize)
                .build();
    }
}
