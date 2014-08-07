package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.utils.Application;

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

    private static final String PATH = "/cache/users/groups";
    private static final String[] FIELDS = new String[]{"id", "label"};

    @Override
    protected Collection<RoomIC> doInBackground(final Void... params) {
        Log.d(TAG, "Action: Get beacons in background");

        try {
            return getBeacons();
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        Log.e(TAG, "Error: Beacons not found");
        return new LinkedList<RoomIC>();
    }

    private Collection<RoomIC> getBeacons() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = getHttpResponse();
        final Collection<RoomIC> result = new LinkedList<RoomIC>();

        final JSONArray beacons = response.getJSONArray();
        for (int i = 0; i < beacons.length(); i++) {
            if (!beacons.isNull(i)) {
                final RoomIC roomIC = getRoom(beacons, i);
                if (isNotIgnored(roomIC)) {
                    result.add(roomIC);
                }
            }
        }

        Log.d(TAG, "Event: " + result.size() + " rooms downloaded");
        return result;
    }

    private RoomIC getRoom(final JSONArray beacons, final int i) throws JSONException {
        final JSONObject beaconsJSON = beacons.getJSONObject(i);
        return new RoomIC(beaconsJSON);
    }

    private boolean isNotIgnored(final RoomIC roomIC) {
        return !IsaaCloudSettings.IGNORED_GROUPS.contains(roomIC.id);
    }

    private HttpResponse getHttpResponse() throws IOException, IsaaCloudConnectionException {
        Log.d(TAG, "Action: Query for beacons");

        return Application.isaacloudConnector
                .path(PATH)
                .withFields(FIELDS)
                .withLimit(IsaaCloudSettings.UNLIMITED)
                .get();
    }
}