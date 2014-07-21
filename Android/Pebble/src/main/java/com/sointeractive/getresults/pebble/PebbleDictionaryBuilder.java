package com.sointeractive.getresults.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;

public class PebbleDictionaryBuilder {
    private static final int RESPONSE_TYPE = 200;
    private static final int RESPONSE_LENGTH = 201;

    private final PebbleDictionary data = new PebbleDictionary();
    private int currentIndex = 0;

    public PebbleDictionaryBuilder(int responseType) {
        data.addInt32(RESPONSE_TYPE, responseType);
    }

    public PebbleDictionaryBuilder addString(String value) {
        data.addString(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public PebbleDictionaryBuilder addInt(int value) {
        data.addInt32(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public PebbleDictionary build() {
        data.addInt32(RESPONSE_LENGTH, currentIndex);
        return data;
    }
}
