package com.sointeractive.getresults.pebble.pebble.utils;

import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;

public class CacheReloader {
    public static final CacheReloader INSTANCE = new CacheReloader();

    private CacheReloader() {
        // Exists only to defeat instantiation.
    }

    public void reload() {
        AchievementsCache.INSTANCE.reload();
        LoginCache.INSTANCE.reload();
        PeopleCache.INSTANCE.reload();
        BeaconsCache.INSTANCE.reload();
    }
}
