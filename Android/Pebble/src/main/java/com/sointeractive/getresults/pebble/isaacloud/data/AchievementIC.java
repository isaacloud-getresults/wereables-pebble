package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.pebble.responses.AchievementDescriptionResponse;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementIC {
    public final int id;
    public final String name;
    public final String description;

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

    public AchievementResponse toAchievementResponse() {
        return new AchievementResponse(id, name, description);
    }

    public Collection<ResponseItem> toAchievementDescriptionResponse() {
        final Collection<ResponseItem> responseItems = new LinkedList<ResponseItem>();
        responseItems.add(new AchievementDescriptionResponse(id, description));
        return responseItems;
    }
}
