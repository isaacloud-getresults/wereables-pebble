package com.sointeractive.getresults.pebble.pebble.data;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.DataProvider;
import com.sointeractive.getresults.pebble.pebble.utils.DictionaryBuilder;

import java.util.List;

public class User implements Sendable {
    public String name;
    public int points;
    public int rank;

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

    public static class GetData implements Response {
        @Override
        public List<Sendable> get(String query) {
            return DataProvider.getUser();
        }
    }
}
