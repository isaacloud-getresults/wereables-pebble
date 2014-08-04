package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetLoginTask extends AsyncTask<String, Integer, UserIC> {
    private static final String TAG = GetLoginTask.class.getSimpleName();

    @Override
    protected UserIC doInBackground(final String... emails) {
        Log.i(TAG, "Action: Login in background");

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

    private UserIC logIn(final String email) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.LOGIN.getResponse();

        final JSONArray users = response.getJSONArray();
        for (int i = 0; i < users.length(); i++) {
            final JSONObject userJSON = (JSONObject) users.get(i);

            if (email.equals(userJSON.get("email"))) {
                Log.i(TAG, "Event: User found: " + userJSON.toString());
                return new UserIC(userJSON);
            }
        }

        Log.i(TAG, "Event: user not found");
        return null;
    }

    @Override
    protected void onPostExecute(final UserIC result) {
        if (result == null) {
            Log.e(TAG, "Error: GetLoginTask returned null");
        }
    }
}