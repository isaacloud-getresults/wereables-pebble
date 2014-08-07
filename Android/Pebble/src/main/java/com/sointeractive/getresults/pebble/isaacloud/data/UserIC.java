package com.sointeractive.getresults.pebble.isaacloud.data;

import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserIC extends PersonIC {
    private static final String TAG = UserIC.class.getSimpleName();

    public int points = 0;
    public int rank = 0;

    public UserIC(final JSONObject json) throws JSONException {
        super(json);

        try {
            final JSONObject leaderboard = getLeaderboard(json.getJSONArray("leaderboards"));
            if (leaderboard != null) {
                points = leaderboard.getInt("score");
                rank = leaderboard.getInt("position");
            }
        } catch (final JSONException e) {
            Log.e(TAG, "Error: Cannot get leaderboard");
        }
    }

    private JSONObject getLeaderboard(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.isNull(i)) {
                final JSONObject leaderboard = jsonArray.getJSONObject(i);
                if (leaderboard.getInt("id") == IsaaCloudSettings.LEADERBOARD_ID) {
                    return leaderboard;
                }
            }
        }
        return null;
    }
}
