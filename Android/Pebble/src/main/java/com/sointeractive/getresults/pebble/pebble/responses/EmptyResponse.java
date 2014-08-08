package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;

import java.util.LinkedList;
import java.util.List;

public class EmptyResponse implements ResponseItem {
    public static final ResponseItem INSTANCE = new EmptyResponse();

    private EmptyResponse() {
        // Exists only to defeat instantiation.
    }

    public List<PebbleDictionary> getData() {
        return new LinkedList<PebbleDictionary>();
    }
}
