package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.google.common.collect.Sets;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.notification.IsaacloudNotification;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserAchievementsProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NewAchievementsChecker {
    private static final String TAG = NewAchievementsChecker.class.getSimpleName();

    public static void check(final Collection<AchievementIC> result) {
        if (UserAchievementsProvider.INSTANCE.isCached()) {
            checkForNew(result);
        }
    }

    private static void checkForNew(final Collection<AchievementIC> result) {
        final Set<AchievementIC> newAchievements = getNewAchievements(result);
        if (!newAchievements.isEmpty()) {
            Log.i(TAG, "Check: New achievements found");
            notifyAchievements(newAchievements);
        }
    }

    private static Set<AchievementIC> getNewAchievements(final Collection<AchievementIC> result) {
        final Collection<AchievementIC> previousCollection = UserAchievementsProvider.INSTANCE.getData();
        final Set<AchievementIC> previousSet = new HashSet<AchievementIC>(previousCollection);
        final Set<AchievementIC> resultSet = new HashSet<AchievementIC>(result);
        return Sets.difference(resultSet, previousSet);
    }

    private static void notifyAchievements(final Collection<AchievementIC> newAchievements) {
        final String title = getTitle(newAchievements);
        final String body = getBody(newAchievements);
        final IsaacloudNotification notification = new IsaacloudNotification(title, body);
        notification.send();

    }

    private static String getBody(final Iterable<AchievementIC> newAchievements) {
        final StringBuilder bodyBuilder = new StringBuilder();

        for (final AchievementIC newAchievement : newAchievements) {
            bodyBuilder.append("-- ");
            bodyBuilder.append(newAchievement.name);
            bodyBuilder.append("\n");
        }

        return bodyBuilder.toString();
    }

    private static String getTitle(final Collection<AchievementIC> newAchievements) {
        final StringBuilder titleBuilder = new StringBuilder();

        titleBuilder.append("New Achievement");
        if (newAchievements.size() > 1) {
            titleBuilder.append("s");
        }

        return titleBuilder.toString();
    }
}
