package com.sointeractive.getresults.pebble.pebble.data;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.DataProvider;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

import java.util.List;

public class Beacon implements Sendable {
    public String name;
    public int distance;
    public int games_active;
    public int games_completed;

    public Beacon(String name, int distance, int games_active, int games_completed) {
        this.name = name;
        this.distance = distance;
        this.games_active = games_active;
        this.games_completed = games_completed;
    }

    @Override
    public PebbleDictionary getDictionary(int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addInt(distance)
                .addInt(games_active)
                .addInt(games_completed)
                .build();
    }

    public static class GetData implements Response {
        @Override
        public List<Sendable> get(String query) {
            return DataProvider.getBeacons();
        }
    }
}
