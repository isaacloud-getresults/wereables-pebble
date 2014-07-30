package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class UserIC extends PersonIC {
    public final Collection<Integer> achievements = new LinkedList<Integer>();
    private final String email;
    private final int level;
    public int points;
    public int rank;

    public UserIC(final JSONObject json) throws JSONException {
        super(json);
        email = json.getString("email");
        level = json.getInt("level");
        setAchievements(json.getJSONArray("gainedAchievements"));

        final JSONObject leaderboard = getLeaderboard(json.getJSONArray("leaderboards"));
        if (leaderboard != null) {
            points = leaderboard.getInt("score");
            rank = leaderboard.getInt("position");
        }
    }

    private void setAchievements(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            achievements.add(counter.getInt("achievement"));
        }
    }

    private JSONObject getLeaderboard(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.isNull(i)) {
                final JSONObject leaderboard = jsonArray.getJSONObject(i);
                if (leaderboard.getInt("id") == 1) {
                    return leaderboard;
                }
            }
        }
        return null;
    }
}
