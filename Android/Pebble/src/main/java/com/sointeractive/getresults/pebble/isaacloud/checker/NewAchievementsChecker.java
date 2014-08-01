package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.google.common.collect.Sets;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.notification.IsaacloudNotification;
import com.sointeractive.getresults.pebble.pebble.cache.AchievementsCache;
import com.sointeractive.getresults.pebble.pebble.communication.Request;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NewAchievementsChecker {
    private static final String TAG = NewAchievementsChecker.class.getSimpleName();

    public static void check(final Collection<AchievementIC> achievements1, final Collection<AchievementIC> achievements2) {
        if (achievements1 != null && achievements2 != null && achievements1.size() != achievements2.size()) {
            Log.i(TAG, "Check: New achievements found");

            final Set<AchievementIC> newAchievements = getNewAchievements(achievements1, achievements2);
            notifyAchievements(newAchievements);
        }
    }

    private static Set<AchievementIC> getNewAchievements(final Collection<AchievementIC> achievements1, final Collection<AchievementIC> achievements2) {
        final Set<AchievementIC> set1 = new HashSet<AchievementIC>(achievements1);
        final Set<AchievementIC> set2 = new HashSet<AchievementIC>(achievements2);
        return Sets.symmetricDifference(set1, set2).immutableCopy();
    }

    private static void notifyAchievements(final Collection<AchievementIC> newAchievements) {
        final String title = getTitle(newAchievements);
        final String body = getBody(newAchievements);
        final IsaacloudNotification notification = new IsaacloudNotification(title, body);
        notification.send();
        final Collection<ResponseItem> responseItems = AchievementsCache.makeResponse(newAchievements);
        Responder.sendResponseToPebble(Request.ACHIEVEMENTS.id, responseItems);
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
