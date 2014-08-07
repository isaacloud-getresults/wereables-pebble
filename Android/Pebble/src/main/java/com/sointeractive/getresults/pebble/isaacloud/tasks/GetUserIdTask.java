package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.utils.Application;

import org.json.JSONException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetUserIdTask extends AsyncTask<String, Integer, Integer> {
    private static final String TAG = GetUserIdTask.class.getSimpleName();

    private static final String PATH = "/cache/users";
    private static final String[] FIELDS = new String[]{"id"};

    @Override
    protected Integer doInBackground(final String... emails) {
        Log.d(TAG, "Action: Get user id in background");

        if (emails.length != 1) {
            throw new IllegalArgumentException("You have to use exactly one email to to get user id");
        }

        try {
            return getId(emails[0]);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return -1;
    }

    private Integer getId(final String email) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = getHttpResponse(email);
        final int id = response.getJSONArray().getJSONObject(0).getInt("id");

        Log.i(TAG, "Event: User: " + email + " logged in with id: " + id);
        return id;
    }

    private HttpResponse getHttpResponse(final String email) throws IOException, IsaaCloudConnectionException {
        Log.d(TAG, "Action: Query for user id");

        final Map<String, Object> query = getQuery(email);

        return Application.isaacloudConnector
                .path(PATH)
                .withFields(FIELDS)
                .withQuery(query)
                .get();
    }

    private Map<String, Object> getQuery(final String email) {
        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("email", email);
        return query;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (result == -1) {
            Log.e(TAG, "Error: User not found");
        }
    }
}