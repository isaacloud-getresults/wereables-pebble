package com.sointeractive.getresults.pebble.config;

public class WebsocketSettings {
    public static final String SERVER_ADDRESS = "http://178.62.191.47:443/";
    public static final String URL_TO_LISTEN = "/queues/notifications?limit=0&query=status:0,typeId:%d,subjectId:%d&fields=data";

    public static final String EMIT_EVENT = "chat message";
    public static final String LISTEN_EVENT = "chat message";
}
