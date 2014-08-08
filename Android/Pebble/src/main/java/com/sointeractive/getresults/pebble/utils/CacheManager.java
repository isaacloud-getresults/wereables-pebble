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

public class CacheManager {
    public static final CacheManager INSTANCE = new CacheManager();

    private static final String TAG = CacheManager.class.getSimpleName();

    private static final int SECOND = 1000;
    private static final int INTERVAL = IsaaCloudSettings.CACHE_RELOAD_INTERVAL_SECONDS * SECOND;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private CacheManager() {
        // Exists only to defeat instantiation.
    }

    public void reload() {
        clear();
        update();
    }

    public void update() {
        Log.d(TAG, "Action: Reload cache");
        AchievementsCache.INSTANCE.reload();
        PeopleCache.INSTANCE.reload();
        BeaconsCache.INSTANCE.reload();
        LoginCache.INSTANCE.reload();
        Log.i(TAG, "Event: Cache reloaded");
    }

    public void clear() {
        Log.d(TAG, "Action: Clear cache");
        AchievementsCache.INSTANCE.clear();
        PeopleCache.INSTANCE.clear();
        BeaconsCache.INSTANCE.clear();
        LoginCache.INSTANCE.clear();
        Log.i(TAG, "Event: Cache cleared");
    }

    public void setAutoReload(final Context context) {
        if (alarmManager == null) {
            Log.i(TAG, "Action: Set cache auto update");
            setAlarmIntent(context);
            setAlarm(context);
        }
    }

    private void setAlarmIntent(final Context context) {
        final Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setAlarm(final Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), INTERVAL, alarmIntent);
    }

    public void stopAutoReload() {
        Log.i(TAG, "Action: Stop cache auto update");
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
            alarmManager = null;
        }
    }
}
