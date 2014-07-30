package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;

interface Sendable {
    public Collection<ResponseItem> getSendable(int query);
}
