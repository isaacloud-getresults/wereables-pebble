package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.AchievementProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class AchievementsCache implements Cache {
    public final static AchievementsCache INSTANCE = new AchievementsCache();
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

    private void reload() {
        final Collection<AchievementIC> allAchievements = AchievementProvider.INSTANCE.getData();
        final UserIC currentUser = UserProvider.INSTANCE.getData();

        achievementsResponse = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : allAchievements) {
            if (currentUser.achievements.contains(achievement.id)) {
                achievementsResponse.add(new AchievementResponse(achievement.name, achievement.description));
            }
        }
    }
}
