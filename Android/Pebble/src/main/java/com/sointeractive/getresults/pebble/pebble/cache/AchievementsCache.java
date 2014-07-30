package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserAchievementsProvider;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache implements Cache {
    public static final AchievementsCache INSTANCE = new AchievementsCache();
    private Collection<ResponseItem> achievementsResponse;

    private AchievementsCache() {
        // Exists only to defeat instantiation.
    }

    @Override
    public Collection<ResponseItem> getData() {
        if (achievementsResponse == null) {
            reload();
        }
        return achievementsResponse;
    }

    @Override
    public Collection<ResponseItem> getUpToDateData() {
        reload();
        return achievementsResponse;
    }

    public void reload() {
        final Collection<AchievementIC> userAchievements = UserAchievementsProvider.INSTANCE.getData();

        achievementsResponse = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : userAchievements) {
            achievementsResponse.add(new AchievementResponse(achievement.name, achievement.description));
        }
    }
}
