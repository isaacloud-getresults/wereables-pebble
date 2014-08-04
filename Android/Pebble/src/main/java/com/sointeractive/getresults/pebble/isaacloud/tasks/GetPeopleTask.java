package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetPeopleTask extends AsyncTask<Void, Integer, Collection<PersonIC>> {
    private static final String TAG = GetPeopleTask.class.getSimpleName();

    @Override
    protected Collection<PersonIC> doInBackground(final Void... params) {
        Log.i(TAG, "Action: Get people in background");

        try {
            return getPeople();
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private Collection<PersonIC> getPeople() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.PEOPLE.getResponse();

        final Collection<PersonIC> people = new LinkedList<PersonIC>();
        final JSONArray peopleJSON = response.getJSONArray();
        for (int i = 0; i < peopleJSON.length(); i++) {
            final JSONObject personJSON = (JSONObject) peopleJSON.get(i);
            people.add(new PersonIC(personJSON));
        }

        Log.i(TAG, "Event: " + people.size() + " people found");
        return people;
    }

    @Override
    protected void onPostExecute(final Collection<PersonIC> result) {
        if (result == null) {
            Log.e(TAG, "Error: GetPeopleTask returned null");
        }
    }
}