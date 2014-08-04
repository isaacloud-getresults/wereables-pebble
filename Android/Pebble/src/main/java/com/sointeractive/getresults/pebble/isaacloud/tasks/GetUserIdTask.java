package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetUserIdTask extends AsyncTask<String, Integer, Integer> {
    private static final String TAG = GetUserIdTask.class.getSimpleName();

    @Override
    protected Integer doInBackground(final String... emails) {
        Log.d(TAG, "Action: Get user id in background");

        try {
            return logIn(emails[0]);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private Integer logIn(final String email) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.getUserIdResponse(email);
        final int id = response.getJSONArray().getJSONObject(0).getInt("id");
        Log.i(TAG, "Event: User: " + email + " logged in with id: " + id);
        return id;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (result == null) {
            Log.e(TAG, "Error: Returned null");
        }
    }
}