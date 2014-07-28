package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.User;
import com.sointeractive.getresults.pebble.isaacloud.responses.UserResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class LoginTask extends AsyncTask<String, Integer, User> {
    private static final String TAG = LoginTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Event: onPreExecute");
    }

    @Override
    protected User doInBackground(final String... emails) {
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

    private User logIn(final String email) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.USERS_LIST.getResponse();

        final JSONArray users = response.getJSONArray();
        for (int i = 0; i < users.length(); i++) {
            final JSONObject user = (JSONObject) users.get(i);

            if (email.equals(user.get(UserResponse.EMAIL))) {
                Log.d(TAG, "Event: User found: " + user.toString());
                return new User(user);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final User result) {
        Log.d(TAG, "Event: onPostExecute");

        if (result != null) {
            Log.d(TAG, "Event: Login success");
        }
    }
}