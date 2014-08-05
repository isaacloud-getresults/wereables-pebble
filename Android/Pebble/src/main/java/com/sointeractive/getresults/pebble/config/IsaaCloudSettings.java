package com.sointeractive.getresults.pebble.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IsaaCloudSettings {
    // Connection settings
    public static final String INSTANCE_ID = "179";
    public static final String APP_SECRET = "cbe82930e310e3519666c8ddf9776cee";

    // Data settings
    public static final int ROOM_COUNTER_ID = 1;
    public static final int LEADERBOARD_ID = 1;

    public static final int CACHE_RELOAD_INTERVAL_SECONDS = 5;
    public static final String ROOM_NOT_FOUND_NAME = "Unknown room";
    public static final List<Integer> IGNORED_GROUPS = new ArrayList<Integer>(Arrays.asList(1, 2));

    public static String LOGIN_EMAIL = "yoda@op.pl";
}
