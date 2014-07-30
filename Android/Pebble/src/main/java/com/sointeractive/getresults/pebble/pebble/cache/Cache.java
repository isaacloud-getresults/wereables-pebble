package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;

public interface Cache {
    Collection<ResponseItem> getData();

    Collection<ResponseItem> getUpToDateData();

    void reload();
}
