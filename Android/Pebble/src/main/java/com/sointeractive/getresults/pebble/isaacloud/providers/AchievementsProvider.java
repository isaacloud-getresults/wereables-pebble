package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.isaacloud.checker.NewAchievementsNotifier;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class AchievementsProvider {
    public final static AchievementsProvider INSTANCE = new AchievementsProvider();

    @NotNull
    private Collection<AchievementIC> achievementsIC = new LinkedList<AchievementIC>();

    private AchievementsProvider() {
        // Exists only to defeat instantiation.
    }

    @NotNull
    public Collection<AchievementIC> getData() {
        reload();
        return achievementsIC;
    }

    private void reload() {
        final GetAchievementsTask getAchievements = new GetAchievementsTask();
        try {
            @Nullable final UserIC user = UserProvider.INSTANCE.getData();
            if (user == null) {
                return;
            }

            @NotNull final Collection<AchievementIC> oldAchievements = achievementsIC;
            @NotNull final Collection<AchievementIC> newAchievements = getAchievements.execute(user.getId()).get();
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

    public void clear() {
        achievementsIC.clear();
    }
}
