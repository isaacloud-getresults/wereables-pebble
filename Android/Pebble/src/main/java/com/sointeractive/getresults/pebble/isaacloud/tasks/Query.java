package com.sointeractive.getresults.pebble.isaacloud.tasks;

import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public enum Query {
    USERS_LIST("/cache/users", new String[]{"id", "firstName", "lastName", "email", "level", "gainedAchievements", "wonGames", "counterValues"}),
    ACHIEVEMENTS("/cache/achievements", new String[]{"id", "name", "description"});

    private static final int RESPONSE_LIMIT = 100000;

    private final String query;
    private final String[] fields;

    Query(final String query, final String[] fields) {
        this.query = query;
        this.fields = fields;
    }

    public HttpResponse getResponse() throws IOException, IsaaCloudConnectionException {
        return getResponse("");
    }

    public HttpResponse getResponse(final String param) throws IOException, IsaaCloudConnectionException {
        return Application.isaacloudConnector
                .path(String.format(query, param))
                .withFields(fields)
                .withLimit(RESPONSE_LIMIT)
                .get();
    }
}
