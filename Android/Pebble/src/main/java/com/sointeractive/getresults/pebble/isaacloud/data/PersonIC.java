package com.sointeractive.getresults.pebble.isaacloud.data;

import android.util.SparseIntArray;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PersonIC {
    public final int id;
    public final int beacon;
    private final String firstName;
    private final String lastName;
    private final SparseIntArray counters = new SparseIntArray();

    public PersonIC(final JSONObject json) throws JSONException {
        id = json.getInt("id");
        firstName = json.getString("firstName");
        lastName = json.getString("lastName");
        setCounterValues(json.getJSONArray("counterValues"));
        beacon = counters.get(IsaaCloudSettings.ROOM_COUNTER_ID, -1);
    }

    void setCounterValues(final JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jsonArray.isNull(i)) {
                final JSONObject counter = jsonArray.getJSONObject(i);
                counters.put(counter.getInt("counter"), counter.getInt("value"));
            }
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public PersonResponse toPersonInResponse(final int roomId) {
        return new PersonResponse(id, getFullName(), roomId);
    }
}
