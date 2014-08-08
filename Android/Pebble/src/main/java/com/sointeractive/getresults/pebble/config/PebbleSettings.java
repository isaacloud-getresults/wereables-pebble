package com.sointeractive.getresults.pebble.config;


import java.util.UUID;

public class PebbleSettings {
    // Connection settings
    public static final String APP_NAME = "GetResults!";
    public static final UUID PEBBLE_APP_UUID = UUID.fromString("51b19145-0542-474f-8b62-c8c34ae4b87b");

    // Response limits
    public static final int MAX_ACHIEVEMENTS_DESCRIPTION_STR_LEN = 78;

    // String values
    public static final String IC_NOTIFICATION_HEADER = "IsaaCloud notification";
}
