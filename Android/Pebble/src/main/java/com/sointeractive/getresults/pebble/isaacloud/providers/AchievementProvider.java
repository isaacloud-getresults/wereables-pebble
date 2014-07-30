package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class AchievementProvider implements Provider {
    public final static AchievementProvider INSTANCE = new AchievementProvider();
    private Collection<AchievementIC> achievementsIC;

    private AchievementProvider() {
        // Exists only to defeat instantiation.
    }

    @Override
    public Collection<AchievementIC> getData() {
        if (achievementsIC == null) {
            reload();
        }
        return achievementsIC;
    }

    @Override
    public Collection<AchievementIC> getUpToDateData() {
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
