package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.util.Log;

import com.sointeractive.getresults.pebble.utils.Application;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public enum Query {
    LOGIN("/cache/users", new String[]{"id", "firstName", "lastName", "email", "level", "counterValues", "leaderboards"}),
    USER("/cache/users/%s", new String[]{"id", "firstName", "lastName", "email", "level", "counterValues", "leaderboards"}),
    ACHIEVEMENTS("/cache/users/%s/achievements", new String[]{"id", "label", "description"}),
    BEACONS("/cache/users/groups", new String[]{"id", "label"}),
    PEOPLE("/cache/users", new String[]{"id", "firstName", "lastName", "counterValues"});

    private static final String TAG = Query.class.getSimpleName();
    private static final int UNLIMITED = 0;

    private final String query;
    private final String[] fields;

    Query(final String query, final String[] fields) {
        this.query = query;
        this.fields = fields;
    }

    public HttpResponse getResponse() throws IOException, IsaaCloudConnectionException {
        return getResponse("");
    }

    HttpResponse getResponse(final String param) throws IOException, IsaaCloudConnectionException {
        final String path = String.format(query, param);
        Log.i(TAG, "Action: query to path: " + path);
        return Application.isaacloudConnector
                .path(path)
                .withFields(fields)
                .withLimit(UNLIMITED)
                .get();
    }
}
