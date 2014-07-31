package com.sointeractive.getresults.pebble.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.sointeractive.getresults.pebble.isaacloud.receivers.AlarmReceiver;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;

public class CacheReloader {
    public static final CacheReloader INSTANCE = new CacheReloader();

    private static final int ONE_MINUTE = 60 * 1000;
    private static final int INTERVAL = 2 * ONE_MINUTE;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private CacheReloader() {
        // Exists only to defeat instantiation.
    }

    public void reload() {
        AchievementsCache.INSTANCE.reload();
        BeaconsCache.INSTANCE.reload();
        LoginCache.INSTANCE.reload();
        PeopleCache.INSTANCE.reload();
    }

    public void setAutoReload(final Context context) {
        final Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), INTERVAL, alarmIntent);
    }

    public void stopAutoReload() {
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }
    }
}
