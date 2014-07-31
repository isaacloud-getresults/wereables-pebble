package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserAchievementsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache {
    public static final AchievementsCache INSTANCE = new AchievementsCache();

    private Collection<ResponseItem> achievementsResponse;

    private AchievementsCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (achievementsResponse == null) {
            reload();
        }
        return achievementsResponse;
    }

    public void reload() {
        final Collection<AchievementIC> userAchievements = UserAchievementsProvider.INSTANCE.getUpToDateData();

        achievementsResponse = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : safe(userAchievements)) {
            achievementsResponse.add(new AchievementResponse(achievement.name, achievement.description));
        }
    }

    private Iterable<AchievementIC> safe(final Iterable<AchievementIC> collection) {
        if (collection == null) {
            return new LinkedList<AchievementIC>();
        } else {
            return collection;
        }
    }
}
