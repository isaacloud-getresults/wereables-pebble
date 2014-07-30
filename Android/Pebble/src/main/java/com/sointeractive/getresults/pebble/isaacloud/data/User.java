package com.sointeractive.getresults.pebble.isaacloud.data;

import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class User {
    public final Collection<Integer> achievements = new LinkedList<Integer>();
    private final int userId;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final int level;
    private final Collection<Integer> games = new LinkedList<Integer>();
    private final SparseIntArray counters = new SparseIntArray();

    public User(final JSONObject json) throws JSONException {
        userId = json.getInt("id");
        firstName = json.getString("firstName");
        lastName = json.getString("lastName");
        email = json.getString("email");
        level = json.getInt("level");
        setAchievements(json.getJSONArray("gainedAchievements"));
        setGames(json.getJSONArray("wonGames"));
        setCounterValues(json.getJSONArray("counterValues"));
    }

    private void setAchievements(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            achievements.add(counter.getInt("achievement"));
        }
    }

    private void setGames(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            games.add(counter.getInt("game"));
        }
    }

    private void setCounterValues(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            counters.put(counter.getInt("counter"), counter.getInt("value"));
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
