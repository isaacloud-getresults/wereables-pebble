package com.sointeractive.getresults.pebble.pebble.responses;

import java.util.LinkedList;
import java.util.List;

public class DataProvider {
    /*
    * ResponseDataProvider mock-up -- will be replaced by data from IsaaCloud.
    */

    private static final List<ResponseItem> beacons = new LinkedList<ResponseItem>();

    static {
        // In range
        beacons.add(new Beacon("Kitchen 1", 1, 0, 6));
        beacons.add(new Beacon("Meeting Room", 2, 1, 5));
        beacons.add(new Beacon("Meeting Room 2", 4, 2, 4));
        beacons.add(new Beacon("Boss Room", 8, 3, 3));

        // Out of range
        beacons.add(new Beacon("Kitchen 2", 0, 4, 2));
        beacons.add(new Beacon("Games Room", 0, 5, 1));
        beacons.add(new Beacon("Games Room 2", 0, 6, 0));
    }

    private static final List<ResponseItem> games = new LinkedList<ResponseItem>();

    static {
        // Active
        games.add(new Game("100 visits", "Visit room 100 times.", 75, 100));
        games.add(new Game("150 visits", "Visit room 150 times.", 75, 150));
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
        final Login login = new Login(getUser(), getPoints(), getRank(), getBeaconsSize(), getAchievememntsSize());
        return listWrap(login);
    }

    private static String getUser() {
        return "Janusz Tester";
    }

    private static int getBeaconsSize() {
        return beacons.size();
    }

    private static int getAchievememntsSize() {
        return achievements.size();
    }

    private static int getPoints() {
        return 58008;
    }

    private static int getRank() {
        return 74;
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