package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.isaacloud.data.DataProvider;
import com.sointeractive.getresults.pebble.pebble.responses.Achievement;
import com.sointeractive.getresults.pebble.pebble.responses.Beacon;
import com.sointeractive.getresults.pebble.pebble.responses.Game;
import com.sointeractive.getresults.pebble.pebble.responses.Login;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
public class IsaacloudProxy {
    /*
    * ResponseDataProvider mock-up -- will be replaced by data from IsaaCloud.
    */

    private static final List<ResponseItem> beacons = new LinkedList<ResponseItem>();

    static {
        // In range
        beacons.add(new Beacon("Kitchen 1", 1, 0, 7));
        beacons.add(new Beacon("Meeting Room", 2, 1, 6));
        beacons.add(new Beacon("Meeting Room 2", 4, 2, 5));
        beacons.add(new Beacon("Boss Room", 8, 3, 4));

        // Out of range
        beacons.add(new Beacon("Kitchen 2", 0, 4, 3));
        beacons.add(new Beacon("Games Room", 0, 5, 2));
        beacons.add(new Beacon("Games Room 2", 0, 6, 1));
    }

    private static final List<ResponseItem> games = new LinkedList<ResponseItem>();

    static {
        // Active
        games.add(new Game("100 visits", "Visit room 100 times.", 75, 100));
        games.add(new Game("150 visit", "Visit room 150 times.", 75, 150));
        games.add(new Game("300 visits", "Visit room 300 times.", 75, 300));
        games.add(new Game("Without queue", "Enter empty room.", 0, 1));

        // Completed
        games.add(new Game("5 visits", "Visit room 5 times.", 5, 5));
        games.add(new Game("25 visits", "Visit room 25 times.", 25, 25));
        games.add(new Game("50 visits", "Visit room 50 times.", 50, 50));
    }

    private static final List<ResponseItem> achievements = new LinkedList<ResponseItem>();

    static {
        achievements.add(new Achievement("Employee of the month", "You were the best employee in month."));
        achievements.add(new Achievement("Quick eater", "You ate whole lunch in less than 15 minutes."));
        achievements.add(new Achievement("Quicker eater", "You ate whole lunch in less than 10 minutes."));
        achievements.add(new Achievement("The quickest eater", "You ate whole lunch in less than 5 minutes."));
        achievements.add(new Achievement("Swimmer 1", "You have swam more than 10km."));
        achievements.add(new Achievement("Swimmer 2", "You have swam more than 50km."));
        achievements.add(new Achievement("Swimmer 3", "You have swam more than 100km."));
    }

    public static List<ResponseItem> getLogin() {
        final ResponseItem login = new Login(getUserName(), getPoints(), getRank(), getBeaconsSize(), getAchievementsSize());
        return listWrap(login);
    }

    private static String getUserName() {
        return DataProvider.getUser().getFullName();
    }

    private static int getPoints() {
        return 58008;
    }

    private static int getRank() {
        return 74;
    }

    private static int getBeaconsSize() {
        return beacons.size();
    }

    private static int getAchievementsSize() {
        return achievements.size();
    }

    private static List<ResponseItem> listWrap(final ResponseItem item) {
        final List<ResponseItem> list = new LinkedList<ResponseItem>();
        list.add(item);
        return list;
    }

    public static List<ResponseItem> getBeacons() {
        return beacons;
    }

    public static List<ResponseItem> getGames() {
        return games;
    }

    public static List<ResponseItem> getAchievements() {
        return achievements;
    }
}