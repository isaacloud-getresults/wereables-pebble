package com.sointeractive.getresults.pebble.utils;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.pebble.communication.Request;

import java.util.Arrays;
import java.util.List;

public class DictionaryBuilder {
    private final PebbleDictionary dictionary = new PebbleDictionary();
    private int currentIndex = Request.RESPONSE_DATA_INDEX;

    public DictionaryBuilder(final int responseType) {
        dictionary.addInt32(Request.RESPONSE_TYPE, responseType);
    }

    public DictionaryBuilder addString(final String value) {
        dictionary.addString(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public DictionaryBuilder addInt(final int value) {
        dictionary.addInt32(currentIndex, value);
        currentIndex += 1;
        return this;
    }

    public List<PebbleDictionary> pack() {
        return Arrays.asList(dictionary);
    }
}
