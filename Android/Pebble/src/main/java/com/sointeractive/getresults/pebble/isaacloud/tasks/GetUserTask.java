package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.utils.Application;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetUserTask extends AsyncTask<Integer, Integer, UserIC> {
    private static final String TAG = GetUserTask.class.getSimpleName();

    private static final String PATH = "/cache/users/%d";
    private static final String[] FIELDS = new String[]{"id", "firstName", "lastName", "level", "counterValues", "leaderboards"};

    @Override
    protected UserIC doInBackground(final Integer... ids) {
        Log.d(TAG, "Action: Get user in background");

        if (ids.length != 1) {
            throw new IllegalArgumentException("You have to use exactly one id to login");
        }

        try {
            return getUserData(ids[0]);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private UserIC getUserData(final int id) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = getHttpResponse(id);
        final JSONObject userJSON = response.getJSONObject();

        return new UserIC(userJSON);
    }

    private HttpResponse getHttpResponse(final int id) throws IOException, IsaaCloudConnectionException {
        Log.d(TAG, "Action: Query for user data");

        final String path = String.format(PATH, id);

        return Application.isaacloudConnector
                .path(path)
                .withFields(FIELDS)
                .get();
    }

    @Override
    protected void onPostExecute(final UserIC result) {
        if (result == null) {
            Log.e(TAG, "Error: User not found");
        }
    }
}