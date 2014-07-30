package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Room {
    public final int id;
    public final String name;

    public Room(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("name");
    }
}
