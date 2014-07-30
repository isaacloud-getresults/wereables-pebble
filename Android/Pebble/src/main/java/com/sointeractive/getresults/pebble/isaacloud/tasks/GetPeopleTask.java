package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.Person;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetPeopleTask extends AsyncTask<Void, Integer, Collection<Person>> {
    private static final String TAG = GetPeopleTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Event: onPreExecute");
    }

    @Override
    protected Collection<Person> doInBackground(final Void... params) {
        Log.d(TAG, "Action: Get people in background");

        try {
            return getPeople();
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<Person> getPeople() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.USERS_LIST.getResponse();

        final Collection<Person> people = new LinkedList<Person>();
        final JSONArray peopleJSON = response.getJSONArray();
        for (int i = 0; i < peopleJSON.length(); i++) {
            final JSONObject personJSON = (JSONObject) peopleJSON.get(i);
            people.add(new Person(personJSON));
        }
        return people;
    }

    @Override
    protected void onPostExecute(final Collection<Person> result) {
        Log.d(TAG, "Event: onPostExecute");

        if (result != null) {
            Log.d(TAG, "Event: Success");
        }
    }
}