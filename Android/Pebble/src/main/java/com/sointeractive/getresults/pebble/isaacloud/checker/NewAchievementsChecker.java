package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.google.common.collect.Sets;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.notification.IsaacloudNotification;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NewAchievementsChecker {
    private static final String TAG = NewAchievementsChecker.class.getSimpleName();

    public static void check(final Collection<AchievementIC> oldAchievements, final Collection<AchievementIC> newAchievements) {
        if (oldAchievements != null && newAchievements != null && oldAchievements.size() != newAchievements.size()) {
            Log.i(TAG, "Checker: New achievements found");

            final Set<AchievementIC> changedAchievements = getNewAchievements(oldAchievements, newAchievements);
            notifyAchievements(changedAchievements);
        }
    }

    private static Set<AchievementIC> getNewAchievements(final Collection<AchievementIC> oldAchievements, final Collection<AchievementIC> newAchievements) {
        final Set<AchievementIC> set1 = new HashSet<AchievementIC>(oldAchievements);
        final Set<AchievementIC> set2 = new HashSet<AchievementIC>(newAchievements);
        return Sets.symmetricDifference(set1, set2).immutableCopy();
    }

    private static void notifyAchievements(final Collection<AchievementIC> changedAchievements) {
        sendListItem(changedAchievements);
        sendNotification(changedAchievements);
    }

    private static void sendListItem(final Iterable<AchievementIC> changedAchievements) {
        final Collection<ResponseItem> responseItems = AchievementsCache.makeResponse(changedAchievements);
        Responder.sendResponseItemsToPebble(responseItems);
    }

    private static void sendNotification(final Collection<AchievementIC> changedAchievements) {
        final String title = getTitle(changedAchievements);
        final String body = getBody(changedAchievements);
        final IsaacloudNotification notification = new IsaacloudNotification(title, body);
        notification.send();
    }

    private static String getBody(final Iterable<AchievementIC> changedAchievements) {
        final StringBuilder bodyBuilder = new StringBuilder();

        for (final AchievementIC newAchievement : changedAchievements) {
            bodyBuilder.append("-- ");
            bodyBuilder.append(newAchievement.name);
            bodyBuilder.append("\n");
        }

        return bodyBuilder.toString();
    }

    private static String getTitle(final Collection<AchievementIC> changedAchievements) {
        final StringBuilder titleBuilder = new StringBuilder();

        titleBuilder.append("New Achievement");
        if (changedAchievements.size() > 1) {
            titleBuilder.append("s");
        }

        return titleBuilder.toString();
    }
}
