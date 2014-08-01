package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONException;
import org.json.JSONObject;

public class AchievementIC {
    public final String name;
    public final String description;

    public final int id;

    public AchievementIC(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("label");
        description = json.getString("description");
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
}
