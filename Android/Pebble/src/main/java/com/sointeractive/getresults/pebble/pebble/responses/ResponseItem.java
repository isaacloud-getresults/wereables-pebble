package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;

import java.util.Collection;

public interface ResponseItem {
    Collection<PebbleDictionary> getData(int responseType);
}
