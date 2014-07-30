package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONException;
import org.json.JSONObject;

public class AchievementIC {
    public final int id;
    public final String name;
    public final String description;

    public AchievementIC(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("name");
        description = json.getString("description");
    }
}
