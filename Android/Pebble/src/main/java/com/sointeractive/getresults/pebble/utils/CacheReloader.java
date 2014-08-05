package com.sointeractive.getresults.pebble.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.receivers.AlarmReceiver;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.cache.BeaconsCache;
import com.sointeractive.getresults.pebble.pebble.cache.LoginCache;
import com.sointeractive.getresults.pebble.pebble.cache.PeopleCache;

public class CacheReloader {
    public static final CacheReloader INSTANCE = new CacheReloader();
    private static final String TAG = CacheReloader.class.getSimpleName();
    private static final int SECOND = 1000;
    private static final int INTERVAL = IsaaCloudSettings.CACHE_RELOAD_INTERVAL_SECONDS * SECOND;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private CacheReloader() {
        // Exists only to defeat instantiation.
    }

    public void reload() {
        Log.d(TAG, "Action: Reload cache");
        AchievementsCache.INSTANCE.reload();
        PeopleCache.INSTANCE.reload();
        BeaconsCache.INSTANCE.reload();
        LoginCache.INSTANCE.reload();
        Log.i(TAG, "Event: Cache reloaded");
    }

    public void setAutoReload(final Context context) {
        if (alarmManager == null) {
            Log.i(TAG, "Action: Set cache auto reload");

            final Intent intent = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), INTERVAL, alarmIntent);
        }
    }

    public void stopAutoReload() {
        Log.i(TAG, "Action: Stop cache auto reload");

        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
            alarmManager = null;
        }
    }
}
