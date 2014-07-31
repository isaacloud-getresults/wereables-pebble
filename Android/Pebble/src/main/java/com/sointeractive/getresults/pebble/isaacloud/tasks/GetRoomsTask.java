package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetRoomsTask extends AsyncTask<Void, Integer, Collection<RoomIC>> {
    private static final String TAG = GetRoomsTask.class.getSimpleName();

    @Override
    protected Collection<RoomIC> doInBackground(final Void... params) {
        Log.i(TAG, "Action: Get beacons in background");

        try {
            return getBeacons();
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<RoomIC> getBeacons() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.BEACONS.getResponse();

        final Collection<RoomIC> result = new LinkedList<RoomIC>();
        final JSONArray beacons = response.getJSONArray();
        for (int i = 0; i < beacons.length(); i++) {
            final JSONObject beaconsJSON = (JSONObject) beacons.get(i);
            result.add(new RoomIC(beaconsJSON));
        }

        Log.i(TAG, "Event: " + result.size() + " rooms found");
        return result;
    }

    @Override
    protected void onPostExecute(final Collection<RoomIC> result) {
        if (result != null) {
            Log.i(TAG, "Event: Success");
        }
    }
}