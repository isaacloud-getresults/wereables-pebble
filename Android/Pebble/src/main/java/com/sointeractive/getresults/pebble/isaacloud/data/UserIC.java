package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserIC extends PersonIC {
    public int points;
    public int rank;

    public UserIC(final JSONObject json) throws JSONException {
        super(json);

        final JSONObject leaderboard = getLeaderboard(json.getJSONArray("leaderboards"));
        if (leaderboard != null) {
            points = leaderboard.getInt("score");
            rank = leaderboard.getInt("position");
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
