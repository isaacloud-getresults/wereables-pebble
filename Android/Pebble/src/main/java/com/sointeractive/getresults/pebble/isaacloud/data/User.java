package com.sointeractive.getresults.pebble.isaacloud.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class User extends Person {
    public final Collection<Integer> achievements = new LinkedList<Integer>();
    private final String email;
    private final int level;
    public int points;

    public User(final JSONObject json) throws JSONException {
        super(json);
        email = json.getString("email");
        level = json.getInt("level");
        setAchievements(json.getJSONArray("gainedAchievements"));
        setPoints();
    }

    private void setAchievements(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            achievements.add(counter.getInt("achievement"));
        }
    }

    private void setPoints() {
        int key;
        Integer val;
        points = 0;
        for (int i = 0; i < counters.size(); i++) {
            key = counters.keyAt(i);
            val = counters.get(key);
            if (key != 6) {
                points += val;
            }
        }
    }
}
