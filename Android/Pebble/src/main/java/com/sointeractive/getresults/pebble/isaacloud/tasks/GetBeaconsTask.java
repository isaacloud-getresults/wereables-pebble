package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.Room;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetBeaconsTask extends AsyncTask<Void, Integer, Collection<Room>> {
    private static final String TAG = GetBeaconsTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Event: onPreExecute");
    }

    @Override
    protected Collection<Room> doInBackground(final Void... params) {
        Log.d(TAG, "Action: Get beacons in background");

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

    private Collection<Room> getBeacons() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.BEACONS.getResponse();
        final Collection<Room> result = new LinkedList<Room>();

        final JSONArray beacons = response.getJSONArray();
        for (int i = 0; i < beacons.length(); i++) {
            final JSONObject beaconsJSON = (JSONObject) beacons.get(i);
            result.add(new Room(beaconsJSON));
        }
        return result;
    }

    @Override
    protected void onPostExecute(final Collection<Room> result) {
        Log.d(TAG, "Event: onPostExecute");

        if (result != null) {
            Log.d(TAG, "Event: Success");
        }
    }
}