package com.sointeractive.getresults.pebble.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class IsaaCloudSettings {
    // Connection settings
    public static final String INSTANCE_ID = "179";
    public static final String APP_SECRET = "cbe82930e310e3519666c8ddf9776cee";
    public static final int UNLIMITED = 0;

    // Data settings
    public static final int ROOM_COUNTER_ID = 1;
    public static final int LEADERBOARD_ID = 1;
    public static final int PEBBLE_NOTIFICATION_ID = 4;
    public static final Collection<Integer> IGNORED_GROUPS = new ArrayList<Integer>(Arrays.asList(1, 2));

    // String values
    public static final String ROOM_NOT_FOUND_NAME = "Unknown room";

    // Caching settings
    public static final int CACHE_RELOAD_INTERVAL_SECONDS = 10;

    // Log in settings
    public static String LOGIN_EMAIL = "yoda@op.pl";
}
