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

public class GetUserTask extends AsyncTask<String, Integer, UserIC> {
    private static final String TAG = GetUserTask.class.getSimpleName();

    @Override
    protected UserIC doInBackground(final String... emails) {
        Log.d(TAG, "Action: Login in background");

        try {
            return logIn(emails[0]);
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserIC logIn(final String email) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.USER.getResponse();

        final JSONArray users = response.getJSONArray();
        for (int i = 0; i < users.length(); i++) {
            final JSONObject userJSON = (JSONObject) users.get(i);

            if (email.equals(userJSON.get("email"))) {
                Log.d(TAG, "Event: User found: " + userJSON.toString());
                return new UserIC(userJSON);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(final UserIC result) {
        if (result != null) {
            Log.d(TAG, "Event: Success");
        }
    }
}