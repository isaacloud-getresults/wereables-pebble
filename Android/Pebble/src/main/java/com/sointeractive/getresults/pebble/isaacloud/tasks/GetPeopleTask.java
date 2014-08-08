package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.utils.Application;

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

    private static final String PATH = "/cache/users";
    private static final String[] FIELDS = new String[]{"id", "firstName", "lastName", "counterValues"};

    @Override
    protected Collection<PersonIC> doInBackground(final Void... params) {
        Log.d(TAG, "Action: Get people in background");

        try {
            return getPeople();
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        Log.e(TAG, "Error: People not found");
        return new LinkedList<PersonIC>();
    }

    private Collection<PersonIC> getPeople() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = getHttpResponse();
        final Collection<PersonIC> people = getPeople(response);
        Log.d(TAG, "Event: " + people.size() + " people downloaded");
        return people;
    }

    private Collection<PersonIC> getPeople(final HttpResponse response) throws JSONException {
        final Collection<PersonIC> people = new LinkedList<PersonIC>();
        final JSONArray peopleJSON = response.getJSONArray();
        for (int i = 0; i < peopleJSON.length(); i++) {
            if (!peopleJSON.isNull(i)) {
                final PersonIC personIC = getPerson(peopleJSON, i);
                people.add(personIC);
            }
        }
        return people;
    }

    private PersonIC getPerson(final JSONArray peopleJSON, final int i) throws JSONException {
        final JSONObject personJSON = peopleJSON.getJSONObject(i);
        return new PersonIC(personJSON);
    }

    private HttpResponse getHttpResponse() throws IOException, IsaaCloudConnectionException {
        Log.d(TAG, "Action: Query for beacons");

        return Application
                .getIsaacloudConnector()
                .path(PATH)
                .withFields(FIELDS)
                .withLimit(IsaaCloudSettings.UNLIMITED)
                .get();
    }
}