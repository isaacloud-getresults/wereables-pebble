package com.sointeractive.getresults.pebble.utils;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble_communication.Response;

public class PebbleDictionaryBuilder {

    private final PebbleDictionary data = new PebbleDictionary();
    private int currentIndex = 0;

    public PebbleDictionaryBuilder(int responseType) {
        data.addInt32(Response.RESPONSE_TYPE, responseType);
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
        data.addInt32(Response.RESPONSE_LENGTH, currentIndex);
        return data;
    }
}
