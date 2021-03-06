package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import org.json.JSONException;
import org.json.JSONObject;

public class AchievementIC {
    private final int id;
    private final String name;
    private final String description;

    public AchievementIC(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("label");
        description = json.getString("description");
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AchievementIC)) {
            return false;
        }

        final AchievementIC achievement = (AchievementIC) obj;
        return id == achievement.id;
    }

    public int hashCode() {
        return id;
    }

    public ResponseItem toAchievementResponse() {
        return new AchievementResponse(id, name, description);
    }
}
