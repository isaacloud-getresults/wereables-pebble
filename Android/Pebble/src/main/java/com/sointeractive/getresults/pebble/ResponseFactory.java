package com.sointeractive.getresults.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;

import java.util.List;

public class ResponseFactory {
    public static PebbleDictionary makeSingleValueResponse(int id, int value) {
        return new PebbleDictionaryBuilder(id)
                .addInt(value)
                .build();
    }

    public static PebbleDictionary makeSingleValueResponse(int id, String value) {
        return new PebbleDictionaryBuilder(id)
                .addString(value)
                .build();
    }

    public static PebbleDictionary makeListResponse(int id, List<String> list) {
        PebbleDictionaryBuilder builder = new PebbleDictionaryBuilder(id);
        for (String item : list) {
            builder.addString(item);
        }
        return builder.build();
    }
}
