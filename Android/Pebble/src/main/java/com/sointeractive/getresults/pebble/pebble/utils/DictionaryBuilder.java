package com.sointeractive.getresults.pebble.pebble.utils;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.Request;

public class DictionaryBuilder {

    private final PebbleDictionary dictionary = new PebbleDictionary();
    private int currentIndex = 2;

    public DictionaryBuilder(int responseType) {
        dictionary.addInt32(Request.RESPONSE_TYPE, responseType);
    }

    public DictionaryBuilder addString(String value) {
        dictionary.addString(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public DictionaryBuilder addInt(int value) {
        dictionary.addInt32(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public PebbleDictionary build() {
        return dictionary;
    }
}
