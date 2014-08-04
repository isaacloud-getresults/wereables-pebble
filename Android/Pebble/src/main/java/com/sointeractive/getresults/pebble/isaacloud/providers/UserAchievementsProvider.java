package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewAchievementsChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserAchievementsTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class UserAchievementsProvider {
    public final static UserAchievementsProvider INSTANCE = new UserAchievementsProvider();

    private Collection<AchievementIC> achievementsIC;

    private UserAchievementsProvider() {
        // Exists only to defeat instantiation.
    }

    public Collection<AchievementIC> getData() {
        reload();
        return safeAchievements();
    }

    private Collection<AchievementIC> safeAchievements() {
        if (achievementsIC == null) {
            return new LinkedList<AchievementIC>();
        } else {
            return achievementsIC;
        }
    }

    private void reload() {
        final GetUserAchievementsTask getAchievements = new GetUserAchievementsTask();
        try {
            final Collection<AchievementIC> oldAchievements = achievementsIC;
            final UserIC user = UserProvider.INSTANCE.getData();

            if (user != null) {
                final Collection<AchievementIC> newAchievements = getAchievements.execute(user.id).get();
                if (newAchievements != null) {
                    achievementsIC = newAchievements;
                    NewAchievementsChecker.check(oldAchievements, newAchievements);
                }
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
