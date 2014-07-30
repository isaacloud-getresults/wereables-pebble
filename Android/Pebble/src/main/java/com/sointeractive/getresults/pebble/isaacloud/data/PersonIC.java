package com.sointeractive.getresults.pebble.isaacloud.data;

import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonIC {
    protected final int userId;
    protected final String firstName;
    protected final String lastName;
    protected final SparseIntArray counters = new SparseIntArray();
    public int beacon;

    public PersonIC(final JSONObject json) throws JSONException {
        userId = json.getInt("id");
        firstName = json.getString("firstName");
        lastName = json.getString("lastName");
        setCounterValues(json.getJSONArray("counterValues"));
        setBeacon();
    }

    protected void setCounterValues(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            final JSONObject counter = jsonArray.getJSONObject(i);
            counters.put(counter.getInt("counter"), counter.getInt("value"));
        }
    }

    protected void setBeacon() {
        beacon = counters.get(6, 0);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}