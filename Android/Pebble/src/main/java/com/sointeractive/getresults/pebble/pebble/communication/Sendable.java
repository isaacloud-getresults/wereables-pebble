package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.List;

public interface Sendable {
    public List<ResponseItem> getSendable(String query);
}
