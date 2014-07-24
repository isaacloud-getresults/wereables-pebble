package com.sointeractive.getresults.pebble.pebble.data;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.DataProvider;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

import java.util.List;

public class Game implements Sendable {
    public String name;
    public String description;
    public int progress;
    public int goal;

    public Game(String name, String description, int progress, int goal) {
        this.name = name;
        this.description = description;
        this.progress = progress;
        this.goal = goal;
    }

    @Override
    public PebbleDictionary getDictionary(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addString(description)
                .addInt(progress)
                .addInt(goal)
                .build();
    }

    public static class GetData implements Response {
        @Override
        public List<Sendable> get(String query) {
            return DataProvider.getGames();
        }
    }
}
