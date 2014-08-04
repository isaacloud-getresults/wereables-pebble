package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetUserTask extends AsyncTask<Integer, Integer, UserIC> {
    private static final String TAG = GetUserTask.class.getSimpleName();

    @Override
    protected UserIC doInBackground(final Integer... ids) {
        Log.i(TAG, "Action: Login in background");

        try {
            return logIn(ids[0].toString());
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private UserIC logIn(final String id) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.USER.getResponse(id);
        final JSONObject userJSON = response.getJSONObject();
        return new UserIC(userJSON);
    }

    @Override
    protected void onPostExecute(final UserIC result) {
        if (result == null) {
            Log.e(TAG, "Error: GetUserTask returned null");
        }
    }
}