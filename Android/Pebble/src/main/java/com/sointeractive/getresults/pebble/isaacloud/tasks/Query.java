package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.util.Log;

import com.sointeractive.getresults.pebble.utils.Application;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public enum Query {
    USER("/cache/users/%s", new String[]{"id", "firstName", "lastName", "level", "counterValues", "leaderboards"}),
    ACHIEVEMENTS("/cache/users/%s/achievements", new String[]{"id", "label", "description"}),
    BEACONS("/cache/users/groups", new String[]{"id", "label"}),
    PEOPLE("/cache/users", new String[]{"id", "firstName", "lastName", "counterValues"});
    static final int UNLIMITED = 0;
    private static final String TAG = Query.class.getSimpleName();
    private final String query;
    private final String[] fields;

    Query(final String query, final String[] fields) {
        this.query = query;
        this.fields = fields;
    }

    static HttpResponse getUserIdResponse(final String email) throws IOException, IsaaCloudConnectionException {
        Log.i(TAG, "Action: Query for user id");

        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("email", email);

        return Application.isaacloudConnector
                .path("/cache/users")
                .withFields("id")
                .withQuery(query)
                .get();
    }

    public HttpResponse getResponse() throws IOException, IsaaCloudConnectionException {
        return getResponse("");
    }

    HttpResponse getResponse(final String param) throws IOException, IsaaCloudConnectionException {
        final String path = String.format(query, param);
        Log.i(TAG, "Action: Query to path: " + path);
        return Application.isaacloudConnector
                .path(path)
                .withFields(fields)
                .withLimit(UNLIMITED)
                .get();
    }
}