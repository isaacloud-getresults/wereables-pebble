package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.AchievementsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache {
    public static final AchievementsCache INSTANCE = new AchievementsCache();

    private Collection<ResponseItem> achievementsResponse = new LinkedList<ResponseItem>();

    private AchievementsCache() {
        // Exists only to defeat instantiation.
    }

    public static Collection<ResponseItem> makeResponse(final Iterable<AchievementIC> collection) {
        final Collection<ResponseItem> response = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : collection) {
            response.add(achievement.toAchievementResponse());
        }
        return response;
    }

    public Collection<ResponseItem> getData() {
        if (achievementsResponse.isEmpty()) {
            reload();
        }
        return achievementsResponse;
    }

    public void reload() {
        final Collection<AchievementIC> achievements = AchievementsProvider.INSTANCE.getData();
        achievementsResponse = makeResponse(achievements);
    }

    public int getSize() {
        return achievementsResponse.size();
    }

    public void clear() {
        AchievementsProvider.INSTANCE.clear();
        achievementsResponse.clear();
    }
}
