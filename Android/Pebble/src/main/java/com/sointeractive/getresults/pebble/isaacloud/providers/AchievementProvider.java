package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class AchievementProvider {
    public final static AchievementProvider INSTANCE = new AchievementProvider();
    private Collection<AchievementIC> achievementsIC;

    private AchievementProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<AchievementIC> get() {
        if (achievementsIC == null) {
            reload();
        }
        return achievementsIC;
    }

    public Collection<AchievementIC> getUpToDate() {
        reload();
        return achievementsIC;
    }

    private void reload() {
        final GetAchievementsTask getAchievements = new GetAchievementsTask();
        try {
            achievementsIC = getAchievements.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
