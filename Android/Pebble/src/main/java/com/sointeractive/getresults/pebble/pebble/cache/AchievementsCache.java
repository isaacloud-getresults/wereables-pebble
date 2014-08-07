package com.sointeractive.getresults.pebble.pebble.cache;

import android.util.SparseArray;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserAchievementsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementDescriptionResponse;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache {
    public static final AchievementsCache INSTANCE = new AchievementsCache();
    private static final SparseArray<Collection<ResponseItem>> achievementDescriptionResponses = new SparseArray<Collection<ResponseItem>>();

    private Collection<ResponseItem> achievementsResponse;

    private AchievementsCache() {
        // Exists only to defeat instantiation.
    }

    public static Collection<ResponseItem> makeResponse(final Iterable<AchievementIC> collection) {
        final Collection<ResponseItem> response = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : safe(collection)) {
            response.add(new AchievementResponse(achievement.id, achievement.name, achievement.description));
            makeAchievementDescriptionResponse(achievement.id, achievement.description);
        }
        return response;
    }

    private static void makeAchievementDescriptionResponse(final int id, final String description) {
        if (achievementDescriptionResponses.get(id) == null) {
            achievementDescriptionResponses.put(id, makeSingleResponse(new AchievementDescriptionResponse(id, description)));
        }
    }

    private static Collection<ResponseItem> makeSingleResponse(final ResponseItem achievementDescriptionResponse) {
        final Collection<ResponseItem> responseItems = new LinkedList<ResponseItem>();
        responseItems.add(achievementDescriptionResponse);
        return responseItems;
    }

    private static Iterable<AchievementIC> safe(final Iterable<AchievementIC> collection) {
        if (collection == null) {
            return new LinkedList<AchievementIC>();
        } else {
            return collection;
        }
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
        Collection<ResponseItem> response = achievementDescriptionResponses.get(id);

        if (response == null) {
            response = new LinkedList<ResponseItem>();
        }

        return response;
    }

    private void reloadIfNeeded() {
        if (achievementsResponse == null) {
            reload();
        }
    }

    public void reload() {
        final Collection<AchievementIC> userAchievements = UserAchievementsProvider.INSTANCE.getData();
        achievementsResponse = makeResponse(userAchievements);
    }

    public void clear() {
        UserAchievementsProvider.INSTANCE.clear();
        achievementsResponse = null;
    }
}
