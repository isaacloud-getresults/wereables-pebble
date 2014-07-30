package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetBeaconsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.LoginTask;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class IsaacloudProxy {
    private static User user;
    private static Collection<Achievement> achievements;
    private static Collection<ResponseItem> userAchievements;
    private static Collection<Room> rooms;
    private static Collection<ResponseItem> beacons;

    private static User getUser() {
        if (user == null) {
            reloadUser();
        }
        return user;
    }

    private static Collection<Achievement> getAchievements() {
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

    private static void reloadAchievements() {
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
        reloadBeacons();
    }

    public static String getUserName() {
        return getUser().getFullName();
    }

    private static void reloadUserAchievements() {
        final Collection<ResponseItem> userAchievements = new LinkedList<ResponseItem>();
        final Collection<Achievement> allAchievements = getAchievements();
        final User currentUser = getUser();

        for (final Achievement achievement : allAchievements) {
            if (currentUser.achievements.contains(achievement.id)) {
                userAchievements.add(new AchievementResponse(achievement.name, achievement.description));
            }
        }

        IsaacloudProxy.userAchievements = userAchievements;
    }

    public static Collection<ResponseItem> getUserAchievements() {
        if (userAchievements == null) {
            reloadUserAchievements();
        }
        return userAchievements;
    }

    public static int getUserAchievementsSize() {
        return getUserAchievements().size();
    }

    private static Collection<Room> getRooms() {
        if (rooms == null) {
            reloadRooms();
        }
        return rooms;
    }

    private static void reloadRooms() {
        final GetBeaconsTask getBeacons = new GetBeaconsTask();
        try {
            rooms = getBeacons.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Collection<ResponseItem> getBeacons() {
        if (beacons == null) {
            reloadBeacons();
        }
        return beacons;
    }

    private static void reloadBeacons() {
        final Collection<ResponseItem> beacons = new LinkedList<ResponseItem>();
        final Collection<Room> rooms = getRooms();

        for (final Room room : rooms) {
            beacons.add(new BeaconResponse(room.name, 0, 0, 0));
        }

        IsaacloudProxy.beacons = beacons;
    }

    public static int getBeaconsSize() {
        return getBeacons().size();
    }
}
