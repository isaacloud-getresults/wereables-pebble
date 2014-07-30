package com.sointeractive.getresults.pebble.isaacloud.tasks;

import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public enum Query {
    USER("/cache/users", new String[]{"id", "firstName", "lastName", "email", "level", "gainedAchievements", "counterValues", "leaderboards"}),
    ACHIEVEMENTS("/cache/users/%s/achievements", new String[]{"id", "name", "description"}),
    BEACONS("/cache/users/groups", new String[]{"id", "name"}),
    PEOPLE("/cache/users", new String[]{"id", "firstName", "lastName", "counterValues"});

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
        return Application.isaacloudConnector
                .path(String.format(query, param))
                .withFields(fields)
                .get();
    }
}
