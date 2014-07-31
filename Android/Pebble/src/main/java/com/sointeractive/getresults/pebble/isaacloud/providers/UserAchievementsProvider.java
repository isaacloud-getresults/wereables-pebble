package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewAchievementsChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserAchievementsTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class UserAchievementsProvider {
    public final static UserAchievementsProvider INSTANCE = new UserAchievementsProvider();

    private Collection<AchievementIC> achievementsIC;

    private UserAchievementsProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<AchievementIC> getData() {
        if (achievementsIC == null) {
            reload();
        }
        return achievementsIC;
    }

    public Collection<AchievementIC> getUpToDateData() {
        reload();
        return achievementsIC;
    }

    private void reload() {
        final GetUserAchievementsTask getAchievements = new GetUserAchievementsTask();
        try {
            final Collection<AchievementIC> oldAchievements = achievementsIC;
            achievementsIC = getAchievements.execute(UserProvider.INSTANCE.getData().id).get();
            NewAchievementsChecker.check(oldAchievements, achievementsIC);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
