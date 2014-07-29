package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.LoginTask;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class DataProvider {
    private static User user;
    private static Collection<Achievement> achievements;

    public static User getUser() {
        if (user == null) {
            reloadUser();
        }
        return user;
    }

    public static Collection<Achievement> getAchievements() {
        if (achievements == null) {
            reloadAchievements();
        }
        return achievements;
    }

    public static void reloadUser() {
        final LoginTask getUser = new LoginTask();
        try {
            user = getUser.execute(Settings.USER_EMAIL).get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void reloadAchievements() {
        final GetAchievementsTask getAchievements = new GetAchievementsTask();
        try {
            achievements = getAchievements.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void reloadAllData() {
        reloadUser();
        reloadAchievements();
    }
}
