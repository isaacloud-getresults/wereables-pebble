package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class RoomIC {
    public final int id;
    public final String name;

    public RoomIC(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("label");
    }

    public BeaconResponse toBeaconResponse(final int peopleNumber) {
        return new BeaconResponse(id, name, peopleNumber);
    }
}
