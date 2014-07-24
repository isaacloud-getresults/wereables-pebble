package com.sointeractive.getresults.pebble.pebble.data;

import java.util.List;

public interface Response {
    public List<Sendable> get(String query);
}
