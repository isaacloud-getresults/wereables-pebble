package com.sointeractive.getresults.pebble.isaacloud.tasks;

import com.sointeractive.getresults.pebble.pebble.utils.Application;

import java.io.IOException;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public enum Query {
    USERS_LIST("/admin/users"),
    GROUPS("/cache/users/groups");

    private static final int RESPONSE_LIMIT = 1000;

    private final String query;

    Query(final String query) {
        this.query = query;
    }

    public HttpResponse getResponse() throws IOException, IsaaCloudConnectionException {
        return getResponse("");
    }

    public HttpResponse getResponse(final String param) throws IOException, IsaaCloudConnectionException {
        return Application.isaacloudConnector
                .path(String.format(query, param))
                .withLimit(RESPONSE_LIMIT)
                .get();
    }
}
