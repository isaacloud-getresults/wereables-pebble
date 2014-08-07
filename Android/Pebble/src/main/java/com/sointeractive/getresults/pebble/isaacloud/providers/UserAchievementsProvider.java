package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewAchievementsNotifier;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserAchievementsTask;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class UserAchievementsProvider {
    public final static UserAchievementsProvider INSTANCE = new UserAchievementsProvider();

    @NotNull
    private Collection<AchievementIC> achievementsIC = new LinkedList<AchievementIC>();

    private UserAchievementsProvider() {
        // Exists only to defeat instantiation.
    }

    @NotNull
    public Collection<AchievementIC> getData() {
        reload();
        return achievementsIC;
    }

    private void reload() {
        final GetUserAchievementsTask getAchievements = new GetUserAchievementsTask();
        try {
            final UserIC user = UserProvider.INSTANCE.getData();
            if (user == null) {
                return;
            }

            @NotNull final Collection<AchievementIC> oldAchievements = achievementsIC;
            @NotNull final Collection<AchievementIC> newAchievements = getAchievements.execute(user.id).get();
            if (newAchievements.size() > oldAchievements.size()) {
                achievementsIC = newAchievements;
                NewAchievementsNotifier.findDifference(oldAchievements, newAchievements);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
