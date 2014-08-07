package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.AchievementsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache {
    public static final AchievementsCache INSTANCE = new AchievementsCache();
    private static final SparseArray<Collection<ResponseItem>> achievementDescriptionResponses = new SparseArray<Collection<ResponseItem>>();

    private Collection<ResponseItem> achievementsResponse = new LinkedList<ResponseItem>();

    private AchievementsCache() {
        // Exists only to defeat instantiation.
    }

    public static Collection<ResponseItem> makeResponse(final Iterable<AchievementIC> collection) {
        final Collection<ResponseItem> response = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : collection) {
            response.add(achievement.toAchievementResponse());

            final Collection<ResponseItem> achievementDescriptionResponse = achievement.toAchievementDescriptionResponse();
            achievementDescriptionResponses.put(achievement.id, achievementDescriptionResponse);
        }
        return response;
    }

    public Collection<ResponseItem> getData() {
        reloadIfNeeded();
        return achievementsResponse;
    }

    public Collection<ResponseItem> getDescriptionData(final int id) {
        reloadIfNeeded();
        return getDescription(id);
    }

    private Collection<ResponseItem> getDescription(final int id) {
        return achievementDescriptionResponses.get(id, new LinkedList<ResponseItem>());
    }

    private void reloadIfNeeded() {
        if (achievementsResponse.isEmpty()) {
            reload();
        }
    }

    public void reload() {
        final Collection<AchievementIC> achievements = AchievementsProvider.INSTANCE.getData();
        achievementsResponse = makeResponse(achievements);
    }

    public void clear() {
        AchievementsProvider.INSTANCE.clear();
        achievementsResponse.clear();
    }
}
