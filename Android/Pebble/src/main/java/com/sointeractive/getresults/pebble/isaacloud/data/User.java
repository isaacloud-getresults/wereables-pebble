package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.isaacloud.responses.UserResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;

public class User {
    public final int userId;
    public final String firstName;
    public final String lastName;
    public final String email;
    public final int level;
    public final Collection<Integer> achievements = new LinkedList<Integer>();
    public final Collection<Integer> games = new LinkedList<Integer>();
    public final Collection<Integer> counters = new LinkedList<Integer>();

    public User(final JSONObject json) throws JSONException {
        userId = json.getInt(UserResponse.ID);
        firstName = json.getString(UserResponse.FIRST_NAME);
        lastName = json.getString(UserResponse.LAST_NAME);
        email = json.getString(UserResponse.EMAIL);
        level = json.getInt(UserResponse.LEVEL);
        setIntegerList(achievements, json.getJSONArray(UserResponse.ACHIEVEMENTS));
        setIntegerList(games, json.getJSONArray(UserResponse.GAMES));
        setIntegerList(counters, json.getJSONArray(UserResponse.COUNTERS));
    }

    private void setIntegerList(final Collection<Integer> achievements, final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            achievements.add(jsonArray.getInt(i));
        }
    }
}
