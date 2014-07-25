package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

public class Game implements ResponseItem {
    private final String name;
    private final String description;
    private final int progress;
    private final int goal;

    public Game(String name, String description, int progress, int goal) {
        this.name = name;
        this.description = description;
        this.progress = progress;
        this.goal = goal;
    }

    @Override
    public PebbleDictionary getData(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addString(description)
                .addInt(progress)
                .addInt(goal)
                .build();
    }
}
