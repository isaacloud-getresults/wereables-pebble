package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.List;

interface Sendable {
    public List<ResponseItem> getSendable(String query);
}
